package cn.lihengrui.langchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识检索结果DTO
 * 用于返回知识检索的结果
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 内容ID
     */
    private Long id;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容摘要
     */
    private String summary;
    
    /**
     * 内容正文（截取前500字符）
     */
    private String content;
    
    /**
     * 来源URL
     */
    private String sourceUrl;
    
    /**
     * 内容类型
     */
    private String contentType;
    
    /**
     * 采集时间
     */
    private LocalDateTime acquisitionTime;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 相似度分数
     */
    private Double similarity;
    
    /**
     * 处理状态
     */
    private Boolean processed;
    
    /**
     * 成功状态
     */
    private Boolean success;
    
    /**
     * 内容长度
     */
    private Integer contentLength;
    
    /**
     * 标签数组
     */
    private String[] tagArray;
}
