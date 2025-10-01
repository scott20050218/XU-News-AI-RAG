package cn.lihengrui.todotask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 知识内容请求DTO类
 *
 * 用于接收前端创建和更新知识内容的请求数据。
 * 包含必要的验证注解，确保数据的完整性和有效性。
 *
 * 主要用途：
 * 1. 创建新的知识内容
 * 2. 更新现有知识内容
 * 3. 数据验证和格式化
 * 4. 与前端API的数据交互
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeContentRequest {

    /**
     * 内容标题
     *
     * 必填字段，长度限制在1-500字符之间
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 500, message = "标题长度不能超过500字符")
    private String title;

    /**
     * 内容正文
     *
     * 可选字段，最大长度10000字符
     */
    @Size(max = 50000, message = "内容长度不能超过50000字符")
    private String content;

    /**
     * 来源URL
     *
     * 必填字段，记录内容的原始来源
     */
    @NotBlank(message = "来源URL不能为空")
    @Size(max = 2000, message = "URL长度不能超过2000字符")
    private String sourceUrl;

    /**
     * 内容类型
     *
     * 必填字段，可选值：RSS、Web、Manual（手动添加）
     */
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    /**
     * 标签信息
     *
     * 可选字段，以逗号分隔的标签字符串
     */
    @Size(max = 1000, message = "标签长度不能超过1000字符")
    private String tags;

    /**
     * 处理状态标识
     *
     * 标识内容是否已经过智能代理的深度处理
     */
    private Boolean processed;

    /**
     * 采集成功状态
     *
     * 标识内容采集是否成功
     */
    @NotNull(message = "成功状态不能为空")
    private Boolean success;

    /**
     * 错误信息
     *
     * 当采集失败时记录具体的错误信息
     */
    @Size(max = 1000, message = "错误信息长度不能超过1000字符")
    private String errorMessage;
}
