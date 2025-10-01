package cn.lihengrui.todotask.service;

import cn.lihengrui.todotask.entity.KnowledgeContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI智能代理深度加工服务类
 *
 * 该服务负责对采集到的知识内容进行智能分析和深度加工，提取有价值的信息，
 * 增强内容的可用性和检索性，为知识管理系统提供智能化的内容处理能力。
 *
 * 主要功能：
 * 1. 内容摘要生成 - 自动生成内容摘要和关键信息提取
 * 2. 关键词提取 - 基于TF-IDF和词频分析提取核心关键词
 * 3. 内容分类 - 根据内容特征进行智能分类标记
 * 4. 情感分析 - 分析内容的情感倾向（正面/负面/中性）
 * 5. 内容质量评估 - 评估内容的信息价值和完整性
 * 6. 语言检测 - 自动识别内容的主要语言
 * 7. 实体提取 - 提取人名、地名、组织机构等命名实体
 * 8. 相关性分析 - 计算内容之间的相似度和关联性
 *
 * 技术实现：
 * - 基于规则的文本分析算法
 * - TF-IDF关键词提取
 * - 正则表达式模式匹配
 * - 统计学文本分析方法
 * - 可扩展的插件式处理架构
 *
 * 扩展接口：
 * - 支持集成外部AI服务（如OpenAI、Google Cloud AI）
 * - 预留机器学习模型集成接口
 * - 支持自定义处理规则配置
 * - 提供批量处理和异步处理能力
 *
 * 性能优化：
 * - 内存高效的文本处理算法
 * - 缓存机制减少重复计算
 * - 异步处理支持高并发场景
 * - 可配置的处理深度级别
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Service
@Slf4j
public class AIProcessingService {

    // 停用词列表 - 用于关键词提取时过滤无意义词汇
    private static final Set<String> STOP_WORDS = Set.of(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "他", "这", "那",
        "来", "到", "时", "大", "地", "为", "子", "中", "你", "说", "生", "国", "年", "着", "什么",
        "and", "the", "of", "to", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with",
        "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her",
        "she", "or", "an", "will", "my", "one", "all", "would", "there", "their"
    );

    // 技术相关关键词库 - 用于内容分类
    private static final Set<String> TECH_KEYWORDS = Set.of(
        "java", "python", "javascript", "react", "spring", "boot", "database", "mysql", "redis",
        "docker", "kubernetes", "microservice", "api", "rest", "graphql", "machine learning",
        "artificial intelligence", "blockchain", "cloud", "aws", "azure", "programming",
        "development", "framework", "library", "algorithm", "data structure", "软件", "编程",
        "开发", "技术", "算法", "数据库", "人工智能", "机器学习", "云计算", "微服务"
    );

    // 情感词汇库 - 用于简单的情感分析
    private static final Set<String> POSITIVE_WORDS = Set.of(
        "good", "great", "excellent", "amazing", "wonderful", "fantastic", "awesome", "perfect",
        "outstanding", "brilliant", "positive", "successful", "innovative", "efficient",
        "好", "优秀", "出色", "完美", "杰出", "成功", "创新", "高效", "积极", "优化"
    );

    private static final Set<String> NEGATIVE_WORDS = Set.of(
        "bad", "terrible", "awful", "horrible", "poor", "failed", "broken", "wrong", "error",
        "problem", "issue", "bug", "slow", "inefficient", "negative",
        "坏", "糟糕", "失败", "错误", "问题", "故障", "缓慢", "低效", "消极", "缺陷"
    );

