package cn.lihengrui.langchain.controller;

import cn.lihengrui.langchain.dto.KnowledgeSearchRequest;
import cn.lihengrui.langchain.dto.KnowledgeSearchResponse;
import cn.lihengrui.langchain.dto.KnowledgeSearchResult;
import cn.lihengrui.langchain.service.KnowledgeSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 知识检索控制器
 * 提供基于用户提问的知识库检索API接口
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识检索", description = "基于用户提问的知识库检索相关API")
public class KnowledgeSearchController {
    
    private final KnowledgeSearchService knowledgeSearchService;
    
    /**
     * 基于用户提问检索知识库内容
     */
    @PostMapping("/search")
    @Operation(summary = "知识库检索", description = "基于用户提问检索知识库内容，结果按相似度排序返回")
    public ResponseEntity<Map<String, Object>> searchKnowledge(@RequestBody KnowledgeSearchRequest request) {
        try {
            log.info("接收知识检索请求: query={}, topK={}", request.getQuery(), request.getTopK());
            
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
            if (request.getMinSimilarity() == null) {
                request.setMinSimilarity(0.0);
            }
            
            // 执行检索
            KnowledgeSearchResponse response = knowledgeSearchService.searchKnowledge(request);
            
            // 构建成功响应
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "检索完成");
            successResponse.put("code", 200);
            successResponse.put("data", response);
            
            log.info("知识检索完成: query={}, 结果数={}, 处理时间={}ms", 
                    request.getQuery(), response.getResultCount(), response.getProcessingTimeMs());
            
            return ResponseEntity.ok(successResponse);
            
        } catch (Exception e) {
            log.error("知识检索异常: query={}", request.getQuery(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检索失败: " + e.getMessage());
            errorResponse.put("code", 500);
            errorResponse.put("data", null);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 快速检索接口（GET方式）
     */
    @GetMapping("/search")
    @Operation(summary = "快速知识检索", description = "基于GET请求的快速知识检索接口")
    public ResponseEntity<Map<String, Object>> quickSearch(
            @Parameter(description = "查询文本") @RequestParam String query,
            @Parameter(description = "返回结果数量") @RequestParam(defaultValue = "10") Integer topK,
            @Parameter(description = "最小相似度阈值") @RequestParam(defaultValue = "0.0") Double minSimilarity,
            @Parameter(description = "内容类型过滤") @RequestParam(required = false) String contentType,
            @Parameter(description = "是否只返回已处理内容") @RequestParam(defaultValue = "false") Boolean processedOnly) {
        
        try {
            log.info("接收快速知识检索请求: query={}, topK={}", query, topK);
            
            // 参数验证
            if (query == null || query.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "查询文本不能为空");
                errorResponse.put("code", 400);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 构建请求对象
            KnowledgeSearchRequest request = new KnowledgeSearchRequest();
            request.setQuery(query.trim());
            request.setTopK(topK);
            request.setMinSimilarity(minSimilarity);
            request.setContentType(contentType);
            request.setProcessedOnly(processedOnly);
            
            // 执行检索
            KnowledgeSearchResponse response = knowledgeSearchService.searchKnowledge(request);
            
            // 构建成功响应
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "检索完成");
            successResponse.put("code", 200);
            successResponse.put("data", response);
            
            log.info("快速知识检索完成: query={}, 结果数={}, 处理时间={}ms", 
                    query, response.getResultCount(), response.getProcessingTimeMs());
            
            return ResponseEntity.ok(successResponse);
            
        } catch (Exception e) {
            log.error("快速知识检索异常: query={}", query, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检索失败: " + e.getMessage());
            errorResponse.put("code", 500);
            errorResponse.put("data", null);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 智能问答接口
     */
    @PostMapping("/ask")
    @Operation(summary = "智能问答", description = "基于知识库的智能问答接口")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");
            if (question == null || question.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "问题不能为空");
                errorResponse.put("code", 400);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("接收智能问答请求: question={}", question);
            
            // 构建检索请求
            KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
            searchRequest.setQuery(question.trim());
            searchRequest.setTopK(5); // 智能问答返回前5个最相关结果
            searchRequest.setMinSimilarity(0.3); // 设置较高的相似度阈值
            searchRequest.setProcessedOnly(true); // 只返回已处理的内容
            
            // 执行检索
            KnowledgeSearchResponse response = knowledgeSearchService.searchKnowledge(searchRequest);
            
            // 构建智能问答响应
            Map<String, Object> answerResponse = new HashMap<>();
            answerResponse.put("success", true);
            answerResponse.put("message", "问答完成");
            answerResponse.put("code", 200);
            answerResponse.put("question", question);
            answerResponse.put("answer", generateAnswer(response));
            answerResponse.put("sources", response.getResults());
            answerResponse.put("confidence", response.getMaxSimilarity());
            answerResponse.put("processingTime", response.getProcessingTimeMs());
            
            log.info("智能问答完成: question={}, 置信度={}, 处理时间={}ms", 
                    question, response.getMaxSimilarity(), response.getProcessingTimeMs());
            
            return ResponseEntity.ok(answerResponse);
            
        } catch (Exception e) {
            log.error("智能问答异常: question={}", request.get("question"), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "问答失败: " + e.getMessage());
            errorResponse.put("code", 500);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 生成智能答案
     */
    private String generateAnswer(KnowledgeSearchResponse response) {
        if (response.getResults().isEmpty()) {
            return "抱歉，我在知识库中没有找到与您问题相关的内容。";
        }
        
        if (response.getMaxSimilarity() < 0.5) {
            return "我找到了一些相关信息，但匹配度较低，建议您重新描述问题或查看以下相关内容：";
        }
        
        StringBuilder answer = new StringBuilder();
        answer.append("基于知识库内容，我为您找到以下相关信息：\n\n");
        
        for (int i = 0; i < Math.min(3, response.getResults().size()); i++) {
            KnowledgeSearchResult result = response.getResults().get(i);
            answer.append(String.format("%d. %s\n", i + 1, result.getTitle()));
            if (result.getSummary() != null && !result.getSummary().isEmpty()) {
                answer.append(String.format("   摘要：%s\n", result.getSummary()));
            }
            answer.append(String.format("   相似度：%.2f\n", result.getSimilarity()));
            answer.append("\n");
        }
        
        return answer.toString();
    }
}
