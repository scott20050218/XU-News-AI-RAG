package cn.lihengrui.langchain.service;

import cn.lihengrui.langchain.dto.KnowledgeContentDto;
import cn.lihengrui.langchain.dto.KnowledgeSearchRequest;
import cn.lihengrui.langchain.dto.KnowledgeSearchResponse;
import cn.lihengrui.langchain.service.WebSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EnhancedKnowledgeSearchService单元测试
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@ExtendWith(MockitoExtension.class)
class EnhancedKnowledgeSearchServiceTest {

    @Mock
    private LangChainService langChainService;

    @Mock
    private ApiClientService apiClientService;
    
    @Mock
    private WebSearchService webSearchService;

    private EnhancedKnowledgeSearchService enhancedKnowledgeSearchService;

    @BeforeEach
    void setUp() {
        enhancedKnowledgeSearchService = new EnhancedKnowledgeSearchService(langChainService, apiClientService, webSearchService);
    }

    @Test
    void testSearchKnowledgeWithLangChain_Success() {
        // Given
        KnowledgeSearchRequest request = new KnowledgeSearchRequest();
        request.setQuery("测试查询");
        request.setTopK(5);

        List<cn.lihengrui.langchain.dto.LangChainSearchResult> mockResults = Arrays.asList(
                new cn.lihengrui.langchain.dto.LangChainSearchResult("文档1", 0.9),
                new cn.lihengrui.langchain.dto.LangChainSearchResult("文档2", 0.8),
                new cn.lihengrui.langchain.dto.LangChainSearchResult("文档3", 0.7)
        );
        when(langChainService.searchRelevantDocumentsWithSimilarity(anyString(), anyInt()))
                .thenReturn(mockResults);

        // When
        KnowledgeSearchResponse response = enhancedKnowledgeSearchService.searchKnowledgeWithLangChain(request);

        // Then
        assertNotNull(response);
        assertEquals("测试查询", response.getQuery());
        assertEquals(3, response.getResultCount());
        assertTrue(response.getProcessingTimeMs() >= 0);
        verify(langChainService).searchRelevantDocumentsWithSimilarity("测试查询", 5);
    }

    @Test
    void testSearchKnowledgeWithLangChain_EmptyResults() {
        // Given
        KnowledgeSearchRequest request = new KnowledgeSearchRequest();
        request.setQuery("测试查询");
        request.setTopK(5);

        when(langChainService.searchRelevantDocumentsWithSimilarity(anyString(), anyInt()))
                .thenReturn(Arrays.asList());

        // When
        KnowledgeSearchResponse response = enhancedKnowledgeSearchService.searchKnowledgeWithLangChain(request);

        // Then
        assertNotNull(response);
        assertEquals("测试查询", response.getQuery());
        assertEquals(0, response.getResultCount());
        assertEquals(0.0, response.getAverageSimilarity());
        verify(langChainService).searchRelevantDocumentsWithSimilarity("测试查询", 5);
    }

    @Test
    void testSearchKnowledgeWithLangChain_WithException() {
        // Given
        KnowledgeSearchRequest request = new KnowledgeSearchRequest();
        request.setQuery("测试查询");
        request.setTopK(5);

        when(langChainService.searchRelevantDocumentsWithSimilarity(anyString(), anyInt()))
                .thenThrow(new RuntimeException("LangChain错误"));

        // When
        KnowledgeSearchResponse response = enhancedKnowledgeSearchService.searchKnowledgeWithLangChain(request);

        // Then
        assertNotNull(response);
        assertEquals("测试查询", response.getQuery());
        assertEquals(0, response.getResultCount());
        assertEquals(0.0, response.getAverageSimilarity());
        verify(langChainService).searchRelevantDocumentsWithSimilarity("测试查询", 5);
    }

