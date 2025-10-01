package cn.lihengrui.langchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页DTO类
 * 用于映射分页响应数据
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDto<T> {
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 总元素数
     */
    private Long totalElements;
    
    /**
     * 页面大小
     */
    private Integer size;
    
    /**
     * 内容列表
     */
    private List<T> content;
    
    /**
     * 当前页码
     */
    private Integer number;
    
    /**
     * 是否第一页
     */
    private boolean first;
    
    /**
     * 是否最后一页
     */
    private boolean last;
    
    /**
     * 是否为空
     */
    private boolean empty;
    
    /**
     * 当前页元素数量
     */
    private Integer numberOfElements;
}
