package cn.lihengrui.langchain.service;

import cn.lihengrui.langchain.dto.ApiResponseDto;
import cn.lihengrui.langchain.dto.KnowledgeContentDto;
import cn.lihengrui.langchain.dto.PageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * API客户端服务
 * 负责从todo-backend API获取知识内容数据
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@Service
public class ApiClientService {
    
    private final WebClient webClient;
    
    @Value("${todo-backend.api.url:http://localhost:8080}")
    private String todoBackendApiUrl;
    
    public ApiClientService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
    
    /**
     * 获取所有知识内容（分页）
     * 
     * @param page 页码（从0开始）
     * @param size 页面大小
     * @return 知识内容列表
     */
    public List<KnowledgeContentDto> getAllKnowledgeContent(int page, int size) {
        return getAllKnowledgeContent(page, size, null, null, null, null, null, null);
    }
    
    /**
     * 获取知识内容（分页，支持过滤）
     * 
     * @param page 页码（从0开始）
     * @param size 页面大小
     * @param sort 排序字段
     * @param direction 排序方向（ASC/DESC）
     * @param contentType 内容类型过滤（RSS/Web/Manual）
     * @param processed 处理状态过滤
     * @param success 成功状态过滤
     * @param keyword 关键词搜索
     * @return 知识内容列表
     */
    public List<KnowledgeContentDto> getAllKnowledgeContent(int page, int size, String sort, String direction, 
                                                           String contentType, Boolean processed, Boolean success, String keyword) {
        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(String.format("%s/api/knowledge-content?page=%d&size=%d", todoBackendApiUrl, page, size));
            
            // 添加排序参数
            if (sort != null && !sort.isEmpty()) {
                urlBuilder.append("&sort=").append(sort);
                if (direction != null && !direction.isEmpty()) {
                    urlBuilder.append("&direction=").append(direction);
                }
            }
            
            // 添加过滤参数
            if (contentType != null && !contentType.isEmpty()) {
                urlBuilder.append("&contentType=").append(contentType);
            }
            if (processed != null) {
                urlBuilder.append("&processed=").append(processed);
            }
            if (success != null) {
                urlBuilder.append("&success=").append(success);
            }
            if (keyword != null && !keyword.isEmpty()) {
                urlBuilder.append("&keyword=").append(java.net.URLEncoder.encode(keyword, "UTF-8"));
            }
            
            String url = urlBuilder.toString();
            log.info("从API获取知识内容: {}", url);
            
            ApiResponseDto<PageDto<KnowledgeContentDto>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<PageDto<KnowledgeContentDto>>>() {})
                    .block();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<KnowledgeContentDto> contents = response.getData().getContent();
                log.info("成功获取知识内容: {} 条记录", contents.size());
                return contents;
            } else {
                log.warn("获取知识内容失败: {}", response != null ? response.getMessage() : "响应为空");
                return List.of();
            }
            
        } catch (Exception e) {
            log.error("获取知识内容异常", e);
            return List.of();
        }
    }
    
    /**
     * 获取所有知识内容（不分页）
     * 
     * @return 所有知识内容列表
     */
    public List<KnowledgeContentDto> getAllKnowledgeContent() {
        List<KnowledgeContentDto> allContents = new java.util.ArrayList<>();
        int page = 0;
        int size = 100; // 每页100条记录
        
        while (true) {
            List<KnowledgeContentDto> contents = getAllKnowledgeContent(page, size);
            
            if (contents.isEmpty()) {
                break;
            }
            
            allContents.addAll(contents);
            page++;
            
            // 如果返回的记录数少于页面大小，说明已经是最后一页
            if (contents.size() < size) {
                break;
            }
        }
        
        log.info("获取所有知识内容完成，共 {} 条记录", allContents.size());
        return allContents;
    }
    
    /**
     * 根据ID获取知识内容
     * 
     * @param id 内容ID
     * @return 知识内容
     */
    public KnowledgeContentDto getKnowledgeContentById(Long id) {
        try {
            String url = String.format("%s/api/knowledge-content/%d", todoBackendApiUrl, id);
            
            log.info("从API获取知识内容: ID={}", id);
            
            ApiResponseDto<KnowledgeContentDto> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<KnowledgeContentDto>>() {})
                    .block();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("成功获取知识内容: ID={}, 标题={}", id, response.getData().getTitle());
                return response.getData();
            } else {
                log.warn("获取知识内容失败: ID={}, 消息={}", id, response != null ? response.getMessage() : "响应为空");
                return null;
            }
            
        } catch (Exception e) {
            log.error("获取知识内容异常: ID={}", id, e);
            return null;
        }
    }
    
    /**
     * 搜索知识内容
     * 
     * @param query 搜索查询
     * @param page 页码
     * @param size 页面大小
     * @return 搜索结果
     */
    public List<KnowledgeContentDto> searchKnowledgeContent(String query, int page, int size) {
        try {
            String url = String.format("%s/api/knowledge-content/search?query=%s&page=%d&size=%d", 
                    todoBackendApiUrl, java.net.URLEncoder.encode(query, "UTF-8"), page, size);
            
            log.info("搜索知识内容: query={}", query);
            
            ApiResponseDto<PageDto<KnowledgeContentDto>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<PageDto<KnowledgeContentDto>>>() {})
                    .block();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<KnowledgeContentDto> contents = response.getData().getContent();
                log.info("搜索完成: query={}, 结果数={}", query, contents.size());
                return contents;
            } else {
                log.warn("搜索失败: query={}, 消息={}", query, response != null ? response.getMessage() : "响应为空");
                return List.of();
            }
            
        } catch (Exception e) {
            log.error("搜索知识内容异常: query={}", query, e);
            return List.of();
        }
    }
    
    /**
     * 获取知识内容统计信息
     * 
     * @return 统计信息Map
     */
    public java.util.Map<String, Object> getStatistics() {
        try {
            String url = String.format("%s/api/knowledge-content/statistics", todoBackendApiUrl);
            
            log.info("获取统计信息: {}", url);
            
            ApiResponseDto<java.util.Map<String, Object>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<java.util.Map<String, Object>>>() {})
                    .block();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("成功获取统计信息");
                return response.getData();
            } else {
                log.warn("获取统计信息失败: {}", response != null ? response.getMessage() : "响应为空");
                return java.util.Map.of();
            }
            
        } catch (Exception e) {
            log.error("获取统计信息异常", e);
            return java.util.Map.of();
        }
    }
    
    /**
     * 根据标签过滤获取知识内容
     * 
     * @param tags 标签字符串（逗号分隔）
     * @param page 页码
     * @param size 页面大小
     * @return 知识内容列表
     */
    public List<KnowledgeContentDto> getKnowledgeContentByTags(String tags, int page, int size) {
        try {
            String url = String.format("%s/api/knowledge-content?page=%d&size=%d&tags=%s", 
                    todoBackendApiUrl, page, size, java.net.URLEncoder.encode(tags, "UTF-8"));
            
            log.info("根据标签获取知识内容: tags={}", tags);
            
            ApiResponseDto<PageDto<KnowledgeContentDto>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<PageDto<KnowledgeContentDto>>>() {})
                    .block();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<KnowledgeContentDto> contents = response.getData().getContent();
                log.info("根据标签获取内容完成: tags={}, 结果数={}", tags, contents.size());
                return contents;
            } else {
                log.warn("根据标签获取内容失败: tags={}, 消息={}", tags, response != null ? response.getMessage() : "响应为空");
                return List.of();
            }
            
        } catch (Exception e) {
            log.error("根据标签获取内容异常: tags={}", tags, e);
            return List.of();
        }
    }
    
    /**
     * 获取已处理的知识内容
     * 
     * @param page 页码
     * @param size 页面大小
     * @return 已处理的知识内容列表
     */
    public List<KnowledgeContentDto> getProcessedKnowledgeContent(int page, int size) {
        return getAllKnowledgeContent(page, size, "acquisitionTime", "DESC", null, true, true, null);
    }
    
    /**
     * 获取未处理的知识内容
     * 
     * @param page 页码
     * @param size 页面大小
     * @return 未处理的知识内容列表
     */
    public List<KnowledgeContentDto> getUnprocessedKnowledgeContent(int page, int size) {
        return getAllKnowledgeContent(page, size, "acquisitionTime", "ASC", null, false, null, null);
    }
    
    /**
     * 根据内容类型获取知识内容
     * 
     * @param contentType 内容类型（RSS/Web/Manual）
     * @param page 页码
     * @param size 页面大小
     * @return 指定类型的知识内容列表
     */
    public List<KnowledgeContentDto> getKnowledgeContentByType(String contentType, int page, int size) {
        return getAllKnowledgeContent(page, size, "acquisitionTime", "DESC", contentType, null, null, null);
    }
    
    /**
     * 测试API连接
     * 
     * @return 是否连接成功
     */
    public boolean testConnection() {
        try {
            String url = String.format("%s/actuator/health", todoBackendApiUrl);
            
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            boolean isHealthy = response != null && response.contains("UP");
            log.info("API连接测试: {}", isHealthy ? "成功" : "失败");
            return isHealthy;
            
        } catch (Exception e) {
            log.error("API连接测试失败", e);
            return false;
        }
    }
}
