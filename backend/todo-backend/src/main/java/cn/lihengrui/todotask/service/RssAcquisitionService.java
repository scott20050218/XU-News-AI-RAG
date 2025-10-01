package cn.lihengrui.todotask.service;

import cn.lihengrui.todotask.entity.KnowledgeContent;
import cn.lihengrui.todotask.repository.KnowledgeContentRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * RSS数据采集服务类
 * 
 * 该服务负责定时从配置的RSS源采集最新的内容信息，解析RSS XML格式数据，
 * 并将提取的内容存储到数据库中，为知识管理系统提供自动化的内容来源。
 * 
 * 主要功能：
 * 1. 定时RSS源数据采集 - 每小时自动执行一次
 * 2. RSS XML解析 - 使用ROME库解析RSS格式
 * 3. 内容数据提取 - 提取标题、描述、链接、分类等信息
 * 4. 数据存储 - 将解析后的内容保存到数据库
 * 5. 错误处理和日志记录 - 记录采集过程和结果统计
 * 
 * 技术实现：
 * - 使用Spring的@Scheduled注解实现定时任务
 * - 使用ROME库解析RSS/Atom格式的数据源
 * - 集成Spring Data JPA进行数据持久化
 * - 使用SLF4J进行日志记录和监控
 * 
 * 配置说明：
 * - RSS源配置：目前硬编码在RSS_FEEDS常量中
 * - 定时间隔：固定每小时执行一次（3600000毫秒）
 * - 建议优化：RSS源配置应移至配置文件或数据库
 * 
 * 性能考虑：
 * - 网络超时：依赖网络连接，可能受到RSS源响应时间影响
 * - 内存使用：大量RSS条目可能占用较多内存
 * - 数据库性能：批量保存时可能影响数据库性能
 * 
 * 扩展方向：
 * 1. 支持动态RSS源配置
 * 2. 实现内容去重机制
 * 3. 添加RSS源状态监控
 * 4. 支持增量更新策略
 * 5. 集成内容质量评估
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Service    // Spring服务层注解，标识这是一个业务逻辑组件
@Slf4j      // Lombok日志注解，自动生成log对象用于日志记录
public class RssAcquisitionService {

    /**
     * 知识内容数据访问对象
     *
     * 通过Spring依赖注入，用于将采集到的RSS内容保存到数据库
     */
    @Autowired
    private KnowledgeContentRepository knowledgeContentRepository;

    /**
     * AI智能代理深度加工服务
     *
     * 通过Spring依赖注入，用于对采集到的内容进行智能分析和加工
     */
    @Autowired
    private AIProcessingService aiProcessingService;

    /**
     * RSS数据源配置列表
     * 
     * 当前配置的RSS源包括：
     * 1. Hacker News最新文章（至少300点赞）- 高质量技术资讯
     * 2. Hacker News最佳评论 - 优质技术讨论内容
     * 
     * 注释掉的源：
     * - B站用户视频RSS：可用于采集特定用户的视频更新
     * - 知乎日报：可用于采集知乎精选内容
     * 
     * TODO: 优化建议
     * - 将RSS源配置移至application.properties或数据库
     * - 支持RSS源的动态管理（增删改查）
     * - 为每个RSS源配置采集频率和优先级
     */
    private static final List<String> RSS_FEEDS = List.of(
            // "https://rsshub.app/bilibili/user/video/33824855", // B站用户视频
            // "https://rsshub.app/zhihu/daily" // 知乎日报
            "https://hnrss.org/newest?points=300",    // Hacker News最新高质量文章
            "https://hnrss.org/bestcomments"          // Hacker News最佳评论
    );

