package cn.lihengrui.todotask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识内容响应DTO类
 *
 * 用于向前端返回知识内容的数据。
 * 包含完整的内容信息和元数据。
 *
 * 主要用途：
 * 1. 返回查询结果
 * 2. API响应数据格式化
 * 3. 前端数据展示
 * 4. 数据传输优化
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeContentResponse {

    /**
     * 知识内容主键ID
     */
    private Long knowId;

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
     * 标签信息
     */
    private String tags;

    /**
     * 处理状态标识
     */
    private Boolean processed;

    /**
     * 采集成功状态
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 内容摘要（截取前200字符）
     */
    private String summary;

    /**
     * 标签数组（将逗号分隔的标签转换为数组）
     */
    private String[] tagArray;

    /**
     * 内容长度
     */
    private Integer contentLength;

    /**
     * 从实体转换为响应DTO的静态方法
     *
     * @param entity 知识内容实体
     * @return 响应DTO
     */
    public static KnowledgeContentResponse fromEntity(cn.lihengrui.todotask.entity.KnowledgeContent entity) {
        if (entity == null) {
            return null;
        }

        KnowledgeContentResponse response = new KnowledgeContentResponse();
        response.setKnowId(entity.getKnowId());
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setSourceUrl(entity.getSourceUrl());
        response.setContentType(entity.getContentType());
        response.setAcquisitionTime(entity.getAcquisitionTime());
        response.setTags(entity.getTags());
        response.setProcessed(entity.isProcessed());
        response.setSuccess(entity.isSuccess());
        response.setErrorMessage(entity.getErrorMessage());

        // 生成摘要
        if (entity.getContent() != null) {
            response.setContentLength(entity.getContent().length());
            if (entity.getContent().length() > 200) {
                response.setSummary(entity.getContent().substring(0, 200) + "...");
            } else {
                response.setSummary(entity.getContent());
            }
        } else {
            response.setContentLength(0);
            response.setSummary("");
        }

        // 转换标签为数组
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            response.setTagArray(entity.getTags().split(","));
        } else {
            response.setTagArray(new String[0]);
        }

        return response;
    }
}