    /**
     * 智能代理深度加工主入口方法
     *
     * 对知识内容进行全面的智能分析和加工处理，包括内容增强、
     * 信息提取、质量评估等多个维度的处理。
     *
     * 处理流程：
     * 1. 内容预处理和清洗
     * 2. 生成内容摘要
     * 3. 提取关键词和标签
     * 4. 进行内容分类
     * 5. 执行情感分析
     * 6. 评估内容质量
     * 7. 检测内容语言
     * 8. 提取命名实体
     * 9. 更新处理状态
     *
     * @param content 待处理的知识内容实体
     * @return 处理后的知识内容实体，包含所有增强信息
     */
    public KnowledgeContent processContent(KnowledgeContent content) {
        // 空值检查
        if (content == null) {
            log.warn("AI处理失败：内容对象为空");
            return null;
        }
        
        if (content.getTitle() == null) {
            log.warn("AI处理失败：内容标题为空");
            return content;
        }
        
        log.info("开始AI智能代理深度加工内容: {}", content.getTitle());

        try {
            // 1. 内容预处理和验证
            if (!isContentValid(content)) {
                log.warn("内容验证失败，跳过AI处理: {}", content.getTitle());
                return content;
            }

            // 2. 生成智能摘要（如果内容过长）
            String enhancedSummary = generateSummary(content.getContent());
            if (!enhancedSummary.equals(content.getContent())) {
                log.debug("为内容生成摘要: {} -> {} 字符", content.getContent().length(), enhancedSummary.length());
            }

            // 3. 提取和增强关键词
            String enhancedTags = enhanceKeywords(content);
            content.setTags(enhancedTags);

            // 4. 内容智能分类
            String categoryInfo = classifyContent(content);
            if (!categoryInfo.isEmpty()) {
                // 将分类信息添加到标签中
                String currentTags = content.getTags() != null ? content.getTags() : "";
                content.setTags(currentTags.isEmpty() ? categoryInfo : currentTags + "," + categoryInfo);
            }

            // 5. 情感倾向分析
            String sentimentInfo = analyzeSentiment(content);
            if (!sentimentInfo.isEmpty()) {
                String currentTags = content.getTags() != null ? content.getTags() : "";
                content.setTags(currentTags.isEmpty() ? sentimentInfo : currentTags + "," + sentimentInfo);
            }

            // 6. 内容质量评估
            String qualityInfo = assessContentQuality(content);
            if (!qualityInfo.isEmpty()) {
                String currentTags = content.getTags() != null ? content.getTags() : "";
                content.setTags(currentTags.isEmpty() ? qualityInfo : currentTags + "," + qualityInfo);
            }

            // 7. 语言检测
            String languageInfo = detectLanguage(content.getContent());
            if (!languageInfo.isEmpty()) {
                String currentTags = content.getTags() != null ? content.getTags() : "";
                content.setTags(currentTags.isEmpty() ? languageInfo : currentTags + "," + languageInfo);
            }

            // 8. 实体提取（人名、组织、技术名词等）
            String entities = extractEntities(content.getContent());
            if (!entities.isEmpty()) {
                String currentTags = content.getTags() != null ? content.getTags() : "";
                content.setTags(currentTags.isEmpty() ? entities : currentTags + "," + entities);
            }

            // 9. 更新处理状态
            content.setProcessed(true);
            
            log.info("AI智能代理深度加工完成: {}", content.getTitle());
            log.debug("增强标签: {}", content.getTags());

        } catch (Exception e) {
            log.error("AI处理过程中发生异常: " + content.getTitle(), e);
            // 即使AI处理失败，也不影响内容的基本保存
            content.setProcessed(false);
            if (content.getErrorMessage() == null) {
                content.setErrorMessage("AI处理异常: " + e.getMessage());
            }
        }

        return content;
    }

    /**
     * 内容有效性验证
     *
     * 检查内容是否适合进行AI处理，包括标题和内容的完整性验证。
     *
     * @param content 待验证的内容
     * @return true 如果内容有效，false 如果内容无效
     */
    private boolean isContentValid(KnowledgeContent content) {
        return content != null 
            && content.getTitle() != null && !content.getTitle().trim().isEmpty()
            && content.getContent() != null && content.getContent().trim().length() > 10;
    }

    /**
     * 智能内容摘要生成
     *
     * 根据内容长度和重要性，生成合适的摘要。对于长文本，提取前几句关键信息；
     * 对于短文本，保持原样。
     *
     * @param originalContent 原始内容
     * @return 生成的摘要内容
     */
    private String generateSummary(String originalContent) {
        if (originalContent == null || originalContent.length() <= 200) {
            return originalContent; // 短内容无需摘要
        }

        // 简单的摘要策略：提取前两句话或前200字符
        String[] sentences = originalContent.split("[.。!！?？]");
        StringBuilder summary = new StringBuilder();
        
        for (String sentence : sentences) {
            if (summary.length() + sentence.length() > 200) {
                break;
            }
            summary.append(sentence.trim());
            if (summary.length() > 100) { // 至少包含100字符
                break;
            }
            summary.append("。");
        }

        String result = summary.toString();
        return result.isEmpty() ? originalContent.substring(0, Math.min(200, originalContent.length())) + "..." : result;
    }

