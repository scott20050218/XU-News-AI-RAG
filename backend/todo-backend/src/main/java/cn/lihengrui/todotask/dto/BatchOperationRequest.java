package cn.lihengrui.todotask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量操作请求DTO类
 *
 * 用于处理批量操作的请求，如批量删除、批量更新状态等。
 * 包含操作的ID列表和相关参数。
 *
 * 主要用途：
 * 1. 批量删除知识内容
 * 2. 批量更新处理状态
 * 3. 批量标签操作
 * 4. 批量数据管理
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationRequest {

    /**
     * 要操作的ID列表
     */
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;

    /**
     * 操作类型
     * 可选值：DELETE, UPDATE_STATUS, ADD_TAGS, REMOVE_TAGS
     */
    private String operation;

    /**
     * 操作参数
     * 根据操作类型的不同，包含不同的参数
     * 例如：对于UPDATE_STATUS，包含新的状态值
     *      对于ADD_TAGS，包含要添加的标签
     */
    private String parameter;

    /**
     * 是否强制执行
     * 对于某些危险操作（如删除），可能需要确认
     */
    private Boolean force = false;
}
