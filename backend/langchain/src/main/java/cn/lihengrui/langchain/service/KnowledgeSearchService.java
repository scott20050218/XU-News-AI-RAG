package cn.lihengrui.langchain.service;

import cn.lihengrui.langchain.dto.KnowledgeContentDto;
import cn.lihengrui.langchain.dto.KnowledgeSearchRequest;
import cn.lihengrui.langchain.dto.KnowledgeSearchResponse;
import cn.lihengrui.langchain.dto.KnowledgeSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识检索服务
 * 负责基于用户提问检索知识库内容
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSearchService {
    
    private final VectorizationService vectorizationService;
    private final FaissVectorStore faissVectorStore;
    
    /**
     * 基于用户提问检索知识库内容
     * 
     * @param request 检索请求
     * @return 检索响应
     */
    public KnowledgeSearchResponse searchKnowledge(KnowledgeSearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始知识检索: query={}, topK={}", request.getQuery(), request.getTopK());
            
            // 1. 向量化用户查询
            long vectorizationStart = System.currentTimeMillis();
            float[] queryVector = vectorizeQuery(request.getQuery());
            long vectorizationTime = System.currentTimeMillis() - vectorizationStart;
            
            // 2. 在向量库中搜索相似内容
            long searchStart = System.currentTimeMillis();
            List<FaissVectorStore.SimilarityResult> similarityResults = 
                    faissVectorStore.searchSimilar(queryVector, request.getTopK());
            long searchTime = System.currentTimeMillis() - searchStart;
            
            // 3. 过滤和转换结果
            List<KnowledgeSearchResult> results = filterAndConvertResults(
                    similarityResults, request);
            
            // 4. 计算统计信息
            double averageSimilarity = calculateAverageSimilarity(results);
            double maxSimilarity = results.isEmpty() ? 0.0 : results.get(0).getSimilarity();
            double minSimilarity = results.isEmpty() ? 0.0 : 
                    results.get(results.size() - 1).getSimilarity();
            
            // 5. 构建响应
            KnowledgeSearchResponse response = new KnowledgeSearchResponse();
            response.setQuery(request.getQuery());
            response.setResults(results);
            response.setResultCount(results.size());
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            response.setVectorizationTimeMs(vectorizationTime);
            response.setSearchTimeMs(searchTime);
            response.setTimestamp(LocalDateTime.now());
            response.setAverageSimilarity(averageSimilarity);
            response.setMaxSimilarity(maxSimilarity);
            response.setMinSimilarity(minSimilarity);
            
            log.info("知识检索完成: query={}, 结果数={}, 处理时间={}ms", 
                    request.getQuery(), results.size(), response.getProcessingTimeMs());
            
            return response;
            
        } catch (Exception e) {
            log.error("知识检索失败: query={}", request.getQuery(), e);
            
            // 返回空结果
            KnowledgeSearchResponse response = new KnowledgeSearchResponse();
            response.setQuery(request.getQuery());
            response.setResults(new ArrayList<>());
            response.setResultCount(0);
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            response.setTimestamp(LocalDateTime.now());
            response.setAverageSimilarity(0.0);
            response.setMaxSimilarity(0.0);
            response.setMinSimilarity(0.0);
            
            return response;
        }
    }
    
    /**
     * 向量化用户查询
     */
    private float[] vectorizeQuery(String query) {
        // 创建临时的知识内容对象用于向量化
        KnowledgeContentDto tempContent = new KnowledgeContentDto();
        tempContent.setTitle(query);
        tempContent.setContent(query);
        
        return vectorizationService.vectorize(tempContent);
    }
    
    /**
     * 过滤和转换结果
     */
    private List<KnowledgeSearchResult> filterAndConvertResults(
            List<FaissVectorStore.SimilarityResult> similarityResults, 
            KnowledgeSearchRequest request) {
        
        return similarityResults.stream()
                .filter(result -> result.getSimilarity() >= request.getMinSimilarity())
                .filter(result -> filterByContentType(result.getContent(), request.getContentType()))
                .filter(result -> filterByProcessedStatus(result.getContent(), request.getProcessedOnly()))
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
    }
    
    /**
     * 按内容类型过滤
     */
    private boolean filterByContentType(KnowledgeContentDto content, String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return true;
        }
        return contentType.equals(content.getContentType());
    }
    
    /**
     * 按处理状态过滤
     */
    private boolean filterByProcessedStatus(KnowledgeContentDto content, Boolean processedOnly) {
        if (processedOnly == null || !processedOnly) {
            return true;
        }
        return content.isProcessed();
    }
    
    /**
     * 转换为搜索结果
     */
    private KnowledgeSearchResult convertToSearchResult(FaissVectorStore.SimilarityResult similarityResult) {
        KnowledgeContentDto content = similarityResult.getContent();
        
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setId(content.getId());
        result.setTitle(content.getTitle());
        result.setSummary(content.getSummary());
        result.setContent(truncateContent(content.getContent(), 500));
        result.setSourceUrl(content.getSourceUrl());
        result.setContentType(content.getContentType());
        result.setAcquisitionTime(content.getAcquisitionTime());
        result.setTags(content.getTags());
        result.setSimilarity((double) similarityResult.getSimilarity());
        result.setProcessed(content.isProcessed());
        result.setSuccess(content.isSuccess());
        result.setContentLength(content.getContentLength());
        result.setTagArray(content.getTagArray() != null ? 
                content.getTagArray().toArray(new String[0]) : new String[0]);
        
        return result;
    }
    
    /**
     * 截取内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
    
    /**
     * 计算平均相似度
     */
    private double calculateAverageSimilarity(List<KnowledgeSearchResult> results) {
        if (results.isEmpty()) {
            return 0.0;
        }
        
        double sum = results.stream()
                .mapToDouble(KnowledgeSearchResult::getSimilarity)
                .sum();
        
        return sum / results.size();
    }
}
