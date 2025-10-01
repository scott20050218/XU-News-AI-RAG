import request from "@/utils/request";

// langchain API 基础配置
const LANGCHAIN_BASE_URL =
  process.env.VUE_APP_LANGCHAIN_BASE_API || "/langchain";

/**
 * LangChain 智能检索
 * @param {Object} params 检索参数
 * @param {string} params.query - 必需，查询文本
 * @param {number} params.topK - 可选，返回结果数量，默认10
 * @param {number} params.minSimilarity - 可选，最小相似度阈值，默认0.0
 * @param {string} params.contentType - 可选，内容类型过滤
 * @param {boolean} params.processedOnly - 可选，是否只返回已处理内容，默认false
 */
export function enhancedSearch(params) {
  return request({
    url: `${LANGCHAIN_BASE_URL}/api/enhanced-knowledge/search`,
    method: "post",
    data: params,
  });
}

/**
 * 增强搜索：知识库检索 + 联网查询 + 大模型推理
 * @param {Object} params 检索参数
 * @param {string} params.query - 必需，查询文本
 * @param {number} params.topK - 可选，返回结果数量，默认10
 */
export function enhancedSearchWithWebFallback(params) {
  return request({
    url: `${LANGCHAIN_BASE_URL}/api/enhanced-knowledge/enhanced-search`,
    method: "post",
    data: params,
  });
}

/**
 * 大模型智能问答
 * @param {string} question - 用户问题
 */
export function askQuestion(question) {
  return request({
    url: `${LANGCHAIN_BASE_URL}/api/enhanced-knowledge/ask`,
    method: "post",
    data: { question },
  });
}

/**
 * 基础知识检索
 * @param {Object} params 检索参数
 * @param {string} params.query - 必需，查询文本
 * @param {number} params.topK - 可选，返回结果数量，默认10
 * @param {number} params.minSimilarity - 可选，最小相似度阈值，默认0.0
 * @param {string} params.contentType - 可选，内容类型过滤
 * @param {boolean} params.processedOnly - 可选，是否只返回已处理内容，默认false
 */
export function basicSearch(params) {
  return request({
    url: `${LANGCHAIN_BASE_URL}/api/knowledge/search`,
    method: "post",
    data: params,
  });
}

/**
 * 基础问答
 * @param {string} question - 用户问题
 */
export function basicAsk(question) {
  return request({
    url: `${LANGCHAIN_BASE_URL}/api/knowledge/ask`,
    method: "post",
    data: { question },
  });
}

/**
 * 查询历史记录（模拟接口，实际可能需要本地存储实现）
 */
export function getSearchHistory() {
  // 从本地存储获取搜索历史
  const history = localStorage.getItem("knowledge_search_history");
  return Promise.resolve({
    data: history ? JSON.parse(history) : [],
  });
}

/**
 * 保存搜索历史
 * @param {Object} searchRecord 搜索记录
 */
export function saveSearchHistory(searchRecord) {
  const history = JSON.parse(
    localStorage.getItem("knowledge_search_history") || "[]"
  );

  // 添加时间戳
  const record = {
    ...searchRecord,
    timestamp: new Date().toISOString(),
    id: Date.now(), // 简单的ID生成
  };

  // 去重：移除相同查询的旧记录
  const filteredHistory = history.filter((item) => item.query !== record.query);

  // 添加到开头，限制历史记录数量
  filteredHistory.unshift(record);
  const limitedHistory = filteredHistory.slice(0, 50); // 保留最近50条

  localStorage.setItem(
    "knowledge_search_history",
    JSON.stringify(limitedHistory)
  );

  return Promise.resolve({ data: record });
}

/**
 * 清除搜索历史
 */
export function clearSearchHistory() {
  localStorage.removeItem("knowledge_search_history");
  return Promise.resolve({ data: "cleared" });
}
