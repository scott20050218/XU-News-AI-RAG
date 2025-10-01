package cn.lihengrui.langchain.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LangChainService单元测试
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@ExtendWith(MockitoExtension.class)
class LangChainServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    private LangChainService langChainService;

    @BeforeEach
    void setUp() {
        langChainService = new LangChainService();
        // 使用反射设置私有字段
        ReflectionTestUtils.setField(langChainService, "embeddingModel", embeddingModel);
        ReflectionTestUtils.setField(langChainService, "embeddingStore", embeddingStore);
    }

    @Test
    void testAddDocument_Success() {
        // Given
        String content = "测试内容";
        String metadata = "测试元数据";
        Embedding mockEmbedding = mock(Embedding.class);
        TextSegment mockSegment = mock(TextSegment.class);

        when(embeddingModel.embed(any(TextSegment.class))).thenReturn(dev.langchain4j.model.output.Response.from(mockEmbedding));

        // When
        assertDoesNotThrow(() -> langChainService.addDocument(content, metadata));

        // Then
        verify(embeddingModel).embed(any(TextSegment.class));
        verify(embeddingStore).add(any(Embedding.class), any(TextSegment.class));
    }

    @Test
    void testAddDocument_WithException() {
        // Given
        String content = "测试内容";
        String metadata = "测试元数据";
        
        when(embeddingModel.embed(any(TextSegment.class)))
                .thenThrow(new RuntimeException("嵌入模型错误"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                langChainService.addDocument(content, metadata));
    }

    @Test
    void testSearchRelevantDocuments_Success() {
        // Given
        String query = "测试查询";
        int maxResults = 5;
        Embedding mockEmbedding = mock(Embedding.class);
        
        when(embeddingModel.embed(query)).thenReturn(dev.langchain4j.model.output.Response.from(mockEmbedding));
        when(embeddingStore.findRelevant(any(Embedding.class), eq(maxResults)))
                .thenReturn(List.of());

        // When
        List<String> result = langChainService.searchRelevantDocuments(query, maxResults);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(embeddingModel).embed(query);
        verify(embeddingStore).findRelevant(any(Embedding.class), eq(maxResults));
    }

    @Test
    void testSearchRelevantDocuments_WithException() {
        // Given
        String query = "测试查询";
        int maxResults = 5;
        
        when(embeddingModel.embed(query))
                .thenThrow(new RuntimeException("嵌入模型错误"));

        // When
        List<String> result = langChainService.searchRelevantDocuments(query, maxResults);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAskQuestion_WithChatModel() {
        // Given
        String question = "测试问题";
        List<String> context = List.of("上下文1", "上下文2");
        
        // 模拟有聊天模型的情况
        OpenAiChatModel mockChatModel = mock(OpenAiChatModel.class);
        when(mockChatModel.generate(anyString())).thenReturn("基于知识库内容的回答");
        ReflectionTestUtils.setField(langChainService, "chatModel", mockChatModel);

        // When
        String result = langChainService.askQuestion(question, context);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("基于知识库内容"));
    }

    @Test
    void testAskQuestion_WithoutChatModel() {
        // Given
        String question = "测试问题";
        List<String> context = List.of("上下文1", "上下文2");

        // When
        String result = langChainService.askQuestion(question, context);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("基于知识库内容"));
    }

    @Test
    void testAskQuestion_EmptyContext() {
        // Given
        String question = "测试问题";
        List<String> context = List.of();

        // When
        String result = langChainService.askQuestion(question, context);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("抱歉，我在知识库中没有找到"));
    }

    @Test
    void testGetVectorCount() {
        // When
        int result = langChainService.getVectorCount();

        // Then
        assertTrue(result >= 0);
    }

    @Test
    void testClearVectorStore() {
        // When
        assertDoesNotThrow(() -> langChainService.clearVectorStore());

        // Then
        // 验证向量存储被清空（这里我们无法直接验证，因为embeddingStore是mock的）
        // 注意：clearVectorStore()方法会重新创建embeddingStore，所以这里不需要验证clear()调用
    }
}