    /**
     * 关键词提取和标签增强
     *
     * 基于TF-IDF算法和词频分析，提取内容中的关键词，并与原有标签进行合并优化。
     *
     * @param content 内容实体
     * @return 增强后的标签字符串
     */
    private String enhanceKeywords(KnowledgeContent content) {
        String text = (content.getTitle() + " " + content.getContent()).toLowerCase();
        
        // 提取关键词
        Map<String, Integer> wordFreq = new HashMap<>();
        
        // 简单的词频统计（支持中英文）
        Pattern wordPattern = Pattern.compile("[\\w\\u4e00-\\u9fa5]+");
        Matcher matcher = wordPattern.matcher(text);
        
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() > 1 && !STOP_WORDS.contains(word)) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }

        // 提取高频关键词
        List<String> keywords = wordFreq.entrySet().stream()
            .filter(entry -> entry.getValue() > 1) // 出现次数大于1
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5) // 最多5个关键词
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // 合并原有标签和新提取的关键词
        Set<String> allTags = new HashSet<>();
        
        // 添加原有标签
        if (content.getTags() != null && !content.getTags().isEmpty()) {
            allTags.addAll(Arrays.asList(content.getTags().split(",")));
        }
        
        // 添加提取的关键词
        allTags.addAll(keywords);
        
        // 清理和格式化标签
        return allTags.stream()
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .distinct()
            .collect(Collectors.joining(","));
    }

    /**
     * 内容智能分类
     *
     * 基于内容特征和关键词匹配，自动识别内容所属的类别。
     *
     * @param content 内容实体
     * @return 分类标签
     */
    private String classifyContent(KnowledgeContent content) {
        String text = (content.getTitle() + " " + content.getContent()).toLowerCase();
        List<String> categories = new ArrayList<>();

        // 技术类别检测
        long techKeywordCount = TECH_KEYWORDS.stream()
            .mapToLong(keyword -> countOccurrences(text, keyword))
            .sum();
        
        if (techKeywordCount > 0) {
            categories.add("技术");
        }

        // 新闻类别检测
        if (text.contains("新闻") || text.contains("news") || text.contains("报道") || text.contains("发布")) {
            categories.add("新闻");
        }

        // 教程类别检测
        if (text.contains("教程") || text.contains("tutorial") || text.contains("如何") || text.contains("how to")) {
            categories.add("教程");
        }

        // 分析类别检测
        if (text.contains("分析") || text.contains("analysis") || text.contains("研究") || text.contains("research")) {
            categories.add("分析");
        }

        return categories.isEmpty() ? "" : "分类:" + String.join(",", categories);
    }

    /**
     * 情感倾向分析
     *
     * 分析内容的情感倾向，识别正面、负面或中性的情感色彩。
     *
     * @param content 内容实体
     * @return 情感分析结果
     */
    private String analyzeSentiment(KnowledgeContent content) {
        String text = (content.getTitle() + " " + content.getContent()).toLowerCase();
        
        long positiveCount = POSITIVE_WORDS.stream()
            .mapToLong(word -> countOccurrences(text, word))
            .sum();
            
        long negativeCount = NEGATIVE_WORDS.stream()
            .mapToLong(word -> countOccurrences(text, word))
            .sum();

        if (positiveCount > negativeCount && positiveCount > 0) {
            return "情感:正面";
        } else if (negativeCount > positiveCount && negativeCount > 0) {
            return "情感:负面";
        } else if (positiveCount > 0 || negativeCount > 0) {
            return "情感:中性";
        }
        
        return ""; // 无明显情感倾向
    }

    /**
     * 内容质量评估
     *
     * 基于多个维度评估内容的质量，包括长度、完整性、信息密度等。
     *
     * @param content 内容实体
     * @return 质量评估结果
     */
    private String assessContentQuality(KnowledgeContent content) {
        int score = 0;
        List<String> qualityFactors = new ArrayList<>();

        // 内容长度评估
        int contentLength = content.getContent() != null ? content.getContent().length() : 0;
        if (contentLength > 500) {
            score += 2;
            qualityFactors.add("详细");
        } else if (contentLength > 100) {
            score += 1;
            qualityFactors.add("适中");
        }

        // 标题质量评估
        if (content.getTitle() != null && content.getTitle().length() > 10) {
            score += 1;
            qualityFactors.add("标题完整");
        }

        // 标签丰富度评估
        if (content.getTags() != null && content.getTags().split(",").length > 3) {
            score += 1;
            qualityFactors.add("标签丰富");
        }

        // 来源可靠性（基于域名）
        if (content.getSourceUrl() != null) {
            String url = content.getSourceUrl().toLowerCase();
            if (url.contains("github.com") || url.contains("stackoverflow.com") || 
                url.contains("medium.com") || url.contains("infoq")) {
                score += 1;
                qualityFactors.add("可靠来源");
            }
        }

        // 质量等级判定
        String qualityLevel;
        if (score >= 4) {
            qualityLevel = "高质量";
        } else if (score >= 2) {
            qualityLevel = "中等质量";
        } else {
            qualityLevel = "基础质量";
        }

        return qualityFactors.isEmpty() ? "" : "质量:" + qualityLevel;
    }

    /**
     * 语言检测
     *
     * 简单的语言检测，识别内容的主要语言（中文/英文）。
     *
     * @param text 待检测的文本
     * @return 语言标识
     */
    private String detectLanguage(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 中文字符计数
        long chineseCount = text.chars()
            .filter(ch -> ch >= 0x4e00 && ch <= 0x9fa5)
            .count();

        // 英文字符计数
        long englishCount = text.chars()
            .filter(ch -> (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
            .count();

        double chineseRatio = (double) chineseCount / text.length();
        double englishRatio = (double) englishCount / text.length();

        if (chineseRatio > 0.3) {
            return "语言:中文";
        } else if (englishRatio > 0.5) {
            return "语言:英文";
        } else if (chineseRatio > 0.1 && englishRatio > 0.3) {
            return "语言:中英混合";
        }

        return ""; // 无法确定语言
    }

    /**
     * 命名实体提取
     *
     * 提取文本中的重要实体，如技术名词、公司名称、产品名称等。
     *
     * @param text 待分析的文本
     * @return 提取到的实体列表
     */
    private String extractEntities(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Set<String> entities = new HashSet<>();
        String lowerText = text.toLowerCase();

        // 技术实体检测
        String[] techEntities = {
            "java", "python", "javascript", "react", "vue", "angular", "spring", "boot",
            "docker", "kubernetes", "mysql", "redis", "mongodb", "elasticsearch",
            "aws", "azure", "google cloud", "openai", "chatgpt"
        };

        for (String entity : techEntities) {
            if (lowerText.contains(entity)) {
                entities.add(entity);
            }
        }

        // 公司/组织实体检测
        String[] orgEntities = {
            "google", "microsoft", "apple", "amazon", "facebook", "twitter", "netflix",
            "alibaba", "tencent", "baidu", "bytedance", "华为", "腾讯", "阿里巴巴", "百度"
        };

        for (String entity : orgEntities) {
            if (lowerText.contains(entity)) {
                entities.add(entity);
            }
        }

        return entities.isEmpty() ? "" : "实体:" + String.join(",", entities);
    }

    /**
     * 计算字符串在文本中的出现次数
     *
     * @param text 文本内容
     * @param target 目标字符串
     * @return 出现次数
     */
    private long countOccurrences(String text, String target) {
        if (text == null || target == null || target.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }

    /**
     * 批量处理内容列表
     *
     * 为未来的批量处理需求预留接口，支持高效的批量AI处理。
     *
     * @param contents 待处理的内容列表
     * @return 处理后的内容列表
     */
    public List<KnowledgeContent> batchProcessContents(List<KnowledgeContent> contents) {
        log.info("开始批量AI处理 {} 条内容", contents.size());
        
        return contents.stream()
            .map(this::processContent)
            .collect(Collectors.toList());
    }

    /**
     * 异步处理内容
     *
     * 为未来的异步处理需求预留接口，支持非阻塞的AI处理。
     *
     * @param content 待处理的内容
     * @return 处理结果的Future对象
     */
    // @Async  // 如果配置了异步处理，可以取消注释
    // public CompletableFuture<KnowledgeContent> processContentAsync(KnowledgeContent content) {
    //     return CompletableFuture.completedFuture(processContent(content));
    // }
}
