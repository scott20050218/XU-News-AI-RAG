package cn.lihengrui.langchain.controller;

import cn.lihengrui.langchain.service.EmailNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件测试控制器
 * 用于测试邮件发送功能
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Tag(name = "邮件测试", description = "邮件发送功能测试API")
public class EmailTestController {
    
    private final EmailNotificationService emailNotificationService;
    
    /**
     * 发送测试邮件
     */
    @PostMapping("/test")
    @Operation(summary = "发送测试邮件", description = "发送一封测试邮件到指定邮箱")
    public ResponseEntity<Map<String, Object>> sendTestEmail() {
        try {
            log.info("接收发送测试邮件请求");
            
            boolean success = emailNotificationService.sendTestEmail();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "测试邮件发送成功" : "测试邮件发送失败");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            log.error("发送测试邮件异常: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "发送测试邮件失败: " + e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 获取邮件配置信息
     */
    @GetMapping("/config")
    @Operation(summary = "获取邮件配置", description = "获取当前邮件配置信息（脱敏）")
    public ResponseEntity<Map<String, Object>> getEmailConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("success", true);
            config.put("message", "获取邮件配置成功");
            config.put("data", Map.of(
                "smtpHost", "smtp.office365.com",
                "smtpPort", 587,
                "targetEmail", "13888528521@163.com",
                "sslEnabled", true,
                "authEnabled", true
            ));
            config.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            log.error("获取邮件配置异常: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取邮件配置失败: " + e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