    /**
     * 定时RSS数据采集任务
     * 
     * 该方法通过Spring的@Scheduled注解配置为定时任务，每小时执行一次。
     * 遍历配置的所有RSS源，解析XML内容，提取有用信息并存储到数据库。
     * 
     * 执行流程：
     * 1. 初始化成功/失败计数器
     * 2. 遍历所有配置的RSS源URL
     * 3. 对每个RSS源：
     *    a. 建立网络连接并下载RSS XML
     *    b. 使用ROME库解析XML结构
     *    c. 遍历所有RSS条目（entry）
     *    d. 提取标题、内容、链接等信息
     *    e. 创建KnowledgeContent实体并保存到数据库
     * 4. 记录采集统计结果
     * 
     * 错误处理：
     * - 网络连接错误：记录日志，继续处理下一个RSS源
     * - XML解析错误：记录日志，跳过当前RSS源
     * - 数据库保存错误：记录日志，但不中断整个采集流程
     * 
     * 性能优化建议：
     * - 可考虑使用并行流处理多个RSS源
     * - 实现连接超时和重试机制
     * - 添加内容去重逻辑避免重复存储
     * 
     * @Scheduled注解参数说明：
     * - fixedRate = 3600000：固定间隔3600秒（1小时）执行一次
     * - 其他可选参数：initialDelay（初始延迟）、cron（cron表达式）
     */
    @Scheduled(fixedRate = 3600000) // 每隔1小时执行一次 (3600000毫秒)
    public void acquireRssData() {
        log.info("开始采集 RSS 数据...");
        int successCount = 0;  // 成功采集的条目数量
        int failureCount = 0;  // 失败的RSS源数量

        // 遍历所有配置的RSS源进行数据采集
        for (String feedUrl : RSS_FEEDS) {
            try {
                // 创建URL对象并建立连接
                URL url = new URL(feedUrl);
                SyndFeedInput input = new SyndFeedInput();
                
                // 使用ROME库解析RSS XML，XmlReader自动处理字符编码
                SyndFeed feed = input.build(new XmlReader(url));

                // 遍历RSS feed中的所有条目
                for (SyndEntry entry : feed.getEntries()) {
                    // 创建新的知识内容实体
                    KnowledgeContent content = new KnowledgeContent();
                    
                    // 设置基本内容信息
                    content.setTitle(entry.getTitle());
                           // 安全地获取描述内容，避免空指针异常
                           String rawContent = entry.getDescription() != null ? entry.getDescription().getValue() : "";
                           // 限制内容长度以避免数据库截断错误
                           if (rawContent.length() > 4000) {
                               content.setContent(rawContent.substring(0, 4000) + "...");
                               log.debug("RSS内容已截断，原长度: {}, 截断后长度: {}", rawContent.length(), content.getContent().length());
                           } else {
                               content.setContent(rawContent);
                           }
                    content.setSourceUrl(entry.getUri());
                    content.setContentType("RSS");  // 标记为RSS类型内容
                    
                    // 转换并设置发布时间，如果没有则使用当前时间
                    content.setAcquisitionTime(convertToLocalDateTime(entry.getPublishedDate()));
                    
                    // 提取并组合分类标签，使用逗号分隔
                    content.setTags(entry.getCategories() != null ? 
                        entry.getCategories().stream()
                            .map(c -> c.getName())
                            .reduce((a, b) -> a + "," + b)
                            .orElse("") : "");
                    
                    // 设置处理状态
                    content.setProcessed(false); // 初始为未加工状态
                    content.setSuccess(true);    // 标记采集成功
                    content.setErrorMessage(null);

                    // AI智能代理深度加工信息处理
                    try {
                        // 调用AI处理服务对内容进行深度加工
                        // 包括：内容摘要生成、关键词提取、内容分类、情感分析、实体提取等
                        if (content != null) {
                            KnowledgeContent processedContent = aiProcessingService.processContent(content);
                            if (processedContent != null) {
                                content = processedContent;
                                log.debug("AI处理完成，内容已增强: {}", content.getTitle());
                            }
                        }
                    } catch (Exception aiException) {
                        // AI处理失败不影响基本的内容保存
                        if (content != null) {
                            log.warn("AI智能处理失败，使用原始内容: " + content.getTitle(), aiException);
                            content.setProcessed(false);
                            content.setErrorMessage("AI处理失败: " + aiException.getMessage());
                        } else {
                            log.warn("AI智能处理失败，内容对象为空", aiException);
                        }
                    }
                    
                    // 记录调试日志
                    if (content != null) {
                        log.debug("RSS content acquired and processed: {}", content.getTitle());
                        
                        // 保存到数据库
                        knowledgeContentRepository.save(content);
                        successCount++;
                    } else {
                        log.warn("RSS内容对象为空，跳过保存");
                    }
                }
            } catch (Exception e) {
                // 捕获所有异常，记录错误日志但不中断整个采集流程
                log.error("采集 RSS 源失败: " + feedUrl, e);
                failureCount++;
                
                // TODO: 可以选择在这里保存一个失败记录到数据库
                // 用于后续的错误分析和RSS源质量监控
                /*
                KnowledgeContent errorRecord = new KnowledgeContent();
                errorRecord.setTitle("RSS采集失败: " + feedUrl);
                errorRecord.setSourceUrl(feedUrl);
                errorRecord.setContentType("RSS");
                errorRecord.setAcquisitionTime(LocalDateTime.now());
                errorRecord.setProcessed(false);
                errorRecord.setSuccess(false);
                errorRecord.setErrorMessage(e.getMessage());
                knowledgeContentRepository.save(errorRecord);
                */
            }
        }
        
        // 记录采集完成的统计信息
        log.info("RSS 数据采集完成。成功: {}, 失败: {}", successCount, failureCount);
    }

    /**
     * 日期时间转换工具方法
     * 
     * 将java.util.Date对象转换为java.time.LocalDateTime对象。
     * RSS feed中的发布时间通常为Date类型，但我们的实体使用LocalDateTime类型。
     * 
     * 转换逻辑：
     * 1. 如果输入的Date不为null，使用系统默认时区进行转换
     * 2. 如果输入的Date为null，返回当前时间作为默认值
     * 
     * 时区处理：
     * - 使用系统默认时区（ZoneId.systemDefault()）
     * - 可根据需要调整为UTC或特定时区
     * 
     * @param date RSS条目的发布时间（可能为null）
     * @return 转换后的LocalDateTime对象，永不为null
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date != null ? 
            LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()) : 
            LocalDateTime.now();
    }
}
