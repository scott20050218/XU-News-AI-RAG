package cn.lihengrui.langchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 信息入库通知数据传输对象
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportNotificationDto {
    
    /**
     * 入库时间
     */
    private LocalDateTime importTime;
    
    /**
     * 成功导入数量
     */
    private int successCount;
    
    /**
     * 失败导入数量
     */
    private int failureCount;
    
    /**
     * 总处理数量
     */
    private int totalCount;
    
    /**
     * 处理耗时（毫秒）
     */
    private long processingTimeMs;
    
    /**
     * 导入类型（全量/分页/单个/按类型/按标签）
     */
    private String importType;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
    
    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }
    
    /**
     * 获取失败率
     */
    public double getFailureRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) failureCount / totalCount * 100;
    }
    
    /**
     * 是否导入成功
     */
    public boolean isImportSuccessful() {
        return successCount > 0 && errorMessage == null;
    }
}

