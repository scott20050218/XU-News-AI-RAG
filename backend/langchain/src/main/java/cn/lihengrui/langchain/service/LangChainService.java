package cn.lihengrui.langchain.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LangChain4j服务
 * 使用真正的LangChain框架和大模型进行知识检索和问答
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@Service
public class LangChainService {

    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;
    private OpenAiChatModel chatModel;
    private OpenAiEmbeddingModel openAiEmbeddingModel;
    private int vectorCount = 0; // 手动跟踪向量数量
    
    @Value("${langchain.openai.api-key:}")
    private String openAiApiKey;
    
    @Value("${langchain.openai.model:gpt-3.5-turbo}")
    private String openAiModel;
    
    @Value("${langchain.embedding.model:all-minilm-l6-v2}")
    private String embeddingModelName;
    
    @PostConstruct
    public void init() {
        try {
            // 初始化嵌入模型
            if ("all-minilm-l6-v2".equals(embeddingModelName)) {
                this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
                log.info("使用本地嵌入模型: AllMiniLmL6V2");
            } else if (!openAiApiKey.isEmpty()) {
                this.openAiEmbeddingModel = OpenAiEmbeddingModel.builder()
                        .apiKey(openAiApiKey)
                        .modelName("text-embedding-ada-002")
                        .build();
                this.embeddingModel = openAiEmbeddingModel;
                log.info("使用OpenAI嵌入模型: text-embedding-ada-002");
            } else {
                this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
                log.info("使用默认本地嵌入模型: AllMiniLmL6V2");
            }
            
            // 初始化向量存储
            this.embeddingStore = new InMemoryEmbeddingStore<>();
            
            // 初始化聊天模型（如果配置了OpenAI API Key）
            if (!openAiApiKey.isEmpty()) {
                this.chatModel = OpenAiChatModel.builder()
                        .apiKey(openAiApiKey)
                        .modelName(openAiModel)
                        .temperature(0.7)
                        .maxTokens(1000)
                        .build();
                log.info("使用OpenAI聊天模型: {}", openAiModel);
            } else {
                log.warn("未配置OpenAI API Key，将使用简单的文本匹配");
            }
            
            log.info("LangChain4j服务初始化完成");
            
        } catch (Exception e) {
            log.error("LangChain4j服务初始化失败", e);
            throw new RuntimeException("LangChain4j服务初始化失败", e);
        }
    }
    
    /**
     * 添加文档到向量存储
     */
    public void addDocument(String content, String metadata) {
        try {
            // 创建文本片段
            TextSegment segment = TextSegment.from(content);

            // 生成嵌入向量
            Embedding embedding = embeddingModel.embed(segment).content();

            // 存储到向量数据库
            embeddingStore.add(embedding, segment);
            
            // 增加计数
            vectorCount++;

            log.info("成功添加文档到向量存储: {}, 当前向量数量: {}", metadata, vectorCount);

        } catch (Exception e) {
            log.error("添加文档失败", e);
            throw new RuntimeException("添加文档失败", e);
        }
    }
    
    /**
     * 基于查询检索相关文档
     */
    public List<String> searchRelevantDocuments(String query, int maxResults) {
        try {
            // 生成查询向量
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            
            // 在向量存储中搜索
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, maxResults);
            
            // 提取文本内容
            List<String> results = matches.stream()
                    .map(match -> match.embedded().text())
                    .collect(Collectors.toList());
            
            log.info("检索到 {} 个相关文档片段", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("文档检索失败", e);
            return List.of();
        }
    }
    
    /**
     * 基于查询检索相关文档（带相似度分数）
     */
    public List<cn.lihengrui.langchain.dto.LangChainSearchResult> searchRelevantDocumentsWithSimilarity(String query, int maxResults) {
        try {
            // 生成查询向量
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            
            // 在向量存储中搜索
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, maxResults);
            
            // 提取文本内容和相似度
            List<cn.lihengrui.langchain.dto.LangChainSearchResult> results = matches.stream()
                    .map(match -> new cn.lihengrui.langchain.dto.LangChainSearchResult(
                            match.embedded().text(), 
                            match.score())) // score() 方法返回相似度分数
                    .collect(Collectors.toList());
            
            log.info("检索到 {} 个相关文档片段", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("文档检索失败", e);
            return List.of();
        }
    }
    
    /**
     * 使用大模型进行智能问答
     */
    public String askQuestion(String question, List<String> context) {
        try {
            if (chatModel == null) {
                return generateSimpleAnswer(question, context);
            }
            
            // 构建上下文
            String contextText = String.join("\n", context);
            
            // 构建提示词
            String prompt = String.format(
                "基于以下上下文信息回答问题：\n\n" +
                "上下文：\n%s\n\n" +
                "问题：%s\n\n" +
                "请基于上下文信息提供准确、有用的答案。如果上下文中没有相关信息，请说明无法回答。",
                contextText, question
            );
            
            // 调用大模型
            String answer = chatModel.generate(prompt);
            
            log.info("大模型问答完成: 问题={}, 上下文片段数={}", question, context.size());
            return answer;
            
        } catch (Exception e) {
            log.error("大模型问答失败", e);
            return generateSimpleAnswer(question, context);
        }
    }
    
    /**
     * 生成简单答案（当没有大模型时）
     */
    private String generateSimpleAnswer(String question, List<String> context) {
        if (context.isEmpty()) {
            return "抱歉，我在知识库中没有找到与您问题相关的内容。";
        }
        
        StringBuilder answer = new StringBuilder();
        answer.append("基于知识库内容，我为您找到以下相关信息：\n\n");
        
        for (int i = 0; i < Math.min(3, context.size()); i++) {
            answer.append(String.format("%d. %s\n\n", i + 1, context.get(i)));
        }
        
        return answer.toString();
    }
    
    /**
     * 获取向量存储统计信息
     */
    public int getVectorCount() {
        log.debug("获取向量数量: {}", vectorCount);
        return vectorCount;
    }
    
    /**
     * 清空向量存储
     */
    public void clearVectorStore() {
        try {
            // 重新创建向量存储来清空
            embeddingStore = new InMemoryEmbeddingStore<>();
            vectorCount = 0; // 重置计数
            log.info("向量存储已清空");
        } catch (Exception e) {
            log.error("清空向量存储失败", e);
        }
    }
}
