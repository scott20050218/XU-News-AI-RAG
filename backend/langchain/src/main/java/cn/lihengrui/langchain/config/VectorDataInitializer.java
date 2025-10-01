package cn.lihengrui.langchain.config;

import cn.lihengrui.langchain.service.VectorizationProcessorService;
import cn.lihengrui.langchain.service.EnhancedKnowledgeSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 向量数据初始化器
 * 在应用启动完成后自动加载向量数据到FAISS和LangChain4j向量存储
 */
@Slf4j
@Component
public class VectorDataInitializer implements ApplicationRunner {

    @Autowired
    private VectorizationProcessorService vectorizationProcessorService;
    
    @Autowired
    private EnhancedKnowledgeSearchService enhancedKnowledgeSearchService;
    
    /**
     * 是否启用启动时自动加载向量数据
     */
    @Value("${vector.auto-load.enabled:true}")
    private boolean autoLoadEnabled;
    
    /**
     * 启动时加载的数据页面大小
     */
    @Value("${vector.auto-load.page-size:100}")
    private int pageSize;
    
    /**
     * 启动延迟时间（秒）
     */
    @Value("${vector.auto-load.delay-seconds:5}")
    private int delaySeconds;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!autoLoadEnabled) {
            log.info("向量数据自动加载已禁用");
            return;
        }
        
        log.info("开始向量数据自动加载初始化...");
        
        // 延迟执行，确保所有服务都已完全启动
        Thread.sleep(delaySeconds * 1000L);
        
        try {
            // 1. 加载数据到FAISS向量存储
            log.info("正在加载数据到FAISS向量存储...");
            int faissProcessedCount = vectorizationProcessorService.processAllKnowledgeContent();
            log.info("FAISS向量存储加载完成，处理了 {} 条记录", faissProcessedCount);
            
            // 2. 加载数据到LangChain4j向量存储
            log.info("正在加载数据到LangChain4j向量存储...");
            int langchainProcessedCount = enhancedKnowledgeSearchService.processKnowledgeContentForLangChain(0, pageSize);
            log.info("LangChain4j向量存储加载完成，处理了 {} 条记录", langchainProcessedCount);
            
            log.info("向量数据自动加载完成！FAISS: {} 条，LangChain4j: {} 条", 
                    faissProcessedCount, langchainProcessedCount);
                    
        } catch (Exception e) {
            log.error("向量数据自动加载失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}
