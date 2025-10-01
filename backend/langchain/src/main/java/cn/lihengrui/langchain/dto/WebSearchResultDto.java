package cn.lihengrui.langchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 联网搜索结果DTO
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSearchResultDto {
    
    /**
     * 搜索结果标题
     */
    private String title;
    
    /**
     * 搜索结果URL
     */
    private String url;
    
    /**
     * 搜索结果摘要
     */
    private String snippet;
    
    /**
     * 搜索来源
     */
    private String source;
    
    /**
     * 搜索时间
     */
    private String searchTime;
    
    /**
     * 相关性评分
     */
    private Double relevanceScore;
}
