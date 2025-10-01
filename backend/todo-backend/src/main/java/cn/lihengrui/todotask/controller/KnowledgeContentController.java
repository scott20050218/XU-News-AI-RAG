package cn.lihengrui.todotask.controller;

import cn.lihengrui.todotask.dto.*;
import cn.lihengrui.todotask.entity.KnowledgeContent;
import cn.lihengrui.todotask.service.KnowledgeContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * 知识内容RESTful API控制器
 *
 * 提供知识内容的完整REST API服务，包括：
 * 1. CRUD操作 - 创建、读取、更新、删除
 * 2. 状态切换 - 处理状态管理
 * 3. 批量操作 - 批量删除、批量更新
 * 4. 分页查询 - 支持大数据集的分页显示
 * 5. 过滤搜索 - 按类型、状态、标签等条件过滤
 *
 * API设计遵循RESTful规范：
 * - GET /api/knowledge-content - 获取列表（支持分页和过滤）
 * - GET /api/knowledge-content/{id} - 获取单个内容
 * - POST /api/knowledge-content - 创建新内容
 * - PUT /api/knowledge-content/{id} - 更新内容
 * - DELETE /api/knowledge-content/{id} - 删除单个内容
 * - POST /api/knowledge-content/batch - 批量操作
 * - PUT /api/knowledge-content/{id}/status - 状态切换
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@RestController
@RequestMapping("/api/knowledge-content")
@Slf4j
public class KnowledgeContentController {

    @Autowired
    private KnowledgeContentService knowledgeContentService;

