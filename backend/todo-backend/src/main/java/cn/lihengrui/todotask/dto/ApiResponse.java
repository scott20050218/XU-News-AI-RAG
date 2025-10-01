package cn.lihengrui.todotask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应DTO类
 *
 * 为所有REST API提供统一的响应格式。
 * 包含状态码、消息、数据和时间戳等标准字段。
 *
 * 主要用途：
 * 1. 统一API响应格式
 * 2. 错误信息处理
 * 3. 成功响应包装
 * 4. 前端状态判断
 *
 * @param <T> 响应数据的类型
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 响应状态码
     * 200: 成功
     * 400: 客户端错误
     * 500: 服务器错误
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
     * 响应时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data, LocalDateTime.now(), true);
    }

    /**
     * 创建成功响应（带自定义消息）
     *
     * @param data 响应数据
     * @param message 自定义消息
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data, LocalDateTime.now(), true);
    }

    /**
     * 创建错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now(), false);
    }

    /**
     * 创建客户端错误响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 客户端错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null, LocalDateTime.now(), false);
    }

    /**
     * 创建服务器错误响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 服务器错误响应
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(500, message, null, LocalDateTime.now(), false);
    }

    /**
     * 创建未找到资源响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 未找到响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null, LocalDateTime.now(), false);
    }
}
