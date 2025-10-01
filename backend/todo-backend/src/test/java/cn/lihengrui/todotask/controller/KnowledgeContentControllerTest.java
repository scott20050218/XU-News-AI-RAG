package cn.lihengrui.todotask.controller;

import cn.lihengrui.todotask.dto.*;
import cn.lihengrui.todotask.service.KnowledgeContentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 知识内容控制器测试类
 *
 * 测试KnowledgeContentController的所有REST API端点，
 * 包括CRUD操作、分页、过滤、批量操作等功能。
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = KnowledgeContentController.class)
@ActiveProfiles("test")
public class KnowledgeContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KnowledgeContentService knowledgeContentService;

    @Autowired
    private ObjectMapper objectMapper;

    private KnowledgeContentResponse testResponse;
    private KnowledgeContentSummary testSummary;
    private KnowledgeContentRequest testRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testResponse = new KnowledgeContentResponse();
        testResponse.setKnowId(1L);
        testResponse.setTitle("测试标题");
        testResponse.setContent("测试内容");
        testResponse.setSourceUrl("http://test.com");
        testResponse.setContentType("Manual");
        testResponse.setAcquisitionTime(LocalDateTime.now());
        testResponse.setTags("测试,技术");
        testResponse.setProcessed(false);
        testResponse.setSuccess(true);
        testResponse.setContentLength(4);
        testResponse.setSummary("测试内容");
        testResponse.setTagArray(new String[]{"测试", "技术"});

        testSummary = new KnowledgeContentSummary();
        testSummary.setKnowId(1L);
        testSummary.setTitle("测试标题");
        testSummary.setSummary("测试内容");
        testSummary.setSourceUrl("http://test.com");
        testSummary.setContentType("Manual");
        testSummary.setAcquisitionTime(LocalDateTime.now());
        testSummary.setPrimaryTags(new String[]{"测试", "技术"});
        testSummary.setProcessed(false);
        testSummary.setSuccess(true);
        testSummary.setContentLength(4);

        testRequest = new KnowledgeContentRequest();
        testRequest.setTitle("测试标题");
        testRequest.setContent("测试内容");
        testRequest.setSourceUrl("http://test.com");
        testRequest.setContentType("Manual");
        testRequest.setTags("测试,技术");
        testRequest.setProcessed(false);
        testRequest.setSuccess(true);
    }

    @Test
    void testGetKnowledgeContents_ShouldReturnPagedResults() throws Exception {
        // 准备模拟数据
        Page<KnowledgeContentSummary> page = new PageImpl<>(
                Arrays.asList(testSummary), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentService.findWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        // 执行请求
        mockMvc.perform(get("/api/knowledge-content")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "acquisitionTime")
                .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].knowId").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("测试标题"));

        // 验证服务调用
        verify(knowledgeContentService).findWithFilters(any(), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null));
    }

    @Test
    void testGetKnowledgeContents_WithFilters_ShouldApplyFilters() throws Exception {
        // 准备模拟数据
        Page<KnowledgeContentSummary> page = new PageImpl<>(
                Arrays.asList(testSummary), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentService.findWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        // 执行带过滤条件的请求
        mockMvc.perform(get("/api/knowledge-content")
                .param("contentType", "RSS")
                .param("processed", "true")
                .param("success", "true")
                .param("keyword", "测试")
                .param("tags", "技术"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证过滤条件被正确传递
        verify(knowledgeContentService).findWithFilters(any(), eq("RSS"), eq(true), eq(true), eq("测试"), eq("技术"), eq(null), eq(null));
    }

    @Test
    void testGetKnowledgeContents_WithTimeFilters_ShouldApplyTimeFilters() throws Exception {
        // 准备模拟数据
        Page<KnowledgeContentSummary> page = new PageImpl<>(
                Arrays.asList(testSummary), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentService.findWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        // 执行带时间过滤条件的请求
        mockMvc.perform(get("/api/knowledge-content")
                .param("beginTime", "2025-09-27 09:00:00")
                .param("endTime", "2025-09-27 18:00:00")
                .param("contentType", "RSS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证时间过滤条件被正确传递
        verify(knowledgeContentService).findWithFilters(
                any(), 
                eq("RSS"), 
                eq(null), 
                eq(null), 
                eq(null), 
                eq(null), 
                eq("2025-09-27 09:00:00"), 
                eq("2025-09-27 18:00:00"));
    }

    @Test
    void testGetKnowledgeContents_WithDateOnlyFilters_ShouldApplyDateFilters() throws Exception {
        // 准备模拟数据
        Page<KnowledgeContentSummary> page = new PageImpl<>(
                Arrays.asList(testSummary), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentService.findWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        // 执行带日期过滤条件的请求
        mockMvc.perform(get("/api/knowledge-content")
                .param("beginTime", "2025-09-27")
                .param("endTime", "2025-09-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证日期过滤条件被正确传递
        verify(knowledgeContentService).findWithFilters(
                any(), 
                eq(null), 
                eq(null), 
                eq(null), 
                eq(null), 
                eq(null), 
                eq("2025-09-27"), 
                eq("2025-09-28"));
    }

    @Test
    void testGetKnowledgeContent_WhenExists_ShouldReturnContent() throws Exception {
        when(knowledgeContentService.findById(1L)).thenReturn(Optional.of(testResponse));

        mockMvc.perform(get("/api/knowledge-content/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.knowId").value(1))
                .andExpect(jsonPath("$.data.title").value("测试标题"));

        verify(knowledgeContentService).findById(1L);
    }

    @Test
    void testGetKnowledgeContent_WhenNotExists_ShouldReturn404() throws Exception {
        when(knowledgeContentService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/knowledge-content/1"))
                .andExpect(status().isNotFound());

        verify(knowledgeContentService).findById(1L);
    }

    @Test
    void testCreateKnowledgeContent_WithValidData_ShouldCreateSuccessfully() throws Exception {
        when(knowledgeContentService.create(any(KnowledgeContentRequest.class)))
                .thenReturn(testResponse);

        mockMvc.perform(post("/api/knowledge-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("测试标题"));

        verify(knowledgeContentService).create(any(KnowledgeContentRequest.class));
    }

    @Test
    void testCreateKnowledgeContent_WithInvalidData_ShouldReturn400() throws Exception {
        KnowledgeContentRequest invalidRequest = new KnowledgeContentRequest();
        // 缺少必填字段

        mockMvc.perform(post("/api/knowledge-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(knowledgeContentService, never()).create(any());
    }

    @Test
    void testUpdateKnowledgeContent_WhenExists_ShouldUpdateSuccessfully() throws Exception {
        when(knowledgeContentService.update(eq(1L), any(KnowledgeContentRequest.class)))
                .thenReturn(Optional.of(testResponse));

        mockMvc.perform(put("/api/knowledge-content/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("测试标题"));

        verify(knowledgeContentService).update(eq(1L), any(KnowledgeContentRequest.class));
    }

    @Test
    void testUpdateKnowledgeContent_WhenNotExists_ShouldReturn404() throws Exception {
        when(knowledgeContentService.update(eq(1L), any(KnowledgeContentRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/knowledge-content/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound());

        verify(knowledgeContentService).update(eq(1L), any(KnowledgeContentRequest.class));
    }

    @Test
    void testDeleteKnowledgeContent_WhenExists_ShouldDeleteSuccessfully() throws Exception {
        when(knowledgeContentService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/knowledge-content/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(knowledgeContentService).delete(1L);
    }

    @Test
    void testDeleteKnowledgeContent_WhenNotExists_ShouldReturn404() throws Exception {
        when(knowledgeContentService.delete(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/knowledge-content/1"))
                .andExpect(status().isNotFound());

        verify(knowledgeContentService).delete(1L);
    }

    @Test
    void testSwitchProcessingStatus_ShouldUpdateStatus() throws Exception {
        when(knowledgeContentService.updateProcessingStatus(1L, true))
                .thenReturn(Optional.of(testResponse));

        mockMvc.perform(put("/api/knowledge-content/1/status")
                .param("processed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(knowledgeContentService).updateProcessingStatus(1L, true);
    }

    @Test
    void testBatchOperation_WithValidRequest_ShouldExecuteSuccessfully() throws Exception {
        BatchOperationRequest batchRequest = new BatchOperationRequest();
        batchRequest.setIds(Arrays.asList(1L, 2L, 3L));
        batchRequest.setOperation("DELETE");
        batchRequest.setForce(true);

        when(knowledgeContentService.batchOperation(any(BatchOperationRequest.class)))
                .thenReturn(3);

        mockMvc.perform(post("/api/knowledge-content/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(knowledgeContentService).batchOperation(any(BatchOperationRequest.class));
    }

    @Test
    void testBatchOperation_WithInvalidRequest_ShouldReturn400() throws Exception {
        BatchOperationRequest invalidRequest = new BatchOperationRequest();
        // 缺少必填字段

        mockMvc.perform(post("/api/knowledge-content/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(knowledgeContentService, never()).batchOperation(any());
    }

    @Test
    void testGetStatistics_ShouldReturnStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", 100L);
        stats.put("processedCount", 60L);

        when(knowledgeContentService.getStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/knowledge-content/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(100))
                .andExpect(jsonPath("$.data.processedCount").value(60));

        verify(knowledgeContentService).getStatistics();
    }

    @Test
    void testSearchKnowledgeContents_ShouldReturnSearchResults() throws Exception {
        Page<KnowledgeContentSummary> page = new PageImpl<>(
                Arrays.asList(testSummary), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentService.search(eq("测试"), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/knowledge-content/search")
                .param("query", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(knowledgeContentService).search(eq("测试"), any());
    }

    @Test
    void testGetAllTags_ShouldReturnTagsList() throws Exception {
        List<String> tags = Arrays.asList("技术", "测试", "Java", "Spring");

        when(knowledgeContentService.getAllTags()).thenReturn(tags);

        mockMvc.perform(get("/api/knowledge-content/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("技术"));

        verify(knowledgeContentService).getAllTags();
    }

    @Test
    void testControllerExceptionHandling_ShouldReturn500() throws Exception {
        when(knowledgeContentService.findById(1L)).thenThrow(new RuntimeException("数据库连接错误"));

        mockMvc.perform(get("/api/knowledge-content/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500));

        verify(knowledgeContentService).findById(1L);
    }
}
