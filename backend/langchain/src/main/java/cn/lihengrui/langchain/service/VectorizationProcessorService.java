package cn.lihengrui.langchain.service;

import cn.lihengrui.langchain.dto.ImportNotificationDto;
import cn.lihengrui.langchain.dto.KnowledgeContentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量化处理服务
 * 负责协调API数据获取、向量化和存储的整个流程
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorizationProcessorService {
    
    private final ApiClientService apiClientService;
    private final VectorizationService vectorizationService;
    private final FaissVectorStore faissVectorStore;
    private final EmailNotificationService emailNotificationService;
    
    /**
     * 处理所有知识内容的向量化
     * 
     * @return 处理的记录数
     */
    public int processAllKnowledgeContent() {
        LocalDateTime startTime = LocalDateTime.now();
        long processingStart = System.currentTimeMillis();
        
        try {
            log.info("开始处理所有知识内容的向量化...");
            
            // 1. 从API获取所有知识内容
            List<KnowledgeContentDto> contents = apiClientService.getAllKnowledgeContent();
            for (KnowledgeContentDto content : contents) {
                log.info("处理知识内容: {}", content.getSummary());
            }
            
            if (contents.isEmpty()) {
                log.warn("未获取到任何知识内容");
                
                // 发送空结果通知
                sendNotification(startTime, 0, 0, 0, 
                        System.currentTimeMillis() - processingStart, "全量导入", null);
                return 0;
            }
            
            log.info("获取到 {} 条知识内容记录", contents.size());
            
            // 2. 批量向量化处理
            ProcessResult result = processKnowledgeContentBatchWithResult(contents);
            
            // 3. 发送成功通知
            sendNotification(startTime, result.successCount, result.failureCount, 
                    contents.size(), System.currentTimeMillis() - processingStart, 
                    "全量导入", result.errorMessage);
            
            return result.successCount;
            
        } catch (Exception e) {
            log.error("处理知识内容向量化失败", e);
            
            // 发送失败通知
            sendNotification(startTime, 0, 0, 0, 
                    System.currentTimeMillis() - processingStart, "全量导入", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 处理指定页面的知识内容向量化
     * 
     * @param page 页码
     * @param size 页面大小
     * @return 处理的记录数
     */
    public int processKnowledgeContentPage(int page, int size) {
        LocalDateTime startTime = LocalDateTime.now();
        long processingStart = System.currentTimeMillis();
        
        try {
            log.info("开始处理第 {} 页知识内容的向量化 (页面大小: {})", page, size);
            
            // 1. 从API获取指定页面的知识内容
            List<KnowledgeContentDto> contents = apiClientService.getAllKnowledgeContent(page, size);
            
            if (contents.isEmpty()) {
                log.warn("第 {} 页未获取到任何知识内容", page);
                
                // 发送空结果通知
                sendNotification(startTime, 0, 0, 0, 
                        System.currentTimeMillis() - processingStart, 
                        "分页导入(第" + page + "页)", null);
                return 0;
            }
            
            log.info("获取到第 {} 页 {} 条知识内容记录", page, contents.size());
            
            // 2. 批量向量化处理
            ProcessResult result = processKnowledgeContentBatchWithResult(contents);
            
            // 3. 发送成功通知
            sendNotification(startTime, result.successCount, result.failureCount, 
                    contents.size(), System.currentTimeMillis() - processingStart, 
                    "分页导入(第" + page + "页)", result.errorMessage);
            
            return result.successCount;
            
        } catch (Exception e) {
            log.error("处理第 {} 页知识内容向量化失败", page, e);
            
            // 发送失败通知
            sendNotification(startTime, 0, 0, 0, 
                    System.currentTimeMillis() - processingStart, 
                    "分页导入(第" + page + "页)", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 处理单个知识内容的向量化
     * 
     * @param contentId 内容ID
     * @return 是否处理成功
     */
    public boolean processSingleKnowledgeContent(Long contentId) {
        try {
            log.info("开始处理单个知识内容的向量化: ID={}", contentId);
            
            // 1. 从API获取指定知识内容
            KnowledgeContentDto content = apiClientService.getKnowledgeContentById(contentId);
            
            if (content == null) {
                log.warn("未找到ID为 {} 的知识内容", contentId);
                return false;
            }
            
            // 2. 向量化处理
            return processKnowledgeContent(content);
            
        } catch (Exception e) {
            log.error("处理单个知识内容向量化失败: ID={}", contentId, e);
            return false;
        }
    }
    
    /**
     * 批量处理知识内容向量化
     * 
     * @param contents 知识内容列表
     * @return 处理的记录数
     */
    private int processKnowledgeContentBatch(List<KnowledgeContentDto> contents) {
        int processedCount = 0;
        int successCount = 0;
        
        try {
            // 批量向量化
            Map<Long, float[]> vectors = new java.util.HashMap<>();
            Map<Long, KnowledgeContentDto> contentMap = new java.util.HashMap<>();
            
            for (KnowledgeContentDto content : contents) {
                try {
                    // 向量化
                    float[] vector = vectorizationService.vectorize(content);
                    vectors.put(content.getId(), vector);
                    contentMap.put(content.getId(), content);
                    
                    processedCount++;
                    
                    if (processedCount % 10 == 0) {
                        log.info("已处理 {} 条记录", processedCount);
                    }
                    
                } catch (Exception e) {
                    log.error("向量化失败: ID={}, 标题={}", content.getId(), content.getTitle(), e);
                }
            }
            
            // 批量存储到FAISS
            if (!vectors.isEmpty()) {
                faissVectorStore.addVectors(vectors, contentMap);
                successCount = vectors.size();
                log.info("批量向量化完成: 处理 {} 条，成功 {} 条", processedCount, successCount);
            }
            
        } catch (Exception e) {
            log.error("批量向量化处理失败", e);
        }
        
        return successCount;
    }
    
    /**
     * 处理单个知识内容向量化
     * 
     * @param content 知识内容
     * @return 是否处理成功
     */
    private boolean processKnowledgeContent(KnowledgeContentDto content) {
        try {
            // 向量化
            float[] vector = vectorizationService.vectorize(content);
            
            // 存储到FAISS
            faissVectorStore.addVector(content.getId(), vector, content);
            
            log.info("成功处理知识内容向量化: ID={}, 标题={}", content.getId(), content.getTitle());
            return true;
            
        } catch (Exception e) {
            log.error("处理知识内容向量化失败: ID={}, 标题={}", content.getId(), content.getTitle(), e);
            return false;
        }
    }
    
    /**
     * 搜索相似内容
     * 
     * @param queryText 查询文本
     * @param topK 返回前K个结果
     * @return 相似度结果列表
     */
    public List<FaissVectorStore.SimilarityResult> searchSimilarContent(String queryText, int topK) {
        try {
            log.info("搜索相似内容: query={}, topK={}", queryText, topK);
            
            // 创建查询向量
            KnowledgeContentDto queryContent = new KnowledgeContentDto();
            queryContent.setTitle(queryText);
            queryContent.setContent(queryText);
            
            float[] queryVector = vectorizationService.vectorize(queryContent);
            
            // 在FAISS中搜索
            List<FaissVectorStore.SimilarityResult> results = faissVectorStore.searchSimilar(queryVector, topK);
            
            log.info("搜索完成: 找到 {} 个相似结果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("搜索相似内容失败: query={}", queryText, e);
            return List.of();
        }
    }
    
    /**
     * 获取向量存储统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getVectorStoreStats() {
        try {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("vectorCount", faissVectorStore.getVectorCount());
            
            // 测试API连接状态
            boolean apiConnectionStatus = apiClientService.testConnection();
            stats.put("apiConnectionStatus", apiConnectionStatus);
            
            // 如果API连接正常，获取远程统计信息
            if (apiConnectionStatus) {
                try {
                    Map<String, Object> remoteStats = apiClientService.getStatistics();
                    stats.put("remoteStats", remoteStats);
                } catch (Exception e) {
                    log.warn("获取远程统计信息失败", e);
                }
            }
            
            log.info("向量存储统计: {}", stats);
            return stats;
            
        } catch (Exception e) {
            log.error("获取向量存储统计信息失败", e);
            return Map.of("error", e.getMessage());
        }
    }
    
    /**
     * 仅处理已成功的知识内容向量化
     * 
     * @return 处理的记录数
     */
    public int processSuccessfulKnowledgeContent() {
        try {
            log.info("开始处理已成功的知识内容向量化...");
            
            // 获取已处理且成功的内容
            List<KnowledgeContentDto> contents = apiClientService.getProcessedKnowledgeContent(0, 100);
            
            if (contents.isEmpty()) {
                log.warn("未获取到已处理的知识内容");
                return 0;
            }
            
            log.info("获取到 {} 条已处理的知识内容记录", contents.size());
            
            // 批量向量化处理
            return processKnowledgeContentBatch(contents);
            
        } catch (Exception e) {
            log.error("处理已成功知识内容向量化失败", e);
            return 0;
        }
    }
    
    /**
     * 根据内容类型处理向量化
     * 
     * @param contentType 内容类型（RSS/Web/Manual）
     * @return 处理的记录数
     */
    public int processKnowledgeContentByType(String contentType) {
        try {
            log.info("开始处理{}类型的知识内容向量化...", contentType);
            
            // 获取指定类型的内容
            List<KnowledgeContentDto> contents = apiClientService.getKnowledgeContentByType(contentType, 0, 100);
            
            if (contents.isEmpty()) {
                log.warn("未获取到{}类型的知识内容", contentType);
                return 0;
            }
            
            log.info("获取到 {} 条{}类型的知识内容记录", contents.size(), contentType);
            
            // 批量向量化处理
            return processKnowledgeContentBatch(contents);
            
        } catch (Exception e) {
            log.error("处理{}类型知识内容向量化失败", contentType, e);
            return 0;
        }
    }
    
    /**
     * 根据标签处理向量化
     * 
     * @param tags 标签字符串（逗号分隔）
     * @return 处理的记录数
     */
    public int processKnowledgeContentByTags(String tags) {
        try {
            log.info("开始处理标签为'{}'的知识内容向量化...", tags);
            
            // 获取指定标签的内容
            List<KnowledgeContentDto> contents = apiClientService.getKnowledgeContentByTags(tags, 0, 100);
            
            if (contents.isEmpty()) {
                log.warn("未获取到标签为'{}'的知识内容", tags);
                return 0;
            }
            
            log.info("获取到 {} 条标签为'{}'的知识内容记录", contents.size(), tags);
            
            // 批量向量化处理
            return processKnowledgeContentBatch(contents);
            
        } catch (Exception e) {
            log.error("处理标签为'{}'的知识内容向量化失败", tags, e);
            return 0;
        }
    }
    
    /**
     * 清空向量存储
     */
    public void clearVectorStore() {
        try {
            faissVectorStore.clearAll();
            log.info("向量存储已清空");
        } catch (Exception e) {
            log.error("清空向量存储失败", e);
        }
    }
    
    /**
     * 保存向量存储到文件
     */
    public void saveVectorStore() {
        try {
            faissVectorStore.saveToFile();
            log.info("向量存储已保存到文件");
        } catch (Exception e) {
            log.error("保存向量存储失败", e);
        }
    }
    
    /**
     * 从文件加载向量存储
     */
    public void loadVectorStore() {
        try {
            faissVectorStore.loadFromFile();
            log.info("向量存储已从文件加载");
        } catch (Exception e) {
            log.error("加载向量存储失败", e);
        }
    }
    
    /**
     * 处理结果内部类
     */
    private static class ProcessResult {
        int successCount;
        int failureCount;
        String errorMessage;
        
        ProcessResult(int successCount, int failureCount, String errorMessage) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errorMessage = errorMessage;
        }
    }
    
    /**
     * 批量处理知识内容向量化（带结果返回）
     * 
     * @param contents 知识内容列表
     * @return 处理结果
     */
    private ProcessResult processKnowledgeContentBatchWithResult(List<KnowledgeContentDto> contents) {
        int processedCount = 0;
        int successCount = 0;
        StringBuilder errorMessages = new StringBuilder();
        
        try {
            // 批量向量化
            Map<Long, float[]> vectors = new java.util.HashMap<>();
            Map<Long, KnowledgeContentDto> contentMap = new java.util.HashMap<>();
            
            for (KnowledgeContentDto content : contents) {
                try {
                    // 向量化
                    float[] vector = vectorizationService.vectorize(content);
                    vectors.put(content.getId(), vector);
                    contentMap.put(content.getId(), content);
                    
                    processedCount++;
                    
                    if (processedCount % 10 == 0) {
                        log.info("已处理 {} 条记录", processedCount);
                    }
                    
                } catch (Exception e) {
                    log.error("向量化失败: ID={}, 标题={}", content.getId(), content.getTitle(), e);
                    if (errorMessages.length() > 0) {
                        errorMessages.append("; ");
                    }
                    errorMessages.append("ID=").append(content.getId()).append(": ").append(e.getMessage());
                }
            }
            
            // 批量存储到FAISS
            if (!vectors.isEmpty()) {
                faissVectorStore.addVectors(vectors, contentMap);
                successCount = vectors.size();
                log.info("批量向量化完成: 处理 {} 条，成功 {} 条", processedCount, successCount);
            }
            
        } catch (Exception e) {
            log.error("批量向量化处理失败", e);
            errorMessages.append("批量处理失败: ").append(e.getMessage());
        }
        
        int failureCount = contents.size() - successCount;
        String errorMessage = errorMessages.length() > 0 ? errorMessages.toString() : null;
        
        return new ProcessResult(successCount, failureCount, errorMessage);
    }
    
    /**
     * 发送邮件通知
     * 
     * @param importTime 导入时间
     * @param successCount 成功数量
     * @param failureCount 失败数量
     * @param totalCount 总数量
     * @param processingTimeMs 处理时长
     * @param importType 导入类型
     * @param errorMessage 错误信息
     */
    private void sendNotification(LocalDateTime importTime, int successCount, int failureCount, 
                                  int totalCount, long processingTimeMs, String importType, String errorMessage) {
        try {
            ImportNotificationDto notification = ImportNotificationDto.builder()
                    .importTime(importTime)
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .totalCount(totalCount)
                    .processingTimeMs(processingTimeMs)
                    .importType(importType)
                    .errorMessage(errorMessage)
                    .build();
            
            // 异步发送邮件通知
            emailNotificationService.sendImportSuccessNotification(notification);
            
        } catch (Exception e) {
            log.error("发送邮件通知失败: {}", e.getMessage(), e);
        }
    }
}
