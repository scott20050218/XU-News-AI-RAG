package cn.lihengrui.langchain.service;

import cn.lihengrui.langchain.dto.KnowledgeContentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 向量化服务类
 * 负责将文本内容转换为向量表示
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Slf4j
@Service
public class VectorizationService {
    
    private static final int VECTOR_DIMENSION = 384; // 向量维度
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    
    /**
     * 将知识内容转换为向量
     * 
     * @param content 知识内容
     * @return 向量数组
     */
    public float[] vectorize(KnowledgeContentDto content) {
        try {
            // 组合文本内容：标题 + 内容 + 标签
            String combinedText = combineText(content);
            
            // 文本预处理
            String processedText = preprocessText(combinedText);
            
            // 生成TF-IDF向量
            float[] vector = generateTfIdfVector(processedText);
            
            log.info("成功向量化内容: {} (向量维度: {})", content.getTitle(), vector.length);
            return vector;
            
        } catch (Exception e) {
            log.error("向量化失败: {}", content.getTitle(), e);
            // 返回零向量作为fallback
            return new float[VECTOR_DIMENSION];
        }
    }
    
    /**
     * 组合文本内容
     */
    private String combineText(KnowledgeContentDto content) {
        StringBuilder sb = new StringBuilder();
        
        if (content.getTitle() != null) {
            sb.append(content.getTitle()).append(" ");
        }
        
        if (content.getContent() != null) {
            sb.append(content.getContent()).append(" ");
        }
        
        if (content.getTags() != null) {
            sb.append(content.getTags()).append(" ");
        }
        
        if (content.getSummary() != null) {
            sb.append(content.getSummary()).append(" ");
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 文本预处理
     */
    private String preprocessText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // 转换为小写
        text = text.toLowerCase();
        
        // 移除特殊字符，保留字母数字和空格
        text = text.replaceAll("[^a-zA-Z0-9\\s]", " ");
        
        // 移除多余空格
        text = text.replaceAll("\\s+", " ");
        
        return text.trim();
    }
    
    /**
     * 生成TF-IDF向量
     */
    private float[] generateTfIdfVector(String text) {
        // 分词
        List<String> words = tokenize(text);
        
        // 计算词频
        Map<String, Integer> wordFreq = calculateWordFrequency(words);
        
        // 生成向量
        float[] vector = new float[VECTOR_DIMENSION];
        
        // 使用简单的哈希函数将词汇映射到向量维度
        for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
            String word = entry.getKey();
            int frequency = entry.getValue();
            
            // 使用词汇哈希值确定向量位置
            int hash = Math.abs(word.hashCode());
            int index = hash % VECTOR_DIMENSION;
            
            // 累加词频到对应位置
            vector[index] += frequency;
        }
        
        // 向量归一化
        normalizeVector(vector);
        
        return vector;
    }
    
    /**
     * 分词
     */
    private List<String> tokenize(String text) {
        List<String> words = new ArrayList<>();
        java.util.regex.Matcher matcher = WORD_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() > 1) { // 过滤单字符词
                words.add(word);
            }
        }
        
        return words;
    }
    
    /**
     * 计算词频
     */
    private Map<String, Integer> calculateWordFrequency(List<String> words) {
        Map<String, Integer> wordFreq = new HashMap<>();
        
        for (String word : words) {
            wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
        }
        
        return wordFreq;
    }
    
    /**
     * 向量归一化
     */
    private void normalizeVector(float[] vector) {
        // 计算向量的模长
        float magnitude = 0.0f;
        for (float value : vector) {
            magnitude += value * value;
        }
        magnitude = (float) Math.sqrt(magnitude);
        
        // 归一化
        if (magnitude > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = vector[i] / magnitude;
            }
        }
    }
    
    /**
     * 计算两个向量的余弦相似度
     */
    public float calculateCosineSimilarity(float[] vector1, float[] vector2) {
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
}
