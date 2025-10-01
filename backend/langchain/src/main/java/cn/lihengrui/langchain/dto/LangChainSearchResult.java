package cn.lihengrui.langchain.dto;

import lombok.Data;

/**
 * LangChain搜索结果数据传输对象
 */
@Data
public class LangChainSearchResult {
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 相似度分数 (0.0 - 1.0)
     */
    private double similarity;
    
    public LangChainSearchResult() {}
    
    public LangChainSearchResult(String content, double similarity) {
        this.content = content;
        this.similarity = similarity;
    }
}
