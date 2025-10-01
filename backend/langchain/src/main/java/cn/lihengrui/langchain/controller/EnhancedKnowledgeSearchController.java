package cn.lihengrui.langchain.controller;

import cn.lihengrui.langchain.dto.KnowledgeSearchRequest;
import cn.lihengrui.langchain.dto.KnowledgeSearchResponse;
import cn.lihengrui.langchain.dto.EnhancedSearchResponseDto;
import cn.lihengrui.langchain.service.EnhancedKnowledgeSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 增强的知识检索控制器
 * 使用LangChain4j和大模型进行智能检索和问答
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@RestController
@RequestMapping("/api/enhanced-knowledge")
@RequiredArgsConstructor
@Tag(name = "增强知识检索", description = "基于LangChain4j和大模型的智能知识检索API")
public class EnhancedKnowledgeSearchController {
    
    private final EnhancedKnowledgeSearchService enhancedKnowledgeSearchService;
    
    /**
     * 基于LangChain的智能知识检索
     */
    @PostMapping("/search")
    @Operation(summary = "LangChain智能检索", description = "使用LangChain4j框架进行智能知识检索")
    public ResponseEntity<Map<String, Object>> searchWithLangChain(@RequestBody(required = false) KnowledgeSearchRequest request) {
        try {
            // 处理空请求体
            if (request == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "请求体不能为空");
                errorResponse.put("code", 400);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("接收LangChain知识检索请求: query={}, topK={}", request.getQuery(), request.getTopK());
            
            // 参数验证
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "查询文本不能为空");
                errorResponse.put("code", 400);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 设置默认值
            if (request.getTopK() == null || request.getTopK() <= 0) {
                request.setTopK(10);
            }
            
            // 执行LangChain检索
            KnowledgeSearchResponse response = enhancedKnowledgeSearchService.searchKnowledgeWithLangChain(request);
            
            // 构建成功响应
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "LangChain检索完成");
            successResponse.put("code", 200);
            successResponse.put("data", response);
            successResponse.put("method", "LangChain4j");
            
            log.info("LangChain知识检索完成: query={}, 结果数={}, 处理时间={}ms", 
                    request.getQuery(), response.getResultCount(), response.getProcessingTimeMs());
            
