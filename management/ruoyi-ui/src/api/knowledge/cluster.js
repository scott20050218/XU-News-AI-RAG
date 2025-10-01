import request from "@/utils/request";

// 获取知识库聚类分析数据
export function getClusterAnalysis(params) {
  return request({
    url: "/api/knowledge-content/cluster-analysis",
    method: "get",
    params,
  });
}

// 获取聚类统计信息
export function getClusterStats() {
  return request({
    url: "/api/knowledge-content/cluster-stats",
    method: "get",
  });
}

// 获取词云数据
export function getWordCloudData(params) {
  return request({
    url: "/api/knowledge-content/word-cloud",
    method: "get",
    params,
  });
}

// 获取内容类型分布
export function getContentTypeDistribution() {
  return request({
    url: "/api/knowledge-content/type-distribution",
    method: "get",
  });
}

// 获取标签分析数据
export function getTagAnalysis() {
  return request({
    url: "/api/knowledge-content/tag-analysis",
    method: "get",
  });
}

// 获取时间趋势分析
export function getTimeTrendAnalysis(params) {
  return request({
    url: "/api/knowledge-content/time-trend",
    method: "get",
    params,
  });
}
