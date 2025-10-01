package cn.lihengrui.todotask.service;

import cn.lihengrui.todotask.entity.KnowledgeContent;
import cn.lihengrui.todotask.repository.KnowledgeContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 网页内容爬取服务类
 * 
 * 该服务负责定时从配置的网站爬取内容信息，使用Jsoup库解析HTML页面，
 * 提取有用的文本内容并存储到数据库中，为知识管理系统提供网页内容来源。
 * 
 * 主要功能：
 * 1. 定时网页内容爬取 - 每2小时自动执行一次
 * 2. HTML页面解析 - 使用Jsoup库解析HTML结构
 * 3. 内容提取 - 智能提取页面标题和正文内容
 * 4. 数据存储 - 将提取的内容保存到数据库
 * 5. 爬虫规范遵守 - 设置用户代理，控制访问频率
 * 
 * 技术实现：
 * - 使用Spring的@Scheduled注解实现定时任务
 * - 使用Jsoup库进行HTML解析和内容提取
 * - 集成Spring Data JPA进行数据持久化
 * - 使用SLF4J进行日志记录和监控
 * 
 * 爬虫规范：
 * - 设置合理的User-Agent模拟真实浏览器
 * - 配置连接超时防止长时间阻塞
 * - 控制爬取频率避免对目标网站造成压力
 * - 遵守robots.txt协议（建议）
 * 
 * 配置说明：
 * - 目标网站：目前硬编码在WEB_URLS常量中
 * - 爬取间隔：固定每2小时执行一次（7200000毫秒）
 * - 连接超时：10秒
 * - 内容提取策略：优先提取<p>标签内容，回退到<body>全文
 * 
 * 性能考虑：
 * - 网络延迟：依赖目标网站的响应速度
 * - 内容解析：大型网页可能消耗较多CPU和内存
 * - 反爬机制：可能遇到验证码、IP限制等反爬措施
 * 
 * 扩展方向：
 * 1. 支持更精确的内容提取规则（CSS选择器配置）
 * 2. 实现智能内容去重和更新检测
 * 3. 添加代理池支持绕过IP限制
 * 4. 支持JavaScript渲染的动态页面（Selenium）
 * 5. 实现分布式爬取架构
 * 6. 添加内容质量评估和过滤
 * 
 * 注意事项：
 * - 需要遵守目标网站的使用条款和robots.txt
 * - 避免过于频繁的请求导致IP被封
 * - 某些网站可能需要登录或特殊处理
 * - 内容长度超过5000字符会被截断
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Service    // Spring服务层注解，标识这是一个业务逻辑组件
@Slf4j      // Lombok日志注解，自动生成log对象用于日志记录
public class WebScrapingService {

    /**
     * 知识内容数据访问对象
     *
     * 通过Spring依赖注入，用于将爬取到的网页内容保存到数据库
     */
    @Autowired
    private KnowledgeContentRepository knowledgeContentRepository;

    /**
     * AI智能代理深度加工服务
     *
     * 通过Spring依赖注入，用于对爬取到的内容进行智能分析和加工
     */
    @Autowired
    private AIProcessingService aiProcessingService;

    /**
     * 网页爬取目标URL配置列表
     * 
     * 当前配置的网站包括：
     * 1. InfoQ资讯频道 - 专业的软件开发资讯网站
     * 2. 博客园首页 - 知名的中文技术社区
     * 
     * 选择标准：
     * - 内容质量高，信息价值大
     * - 更新频率适中，不会过于频繁
     * - 网站结构相对稳定，便于解析
     * - 没有严格的反爬机制
     * 
     * TODO: 优化建议
     * - 将URL配置移至application.properties或数据库
     * - 为每个网站配置专门的解析规则
     * - 支持网站配置的动态管理
     * - 添加robots.txt检查和遵守机制
     * - 配置不同网站的爬取频率和优先级
     */
    private static final List<String> WEB_URLS = List.of(
            "https://www.infoq.cn/news",    // InfoQ 资讯频道
            "https://www.cnblogs.com/"      // 博客园首页
    );

