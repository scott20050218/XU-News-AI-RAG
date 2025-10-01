package cn.lihengrui.langchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识检索响应DTO
 * 用于返回知识检索的完整响应
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 查询文本
     */
    private String query;
    
    /**
     * 检索结果列表
     */
    private List<KnowledgeSearchResult> results;
    
    /**
     * 结果数量
     */
    private Integer resultCount;
    
    /**
     * 总处理时间（毫秒）
     */
    private Long processingTimeMs;
    
    /**
     * 向量化时间（毫秒）
     */
    private Long vectorizationTimeMs;
    
    /**
     * 搜索时间（毫秒）
     */
    private Long searchTimeMs;
    
    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 平均相似度
     */
    private Double averageSimilarity;
    
    /**
     * 最高相似度
     */
    private Double maxSimilarity;
    
    /**
     * 最低相似度
     */
    private Double minSimilarity;
}