    /**
     * 获取知识内容列表（支持分页和过滤）
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序字段
     * @param direction 排序方向（ASC/DESC）
     * @param beginTime 开始时间过滤（格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     * @param endTime 结束时间过滤（格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     * @param contentType 内容类型过滤
     * @param processed 处理状态过滤
     * @param success 成功状态过滤
     * @param keyword 关键词搜索
     * @param tags 标签过滤
     * @return 分页的知识内容列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<KnowledgeContentSummary>>> getKnowledgeContents(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "acquisitionTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) Boolean processed,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tags) {

        try {
            // 创建排序对象
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Sort sorting = Sort.by(sortDirection, sort);
            Pageable pageable = PageRequest.of(pageNum, pageSize, sorting);

            // 调用服务层进行查询
            Page<KnowledgeContentSummary> results = knowledgeContentService.findWithFilters(
                    pageable, contentType, processed, success, keyword, tags,beginTime,endTime);

            log.info("查询知识内容列表成功，返回 {} 条记录，共 {} 页", 
                    results.getNumberOfElements(), results.getTotalPages());

            return ResponseEntity.ok(ApiResponse.success(results, "查询成功"));

        } catch (Exception e) {
            log.error("查询知识内容列表失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 根据ID获取单个知识内容
     *
     * @param id 内容ID
     * @return 知识内容详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgeContentResponse>> getKnowledgeContent(@PathVariable Long id) {
        try {
            Optional<KnowledgeContentResponse> content = knowledgeContentService.findById(id);
            
            if (content.isPresent()) {
                log.info("查询知识内容成功，ID: {}", id);
                return ResponseEntity.ok(ApiResponse.success(content.get(), "查询成功"));
            } else {
                log.warn("知识内容不存在，ID: {}", id);
                return ResponseEntity.notFound()
                        .build();
            }

        } catch (Exception e) {
            log.error("查询知识内容失败，ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 创建新的知识内容
     *
     * @param request 创建请求
     * @return 创建的知识内容
     */
    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeContentResponse>> createKnowledgeContent(
            @Valid @RequestBody KnowledgeContentRequest request) {
        try {
            KnowledgeContentResponse created = knowledgeContentService.create(request);
            log.info("创建知识内容成功，ID: {}, 标题: {}", created.getKnowId(), created.getTitle());
            return ResponseEntity.ok(ApiResponse.success(created, "创建成功"));

        } catch (Exception e) {
            log.error("创建知识内容失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("创建失败：" + e.getMessage()));
        }
    }

    /**
     * 更新知识内容
     *
     * @param id 内容ID
     * @param request 更新请求
     * @return 更新后的知识内容
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgeContentResponse>> updateKnowledgeContent(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeContentRequest request) {
        try {
            Optional<KnowledgeContentResponse> updated = knowledgeContentService.update(id, request);
            
            if (updated.isPresent()) {
                log.info("更新知识内容成功，ID: {}", id);
                return ResponseEntity.ok(ApiResponse.success(updated.get(), "更新成功"));
            } else {
                log.warn("要更新的知识内容不存在，ID: {}", id);
                return ResponseEntity.notFound()
                        .build();
            }

        } catch (Exception e) {
            log.error("更新知识内容失败，ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("更新失败：" + e.getMessage()));
        }
    }

    /**
     * 删除知识内容
     *
     * @param id 内容ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKnowledgeContent(@PathVariable Long id) {
        try {
            boolean deleted = knowledgeContentService.delete(id);
            
            if (deleted) {
                log.info("删除知识内容成功，ID: {}", id);
                return ResponseEntity.ok(ApiResponse.success(null, "删除成功"));
            } else {
                log.warn("要删除的知识内容不存在，ID: {}", id);
                return ResponseEntity.notFound()
                        .build();
            }

        } catch (Exception e) {
            log.error("删除知识内容失败，ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("删除失败：" + e.getMessage()));
        }
    }

    /**
     * 切换知识内容的处理状态
     *
     * @param id 内容ID
     * @param processed 新的处理状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<KnowledgeContentResponse>> switchProcessingStatus(
            @PathVariable Long id,
            @RequestParam Boolean processed) {
        try {
            Optional<KnowledgeContentResponse> updated = knowledgeContentService.updateProcessingStatus(id, processed);
            
            if (updated.isPresent()) {
                log.info("切换知识内容处理状态成功，ID: {}, 新状态: {}", id, processed);
                return ResponseEntity.ok(ApiResponse.success(updated.get(), "状态更新成功"));
            } else {
                log.warn("要更新状态的知识内容不存在，ID: {}", id);
                return ResponseEntity.notFound()
                        .build();
            }

        } catch (Exception e) {
            log.error("切换知识内容处理状态失败，ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("状态更新失败：" + e.getMessage()));
        }
    }

    /**
     * 批量操作知识内容
     *
     * @param request 批量操作请求
     * @return 操作结果
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<String>> batchOperation(@Valid @RequestBody BatchOperationRequest request) {
        try {
            int affectedCount = knowledgeContentService.batchOperation(request);
            String message = String.format("批量%s操作成功，影响 %d 条记录", request.getOperation(), affectedCount);
            
            log.info("批量操作成功，操作类型: {}, 影响记录数: {}", request.getOperation(), affectedCount);
            return ResponseEntity.ok(ApiResponse.success(message, "批量操作成功"));

        } catch (Exception e) {
            log.error("批量操作失败，操作类型: {}", request.getOperation(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("批量操作失败：" + e.getMessage()));
        }
    }

    /**
     * 获取内容统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getStatistics() {
        try {
            Object statistics = knowledgeContentService.getStatistics();
            log.info("获取统计信息成功");
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取统计信息成功"));

        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("获取统计信息失败：" + e.getMessage()));
        }
    }

    /**
     * 搜索知识内容
     *
     * @param query 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<KnowledgeContentSummary>>> searchKnowledgeContents(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "acquisitionTime"));
            Page<KnowledgeContentSummary> results = knowledgeContentService.search(query, pageable);
            
            log.info("搜索知识内容成功，关键词: {}, 返回 {} 条记录", query, results.getNumberOfElements());
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));

        } catch (Exception e) {
            log.error("搜索知识内容失败，关键词: {}", query, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("搜索失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<String>>> getAllTags() {
        try {
            List<String> tags = knowledgeContentService.getAllTags();
            log.info("获取所有标签成功，共 {} 个标签", tags.size());
            return ResponseEntity.ok(ApiResponse.success(tags, "获取标签成功"));

        } catch (Exception e) {
            log.error("获取标签失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("获取标签失败：" + e.getMessage()));
        }
    }

    // ==================== 聚类分析相关API ====================

    /**
     * 获取聚类分析数据
     *
     * @return 聚类分析结果
     */
    @GetMapping("/cluster-analysis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClusterAnalysis() {
        try {
            Map<String, Object> clusterData = knowledgeContentService.getClusterAnalysis();
            log.info("获取聚类分析数据成功");
            return ResponseEntity.ok(ApiResponse.success(clusterData, "获取聚类分析数据成功"));

        } catch (Exception e) {
            log.error("获取聚类分析数据失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("获取聚类分析数据失败：" + e.getMessage()));
        }
    }

    /**
     * 获取聚类统计信息
     *
     * @return 聚类统计数据
     */
    @GetMapping("/cluster-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClusterStats() {
        try {
            Map<String, Object> stats = knowledgeContentService.getClusterStats();
            log.info("获取聚类统计信息成功");
            return ResponseEntity.ok(ApiResponse.success(stats, "获取聚类统计信息成功"));

        } catch (Exception e) {
            log.error("获取聚类统计信息失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("获取聚类统计信息失败：" + e.getMessage()));
        }
    }

    /**
     * 获取词云数据
     *
     * @return 词云数据
     */
    @GetMapping("/word-cloud")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWordCloudData() {
        try {
            List<Map<String, Object>> wordCloudData = knowledgeContentService.getWordCloudData();
            log.info("获取词云数据成功，共 {} 个词", wordCloudData.size());
            return ResponseEntity.ok(ApiResponse.success(wordCloudData, "获取词云数据成功"));

        } catch (Exception e) {
            log.error("获取词云数据失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("获取词云数据失败：" + e.getMessage()));
        }
    }

    /**
     * 获取内容类型分布
     *
     * @return 内容类型分布数据
     */
    @GetMapping("/type-distribution")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getContentTypeDistribution() {
        try {
            List<Map<String, Object>> distribution = knowledgeContentService.getContentTypeDistribution();
            log.info("获取内容类型分布成功");
            return ResponseEntity.ok(ApiResponse.success(distribution, "获取内容类型分布成功"));

        } catch (Exception e) {
            log.error("获取内容类型分布失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("获取内容类型分布失败：" + e.getMessage()));
        }
    }

    /**
     * 获取标签分析数据
     *
     * @return 标签分析数据
     */
    @GetMapping("/tag-analysis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTagAnalysis() {
        try {
            Map<String, Object> tagAnalysis = knowledgeContentService.getTagAnalysis();
            log.info("获取标签分析数据成功");
            return ResponseEntity.ok(ApiResponse.success(tagAnalysis, "获取标签分析数据成功"));

        } catch (Exception e) {
            log.error("获取标签分析数据失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("获取标签分析数据失败：" + e.getMessage()));
        }
    }

    /**
     * 获取时间趋势分析
     *
     * @param period 时间周期 (7d, 30d, 90d)
     * @return 时间趋势数据
     */
    @GetMapping("/time-trend")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTimeTrendAnalysis(
            @RequestParam(defaultValue = "30d") String period) {
        try {
            Map<String, Object> trendData = knowledgeContentService.getTimeTrendAnalysis(period);
            log.info("获取时间趋势分析成功，周期: {}", period);
            return ResponseEntity.ok(ApiResponse.success(trendData, "获取时间趋势分析成功"));

        } catch (Exception e) {
            log.error("获取时间趋势分析失败，周期: {}", period, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.serverError("获取时间趋势分析失败：" + e.getMessage()));
        }
    }
}