    /**
     * 定时网页数据爬取任务
     * 
     * 该方法通过Spring的@Scheduled注解配置为定时任务，每2小时执行一次。
     * 遍历配置的所有目标网站，使用Jsoup解析HTML内容，提取有价值信息并存储。
     * 
     * 执行流程：
     * 1. 初始化成功/失败计数器
     * 2. 遍历所有配置的目标网站URL
     * 3. 对每个网站：
     *    a. 建立HTTP连接并下载HTML页面
     *    b. 使用Jsoup解析HTML文档结构
     *    c. 提取页面标题和正文内容
     *    d. 创建KnowledgeContent实体并保存到数据库
     * 4. 记录爬取统计结果
     * 
     * 内容提取策略：
     * 1. 标题：直接获取HTML的<title>标签内容
     * 2. 正文：优先提取所有<p>标签的文本内容
     * 3. 回退：如果没有<p>标签，则提取整个<body>的文本内容
     * 4. 过滤：自动去除HTML标签，保留纯文本
     * 
     * 爬虫规范实现：
     * - User-Agent：设置为Chrome浏览器标识，避免被识别为机器人
     * - 超时控制：设置10秒连接超时，防止长时间等待
     * - 访问频率：2小时间隔，对目标网站友好
     * 
     * 错误处理：
     * - IOException：网络连接错误、超时等
     * - 其他异常：HTML解析错误、数据库保存错误等
     * - 错误恢复：单个网站失败不影响其他网站的爬取
     * 
     * 性能优化建议：
     * - 可考虑并行处理多个网站
     * - 实现连接池复用HTTP连接
     * - 添加缓存机制避免重复爬取
     * - 实现增量更新检测
     * 
     * @Scheduled注解参数说明：
     * - fixedRate = 7200000：固定间隔7200秒（2小时）执行一次
     * - 选择2小时是为了平衡数据更新频率和网站负载
     */
    @Scheduled(fixedRate = 7200000) // 每隔2小时执行一次 (7200000毫秒)
    public void acquireWebData() {
        log.info("开始采集网页数据...");
        int successCount = 0;  // 成功爬取的网站数量
        int failureCount = 0;  // 失败的网站数量

        // 遍历所有配置的目标网站进行数据爬取
        for (String url : WEB_URLS) {
            try {
                // 使用Jsoup建立HTTP连接并获取HTML文档
                // 遵守网络爬虫规范与安全准则：添加用户代理，模拟浏览器访问
                Document doc = Jsoup.connect(url)
                        // 设置User-Agent为Chrome浏览器，避免被反爬机制拦截
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000) // 设置10秒连接超时，防止长时间阻塞
                        .get();         // 执行GET请求获取页面内容

                // 提取页面标题
                String title = doc.title();
                
                // 智能提取页面正文内容
                // 策略1：优先提取所有段落（<p>标签）的文本内容
                Elements paragraphs = doc.select("p");
                StringBuilder contentBuilder = new StringBuilder();
                paragraphs.forEach(p -> contentBuilder.append(p.text()).append("\n"));
                String contentText = contentBuilder.toString().trim();
                
                // 策略2：如果没有找到段落内容，回退到提取整个body的文本
                if (contentText.isEmpty() && doc.body() != null) {
                    contentText = doc.body().text(); // 提取body标签内的所有文本内容
                }

                // 创建知识内容实体并设置属性
                KnowledgeContent content = new KnowledgeContent();
                       content.setTitle(title);
                       // 限制内容长度以避免数据库截断错误
                       if (contentText != null && contentText.length() > 4000) {
                           content.setContent(contentText.substring(0, 4000) + "...");
                           log.debug("网页内容已截断，原长度: {}, 截断后长度: {}", contentText.length(), content.getContent().length());
                       } else {
                           content.setContent(contentText);
                       }
                content.setSourceUrl(url);
                content.setContentType("Web");  // 标记为网页类型内容
                content.setAcquisitionTime(LocalDateTime.now());  // 设置当前时间为采集时间
                content.setTags("网页抓取"); // 设置默认标签，可根据网站特点定制
                content.setProcessed(false); // 初始标记为未处理状态
                content.setSuccess(true);    // 标记采集成功
                content.setErrorMessage(null);

                       // AI智能代理深度加工信息处理
                       try {
                           // 调用AI处理服务对网页内容进行深度加工
                           // 包括：内容摘要和关键词提取、网页内容分类和标签自动生成、
                           // 内容质量评分、语言检测、实体提取、情感分析等
                           if (content != null) {
                               KnowledgeContent processedContent = aiProcessingService.processContent(content);
                               if (processedContent != null) {
                                   content = processedContent;
                                   log.debug("AI处理完成，网页内容已增强: {}", content.getTitle());
                               }
                           }
                       } catch (Exception aiException) {
                           // AI处理失败不影响基本的内容保存
                           if (content != null) {
                               log.warn("AI智能处理失败，使用原始网页内容: " + content.getTitle(), aiException);
                               content.setProcessed(false);
                               content.setErrorMessage("AI处理失败: " + aiException.getMessage());
                           } else {
                               log.warn("AI智能处理失败，网页内容对象为空", aiException);
                           }
                       }

                // 记录调试级别日志
                if (content != null) {
                    log.debug("Web content acquired and processed: {}", content.getTitle());

                    // 保存内容到数据库
                    knowledgeContentRepository.save(content);
                    successCount++;
                } else {
                    log.warn("网页内容对象为空，跳过保存");
                }

            } catch (IOException e) {
                // 处理网络相关异常：连接超时、DNS解析失败、HTTP错误等
                log.error("采集网页失败: " + url, e);
                failureCount++;
                
                // TODO: 可以选择在这里保存一个失败记录到数据库
                // 用于后续的错误分析和网站状态监控
                /*
                KnowledgeContent errorRecord = new KnowledgeContent();
                errorRecord.setTitle("网页采集失败: " + url);
                errorRecord.setSourceUrl(url);
                errorRecord.setContentType("Web");
                errorRecord.setAcquisitionTime(LocalDateTime.now());
                errorRecord.setProcessed(false);
                errorRecord.setSuccess(false);
                errorRecord.setErrorMessage("网络连接错误: " + e.getMessage());
                knowledgeContentRepository.save(errorRecord);
                */
                
            } catch (Exception e) {
                // 处理其他异常：HTML解析错误、数据库保存错误等
                log.error("处理网页数据失败: " + url, e);
                failureCount++;
                
                // 同样可以记录非网络类型的错误到数据库
            }
        }
        
        // 记录本次爬取任务的统计结果
        log.info("网页数据采集完成。成功: {}, 失败: {}", successCount, failureCount);
    }
}
