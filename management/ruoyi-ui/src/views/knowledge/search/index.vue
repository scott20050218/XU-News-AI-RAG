<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <el-row class="mb8">
      <el-col>
        <h3>智能语义查询</h3>
        <p style="color: #666; margin-bottom: 20px">
          基于AI技术的智能知识检索，支持自然语言查询和语义理解
        </p>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <el-card class="search-card mb8">
      <div slot="header">
        <span>知识检索</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="showAdvanced = !showAdvanced"
          icon="el-icon-setting"
        >
          {{ showAdvanced ? "收起" : "高级" }}
        </el-button>
      </div>

      <!-- 主搜索框 -->
      <el-row>
        <el-col :span="24">
          <el-input
            v-model="searchQuery"
            placeholder="请输入您的问题，例如：什么是Vue.js？如何使用Spring Boot？"
            clearable
            :disabled="searching"
            @keyup.enter.native="handleSearch"
            class="search-input"
          >
            <template slot="prepend">
              <el-select v-model="searchMode" style="width: 120px">
                <el-option label="智能检索" value="enhanced"></el-option>
                <el-option label="智能问答" value="ask"></el-option>
                <el-option label="基础检索" value="basic"></el-option>
              </el-select>
            </template>
            <el-button
              slot="append"
              type="primary"
              :loading="searching"
              @click="handleSearch"
              icon="el-icon-search"
            >
              {{ searching ? "检索中..." : "搜索" }}
            </el-button>
          </el-input>
        </el-col>
      </el-row>

      <!-- 高级搜索选项 -->
      <el-collapse-transition>
        <div v-show="showAdvanced" class="advanced-options">
          <el-divider content-position="left">高级选项</el-divider>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="结果数量">
                <el-slider
                  v-model="searchParams.topK"
                  :min="1"
                  :max="50"
                  :step="1"
                  show-stops
                  show-input
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="相似度阈值">
                <el-slider
                  v-model="searchParams.minSimilarity"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  show-input
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="内容类型">
                <el-select
                  v-model="searchParams.contentType"
                  placeholder="全部类型"
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="dict in dict.type.data_status"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item>
                <el-checkbox v-model="searchParams.processedOnly">
                  仅显示已处理内容
                </el-checkbox>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 搜索结果 -->
    <el-card v-show="hasSearched" class="results-card">
      <div slot="header">
        <span>搜索结果</span>
        <div style="float: right">
          <el-tag v-if="searchResults.length > 0" type="success" size="small">
            知识库: {{ searchResults.length }} 条
          </el-tag>
          <el-tag
            v-if="webSearchResults.length > 0"
            type="warning"
            size="small"
            style="margin-left: 5px"
          >
            联网: {{ webSearchResults.length }} 条
          </el-tag>
          <el-tag
            v-if="searchStats.searchMethod"
            type="info"
            size="small"
            style="margin-left: 5px"
          >
            {{ searchStats.searchMethod }}
          </el-tag>
          <el-tag
            v-else-if="searchResults.length === 0"
            type="info"
            size="small"
            >暂无结果</el-tag
          >
          <span style="margin-left: 10px; color: #666; font-size: 12px">
            耗时: {{ searchStats.processingTimeMs || 0 }}ms
          </span>
        </div>
      </div>

      <!-- 问答模式结果 -->
      <div v-if="searchMode === 'ask' && answerResult" class="answer-section">
        <el-alert
          :title="searchQuery"
          type="info"
          :closable="false"
          show-icon
          class="mb8"
        >
          <template slot="default">
            <strong>您的问题：</strong>{{ searchQuery }}
          </template>
        </el-alert>
        <div class="answer-content">
          <h4>
            <i class="el-icon-chat-dot-round"></i>
            AI 回答：
          </h4>
          <div class="answer-text" v-html="formatAnswer(answerResult)"></div>
        </div>
      </div>

      <!-- 调试信息 -->
      <div
        v-if="hasSearched"
        style="
          background: #f0f0f0;
          padding: 10px;
          margin: 10px 0;
          border-radius: 4px;
        "
      >
        <h4>调试信息：</h4>
        <p>联网搜索结果数量: {{ webSearchResults.length }}</p>
        <p>大模型推理结果: {{ llmInferenceResult ? "有" : "无" }}</p>
        <p>搜索方法: {{ searchStats.searchMethod }}</p>
        <p>联网搜索触发: {{ searchStats.webSearchTriggered }}</p>
        <p>知识库结果数量: {{ searchResults.length }}</p>
        <p>当前搜索模式: {{ searchMode }}</p>
        <p>搜索状态: {{ searching ? "搜索中" : "已完成" }}</p>
        <p>是否已搜索: {{ hasSearched }}</p>
        <el-button type="primary" size="mini" @click="testApiCall"
          >测试API调用</el-button
        >
      </div>

      <!-- 大模型推理结果 -->
      <div v-if="llmInferenceResult" class="llm-inference-section">
        <el-alert
          title="AI 智能分析"
          type="success"
          :closable="false"
          show-icon
          class="mb8"
        >
          <template slot="default">
            <strong>基于知识库和联网搜索的综合分析：</strong>
          </template>
        </el-alert>
        <div class="llm-inference-content">
          <div
            class="inference-text"
            v-html="formatAnswer(llmInferenceResult)"
          ></div>
        </div>
      </div>

      <!-- 联网搜索结果 -->
      <div v-if="webSearchResults.length > 0" class="web-search-results">
        <el-divider content-position="left">
          <span style="color: #e6a23c">
            <i class="el-icon-link"></i>
            联网搜索结果
          </span>
        </el-divider>
        <el-row :gutter="16">
          <el-col
            v-for="(result, index) in webSearchResults"
            :key="'web-' + index"
            :span="24"
            class="mb8"
          >
            <el-card shadow="hover" class="web-result-item">
              <div class="web-result-header">
                <h4 class="web-result-title">
                  <el-link
                    :href="result.url"
                    target="_blank"
                    :underline="false"
                  >
                    {{ result.title }}
                  </el-link>
                </h4>
                <div class="web-result-meta">
                  <el-tag type="warning" size="mini">
                    {{ result.source }}
                  </el-tag>
                  <span class="meta-item">
                    <i class="el-icon-time"></i>
                    {{ result.searchTime }}
                  </span>
                </div>
              </div>
              <div class="web-result-content">
                <p class="web-result-snippet">
                  {{ result.snippet }}
                </p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 知识库检索结果 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <el-divider content-position="left">
          <span style="color: #67c23a">
            <i class="el-icon-document"></i>
            知识库检索结果
          </span>
        </el-divider>
        <el-row :gutter="16">
          <el-col
            v-for="(result, index) in searchResults"
            :key="index"
            :span="24"
            class="mb8"
          >
            <el-card shadow="hover" class="result-item">
              <div class="result-header">
                <h4 class="result-title">
                  <el-link
                    :href="result.sourceUrl"
                    target="_blank"
                    :underline="false"
                  >
                    {{ result.title || "无标题" }}
                  </el-link>
                </h4>
                <div class="result-meta">
                  <el-tag
                    :type="getSimilarityTagType(result.similarity)"
                    size="mini"
                  >
                    相似度: {{ (result.similarity * 100).toFixed(1) }}%
                  </el-tag>
                  <span class="meta-item">
                    <i class="el-icon-time"></i>
                    {{ formatTime(result.acquisitionTime) }}
                  </span>
                </div>
              </div>

              <div class="result-content">
                <p class="result-summary" v-if="result.summary">
                  <strong>摘要：</strong>{{ result.summary }}
                </p>
                <p class="result-excerpt">
                  {{ truncateText(result.content, 200) }}
                </p>
              </div>

              <div class="result-footer">
                <div class="result-tags">
                  <el-tag
                    v-for="tag in result.tagArray"
                    :key="tag"
                    size="mini"
                    type="info"
                    style="margin-right: 5px"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="result-actions">
                  <el-button
                    type="text"
                    size="mini"
                    @click="viewDetail(result)"
                    icon="el-icon-view"
                  >
                    查看详情
                  </el-button>
                  <el-button
                    type="text"
                    size="mini"
                    @click="askAbout(result)"
                    icon="el-icon-chat-dot-round"
                  >
                    相关问答
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 无结果提示 -->
      <el-empty
        v-else-if="
          hasSearched &&
          searchResults.length === 0 &&
          webSearchResults.length === 0 &&
          !llmInferenceResult
        "
        description="未找到相关内容"
        :image-size="100"
      >
        <el-button type="primary" @click="showSearchTips = true">
          查看搜索技巧
        </el-button>
      </el-empty>
    </el-card>

    <!-- 搜索历史 -->
    <el-card
      class="history-card mt8"
      v-show="!hasSearched || searchHistory.length > 0"
    >
      <div slot="header">
        <span>搜索历史</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="clearHistory"
          icon="el-icon-delete"
          v-show="searchHistory.length > 0"
        >
          清空
        </el-button>
      </div>

      <div v-if="searchHistory.length > 0" class="history-list">
        <el-tag
          v-for="item in recentHistory"
          :key="item.id"
          class="history-item"
          type="info"
          size="small"
          @click="repeatSearch(item)"
          style="cursor: pointer; margin: 0 8px 8px 0"
        >
          <i class="el-icon-search"></i>
          {{ truncateText(item.query, 30) }}
        </el-tag>
      </div>

      <el-empty v-else description="暂无搜索历史" :image-size="60" />
    </el-card>

    <!-- 搜索技巧对话框 -->
    <el-dialog title="搜索技巧" :visible.sync="showSearchTips" width="600px">
      <div class="search-tips">
        <h4>🔍 如何获得更好的搜索结果？</h4>
        <ul>
          <li><strong>使用自然语言：</strong>直接提问，如"什么是Vue.js？"</li>
          <li><strong>具体化描述：</strong>提供更多上下文信息</li>
          <li><strong>使用关键词：</strong>包含技术术语和专业词汇</li>
          <li><strong>调整参数：</strong>使用高级选项调整结果数量和相似度</li>
          <li>
            <strong>尝试不同模式：</strong
            >智能检索适合查找资料，智能问答适合获取答案
          </li>
        </ul>

        <h4>💡 搜索示例：</h4>
        <div class="example-queries">
          <el-tag
            v-for="example in exampleQueries"
            :key="example"
            class="example-tag"
            @click="useExample(example)"
          >
            {{ example }}
          </el-tag>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showSearchTips = false">知道了</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  enhancedSearchWithWebFallback,
  askQuestion,
  basicSearch,
  getSearchHistory,
  saveSearchHistory,
  clearSearchHistory,
} from "@/api/knowledge/search";