            return ResponseEntity.ok(successResponse);
            
        } catch (org.springframework.http.converter.HttpMessageNotReadableException e) {
            log.error("JSON解析错误: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "JSON格式错误: " + e.getMessage());
            errorResponse.put("code", 400);
            errorResponse.put("data", null);
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("LangChain知识检索异常: request={}", request, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "LangChain检索失败: " + e.getMessage());
            errorResponse.put("code", 500);
            errorResponse.put("data", null);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 大模型智能问答
     */
    @PostMapping("/ask")
    @Operation(summary = "大模型智能问答", description = "使用大语言模型进行智能问答")
    public ResponseEntity<Map<String, Object>> askWithLLM(@RequestBody(required = false) Map<String, String> request) {
        try {
            // 处理空请求体
            if (request == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "请求体不能为空");
                errorResponse.put("code", 400);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String question = request.get("question");
            if (question == null || question.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "问题不能为空");
                errorResponse.put("code", 400);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("接收大模型问答请求: question={}", question);
            
            // 执行大模型问答
            String answer = enhancedKnowledgeSearchService.askQuestionWithLLM(question.trim());
            
            // 构建成功响应
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "大模型问答完成");
            successResponse.put("code", 200);
            successResponse.put("question", question);
            successResponse.put("answer", answer);
            successResponse.put("method", "LLM");
            
            log.info("大模型问答完成: question={}", question);
            
            return ResponseEntity.ok(successResponse);
            
        } catch (org.springframework.http.converter.HttpMessageNotReadableException e) {
            log.error("JSON解析错误: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "JSON格式错误: " + e.getMessage());
            errorResponse.put("code", 400);
            errorResponse.put("data", null);
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("大模型问答异常: request={}", request, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "大模型问答失败: " + e.getMessage());
            errorResponse.put("code", 500);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 处理知识内容到LangChain向量存储
     */
    @PostMapping("/process-langchain")
    @Operation(summary = "处理知识内容到LangChain", description = "将知识内容处理并添加到LangChain向量存储")
    public ResponseEntity<Map<String, Object>> processToLangChain(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("开始处理知识内容到LangChain向量存储: page={}, size={}", page, size);
            
            // 执行处理
            int processedCount = enhancedKnowledgeSearchService.processKnowledgeContentForLangChain(page, size);
            
            // 构建成功响应
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "LangChain向量存储处理完成");
            successResponse.put("code", 200);
            successResponse.put("processedCount", processedCount);
            successResponse.put("page", page);
            successResponse.put("size", size);
            successResponse.put("method", "LangChain4j");
            
            log.info("LangChain向量存储处理完成: 处理 {} 条记录", processedCount);
            
            return ResponseEntity.ok(successResponse);
            
        } catch (Exception e) {
            log.error("处理知识内容到LangChain失败: page={}, size={}", page, size, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "处理失败: " + e.getMessage());
            errorResponse.put("code", 500);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 获取LangChain服务状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取LangChain服务状态", description = "获取LangChain服务的状态信息")
    public ResponseEntity<Map<String, Object>> getLangChainStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("langchainEnabled", true);
            status.put("embeddingModel", "AllMiniLmL6V2");
            status.put("vectorStore", "InMemoryEmbeddingStore");
            status.put("chatModel", "OpenAI GPT-3.5-turbo");
            status.put("vectorCount", enhancedKnowledgeSearchService.getLangChainVectorCount()); // 从服务获取实际数量
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "LangChain服务状态正常");
            response.put("code", 200);
            response.put("data", status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取LangChain服务状态失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取状态失败: " + e.getMessage());
            errorResponse.put("code", 500);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 增强搜索：知识库检索 + 联网查询 + 大模型推理
     * 如果知识库未匹配到相关数据，自动触发联网查询
     */
    @PostMapping("/enhanced-search")
    @Operation(summary = "增强搜索", description = "知识库检索+联网查询+大模型推理，自动触发联网查询")
    public ResponseEntity<Map<String, Object>> enhancedSearchWithWebFallback(@RequestBody(required = false) KnowledgeSearchRequest request) {
        try {
            // 处理空请求体
            if (request == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "请求体不能为空");
                errorResponse.put("code", 400);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("接收增强搜索请求: query={}, topK={}", request.getQuery(), request.getTopK());
            
            // 参数验证
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "查询文本不能为空");
                errorResponse.put("code", 400);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 设置默认值
            if (request.getTopK() == null || request.getTopK() <= 0) {
                request.setTopK(10);
            }
            
            // 执行增强搜索
            EnhancedSearchResponseDto response = enhancedKnowledgeSearchService.enhancedSearchWithWebFallback(request);
            
            // 构建成功响应
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "增强搜索完成");
            successResponse.put("code", 200);
            successResponse.put("data", response);
            successResponse.put("method", response.getSearchMethod());
            
            log.info("增强搜索完成: query={}, 知识库结果={}, 联网结果={}, 总耗时={}ms", 
                    request.getQuery(), response.getKnowledgeResultCount(), 
                    response.getWebSearchResultCount(), response.getTotalProcessingTimeMs());
            
            return ResponseEntity.ok(successResponse);
            
        } catch (org.springframework.http.converter.HttpMessageNotReadableException e) {
            log.error("JSON解析错误: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "JSON格式错误: " + e.getMessage());
            errorResponse.put("code", 400);
            errorResponse.put("data", null);
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("增强搜索异常: request={}", request, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "增强搜索失败: " + e.getMessage());
            errorResponse.put("code", 500);
            errorResponse.put("data", null);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 获取详细的向量存储信息
     */
    @GetMapping("/debug/vector-info")
    @Operation(summary = "获取详细向量存储信息", description = "获取AllMiniLmL6V2嵌入模型的详细存储信息")
    public ResponseEntity<Map<String, Object>> getDetailedVectorInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            
            // 基本信息
            info.put("embeddingModel", "AllMiniLmL6V2");
            info.put("modelType", "本地嵌入模型");
            info.put("vectorDimension", 384);
            info.put("vectorCount", enhancedKnowledgeSearchService.getLangChainVectorCount());
            
            // 存储信息
            info.put("vectorStore", "InMemoryEmbeddingStore");
            info.put("storageType", "内存存储");
            info.put("persistenceEnabled", true);
            
            // 性能信息
            info.put("modelStatus", "已加载");
            info.put("initializationTime", "应用启动时");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取详细向量存储信息成功");
            response.put("code", 200);
            response.put("data", info);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取详细向量存储信息失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取详细信息失败: " + e.getMessage());
            errorResponse.put("code", 500);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