    @Test
    void testAskQuestionWithLLM_Success() {
        // Given
        String question = "什么是博客园？";
        List<String> mockContext = Arrays.asList("上下文1", "上下文2");
        String mockAnswer = "博客园是一个开发者社区";

        when(langChainService.searchRelevantDocuments(question, 5))
                .thenReturn(mockContext);
        when(langChainService.askQuestion(question, mockContext))
                .thenReturn(mockAnswer);

        // When
        String result = enhancedKnowledgeSearchService.askQuestionWithLLM(question);

        // Then
        assertNotNull(result);
        assertEquals(mockAnswer, result);
        verify(langChainService).searchRelevantDocuments(question, 5);
        verify(langChainService).askQuestion(question, mockContext);
    }

    @Test
    void testAskQuestionWithLLM_WithException() {
        // Given
        String question = "什么是博客园？";

        when(langChainService.searchRelevantDocuments(question, 5))
                .thenThrow(new RuntimeException("搜索错误"));

        // When
        String result = enhancedKnowledgeSearchService.askQuestionWithLLM(question);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("遇到了错误"));
    }

    @Test
    void testProcessKnowledgeContentForLangChain_Success() {
        // Given
        int page = 0;
        int size = 10;
        
        KnowledgeContentDto content1 = new KnowledgeContentDto();
        content1.setId(1L);
        content1.setTitle("测试标题1");
        content1.setContent("测试内容1");
        
        KnowledgeContentDto content2 = new KnowledgeContentDto();
        content2.setId(2L);
        content2.setTitle("测试标题2");
        content2.setContent("测试内容2");
        
        List<KnowledgeContentDto> contents = Arrays.asList(content1, content2);
        when(apiClientService.getAllKnowledgeContent(page, size))
                .thenReturn(contents);

        // When
        int result = enhancedKnowledgeSearchService.processKnowledgeContentForLangChain(page, size);

        // Then
        assertEquals(2, result);
        verify(apiClientService).getAllKnowledgeContent(page, size);
        verify(langChainService, times(2)).addDocument(anyString(), anyString());
    }

    @Test
    void testProcessKnowledgeContentForLangChain_EmptyContent() {
        // Given
        int page = 0;
        int size = 10;
        
        when(apiClientService.getAllKnowledgeContent(page, size))
                .thenReturn(Arrays.asList());

        // When
        int result = enhancedKnowledgeSearchService.processKnowledgeContentForLangChain(page, size);

        // Then
        assertEquals(0, result);
        verify(apiClientService).getAllKnowledgeContent(page, size);
        verify(langChainService, never()).addDocument(anyString(), anyString());
    }

    @Test
    void testProcessKnowledgeContentForLangChain_NullContent() {
        // Given
        int page = 0;
        int size = 10;
        
        when(apiClientService.getAllKnowledgeContent(page, size))
                .thenReturn(null);

        // When
        int result = enhancedKnowledgeSearchService.processKnowledgeContentForLangChain(page, size);

        // Then
        assertEquals(0, result);
        verify(apiClientService).getAllKnowledgeContent(page, size);
        verify(langChainService, never()).addDocument(anyString(), anyString());
    }

    @Test
    void testProcessKnowledgeContentForLangChain_WithException() {
        // Given
        int page = 0;
        int size = 10;
        
        when(apiClientService.getAllKnowledgeContent(page, size))
                .thenThrow(new RuntimeException("API错误"));

        // When
        int result = enhancedKnowledgeSearchService.processKnowledgeContentForLangChain(page, size);

        // Then
        assertEquals(0, result);
        verify(apiClientService).getAllKnowledgeContent(page, size);
    }

    @Test
    void testProcessKnowledgeContentForLangChain_EmptyContentString() {
        // Given
        int page = 0;
        int size = 10;
        
        KnowledgeContentDto content = new KnowledgeContentDto();
        content.setId(1L);
        content.setTitle("测试标题");
        content.setContent(""); // 空内容
        
        when(apiClientService.getAllKnowledgeContent(page, size))
                .thenReturn(Arrays.asList(content));

        // When
        int result = enhancedKnowledgeSearchService.processKnowledgeContentForLangChain(page, size);

        // Then
        assertEquals(0, result);
        verify(apiClientService).getAllKnowledgeContent(page, size);
        verify(langChainService, never()).addDocument(anyString(), anyString());
    }
}
