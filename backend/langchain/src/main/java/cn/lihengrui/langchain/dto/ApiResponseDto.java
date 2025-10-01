package cn.lihengrui.langchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API响应DTO类
 * 用于映射从todo-backend API返回的响应数据
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {
    
    /**
     * 响应代码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 是否成功
     */
    private boolean success;
}
