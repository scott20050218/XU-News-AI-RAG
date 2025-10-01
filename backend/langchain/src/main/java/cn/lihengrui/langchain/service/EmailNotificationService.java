package cn.lihengrui.langchain.service;

import cn.lihengrui.langchain.dto.ImportNotificationDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

/**
 * 邮件通知服务
 * 负责发送信息入库成功的邮件通知
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${app.notification.email.from:noreply@ha72.com}")
    private String fromEmail;
    
    @Value("${app.notification.email.to:13888528521@163.com}")
    private String toEmail;
    
    @Value("${spring.application.name:langchain}")
    private String applicationName;
    
    /**
     * 发送信息入库成功通知邮件
     * 
     * @param notification 导入通知信息
     */
    public void sendImportSuccessNotification(ImportNotificationDto notification) {
        try {
            log.info("开始发送信息入库成功通知邮件: 成功={}, 失败={}", 
                    notification.getSuccessCount(), notification.getFailureCount());
            
            // 构建邮件
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // 设置邮件基本信息
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("信息入库成功");
            
            // 构建邮件内容
            String emailContent = buildEmailContent(notification);
            helper.setText(emailContent, true);
            
            // 发送邮件
            javaMailSender.send(message);
            
            log.info("信息入库成功通知邮件发送完成: to={}, 成功={}, 失败={}", 
                    toEmail, notification.getSuccessCount(), notification.getFailureCount());
            
        } catch (MessagingException e) {
            log.error("发送信息入库成功通知邮件失败: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("邮件发送过程中发生未知错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 构建邮件内容
     * 
     * @param notification 导入通知信息
     * @return 邮件HTML内容
     */
    private String buildEmailContent(ImportNotificationDto notification) {
        try {
            // 使用Thymeleaf模板
            Context context = new Context();
            context.setVariable("notification", notification);
            context.setVariable("applicationName", applicationName);
            context.setVariable("formatTime", notification.getImportTime().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return templateEngine.process("email/import-success", context);
            
        } catch (Exception e) {
            log.warn("使用模板构建邮件内容失败，使用简单文本格式: {}", e.getMessage());
            return buildSimpleEmailContent(notification);
        }
    }
    
    /**
     * 构建简单的邮件内容（备用方案）
     * 
     * @param notification 导入通知信息
     * @return 邮件HTML内容
     */
    private String buildSimpleEmailContent(ImportNotificationDto notification) {
        StringBuilder content = new StringBuilder();
        
        content.append("<!DOCTYPE html>");
        content.append("<html>");
        content.append("<head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<title>信息入库成功通知</title>");
        content.append("<style>");
        content.append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 20px; }");
        content.append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }");
        content.append(".content { padding: 20px; border: 1px solid #ddd; }");
        content.append(".stats { background-color: #f9f9f9; padding: 15px; margin: 10px 0; }");
        content.append(".success { color: #4CAF50; font-weight: bold; }");
        content.append(".failure { color: #f44336; font-weight: bold; }");
        content.append(".footer { margin-top: 20px; color: #666; font-size: 12px; }");
        content.append("</style>");
        content.append("</head>");
        content.append("<body>");
        
        // 邮件头部
        content.append("<div class='header'>");
        content.append("<h1>📧 信息入库成功通知</h1>");
        content.append("</div>");
        
        // 邮件内容
        content.append("<div class='content'>");
        content.append("<h2>导入统计信息</h2>");
        
        content.append("<div class='stats'>");
        content.append("<p><strong>📅 导入时间：</strong>")
               .append(notification.getImportTime().format(
                       DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")))
               .append("</p>");
        
        content.append("<p><strong>✅ 成功导入：</strong>")
               .append("<span class='success'>").append(notification.getSuccessCount()).append(" 条</span>")
               .append("</p>");
        
        content.append("<p><strong>❌ 失败导入：</strong>")
               .append("<span class='failure'>").append(notification.getFailureCount()).append(" 条</span>")
               .append("</p>");
        
        content.append("<p><strong>📊 总计：</strong>").append(notification.getTotalCount()).append(" 条</p>");
        
        content.append("<p><strong>⏱️ 处理耗时：</strong>").append(notification.getProcessingTimeMs()).append(" 毫秒</p>");
        
        content.append("<p><strong>📈 成功率：</strong>")
               .append(String.format("%.2f%%", notification.getSuccessRate()))
               .append("</p>");
        
        if (notification.getImportType() != null) {
            content.append("<p><strong>🔄 导入类型：</strong>").append(notification.getImportType()).append("</p>");
        }
        
        content.append("</div>");
        
        // 状态总结
        if (notification.isImportSuccessful()) {
            content.append("<div style='background-color: #d4edda; border: 1px solid #c3e6cb; color: #155724; padding: 15px; margin: 10px 0; border-radius: 5px;'>");
            content.append("<h3>🎉 导入状态：成功</h3>");
            content.append("<p>本次信息导入已成功完成，数据已存储到向量数据库中，可用于智能检索服务。</p>");
            content.append("</div>");
        } else {
            content.append("<div style='background-color: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; padding: 15px; margin: 10px 0; border-radius: 5px;'>");
            content.append("<h3>⚠️ 导入状态：部分失败</h3>");
            if (notification.getErrorMessage() != null) {
                content.append("<p><strong>错误信息：</strong>").append(notification.getErrorMessage()).append("</p>");
            }
            content.append("</div>");
        }
        
        content.append("</div>");
        
        // 邮件尾部
        content.append("<div class='footer'>");
        content.append("<p>此邮件由 ").append(applicationName).append(" 系统自动发送，请勿回复。</p>");
        content.append("<p>如有问题，请联系系统管理员。</p>");
        content.append("<p>发送时间：").append(java.time.LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        content.append("</div>");
        
        content.append("</body>");
        content.append("</html>");
        
        return content.toString();
    }
    
    /**
     * 测试邮件发送功能
     * 
     * @return 是否发送成功
     */
    public boolean sendTestEmail() {
        try {
            log.info("发送测试邮件到: {}", toEmail);
            
            // 创建测试通知
            ImportNotificationDto testNotification = ImportNotificationDto.builder()
                    .importTime(java.time.LocalDateTime.now())
                    .successCount(5)
                    .failureCount(0)
                    .totalCount(5)
                    .processingTimeMs(1500)
                    .importType("测试导入")
                    .build();
            
            // 直接发送测试邮件，不使用sendImportSuccessNotification避免异常被吞掉
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // 设置邮件基本信息
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("测试邮件 - 信息入库成功");
            
            // 构建简单测试邮件内容
            String emailContent = buildSimpleEmailContent(testNotification);
            helper.setText(emailContent, true);
            
            // 发送邮件
            javaMailSender.send(message);
            
            log.info("测试邮件发送成功: to={}", toEmail);
            return true;
            
        } catch (Exception e) {
            log.error("发送测试邮件失败: {}", e.getMessage(), e);
            return false;
        }
    }
}

