package cn.lihengrui.todotask.service;

import cn.lihengrui.todotask.dto.*;
import cn.lihengrui.todotask.entity.KnowledgeContent;
import cn.lihengrui.todotask.repository.KnowledgeContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识内容服务类
 *
 * 提供知识内容的业务逻辑处理，包括：
 * 1. CRUD操作的业务逻辑
 * 2. 复杂查询和过滤
 * 3. 批量操作处理
 * 4. 数据统计和分析
 * 5. 搜索功能
 *
 * 该服务作为控制器和数据访问层之间的桥梁，
 * 处理业务规则、数据转换和事务管理。
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Service
@Transactional
@Slf4j
public class KnowledgeContentService {

    @Autowired
    private KnowledgeContentRepository knowledgeContentRepository;

    @Autowired
    private AIProcessingService aiProcessingService;

    /**
     * 根据过滤条件查询知识内容（支持分页）
     *
     * @param pageable 分页参数
     * @param contentType 内容类型过滤
     * @param processed 处理状态过滤
     * @param success 成功状态过滤
     * @param keyword 关键词搜索
     * @param tags 标签过滤
     * @param beginTime 开始时间（格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     * @param endTime 结束时间（格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     * @return 分页的知识内容摘要列表
     */
    @Transactional(readOnly = true)
    public Page<KnowledgeContentSummary> findWithFilters(Pageable pageable, String contentType, 
            Boolean processed, Boolean success, String keyword, String tags, String beginTime, String endTime) {
        
        Specification<KnowledgeContent> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 开始时间过滤
            if (beginTime != null && !beginTime.isEmpty()) {
                try {
                    LocalDateTime startDateTime = parseDateTime(beginTime, true);
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("acquisitionTime"), startDateTime));
                } catch (DateTimeParseException e) {
                    log.warn("开始时间格式不正确: {}", beginTime, e);
                }
            }
            
            // 结束时间过滤
            if (endTime != null && !endTime.isEmpty()) {
                try {
                    LocalDateTime endDateTime = parseDateTime(endTime, false);
                    log.info("解析结束时间: 输入='{}', 解析结果='{}', acquisitionTime='{}'", endTime, endDateTime,root.get("acquisitionTime"));
                    predicates.add(criteriaBuilder.lessThan(root.get("acquisitionTime"), endDateTime));
                } catch (DateTimeParseException e) {
                    log.warn("结束时间格式不正确: {}", endTime, e);
                }
            }
            // 内容类型过滤
            if (contentType != null && !contentType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("contentType"), contentType));
            }

            // 处理状态过滤
            if (processed != null) {
                predicates.add(criteriaBuilder.equal(root.get("processed"), processed));
            }

            // 成功状态过滤
            if (success != null) {
                predicates.add(criteriaBuilder.equal(root.get("success"), success));
            }

            // 关键词搜索（在标题和内容中搜索）
            if (keyword != null && !keyword.isEmpty()) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                Predicate titleSearch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate contentSearch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("content")), searchPattern);
                predicates.add(criteriaBuilder.or(titleSearch, contentSearch));
            }

            // 标签过滤
            if (tags != null && !tags.isEmpty()) {
                String tagPattern = "%" + tags.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("tags")), tagPattern));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<KnowledgeContent> entityPage = knowledgeContentRepository.findAll(spec, pageable);
        
        List<KnowledgeContentSummary> summaries = entityPage.getContent().stream()
                .map(KnowledgeContentSummary::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, entityPage.getTotalElements());
    }

    /**
     * 根据ID查询知识内容
     *
     * @param id 内容ID
     * @return 知识内容详情
     */
    @Transactional(readOnly = true)
    public Optional<KnowledgeContentResponse> findById(Long id) {
        return knowledgeContentRepository.findById(id)
                .map(KnowledgeContentResponse::fromEntity);
    }

    /**
     * 创建新的知识内容
     *
     * @param request 创建请求
     * @return 创建的知识内容
     */
    public KnowledgeContentResponse create(KnowledgeContentRequest request) {
        KnowledgeContent entity = new KnowledgeContent();
        mapRequestToEntity(request, entity);
        entity.setAcquisitionTime(LocalDateTime.now());

        // 如果是手动创建且内容不为空，可以尝试AI处理
        if ("Manual".equals(request.getContentType()) && 
            request.getContent() != null && !request.getContent().trim().isEmpty()) {
            try {
                entity = aiProcessingService.processContent(entity);
                log.info("新创建的内容已完成AI处理: {}", entity.getTitle());
            } catch (Exception e) {
                log.warn("新创建内容的AI处理失败: {}", entity.getTitle(), e);
                entity.setProcessed(false);
                entity.setErrorMessage("AI处理失败: " + e.getMessage());
            }
        }

        KnowledgeContent saved = knowledgeContentRepository.save(entity);
        return KnowledgeContentResponse.fromEntity(saved);
    }

    /**
     * 更新知识内容
     *
     * @param id 内容ID
     * @param request 更新请求
     * @return 更新后的知识内容
     */
    public Optional<KnowledgeContentResponse> update(Long id, KnowledgeContentRequest request) {
        return knowledgeContentRepository.findById(id)
                .map(entity -> {
                    mapRequestToEntity(request, entity);
                    KnowledgeContent saved = knowledgeContentRepository.save(entity);
                    return KnowledgeContentResponse.fromEntity(saved);
                });
    }

    /**
     * 删除知识内容
     *
     * @param id 内容ID
     * @return 是否成功删除
     */
    public boolean delete(Long id) {
        if (knowledgeContentRepository.existsById(id)) {
            knowledgeContentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 更新处理状态
     *
     * @param id 内容ID
     * @param processed 新的处理状态
     * @return 更新后的知识内容
     */
    public Optional<KnowledgeContentResponse> updateProcessingStatus(Long id, Boolean processed) {
        return knowledgeContentRepository.findById(id)
                .map(entity -> {
                    boolean wasProcessed = entity.isProcessed(); // 记录原始状态
                    entity.setProcessed(processed);
                    
                    // 如果设置为需要处理且原来未处理，尝试AI处理
                    if (processed && !wasProcessed && 
                        entity.getContent() != null && !entity.getContent().trim().isEmpty()) {
                        try {
                            entity = aiProcessingService.processContent(entity);
                            log.info("内容重新AI处理完成: {}", entity.getTitle());
                        } catch (Exception e) {
                            log.warn("内容重新AI处理失败: {}", entity.getTitle(), e);
                            entity.setErrorMessage("AI处理失败: " + e.getMessage());
                        }
                    }
                    
                    KnowledgeContent saved = knowledgeContentRepository.save(entity);
                    return KnowledgeContentResponse.fromEntity(saved);
                });
    }

    /**
     * 批量操作
     *
     * @param request 批量操作请求
     * @return 影响的记录数
     */
    public int batchOperation(BatchOperationRequest request) {
        List<Long> ids = request.getIds();
        String operation = request.getOperation();
        
        switch (operation.toUpperCase()) {
            case "DELETE":
                return batchDelete(ids);
            case "UPDATE_STATUS":
                Boolean newStatus = Boolean.valueOf(request.getParameter());
                return batchUpdateStatus(ids, newStatus);
            case "ADD_TAGS":
                return batchAddTags(ids, request.getParameter());
            case "REMOVE_TAGS":
                return batchRemoveTags(ids, request.getParameter());
            default:
                throw new IllegalArgumentException("不支持的批量操作类型: " + operation);
        }
    }

    /**
     * 批量删除
     *
     * @param ids ID列表
     * @return 删除的记录数
     */
    private int batchDelete(List<Long> ids) {
        List<KnowledgeContent> entities = knowledgeContentRepository.findAllById(ids);
        knowledgeContentRepository.deleteAll(entities);
        return entities.size();
    }

    /**
     * 批量更新状态
     *
     * @param ids ID列表
     * @param processed 新的处理状态
     * @return 更新的记录数
     */
    private int batchUpdateStatus(List<Long> ids, Boolean processed) {
        List<KnowledgeContent> entities = knowledgeContentRepository.findAllById(ids);
        
        for (KnowledgeContent entity : entities) {
            entity.setProcessed(processed);
            
            // 如果设置为需要处理，尝试AI处理
            if (processed && entity.getContent() != null && !entity.getContent().trim().isEmpty()) {
                try {
                    entity = aiProcessingService.processContent(entity);
                } catch (Exception e) {
                    log.warn("批量AI处理失败: {}", entity.getTitle(), e);
                    entity.setErrorMessage("AI处理失败: " + e.getMessage());
                }
            }
        }
        
        knowledgeContentRepository.saveAll(entities);
        return entities.size();
    }

    /**
     * 批量添加标签
     *
     * @param ids ID列表
     * @param tagsToAdd 要添加的标签
     * @return 更新的记录数
     */
    private int batchAddTags(List<Long> ids, String tagsToAdd) {
        List<KnowledgeContent> entities = knowledgeContentRepository.findAllById(ids);
        
        for (KnowledgeContent entity : entities) {
            String currentTags = entity.getTags();
            if (currentTags == null || currentTags.isEmpty()) {
                entity.setTags(tagsToAdd);
            } else {
                entity.setTags(currentTags + "," + tagsToAdd);
            }
        }
        
        knowledgeContentRepository.saveAll(entities);
        return entities.size();
    }

    /**
     * 批量移除标签
     *
     * @param ids ID列表
     * @param tagsToRemove 要移除的标签
     * @return 更新的记录数
     */
    private int batchRemoveTags(List<Long> ids, String tagsToRemove) {
        List<KnowledgeContent> entities = knowledgeContentRepository.findAllById(ids);
        
        for (KnowledgeContent entity : entities) {
            String currentTags = entity.getTags();
            if (currentTags != null && !currentTags.isEmpty()) {
                String updatedTags = Arrays.stream(currentTags.split(","))
                        .filter(tag -> !tag.trim().equals(tagsToRemove.trim()))
                        .collect(Collectors.joining(","));
                entity.setTags(updatedTags);
            }
        }
        
        knowledgeContentRepository.saveAll(entities);
        return entities.size();
    }

    /**
     * 搜索知识内容
     *
     * @param query 搜索关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    @Transactional(readOnly = true)
    public Page<KnowledgeContentSummary> search(String query, Pageable pageable) {
        return findWithFilters(pageable, null, null, null, query, null, null, null);
    }

    /**
     * 获取统计信息
     *
     * @return 统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总数统计
        long totalCount = knowledgeContentRepository.count();
        stats.put("totalCount", totalCount);
        
        // 按类型统计
        Map<String, Long> countByType = new HashMap<>();
        countByType.put("RSS", knowledgeContentRepository.countByContentType("RSS"));
        countByType.put("Web", knowledgeContentRepository.countByContentType("Web"));
        countByType.put("Manual", knowledgeContentRepository.countByContentType("Manual"));
        stats.put("countByType", countByType);
        
        // 按状态统计
        Map<String, Long> countByStatus = new HashMap<>();
        countByStatus.put("processed", knowledgeContentRepository.countByProcessed(true));
        countByStatus.put("unprocessed", knowledgeContentRepository.countByProcessed(false));
        countByStatus.put("success", knowledgeContentRepository.countBySuccess(true));
        countByStatus.put("failed", knowledgeContentRepository.countBySuccess(false));
        stats.put("countByStatus", countByStatus);
        
        // 最近添加的统计
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        Map<String, Long> recentAdditions = new HashMap<>();
        recentAdditions.put("lastDay", knowledgeContentRepository.countByAcquisitionTimeAfter(oneDayAgo));
        recentAdditions.put("lastWeek", knowledgeContentRepository.countByAcquisitionTimeAfter(oneWeekAgo));
        stats.put("recentAdditions", recentAdditions);
        
        return stats;
    }

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        List<KnowledgeContent> allContents = knowledgeContentRepository.findAll();
        
        Set<String> uniqueTags = new HashSet<>();
        for (KnowledgeContent content : allContents) {
            if (content.getTags() != null && !content.getTags().isEmpty()) {
                String[] tags = content.getTags().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        uniqueTags.add(trimmedTag);
                    }
                }
            }
        }
        
        return new ArrayList<>(uniqueTags);
    }

    /**
     * 解析时间字符串为LocalDateTime
     * 支持格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
     * 
     * @param timeString 时间字符串
     * @param isStartTime 是否为开始时间（用于日期格式时确定具体时间）
     * @return LocalDateTime对象
     * @throws DateTimeParseException 时间格式不正确时抛出
     */
    private LocalDateTime parseDateTime(String timeString, boolean isStartTime) throws DateTimeParseException {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        
        String trimmedTime = timeString.trim();
        
        // 尝试解析完整的日期时间格式 yyyy-MM-dd HH:mm:ss
        try {
            return LocalDateTime.parse(trimmedTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            // 如果失败，尝试解析日期格式 yyyy-MM-dd
            try {
                if (isStartTime) {
                    // 开始时间：设置为当天的00:00:00
                    return LocalDateTime.parse(trimmedTime + " 00:00:00", 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    // 结束时间：设置为下一天的00:00:00（使用严格小于）
                    LocalDateTime date = LocalDate.parse(trimmedTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .atStartOfDay();
                    return date.plusDays(1);
                    // return date;
                }
            } catch (DateTimeParseException ex) {
                // 如果还是失败，尝试ISO格式
                return LocalDateTime.parse(trimmedTime);
            }
        }
    }

    /**
     * 将请求DTO映射到实体
     *
     * @param request 请求DTO
     * @param entity 实体对象
     */
    private void mapRequestToEntity(KnowledgeContentRequest request, KnowledgeContent entity) {
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setSourceUrl(request.getSourceUrl());
        entity.setContentType(request.getContentType());
        entity.setTags(request.getTags());
        
        if (request.getProcessed() != null) {
            entity.setProcessed(request.getProcessed());
        }
        
        entity.setSuccess(request.getSuccess());
        entity.setErrorMessage(request.getErrorMessage());
    }

    // ==================== 聚类分析相关方法 ====================

    /**
     * 获取聚类分析数据
     *
     * @return 聚类分析结果
     */
    public Map<String, Object> getClusterAnalysis() {
        log.info("开始执行聚类分析");
        
        List<KnowledgeContent> allContent = knowledgeContentRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        
        // 模拟聚类分析逻辑
        List<Map<String, Object>> clusters = performClustering(allContent);
        List<List<Double>> scatterData = generateScatterData(allContent);
        
        result.put("clusters", clusters);
        result.put("scatterData", scatterData);
        
        log.info("聚类分析完成，生成 {} 个聚类", clusters.size());
        return result;
    }

    /**
     * 获取聚类统计信息
     *
     * @return 聚类统计数据
     */
    public Map<String, Object> getClusterStats() {
        log.info("获取聚类统计信息");
        
        long totalDocuments = knowledgeContentRepository.count();
        List<String> allTags = getAllTags();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClusters", Math.min(8, (int) Math.ceil(totalDocuments / 3.0))); // 模拟聚类数量
        stats.put("totalDocuments", totalDocuments);
        stats.put("totalTags", allTags.size());
        stats.put("avgSimilarity", 75); // 模拟平均相似度
        
        return stats;
    }

    /**
     * 获取词云数据
     *
     * @return 词云数据
     */
    public List<Map<String, Object>> getWordCloudData() {
        log.info("生成词云数据");
        
        List<String> allTags = getAllTags();
        List<KnowledgeContent> allContent = knowledgeContentRepository.findAll();
        
        // 统计词频
        Map<String, Integer> wordFreq = new HashMap<>();
        
        // 统计标签频率
        for (KnowledgeContent content : allContent) {
            if (content.getTags() != null && !content.getTags().trim().isEmpty()) {
                String[] tags = content.getTags().split("[,，;；\\s]+");
                for (String tag : tags) {
                    tag = tag.trim();
                    if (!tag.isEmpty()) {
                        wordFreq.put(tag, wordFreq.getOrDefault(tag, 0) + 1);
                    }
                }
            }
            
            // 从标题中提取关键词
            if (content.getTitle() != null) {
                String[] words = content.getTitle().split("[\\s\\-_]+");
                for (String word : words) {
                    word = word.trim();
                    if (word.length() > 1) {
                        wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }
        
        // 转换为词云格式
        return wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(50) // 限制词云词数
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取内容类型分布
     *
     * @return 内容类型分布数据
     */
    public List<Map<String, Object>> getContentTypeDistribution() {
        log.info("获取内容类型分布");
        
        List<Object[]> typeStats = knowledgeContentRepository.findContentTypeDistribution();
        
        return typeStats.stream()
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", row[0] != null ? row[0].toString() : "未知");
                    item.put("value", ((Number) row[1]).intValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取标签分析数据
     *
     * @return 标签分析数据
     */
    public Map<String, Object> getTagAnalysis() {
        log.info("执行标签分析");
        
        List<String> allTags = getAllTags();
        List<KnowledgeContent> allContent = knowledgeContentRepository.findAll();
        
        Map<String, Integer> tagFreq = new HashMap<>();
        
        for (KnowledgeContent content : allContent) {
            if (content.getTags() != null && !content.getTags().trim().isEmpty()) {
                String[] tags = content.getTags().split("[,，;；\\s]+");
                for (String tag : tags) {
                    tag = tag.trim();
                    if (!tag.isEmpty()) {
                        tagFreq.put(tag, tagFreq.getOrDefault(tag, 0) + 1);
                    }
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalTags", allTags.size());
        result.put("tagFrequency", tagFreq);
        result.put("topTags", tagFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                )));
        
        return result;
    }

    /**
     * 获取时间趋势分析
     *
     * @param period 时间周期
     * @return 时间趋势数据
     */
    public Map<String, Object> getTimeTrendAnalysis(String period) {
        log.info("获取时间趋势分析，周期: {}", period);
        
        int days;
        switch (period) {
            case "7d":
                days = 7;
                break;
            case "90d":
                days = 90;
                break;
            default:
                days = 30;
                break;
        }
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<KnowledgeContent> recentContent = knowledgeContentRepository
                .findByAcquisitionTimeBetween(startDate, endDate);
        
        // 按日期分组统计
        Map<LocalDate, Long> dailyCounts = recentContent.stream()
                .collect(Collectors.groupingBy(
                        content -> content.getAcquisitionTime().toLocalDate(),
                        Collectors.counting()
                ));
        
        // 生成完整的日期序列
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            counts.add(dailyCounts.getOrDefault(date, 0L));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        result.put("period", period);
        result.put("totalCount", recentContent.size());
        
        return result;
    }

    /**
     * 执行聚类分析（模拟实现）
     *
     * @param contents 内容列表
     * @return 聚类结果
     */
    private List<Map<String, Object>> performClustering(List<KnowledgeContent> contents) {
        List<Map<String, Object>> clusters = new ArrayList<>();
        
        // 按内容类型进行简单聚类
        Map<String, List<KnowledgeContent>> typeGroups = contents.stream()
                .collect(Collectors.groupingBy(
                        content -> content.getContentType() != null ? content.getContentType() : "未知"
                ));
        
        int clusterId = 0;
        for (Map.Entry<String, List<KnowledgeContent>> entry : typeGroups.entrySet()) {
            Map<String, Object> cluster = new HashMap<>();
            cluster.put("clusterId", clusterId++);
            cluster.put("clusterName", entry.getKey() + "聚类");
            cluster.put("documentCount", entry.getValue().size());
            cluster.put("avgSimilarity", 0.7 + Math.random() * 0.2); // 模拟相似度
            
            // 提取关键词
            Set<String> keywords = new HashSet<>();
            Set<String> contentTypes = new HashSet<>();
            
            for (KnowledgeContent content : entry.getValue()) {
                if (content.getTags() != null) {
                    String[] tags = content.getTags().split("[,，;；\\s]+");
                    for (String tag : tags) {
                        tag = tag.trim();
                        if (!tag.isEmpty() && keywords.size() < 5) {
                            keywords.add(tag);
                        }
                    }
                }
                contentTypes.add(content.getContentType());
            }
            
            cluster.put("topKeywords", new ArrayList<>(keywords));
            cluster.put("contentTypes", new ArrayList<>(contentTypes));
            
            clusters.add(cluster);
        }
        
        return clusters;
    }

    /**
     * 生成散点图数据（模拟实现）
     *
     * @param contents 内容列表
     * @return 散点图数据
     */
    private List<List<Double>> generateScatterData(List<KnowledgeContent> contents) {
        List<List<Double>> scatterData = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < contents.size(); i++) {
            KnowledgeContent content = contents.get(i);
            List<Double> point = new ArrayList<>();
            
            // X坐标（模拟）
            point.add(random.nextGaussian() * 2 + (i % 3) * 5);
            // Y坐标（模拟）
            point.add(random.nextGaussian() * 2 + (i % 3) * 5);
            // 聚类ID
            point.add((double) (i % 3));
            // 相似度
            point.add(0.5 + random.nextDouble() * 0.5);
            
            scatterData.add(point);
        }
        
        return scatterData;
    }
}
