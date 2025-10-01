package cn.lihengrui.langchain.service;

import cn.lihengrui.langchain.dto.KnowledgeContentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FAISS向量存储服务
 * 使用内存存储实现向量数据库功能
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@Service
public class FaissVectorStore {
    
    private static final String VECTOR_STORE_FILE = "vector_store.dat";
    private static final int VECTOR_DIMENSION = 384;
    
    // 内存中的向量存储
    private final Map<Long, float[]> vectorStore = new ConcurrentHashMap<>();
    private final Map<Long, KnowledgeContentDto> contentStore = new ConcurrentHashMap<>();
    
    /**
     * 添加向量到存储
     * 
     * @param contentId 内容ID
     * @param vector 向量
     * @param content 内容对象
     */
    public void addVector(Long contentId, float[] vector, KnowledgeContentDto content) {
        try {
            // 验证向量维度
            if (vector.length != VECTOR_DIMENSION) {
                throw new IllegalArgumentException("向量维度不匹配，期望: " + VECTOR_DIMENSION + "，实际: " + vector.length);
            }
            
            // 存储向量和内容
            vectorStore.put(contentId, vector.clone());
            contentStore.put(contentId, content);
            
            log.info("成功添加向量到存储: ID={}, 标题={}", contentId, content.getTitle());
            
        } catch (Exception e) {
            log.error("添加向量失败: ID={}", contentId, e);
            throw new RuntimeException("添加向量失败", e);
        }
    }
    
    /**
     * 批量添加向量
     * 
     * @param vectors 向量映射
     * @param contents 内容映射
     */
    public void addVectors(Map<Long, float[]> vectors, Map<Long, KnowledgeContentDto> contents) {
        try {
            for (Map.Entry<Long, float[]> entry : vectors.entrySet()) {
                Long contentId = entry.getKey();
                float[] vector = entry.getValue();
                KnowledgeContentDto content = contents.get(contentId);
                
                if (content != null) {
                    addVector(contentId, vector, content);
                }
            }
            
            log.info("批量添加向量完成，共添加 {} 个向量", vectors.size());
            
        } catch (Exception e) {
            log.error("批量添加向量失败", e);
            throw new RuntimeException("批量添加向量失败", e);
        }
    }
    
    /**
     * 搜索相似向量
     * 
     * @param queryVector 查询向量
     * @param topK 返回前K个结果
     * @return 相似度结果列表
     */
    public List<SimilarityResult> searchSimilar(float[] queryVector, int topK) {
        List<SimilarityResult> results = new ArrayList<>();
        
        try {
            // 计算与所有向量的相似度
            for (Map.Entry<Long, float[]> entry : vectorStore.entrySet()) {
                Long contentId = entry.getKey();
                float[] storedVector = entry.getValue();
                
                // 计算余弦相似度
                float similarity = calculateCosineSimilarity(queryVector, storedVector);
                
                KnowledgeContentDto content = contentStore.get(contentId);
                if (content != null) {
                    results.add(new SimilarityResult(contentId, content, similarity));
                }
            }
            
            // 按相似度降序排序
            results.sort((a, b) -> Float.compare(b.getSimilarity(), a.getSimilarity()));
            
            // 返回前K个结果
            return results.subList(0, Math.min(topK, results.size()));
            
        } catch (Exception e) {
            log.error("搜索相似向量失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据内容ID获取向量
     * 
     * @param contentId 内容ID
     * @return 向量
     */
    public float[] getVector(Long contentId) {
        return vectorStore.get(contentId);
    }
    
    /**
     * 根据内容ID获取内容
     * 
     * @param contentId 内容ID
     * @return 内容对象
     */
    public KnowledgeContentDto getContent(Long contentId) {
        return contentStore.get(contentId);
    }
    
    /**
     * 删除向量
     * 
     * @param contentId 内容ID
     */
    public void removeVector(Long contentId) {
        vectorStore.remove(contentId);
        contentStore.remove(contentId);
        log.info("删除向量: ID={}", contentId);
    }
    
    /**
     * 获取存储的向量数量
     * 
     * @return 向量数量
     */
    public int getVectorCount() {
        return vectorStore.size();
    }
    
    /**
     * 清空所有向量
     */
    public void clearAll() {
        vectorStore.clear();
        contentStore.clear();
        log.info("清空所有向量存储");
    }
    
    /**
     * 持久化向量存储到文件
     */
    public void saveToFile() {
        try {
            Path filePath = Paths.get(VECTOR_STORE_FILE);
            
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
                oos.writeObject(new VectorStoreData(vectorStore, contentStore));
                log.info("向量存储已保存到文件: {}", VECTOR_STORE_FILE);
            }
            
        } catch (Exception e) {
            log.error("保存向量存储失败", e);
        }
    }
    
    /**
     * 从文件加载向量存储
     */
    public void loadFromFile() {
        try {
            Path filePath = Paths.get(VECTOR_STORE_FILE);
            
            if (Files.exists(filePath)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
                    VectorStoreData data = (VectorStoreData) ois.readObject();
                    vectorStore.putAll(data.getVectorStore());
                    contentStore.putAll(data.getContentStore());
                    log.info("向量存储已从文件加载: {} 个向量", vectorStore.size());
                }
            }
            
        } catch (Exception e) {
            log.error("加载向量存储失败", e);
        }
    }
    
    /**
     * 计算余弦相似度
     */
    private float calculateCosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }
        
        float dotProduct = 0.0f;
        float magnitude1 = 0.0f;
        float magnitude2 = 0.0f;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            magnitude1 += vector1[i] * vector1[i];
            magnitude2 += vector2[i] * vector2[i];
        }
        
        magnitude1 = (float) Math.sqrt(magnitude1);
        magnitude2 = (float) Math.sqrt(magnitude2);
        
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0f;
        }
        
        return dotProduct / (magnitude1 * magnitude2);
    }
    
    /**
     * 相似度结果类
     */
    public static class SimilarityResult {
        private final Long contentId;
        private final KnowledgeContentDto content;
        private final float similarity;
        
        public SimilarityResult(Long contentId, KnowledgeContentDto content, float similarity) {
            this.contentId = contentId;
            this.content = content;
            this.similarity = similarity;
        }
        
        public Long getContentId() {
            return contentId;
        }
        
        public KnowledgeContentDto getContent() {
            return content;
        }
        
        public float getSimilarity() {
            return similarity;
        }
    }
    
    /**
     * 向量存储数据类
     */
    private static class VectorStoreData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Map<Long, float[]> vectorStore;
        private final Map<Long, KnowledgeContentDto> contentStore;
        
        public VectorStoreData(Map<Long, float[]> vectorStore, Map<Long, KnowledgeContentDto> contentStore) {
            this.vectorStore = new HashMap<>(vectorStore);
            this.contentStore = new HashMap<>(contentStore);
        }
        
        public Map<Long, float[]> getVectorStore() {
            return vectorStore;
        }
        
        public Map<Long, KnowledgeContentDto> getContentStore() {
            return contentStore;
        }
    }
}
