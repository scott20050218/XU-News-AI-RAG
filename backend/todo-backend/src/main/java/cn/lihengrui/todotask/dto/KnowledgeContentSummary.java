package cn.lihengrui.todotask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识内容摘要DTO类
 *
 * 用于列表查询时返回简化的内容信息，提高查询性能。
 * 只包含必要的字段，适用于分页列表、搜索结果等场景。
 *
 * 主要用途：
 * 1. 分页列表展示
 * 2. 搜索结果返回
 * 3. 性能优化的查询
 * 4. 移动端数据传输
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeContentSummary {

    /**
     * 知识内容主键ID
     */
    private Long knowId;

    /**
     * 内容标题
     */
    private String title;

    /**
     * 内容摘要（前200字符）
     */
    private String summary;

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
     * 主要标签（前3个）
     */
    private String[] primaryTags;

    /**
     * 处理状态标识
     */
    private Boolean processed;

    /**
     * 采集成功状态
     */
    private Boolean success;

    /**
     * 内容长度
     */
    private Integer contentLength;

    /**
     * 从实体转换为摘要DTO的静态方法
     *
     * @param entity 知识内容实体
     * @return 摘要DTO
     */
    public static KnowledgeContentSummary fromEntity(cn.lihengrui.todotask.entity.KnowledgeContent entity) {
        if (entity == null) {
            return null;
        }

        KnowledgeContentSummary summary = new KnowledgeContentSummary();
        summary.setKnowId(entity.getKnowId());
        summary.setTitle(entity.getTitle());
        summary.setSourceUrl(entity.getSourceUrl());
        summary.setContentType(entity.getContentType());
        summary.setAcquisitionTime(entity.getAcquisitionTime());
        summary.setProcessed(entity.isProcessed());
        summary.setSuccess(entity.isSuccess());

        // 生成内容摘要
        if (entity.getContent() != null) {
            summary.setContentLength(entity.getContent().length());
            if (entity.getContent().length() > 200) {
                summary.setSummary(entity.getContent().substring(0, 200) + "...");
            } else {
                summary.setSummary(entity.getContent());
            }
        } else {
            summary.setContentLength(0);
            summary.setSummary("");
        }

        // 提取主要标签（前3个）
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            String[] allTags = entity.getTags().split(",");
            if (allTags.length <= 3) {
                summary.setPrimaryTags(allTags);
            } else {
                String[] primaryTags = new String[3];
                System.arraycopy(allTags, 0, primaryTags, 0, 3);
                summary.setPrimaryTags(primaryTags);
            }
        } else {
            summary.setPrimaryTags(new String[0]);
        }

        return summary;
    }
}