export default {
  name: "KnowledgeSearch",
  dicts: ["data_status"],
  data() {
    return {
      // 搜索状态
      searchQuery: "",
      searchMode: "enhanced", // enhanced, ask, basic
      searching: false,
      hasSearched: false,
      showAdvanced: false,

      // 搜索参数
      searchParams: {
        topK: 10,
        minSimilarity: 0.0,
        contentType: "",
        processedOnly: false,
      },

      // 搜索结果
      searchResults: [],
      answerResult: "",
      searchStats: {},

      // 增强搜索结果
      webSearchResults: [],
      llmInferenceResult: "",

      // 搜索历史
      searchHistory: [],

      // UI 状态
      showSearchTips: false,

      // 示例查询
      exampleQueries: [
        "什么是Spring Boot？",
        "Vue.js如何进行组件通信？",
        "如何优化数据库性能？",
        "微服务架构的优缺点",
        "前端性能优化技巧",
      ],
    };
  },
  computed: {
    recentHistory() {
      return this.searchHistory.slice(0, 10); // 显示最近10条
    },
  },
  created() {
    this.loadSearchHistory();
  },
  methods: {
    /** 执行搜索 */
    async handleSearch() {
      if (!this.searchQuery.trim()) {
        this.$message.warning("请输入搜索内容");
        return;
      }

      this.searching = true;
      this.hasSearched = true;
      this.searchResults = [];
      this.answerResult = "";
      this.searchStats = {};
      this.webSearchResults = [];
      this.llmInferenceResult = "";

      try {
        let response;
        const params = {
          query: this.searchQuery,
          ...this.searchParams,
        };

        console.log("=== 搜索开始 ===");
        console.log("搜索模式:", this.searchMode);
        console.log("搜索参数:", params);

        // 根据搜索模式调用不同API
        switch (this.searchMode) {
          case "enhanced":
            console.log("调用增强搜索API，参数:", params);
            try {
              response = await enhancedSearchWithWebFallback(params);
              console.log("增强搜索API响应:", response);
              this.handleEnhancedSearchResponse(response);
            } catch (apiError) {
              console.error("增强搜索API调用失败:", apiError);
              throw apiError;
            }
            break;
          case "ask":
            console.log("调用问答API，查询:", this.searchQuery);
            try {
              response = await askQuestion(this.searchQuery);
              console.log("问答API响应:", response);
              this.handleAskResponse(response);
            } catch (apiError) {
              console.error("问答API调用失败:", apiError);
              throw apiError;
            }
            break;
          case "basic":
            console.log("调用基础搜索API，参数:", params);
            try {
              response = await basicSearch(params);
              console.log("基础搜索API响应:", response);
              this.handleSearchResponse(response);
            } catch (apiError) {
              console.error("基础搜索API调用失败:", apiError);
              throw apiError;
            }
            break;
        }

        // 保存搜索历史
        await this.saveSearch();

        this.$message.success("搜索完成");
      } catch (error) {
        console.error("搜索失败:", error);
        this.$message.error("搜索失败，请稍后重试");
      } finally {
        this.searching = false;
      }
    },

    /** 处理搜索响应 */
    handleSearchResponse(response) {
      if (response.data && response.data.results) {
        this.searchResults = response.data.results;
        this.searchStats = {
          processingTimeMs: response.data.processingTimeMs,
          resultCount: response.data.resultCount,
          averageSimilarity: response.data.averageSimilarity,
        };
      }
    },

    /** 处理增强搜索响应（包含联网查询） */
    handleEnhancedSearchResponse(response) {
      console.log("=== handleEnhancedSearchResponse 开始 ===");
      console.log("增强搜索响应:", response);
      console.log("response.data:", response.data);
      console.log(
        "response.data.data:",
        response.data ? response.data.webSearchResults : "undefined"
      );

      if (response.data) {
        const data = response.data;
        console.log("解析的数据:", data);

        // 设置知识库搜索结果
        this.searchResults = data.knowledgeResults || [];
        console.log("知识库搜索结果:", this.searchResults);

        // 设置统计信息
        this.searchStats = {
          processingTimeMs: data.totalProcessingTimeMs,
          resultCount: data.knowledgeResultCount,
          averageSimilarity: data.averageSimilarity,
          webSearchTriggered: data.webSearchTriggered,
          webSearchResultCount: data.webSearchResultCount,
          searchMethod: data.searchMethod,
        };
        console.log("搜索统计信息:", this.searchStats);

        // 直接设置联网搜索结果，不通过方法调用
        if (data.webSearchResults && data.webSearchResults.length > 0) {
          console.log("联网搜索结果:", data.webSearchResults);
          this.webSearchResults = data.webSearchResults;
          console.log("当前联网搜索结果:", this.webSearchResults);
        }

        // 直接设置大模型推理结果，不通过方法调用
        if (data.llmInference) {
          console.log("大模型推理结果:", data.llmInference);
          this.llmInferenceResult = data.llmInference;
          console.log("当前大模型推理结果:", this.llmInferenceResult);
        }
      } else {
        console.log("响应数据结构不正确:", response);
      }
    },

    /** 处理问答响应 */
    handleAskResponse(response) {
      if (response.data && response.data.answer) {
        this.answerResult = response.data.answer;
        this.searchStats = {
          processingTimeMs: response.data.processingTimeMs || 0,
        };
      }
    },

    /** 保存搜索记录 */
    async saveSearch() {
      const searchRecord = {
        query: this.searchQuery,
        mode: this.searchMode,
        resultCount: this.searchResults.length,
        hasAnswer: !!this.answerResult,
      };

      await saveSearchHistory(searchRecord);
      await this.loadSearchHistory();
    },

    /** 加载搜索历史 */
    async loadSearchHistory() {
      try {
        const response = await getSearchHistory();
        this.searchHistory = response.data || [];
      } catch (error) {
        console.error("加载搜索历史失败:", error);
      }
    },

    /** 重复搜索 */
    repeatSearch(historyItem) {
      this.searchQuery = historyItem.query;
      this.searchMode = historyItem.mode || "enhanced";
      this.handleSearch();
    },

    /** 清空搜索历史 */
    async clearHistory() {
      try {
        await clearSearchHistory();
        this.searchHistory = [];
        this.$message.success("搜索历史已清空");
      } catch (error) {
        console.error("清空历史失败:", error);
        this.$message.error("清空失败");
      }
    },

    /** 使用示例查询 */
    useExample(example) {
      this.searchQuery = example;
      this.showSearchTips = false;
      this.handleSearch();
    },

    /** 查看详情 */
    viewDetail(result) {
      // 跳转到知识数据详情页
      this.$router.push({
        path: "/knowledge/data",
        query: { sourceUrl: result.sourceUrl },
      });
    },

    /** 基于内容提问 */
    askAbout(result) {
      this.searchQuery = `请详细介绍一下：${result.title}`;
      this.searchMode = "ask";
      this.handleSearch();
    },

    /** 格式化答案 */
    formatAnswer(answer) {
      // 简单的换行处理
      return answer.replace(/\n/g, "<br>");
    },

    /** 格式化时间 */
    formatTime(timeString) {
      if (!timeString) return "";
      const date = new Date(timeString);
      return date.toLocaleDateString("zh-CN");
    },

    /** 截断文本 */
    truncateText(text, maxLength) {
      if (!text) return "";
      return text.length > maxLength
        ? text.substring(0, maxLength) + "..."
        : text;
    },

    /** 获取相似度标签类型 */
    getSimilarityTagType(similarity) {
      if (similarity >= 0.8) return "success";
      if (similarity >= 0.6) return "warning";
      return "info";
    },

    /** 显示联网搜索结果 */
    showWebSearchResults(webResults) {
      console.log("设置联网搜索结果:", webResults);
      this.webSearchResults = webResults;
      console.log("当前联网搜索结果:", this.webSearchResults);
    },

    /** 显示大模型推理结果 */
    showLLMInference(inference) {
      console.log("设置大模型推理结果:", inference);
      this.llmInferenceResult = inference;
      console.log("当前大模型推理结果:", this.llmInferenceResult);
    },

    /** 测试API调用 */
    async testApiCall() {
      console.log("=== 开始测试API调用 ===");
      try {
        const params = {
          query: "德国国庆",
          topK: 5,
        };

        console.log("调用API参数:", params);
        const response = await enhancedSearchWithWebFallback(params);
        console.log("API响应:", response);

        // 直接处理响应
        this.handleEnhancedSearchResponse(response);

        this.$message.success("API测试成功");
      } catch (error) {
        console.error("API测试失败:", error);
        this.$message.error("API测试失败: " + error.message);
      }
    },
  },
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mb8 {
  margin-bottom: 8px;
}

.mt8 {
  margin-top: 8px;
}

.search-card {
  margin-bottom: 20px;
}

.search-input {
  font-size: 16px;
}

.search-input >>> .el-input__inner {
  height: 50px;
  line-height: 50px;
  font-size: 16px;
}

.search-input >>> .el-input-group__prepend {
  background-color: #f5f7fa;
}

.advanced-options {
  margin-top: 20px;
  padding: 20px;
  background-color: #fafbfc;
  border-radius: 6px;
}

.results-card {
  margin-bottom: 20px;
}

.answer-section {
  margin-bottom: 20px;
}

.answer-content {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.answer-content h4 {
  margin: 0 0 15px 0;
  color: #409eff;
}

.answer-text {
  line-height: 1.8;
  color: #303133;
}

.result-item {
  margin-bottom: 16px;
  transition: all 0.3s;
}

.result-item:hover {
  transform: translateY(-2px);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.result-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.4;
  flex: 1;
}

.result-title a {
  color: #409eff;
  text-decoration: none;
}

.result-title a:hover {
  color: #66b1ff;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: 15px;
}

.meta-item {
  font-size: 12px;
  color: #909399;
}

.result-content {
  margin-bottom: 15px;
}

.result-summary {
  color: #606266;
  margin-bottom: 8px;
  font-style: italic;
}

.result-excerpt {
  color: #303133;
  line-height: 1.6;
  margin: 0;
}

.result-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.result-tags {
  flex: 1;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.history-card {
  margin-top: 20px;
}

.history-list {
  max-height: 200px;
  overflow-y: auto;
}

.history-item:hover {
  background-color: #409eff;
  color: white;
}

.search-tips ul {
  padding-left: 20px;
  line-height: 1.8;
}

.search-tips li {
  margin-bottom: 8px;
}

.example-queries {
  margin-top: 15px;
}

.example-tag {
  margin: 0 8px 8px 0;
  cursor: pointer;
}

.example-tag:hover {
  background-color: #409eff;
  color: white;
}

h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.el-form-item {
  margin-bottom: 18px;
}

.el-divider--horizontal {
  margin: 15px 0;
}

/* 大模型推理结果样式 */
.llm-inference-section {
  margin-bottom: 20px;
}

.llm-inference-content {
  background-color: #f0f9ff;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
}

.inference-text {
  line-height: 1.8;
  color: #303133;
}

/* 联网搜索结果样式 */
.web-search-results {
  margin-bottom: 20px;
}

.web-result-item {
  margin-bottom: 16px;
  transition: all 0.3s;
  border-left: 3px solid #e6a23c;
}

.web-result-item:hover {
  transform: translateY(-2px);
}

.web-result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.web-result-title {
  margin: 0;
  font-size: 16px;
  line-height: 1.4;
  flex: 1;
}

.web-result-title a {
  color: #e6a23c;
  text-decoration: none;
}

.web-result-title a:hover {
  color: #f0c78a;
}

.web-result-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: 15px;
}

.web-result-content {
  margin-bottom: 10px;
}

.web-result-snippet {
  color: #606266;
  line-height: 1.6;
  margin: 0;
  font-style: italic;
}
</style>
