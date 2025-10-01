package cn.lihengrui.todotask.service;

import cn.lihengrui.todotask.entity.KnowledgeContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI智能代理深度加工服务单元测试类
 *
 * 测试AIProcessingService的各项功能，包括内容分析、关键词提取、
 * 情感分析、内容分类等AI处理能力。
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@ExtendWith(MockitoExtension.class)
public class AIProcessingServiceTest {

    @InjectMocks
    private AIProcessingService aiProcessingService;

    private KnowledgeContent testContent;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testContent = new KnowledgeContent();
        testContent.setKnowId(1L);
        testContent.setTitle("Spring Boot实战指南：构建高性能微服务架构");
        testContent.setContent("Spring Boot是一个优秀的Java框架，它简化了Spring应用的开发过程。" +
                "通过自动配置和starter依赖，开发者可以快速搭建企业级应用。" +
                "本文将介绍如何使用Spring Boot构建微服务架构，包括服务发现、配置管理、" +
                "负载均衡等关键技术。Spring Boot的优势在于其convention over configuration的理念。");
        testContent.setSourceUrl("https://example.com/spring-boot-guide");
        testContent.setContentType("RSS");
        testContent.setAcquisitionTime(LocalDateTime.now());
        testContent.setTags("编程,框架");
        testContent.setProcessed(false);
        testContent.setSuccess(true);
        testContent.setErrorMessage(null);
    }

    @Test
    void testProcessContent_ShouldEnhanceContentWithAIProcessing() {
        // 执行AI处理
        KnowledgeContent processedContent = aiProcessingService.processContent(testContent);

        // 验证内容已被处理
        assertThat(processedContent).isNotNull();
        assertThat(processedContent.isProcessed()).isTrue();
        
        // 验证标签得到增强
        assertThat(processedContent.getTags()).isNotNull();
        assertThat(processedContent.getTags()).isNotEmpty();
        assertThat(processedContent.getTags()).contains("编程,框架");
        
        // 验证基本信息保持不变
        assertThat(processedContent.getTitle()).isEqualTo(testContent.getTitle());
        assertThat(processedContent.getSourceUrl()).isEqualTo(testContent.getSourceUrl());
        assertThat(processedContent.getContentType()).isEqualTo(testContent.getContentType());
        
        System.out.println("增强后的标签: " + processedContent.getTags());
    }

    @Test
    void testProcessContent_WithTechnicalContent_ShouldAddTechClassification() {
        // 使用技术内容进行测试
        testContent.setTitle("深入理解Java虚拟机：JVM性能调优实践");
        testContent.setContent("Java虚拟机（JVM）是Java程序运行的核心环境。" +
                "本文详细介绍JVM的内存模型、垃圾回收机制，以及各种性能调优技术。" +
                "包括堆内存配置、GC算法选择、JIT编译优化等关键内容。");

        KnowledgeContent processedContent = aiProcessingService.processContent(testContent);

        assertThat(processedContent.getTags()).contains("技术");
        assertThat(processedContent.isProcessed()).isTrue();
    }

    @Test
    void testProcessContent_WithPositiveContent_ShouldDetectPositiveSentiment() {
        // 使用正面情感内容进行测试
        testContent.setTitle("优秀的开源项目推荐");
        testContent.setContent("这个项目非常出色，代码质量excellent，文档完善，" +
                "社区活跃度高。开发团队的响应速度很快，是一个值得推荐的优秀框架。" +
                "项目的创新性和实用性都很强，给开发者带来了很好的体验。");

        KnowledgeContent processedContent = aiProcessingService.processContent(testContent);

        assertThat(processedContent.getTags()).contains("情感:正面");
        assertThat(processedContent.isProcessed()).isTrue();
    }

    @Test
    void testProcessContent_WithNegativeContent_ShouldDetectNegativeSentiment() {
        // 使用负面情感内容进行测试
        testContent.setTitle("项目开发中遇到的问题分析");
        testContent.setContent("这个版本存在严重的bug，性能terrible，用户体验很差。" +
                "系统经常出现error，响应速度slow，问题很多需要修复。" +
                "开发过程中遇到了很多困难和挑战。");

        KnowledgeContent processedContent = aiProcessingService.processContent(testContent);

        assertThat(processedContent.getTags()).contains("情感:负面");
        assertThat(processedContent.isProcessed()).isTrue();
    }

    @Test
    void testProcessContent_WithChineseContent_ShouldDetectChineseLanguage() {
        // 测试中文内容的语言检测
        testContent.setTitle("人工智能技术在医疗领域的应用研究");
        testContent.setContent("人工智能技术正在深刻改变医疗行业。通过机器学习算法，" +
                "医生可以更准确地诊断疾病。深度学习在医学影像分析中展现出巨大潜力，" +
                "为精准医疗提供了新的解决方案。");

        KnowledgeContent processedContent = aiProcessingService.processContent(testContent);

        assertThat(processedContent.getTags()).contains("语言:中文");
        assertThat(processedContent.isProcessed()).isTrue();
    }

    @Test
    void testProcessContent_WithEnglishContent_ShouldDetectEnglishLanguage() {
        // 测试英文内容的语言检测
        testContent.setTitle("Machine Learning Advances in Natural Language Processing");
        testContent.setContent("Recent advances in machine learning have revolutionized natural language processing. " +
                "Deep learning models like transformers have achieved remarkable performance in various NLP tasks. " +
                "These breakthrough technologies are enabling new applications in artificial intelligence.");

        KnowledgeContent processedContent = aiProcessingService.processContent(testContent);

        assertThat(processedContent.getTags()).contains("语言:英文");
        assertThat(processedContent.isProcessed()).isTrue();
    }

    @Test
    void testProcessContent_WithTechEntities_ShouldExtractEntities() {
        // 测试实体提取功能
        testContent.setTitle("云计算平台比较：AWS vs Azure vs Google Cloud");
        testContent.setContent("Amazon Web Services (AWS) 是目前最大的云服务提供商。" +
                "Microsoft Azure和Google Cloud Platform也是重要的竞争者。" +
                "这些平台都提供docker容器服务和kubernetes编排功能。");

        KnowledgeContent processedContent = aiProcessingService.processContent(testContent);

        String tags = processedContent.getTags();
        assertThat(tags).containsAnyOf("aws", "azure", "google cloud", "docker", "kubernetes");
        assertThat(processedContent.isProcessed()).isTrue();
    }

    @Test
    void testProcessContent_WithLongContent_ShouldAssessQuality() {
        // 测试内容质量评估
        testContent.setContent("这是一篇详细的技术文章，包含了大量的实用信息和代码示例。" +
                "文章结构清晰，从基础概念开始，逐步深入到高级主题。" +
                "作者通过实际案例和最佳实践，帮助读者深入理解相关技术。" +
                "文章还提供了丰富的参考资料和扩展阅读建议。" +
                "这样的内容对开发者学习和提升技能非常有帮助。" +
                "通过理论与实践相结合的方式，读者可以获得全面的知识。" +
                "文章的可读性强，技术深度适中，适合不同水平的读者。");
        testContent.setTags("Java,Spring,微服务,架构,最佳实践");

        KnowledgeContent processedContent = aiProcessingService.processContent(testContent);

        assertThat(processedContent.getTags()).containsAnyOf("质量:高质量", "质量:中等质量");
        assertThat(processedContent.isProcessed()).isTrue();
    }

    @Test
    void testProcessContent_WithInvalidContent_ShouldHandleGracefully() {
        // 测试无效内容的处理
        KnowledgeContent invalidContent = new KnowledgeContent();
        invalidContent.setTitle(""); // 空标题
        invalidContent.setContent("短"); // 过短内容

        KnowledgeContent processedContent = aiProcessingService.processContent(invalidContent);

        // 无效内容应该原样返回，不进行处理
        assertThat(processedContent).isNotNull();
        assertThat(processedContent.isProcessed()).isFalse();
    }

    @Test
    void testBatchProcessContents_ShouldProcessAllContents() {
        // 准备批量测试数据
        KnowledgeContent content1 = new KnowledgeContent();
        content1.setTitle("Java编程基础");
        content1.setContent("Java是一种面向对象的编程语言，具有跨平台特性。");
        content1.setSourceUrl("https://example.com/java-basics");
        content1.setContentType("RSS");
        content1.setAcquisitionTime(LocalDateTime.now());
        content1.setProcessed(false);
        content1.setSuccess(true);

        KnowledgeContent content2 = new KnowledgeContent();
        content2.setTitle("Python数据分析指南");
        content2.setContent("Python在数据科学领域具有强大的生态系统，包括pandas、numpy等库。");
        content2.setSourceUrl("https://example.com/python-data");
        content2.setContentType("Web");
        content2.setAcquisitionTime(LocalDateTime.now());
        content2.setProcessed(false);
        content2.setSuccess(true);

        List<KnowledgeContent> contents = Arrays.asList(content1, content2);

        // 执行批量处理
        List<KnowledgeContent> processedContents = aiProcessingService.batchProcessContents(contents);

        // 验证处理结果
        assertThat(processedContents).hasSize(2);
        assertThat(processedContents.get(0).isProcessed()).isTrue();
        assertThat(processedContents.get(1).isProcessed()).isTrue();
        assertThat(processedContents.get(0).getTags()).isNotEmpty();
        assertThat(processedContents.get(1).getTags()).isNotEmpty();
    }

    @Test
    void testProcessContent_WithExceptionHandling_ShouldNotCrash() {
        // 测试异常处理
        KnowledgeContent nullContentTest = null;
        
        // 应该能够处理null输入而不崩溃
        try {
            aiProcessingService.processContent(nullContentTest);
        } catch (Exception e) {
            // 应该优雅地处理异常
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }
}
