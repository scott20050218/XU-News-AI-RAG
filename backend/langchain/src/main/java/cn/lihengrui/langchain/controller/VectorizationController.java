package cn.lihengrui.langchain.controller;

import cn.lihengrui.langchain.dto.KnowledgeContentDto;
import cn.lihengrui.langchain.service.FaissVectorStore;
import cn.lihengrui.langchain.service.VectorizationProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量化处理控制器
 * 提供向量化处理的REST API接口
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@RestController
@RequestMapping("/api/vectorization")
@RequiredArgsConstructor
@Tag(name = "向量化处理", description = "知识内容向量化处理相关API")
public class VectorizationController {
    
    private final VectorizationProcessorService vectorizationProcessorService;
    
    /**
     * 处理所有知识内容的向量化
     */
    @PostMapping("/process-all")
    @Operation(summary = "处理所有知识内容向量化", description = "从API获取所有知识内容并进行向量化处理")
    public ResponseEntity<Map<String, Object>> processAllKnowledgeContent() {
        try {
            log.info("开始处理所有知识内容向量化");
            
            int processedCount = vectorizationProcessorService.processAllKnowledgeContent();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "处理完成");
            response.put("processedCount", processedCount);
            
            log.info("处理所有知识内容向量化完成: {} 条记录", processedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理所有知识内容向量化失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
            response.put("processedCount", 0);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 处理指定页面的知识内容向量化
     */
    @PostMapping("/process-page")
    @Operation(summary = "处理指定页面知识内容向量化", description = "处理指定页面的知识内容向量化")
    public ResponseEntity<Map<String, Object>> processKnowledgeContentPage(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "100") int size) {
        try {
            log.info("开始处理第 {} 页知识内容向量化 (页面大小: {})", page, size);
            
            int processedCount = vectorizationProcessorService.processKnowledgeContentPage(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "处理完成");
            response.put("processedCount", processedCount);
            response.put("page", page);
            response.put("size", size);
            
            log.info("处理第 {} 页知识内容向量化完成: {} 条记录", page, processedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理第 {} 页知识内容向量化失败", page, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
            response.put("processedCount", 0);
            response.put("page", page);
            response.put("size", size);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 处理单个知识内容的向量化
     */
    @PostMapping("/process-single/{id}")
    @Operation(summary = "处理单个知识内容向量化", description = "处理指定ID的知识内容向量化")
    public ResponseEntity<Map<String, Object>> processSingleKnowledgeContent(
            @Parameter(description = "知识内容ID") @PathVariable Long id) {
        try {
            log.info("开始处理单个知识内容向量化: ID={}", id);
            
            boolean success = vectorizationProcessorService.processSingleKnowledgeContent(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "处理成功" : "处理失败");
            response.put("contentId", id);
            
            log.info("处理单个知识内容向量化完成: ID={}, 成功={}", id, success);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理单个知识内容向量化失败: ID={}", id, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
            response.put("contentId", id);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 搜索相似内容
     */
    @GetMapping("/search")
    @Operation(summary = "搜索相似内容", description = "根据查询文本搜索相似的知识内容")
    public ResponseEntity<Map<String, Object>> searchSimilarContent(
            @Parameter(description = "查询文本") @RequestParam String query,
            @Parameter(description = "返回结果数量") @RequestParam(defaultValue = "10") int topK) {
        try {
            log.info("搜索相似内容: query={}, topK={}", query, topK);
            
            List<FaissVectorStore.SimilarityResult> results = 
                    vectorizationProcessorService.searchSimilarContent(query, topK);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "搜索完成");
            response.put("query", query);
            response.put("topK", topK);
            response.put("results", results);
            response.put("resultCount", results.size());
            
            log.info("搜索相似内容完成: query={}, 结果数={}", query, results.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("搜索相似内容失败: query={}", query, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            response.put("query", query);
            response.put("topK", topK);
            response.put("results", List.of());
            response.put("resultCount", 0);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取向量存储统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取向量存储统计信息", description = "获取向量存储的统计信息")
    public ResponseEntity<Map<String, Object>> getVectorStoreStats() {
        try {
            log.info("获取向量存储统计信息");
            
            Map<String, Object> stats = vectorizationProcessorService.getVectorStoreStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取统计信息成功");
            response.put("stats", stats);
            
            log.info("获取向量存储统计信息完成: {}", stats);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取向量存储统计信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            response.put("stats", Map.of());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 清空向量存储
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空向量存储", description = "清空所有向量存储数据")
    public ResponseEntity<Map<String, Object>> clearVectorStore() {
        try {
            log.info("清空向量存储");
            
            vectorizationProcessorService.clearVectorStore();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "向量存储已清空");
            
            log.info("清空向量存储完成");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("清空向量存储失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清空向量存储失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 保存向量存储到文件
     */
    @PostMapping("/save")
    @Operation(summary = "保存向量存储", description = "将向量存储保存到文件")
    public ResponseEntity<Map<String, Object>> saveVectorStore() {
        try {
            log.info("保存向量存储到文件");
            
            vectorizationProcessorService.saveVectorStore();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "向量存储已保存到文件");
            
            log.info("保存向量存储到文件完成");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("保存向量存储失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存向量存储失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 从文件加载向量存储
     */
    @PostMapping("/load")
    @Operation(summary = "加载向量存储", description = "从文件加载向量存储")
    public ResponseEntity<Map<String, Object>> loadVectorStore() {
        try {
            log.info("从文件加载向量存储");
            
            vectorizationProcessorService.loadVectorStore();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "向量存储已从文件加载");
            
            log.info("从文件加载向量存储完成");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("加载向量存储失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "加载向量存储失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 仅处理已成功的知识内容向量化
     */
    @PostMapping("/process-successful")
    @Operation(summary = "处理已成功的知识内容", description = "仅对已处理且成功的知识内容进行向量化")
    public ResponseEntity<Map<String, Object>> processSuccessfulKnowledgeContent() {
        try {
            log.info("开始处理已成功的知识内容向量化");
            
            int processedCount = vectorizationProcessorService.processSuccessfulKnowledgeContent();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "处理完成");
            response.put("processedCount", processedCount);
            
            log.info("处理已成功的知识内容向量化完成: {} 条记录", processedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理已成功的知识内容向量化失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
            response.put("processedCount", 0);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据内容类型处理向量化
     */
    @PostMapping("/process-by-type")
    @Operation(summary = "根据内容类型处理向量化", description = "对指定类型的知识内容进行向量化")
    public ResponseEntity<Map<String, Object>> processKnowledgeContentByType(
            @RequestParam @Parameter(description = "内容类型（RSS/Web/Manual）", example = "RSS") String contentType) {
        try {
            log.info("开始处理{}类型的知识内容向量化", contentType);
            
            int processedCount = vectorizationProcessorService.processKnowledgeContentByType(contentType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "处理完成");
            response.put("processedCount", processedCount);
            response.put("contentType", contentType);
            
            log.info("处理{}类型的知识内容向量化完成: {} 条记录", contentType, processedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理{}类型的知识内容向量化失败", contentType, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
            response.put("processedCount", 0);
            response.put("contentType", contentType);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据标签处理向量化
     */
    @PostMapping("/process-by-tags")
    @Operation(summary = "根据标签处理向量化", description = "对包含指定标签的知识内容进行向量化")
    public ResponseEntity<Map<String, Object>> processKnowledgeContentByTags(
            @RequestParam @Parameter(description = "标签（逗号分隔）", example = "Java,Spring") String tags) {
        try {
            log.info("开始处理标签为'{}'的知识内容向量化", tags);
            
            int processedCount = vectorizationProcessorService.processKnowledgeContentByTags(tags);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "处理完成");
            response.put("processedCount", processedCount);
            response.put("tags", tags);
            
            log.info("处理标签为'{}'的知识内容向量化完成: {} 条记录", tags, processedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理标签为'{}'的知识内容向量化失败", tags, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
            response.put("processedCount", 0);
            response.put("tags", tags);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
