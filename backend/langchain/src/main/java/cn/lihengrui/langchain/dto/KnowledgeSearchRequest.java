package cn.lihengrui.langchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 知识检索请求DTO
 * 用于接收用户的知识检索请求
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户提问/查询文本
     */
    private String query;
    
    /**
     * 返回结果数量，默认10
     */
    private Integer topK = 10;
    
    /**
     * 最小相似度阈值，默认0.0
     */
    private Double minSimilarity = 0.0;
    
    /**
     * 内容类型过滤
     */
    private String contentType;
    
    /**
     * 是否只返回已处理的内容
     */
    private Boolean processedOnly = false;
}
