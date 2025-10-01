package cn.lihengrui.langchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 增强搜索结果响应DTO
 * 包含知识库检索结果和联网搜索结果
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedSearchResponseDto {
    
    /**
     * 查询关键词
     */
    private String query;
    
    /**
     * 知识库检索结果
     */
    private List<KnowledgeSearchResult> knowledgeResults;
    
    /**
     * 知识库结果数量
     */
    private Integer knowledgeResultCount;
    
    /**
     * 联网搜索结果
     */
    private List<WebSearchResultDto> webSearchResults;
    
    /**
     * 联网搜索结果数量
     */
    private Integer webSearchResultCount;
    
    /**
     * 大模型推理结果
     */
    private String llmInference;
    
    /**
     * 是否触发了联网查询
     */
    private Boolean webSearchTriggered;
    
    /**
     * 总处理时间（毫秒）
     */
    private Long totalProcessingTimeMs;
    
    /**
     * 知识库检索时间（毫秒）
     */
    private Long knowledgeSearchTimeMs;
    
    /**
     * 联网搜索时间（毫秒）
     */
    private Long webSearchTimeMs;
    
    /**
     * 大模型推理时间（毫秒）
     */
    private Long llmInferenceTimeMs;
    
    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 平均相似度（知识库结果）
     */
    private Double averageSimilarity;
    
    /**
     * 最大相似度（知识库结果）
     */
    private Double maxSimilarity;
    
    /**
     * 最小相似度（知识库结果）
     */
    private Double minSimilarity;
    
    /**
     * 搜索方法
     */
    private String searchMethod;
}
