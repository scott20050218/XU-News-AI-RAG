package cn.lihengrui.langchain.service;

import cn.lihengrui.langchain.dto.KnowledgeContentDto;
import cn.lihengrui.langchain.dto.KnowledgeSearchRequest;
import cn.lihengrui.langchain.dto.KnowledgeSearchResponse;
import cn.lihengrui.langchain.dto.KnowledgeSearchResult;
import cn.lihengrui.langchain.dto.WebSearchResultDto;
import cn.lihengrui.langchain.dto.EnhancedSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增强的知识检索服务
 * 使用LangChain4j和大模型进行智能检索和问答
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedKnowledgeSearchService {
    
    private final LangChainService langChainService;
    private final ApiClientService apiClientService;
    private final WebSearchService webSearchService;
    
    /**
     * 基于用户提问检索知识库内容（使用LangChain）
     */
    public KnowledgeSearchResponse searchKnowledgeWithLangChain(KnowledgeSearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始LangChain知识检索: query={}, topK={}", request.getQuery(), request.getTopK());
            
            // 1. 使用LangChain检索相关文档（带相似度）
            long searchStart = System.currentTimeMillis();
            List<cn.lihengrui.langchain.dto.LangChainSearchResult> relevantDocuments = 
                    langChainService.searchRelevantDocumentsWithSimilarity(request.getQuery(), request.getTopK());
            long searchTime = System.currentTimeMillis() - searchStart;
            
            // 2. 转换为搜索结果
            List<KnowledgeSearchResult> results = convertToSearchResultsWithSimilarity(relevantDocuments, request.getQuery());
            
            // 3. 计算统计信息
            double averageSimilarity = calculateAverageSimilarity(results);
            double maxSimilarity = results.isEmpty() ? 0.0 : 
                    results.stream().mapToDouble(KnowledgeSearchResult::getSimilarity).max().orElse(0.0);
            double minSimilarity = results.isEmpty() ? 0.0 : 
                    results.stream().mapToDouble(KnowledgeSearchResult::getSimilarity).min().orElse(0.0);
            
            // 4. 构建响应
            KnowledgeSearchResponse response = new KnowledgeSearchResponse();
            response.setQuery(request.getQuery());
            response.setResults(results);
            response.setResultCount(results.size());
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            response.setVectorizationTimeMs(0L); // LangChain内部处理
            response.setSearchTimeMs(searchTime);
            response.setTimestamp(LocalDateTime.now());
            response.setAverageSimilarity(averageSimilarity);
            response.setMaxSimilarity(maxSimilarity);
            response.setMinSimilarity(minSimilarity);
            
            log.info("LangChain知识检索完成: query={}, 结果数={}, 处理时间={}ms", 
                    request.getQuery(), results.size(), response.getProcessingTimeMs());
            
            return response;
            
        } catch (Exception e) {
            log.error("LangChain知识检索失败: query={}", request.getQuery(), e);
            
            // 返回空结果
            KnowledgeSearchResponse response = new KnowledgeSearchResponse();
            response.setQuery(request.getQuery());
            response.setResults(new ArrayList<>());
            response.setResultCount(0);
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            response.setTimestamp(LocalDateTime.now());
            response.setAverageSimilarity(0.0);
            response.setMaxSimilarity(0.0);
            response.setMinSimilarity(0.0);
            
            return response;
        }
    }
    
    /**
     * 使用大模型进行智能问答
     */
    public String askQuestionWithLLM(String question) {
        try {
            log.info("开始大模型问答: question={}", question);
            
            // 1. 检索相关文档
            List<String> context = langChainService.searchRelevantDocuments(question, 5);
            
            // 2. 使用大模型生成答案
            String answer = langChainService.askQuestion(question, context);
            
            log.info("大模型问答完成: question={}, 上下文片段数={}", question, context.size());
            return answer;
            
        } catch (Exception e) {
            log.error("大模型问答失败: question={}", question, e);
            return "抱歉，我在处理您的问题时遇到了错误。";
        }
    }
    
    /**
     * 批量处理知识内容并添加到LangChain向量存储
     */
    public int processKnowledgeContentForLangChain(int page, int size) {
        try {
            log.info("开始处理知识内容到LangChain向量存储: page={}, size={}", page, size);
            
            // 1. 从API获取知识内容
            List<KnowledgeContentDto> contents = apiClientService.getAllKnowledgeContent(page, size);
            
            if (contents == null || contents.isEmpty()) {
                log.info("没有找到知识内容");
                return 0;
            }
            
            // 2. 处理每个内容
            int processedCount = 0;
            for (KnowledgeContentDto content : contents) {
                // 检查是否有可用的内容（content 或 summary）
                boolean hasContent = (content.getContent() != null && !content.getContent().trim().isEmpty()) ||
                                   (content.getSummary() != null && !content.getSummary().trim().isEmpty());
                
                if (hasContent) {
                    log.info("处理知识内容: {}", content.getTitle());
                    // 构建文档内容
                    String documentContent = buildDocumentContent(content);
                    
                    // 添加到LangChain向量存储
                    langChainService.addDocument(documentContent, content.getTitle());
                    processedCount++;
                } else {
                    log.warn("跳过内容ID {}，因为内容和摘要都为空", content.getId());
                }
            }
            
            log.info("LangChain向量存储处理完成: 处理 {} 条记录", processedCount);
            return processedCount;
            
        } catch (Exception e) {
            log.error("处理知识内容到LangChain失败", e);
            return 0;
        }
    }
    
    /**
     * 构建文档内容
     */
    private String buildDocumentContent(KnowledgeContentDto content) {
        StringBuilder document = new StringBuilder();
        
        // 标题
        if (content.getTitle() != null) {
            document.append("标题: ").append(content.getTitle()).append("\n\n");
        }
        
        // 内容
        if (content.getContent() != null) {
            document.append("内容: ").append(content.getContent()).append("\n\n");
        }
        
        // 摘要
        if (content.getSummary() != null && !content.getSummary().isEmpty()) {
            document.append("摘要: ").append(content.getSummary()).append("\n\n");
        }
        
        // 标签
        if (content.getTags() != null && !content.getTags().isEmpty()) {
            document.append("标签: ").append(content.getTags()).append("\n\n");
        }
        
        // 来源
        if (content.getSourceUrl() != null) {
            document.append("来源: ").append(content.getSourceUrl());
        }
        
        return document.toString();
    }
    
    /**
     * 转换为搜索结果
     */
    private List<KnowledgeSearchResult> convertToSearchResults(List<String> documents, String query) {
        return documents.stream()
                .map(doc -> {
                    KnowledgeSearchResult result = new KnowledgeSearchResult();
                    result.setTitle(extractTitle(doc));
                    result.setContent(truncateContent(doc, 500));
                    result.setSummary(extractSummary(doc));
                    result.setSimilarity(0.9); // LangChain返回的结果相似度较高
                    result.setProcessed(true);
                    result.setSuccess(true);
                    return result;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为搜索结果（带真实相似度）
     */
    private List<KnowledgeSearchResult> convertToSearchResultsWithSimilarity(
            List<cn.lihengrui.langchain.dto.LangChainSearchResult> documents, String query) {
        return documents.stream()
                .map(doc -> {
                    KnowledgeSearchResult result = new KnowledgeSearchResult();
                    result.setTitle(extractTitle(doc.getContent()));
                    result.setContent(truncateContent(doc.getContent(), 500));
                    result.setSummary(extractSummary(doc.getContent()));
                    result.setSimilarity(doc.getSimilarity()); // 使用真实的相似度分数
                    result.setProcessed(true);
                    result.setSuccess(true);
                    return result;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 提取标题
     */
    private String extractTitle(String document) {
        if (document.startsWith("标题: ")) {
            int endIndex = document.indexOf("\n\n");
            if (endIndex > 0) {
                return document.substring(3, endIndex);
            }
        }
        return document.length() > 50 ? document.substring(0, 50) + "..." : document;
    }
    
    /**
     * 提取摘要
     */
    private String extractSummary(String document) {
        if (document.contains("摘要: ")) {
            int startIndex = document.indexOf("摘要: ");
            int endIndex = document.indexOf("\n\n", startIndex);
            if (endIndex > startIndex) {
                return document.substring(startIndex + 3, endIndex);
            }
        }
        return document.length() > 200 ? document.substring(0, 200) + "..." : document;
    }
    
    /**
     * 截取内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
    
    /**
     * 计算平均相似度
     */
    private double calculateAverageSimilarity(List<KnowledgeSearchResult> results) {
        if (results.isEmpty()) {
            return 0.0;
        }
        
        double sum = results.stream()
                .mapToDouble(KnowledgeSearchResult::getSimilarity)
                .sum();
        
        return sum / results.size();
    }
    
    /**
     * 获取LangChain向量存储中的向量数量
     */
    public int getLangChainVectorCount() {
        return langChainService.getVectorCount();
    }
    
    /**
     * 增强搜索：知识库检索 + 联网查询 + 大模型推理
     * 如果知识库未匹配到相关数据，自动触发联网查询
     */
    public EnhancedSearchResponseDto enhancedSearchWithWebFallback(KnowledgeSearchRequest request) {
        long totalStartTime = System.currentTimeMillis();
        
        try {
            log.info("开始增强搜索: query={}, topK={}", request.getQuery(), request.getTopK());
            
            // 1. 首先进行知识库检索
            long knowledgeSearchStart = System.currentTimeMillis();
            KnowledgeSearchResponse knowledgeResponse = searchKnowledgeWithLangChain(request);
            long knowledgeSearchTime = System.currentTimeMillis() - knowledgeSearchStart;
            
            // 2. 判断是否需要联网查询
            boolean needWebSearch = shouldTriggerWebSearch(knowledgeResponse, request);
            List<WebSearchResultDto> webSearchResults = new ArrayList<>();
            long webSearchTime = 0L;
            log.info("needWebSearch: {}", needWebSearch);
            if (needWebSearch) {
                log.info("知识库检索结果不足，触发联网查询: query={}", request.getQuery());
                
                // 3. 执行联网查询
                long webSearchStart = System.currentTimeMillis();
                List<WebSearchService.WebSearchResult> webResults = 
                    webSearchService.searchBaidu(request.getQuery(), 3);
                webSearchTime = System.currentTimeMillis() - webSearchStart;
                
                // 4. 转换联网搜索结果
                webSearchResults = convertWebSearchResults(webResults);
                
                log.info("联网查询完成: 返回 {} 条结果", webSearchResults.size());
            }
            
            // 5. 大模型推理
            long llmStart = System.currentTimeMillis();
            String llmInference = generateLLMInference(request.getQuery(), knowledgeResponse, webSearchResults);
            long llmTime = System.currentTimeMillis() - llmStart;
            
            // 6. 构建增强响应
            EnhancedSearchResponseDto response = EnhancedSearchResponseDto.builder()
                    .query(request.getQuery())
                    .knowledgeResults(knowledgeResponse.getResults())
                    .knowledgeResultCount(knowledgeResponse.getResultCount())
                    .webSearchResults(webSearchResults)
                    .webSearchResultCount(webSearchResults.size())
                    .llmInference(llmInference)
                    .webSearchTriggered(needWebSearch)
                    .totalProcessingTimeMs(System.currentTimeMillis() - totalStartTime)
                    .knowledgeSearchTimeMs(knowledgeSearchTime)
                    .webSearchTimeMs(webSearchTime)
                    .llmInferenceTimeMs(llmTime)
                    .timestamp(LocalDateTime.now())
                    .averageSimilarity(knowledgeResponse.getAverageSimilarity())
                    .maxSimilarity(knowledgeResponse.getMaxSimilarity())
                    .minSimilarity(knowledgeResponse.getMinSimilarity())
                    .searchMethod(needWebSearch ? "知识库+联网+LLM" : "知识库+LLM")
                    .build();
            
            log.info("增强搜索完成: query={}, 知识库结果={}, 联网结果={}, 总耗时={}ms", 
                    request.getQuery(), knowledgeResponse.getResultCount(), 
                    webSearchResults.size(), response.getTotalProcessingTimeMs());
            
            return response;
            
        } catch (Exception e) {
            log.error("增强搜索失败: query={}", request.getQuery(), e);
            
            // 返回错误响应
            return EnhancedSearchResponseDto.builder()
                    .query(request.getQuery())
                    .knowledgeResults(new ArrayList<>())
                    .knowledgeResultCount(0)
                    .webSearchResults(new ArrayList<>())
                    .webSearchResultCount(0)
                    .llmInference("抱歉，搜索过程中遇到错误，请稍后重试。")
                    .webSearchTriggered(false)
                    .totalProcessingTimeMs(System.currentTimeMillis() - totalStartTime)
                    .timestamp(LocalDateTime.now())
                    .searchMethod("错误")
                    .build();
        }
    }
    
    /**
     * 判断是否需要触发联网查询
     */
    private boolean shouldTriggerWebSearch(KnowledgeSearchResponse knowledgeResponse, KnowledgeSearchRequest request) {
        // 如果知识库没有结果，触发联网查询
        if (knowledgeResponse.getResultCount() == 0) {
            return true;
        }
        
        // 如果知识库结果的平均相似度太低，触发联网查询
        if (knowledgeResponse.getAverageSimilarity() < 0.5) {
            return true;
        }
        
        // 如果知识库结果数量少于请求的topK的一半，触发联网查询
        if (knowledgeResponse.getResultCount() < request.getTopK() / 2) {
            return true;
        }
        
        // 为了测试联网查询功能，临时降低阈值
        if (knowledgeResponse.getAverageSimilarity() < 0.8) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 转换联网搜索结果
     */
    private List<WebSearchResultDto> convertWebSearchResults(List<WebSearchService.WebSearchResult> webResults) {
        return webResults.stream()
                .map(result -> WebSearchResultDto.builder()
                        .title(result.getTitle())
                        .url(result.getUrl())
                        .snippet(result.getSnippet())
                        .source(result.getSource())
                        .searchTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .relevanceScore(0.8) // 默认相关性评分
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * 生成大模型推理结果
     */
    private String generateLLMInference(String query, KnowledgeSearchResponse knowledgeResponse, 
                                       List<WebSearchResultDto> webSearchResults) {
        try {
            // 构建上下文信息
            StringBuilder context = new StringBuilder();
            context.append("用户查询：").append(query).append("\n\n");
            
            // 添加知识库结果
            if (knowledgeResponse.getResultCount() > 0) {
                context.append("知识库检索结果：\n");
                for (int i = 0; i < Math.min(knowledgeResponse.getResults().size(), 3); i++) {
                    KnowledgeSearchResult result = knowledgeResponse.getResults().get(i);
                    context.append(i + 1).append(". ").append(result.getTitle()).append("\n");
                    context.append("   摘要：").append(result.getSummary()).append("\n");
                    context.append("   相似度：").append(String.format("%.2f", result.getSimilarity())).append("\n\n");
                }
            }
            
            // 添加联网搜索结果
            if (!webSearchResults.isEmpty()) {
                context.append("联网搜索结果：\n");
                for (int i = 0; i < Math.min(webSearchResults.size(), 3); i++) {
                    WebSearchResultDto result = webSearchResults.get(i);
                    context.append(i + 1).append(". ").append(result.getTitle()).append("\n");
                    context.append("   摘要：").append(result.getSnippet()).append("\n");
                    context.append("   来源：").append(result.getSource()).append("\n\n");
                }
            }
            
            // 使用大模型生成推理结果
            String prompt = "基于以下信息，为用户的问题提供准确、有用的回答：\n\n" + context.toString();
            String answer = langChainService.askQuestion(query, List.of(prompt));
            
            return answer;
            
        } catch (Exception e) {
            log.error("生成大模型推理结果失败: query={}", query, e);
            return "基于现有信息，我无法为您提供准确的答案。请尝试重新表述您的问题。";
        }
    }
}
