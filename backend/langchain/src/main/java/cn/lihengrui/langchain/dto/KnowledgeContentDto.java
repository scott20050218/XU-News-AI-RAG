package cn.lihengrui.langchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识内容DTO类
 * 用于映射从todo-backend API获取的知识内容数据
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeContentDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @JsonProperty("knowId")
    private Long id;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容正文
     */
    private String content;
    
    /**
     * 来源URL
     */
    @JsonProperty("sourceUrl")
    private String sourceUrl;
    
    /**
     * 内容类型
     */
    @JsonProperty("contentType")
    private String contentType;
    
    /**
     * 采集时间
     */
    @JsonProperty("acquisitionTime")
    private LocalDateTime acquisitionTime;
    
    /**
     * 标签信息
     */
    private String tags;
    
    /**
     * 处理状态
     */
    private boolean processed;
    
    /**
     * 成功状态
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    /**
     * 内容摘要
     */
    private String summary;
    
    /**
     * 标签数组
     */
    @JsonProperty("primaryTags")
    private List<String> tagArray;
    
    /**
     * 内容长度
     */
    @JsonProperty("contentLength")
    private Integer contentLength;
}
