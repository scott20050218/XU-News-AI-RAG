<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>知识库聚类分析报告</h2>
      <p class="page-description">
        基于内容相似度和标签的知识库数据聚类分析与可视化展示
      </p>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-data-analysis"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ clusterStats.totalClusters || 0 }}</div>
            <div class="stat-label">聚类数量</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">
              {{ clusterStats.totalDocuments || 0 }}
            </div>
            <div class="stat-label">文档总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-collection-tag"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ clusterStats.totalTags || 0 }}</div>
            <div class="stat-label">标签总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-pie-chart"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">
              {{ clusterStats.avgSimilarity || 0 }}%
            </div>
            <div class="stat-label">平均相似度</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表展示区域 -->
    <el-row :gutter="20" class="chart-section">
      <!-- 聚类散点图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="card-header">
            <span>知识库聚类分布图</span>
            <el-button
              type="text"
              icon="el-icon-refresh"
              @click="refreshClusterChart"
              :loading="clusterLoading"
            >
              刷新
            </el-button>
          </div>
          <div ref="clusterChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 内容类型分布饼图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="card-header">
            <span>内容类型分布</span>
            <el-button
              type="text"
              icon="el-icon-refresh"
              @click="refreshTypeChart"
              :loading="typeLoading"
            >
              刷新
            </el-button>
          </div>
          <div ref="typeChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <!-- 词云图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="card-header">
            <span>关键词云图</span>
            <el-button
              type="text"
              icon="el-icon-refresh"
              @click="refreshWordCloud"
              :loading="wordCloudLoading"
            >
              刷新
            </el-button>
          </div>
          <div ref="wordCloudChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 时间趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="card-header">
            <span>内容增长趋势</span>
            <el-select
              v-model="timePeriod"
              @change="refreshTrendChart"
              size="small"
              style="margin-left: 10px"
            >
              <el-option label="最近7天" value="7d"></el-option>
              <el-option label="最近30天" value="30d"></el-option>
              <el-option label="最近90天" value="90d"></el-option>
            </el-select>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 聚类详情表格 -->
    <el-card class="cluster-details">
      <div slot="header" class="card-header">
        <span>聚类详情分析</span>
        <el-button
          type="primary"
          size="small"
          icon="el-icon-download"
          @click="exportReport"
        >
          导出报告
        </el-button>
      </div>

      <el-table
        :data="clusterDetails"
        v-loading="tableLoading"
        style="width: 100%"
      >
        <el-table-column
          prop="clusterId"
          label="聚类ID"
          width="80"
          align="center"
        >
          <template slot-scope="scope">
            <el-tag :type="getClusterTagType(scope.row.clusterId)">
              {{ scope.row.clusterId }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clusterName" label="聚类名称" width="150">
          <template slot-scope="scope">
            <span class="cluster-name">{{ scope.row.clusterName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="documentCount"
          label="文档数量"
          width="100"
          align="center"
        >
          <template slot-scope="scope">
            <el-badge :value="scope.row.documentCount" class="item">
              <i class="el-icon-document"></i>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column
          prop="avgSimilarity"
          label="平均相似度"
          width="120"
          align="center"
        >
          <template slot-scope="scope">
            <el-progress
              :percentage="Math.round(scope.row.avgSimilarity * 100)"
              :color="getSimilarityColor(scope.row.avgSimilarity)"
              :show-text="true"
            ></el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="topKeywords" label="关键词" min-width="200">
          <template slot-scope="scope">
            <el-tag
              v-for="keyword in scope.row.topKeywords"
              :key="keyword"
              size="small"
              style="margin-right: 5px; margin-bottom: 3px"
            >
              {{ keyword }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contentTypes" label="内容类型" width="150">
          <template slot-scope="scope">
            <div class="content-types">
              <span
                v-for="type in scope.row.contentTypes"
                :key="type"
                class="type-badge"
              >
                {{ type }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="viewClusterDetails(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 聚类详情对话框 -->
    <el-dialog
      :title="'聚类 ' + selectedCluster.clusterId + ' 详情'"
      :visible.sync="detailDialogVisible"
      width="80%"
      :before-close="handleDetailClose"
    >
      <div v-if="selectedCluster.clusterId">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="聚类名称">{{
            selectedCluster.clusterName
          }}</el-descriptions-item>
          <el-descriptions-item label="文档数量">{{
            selectedCluster.documentCount
          }}</el-descriptions-item>
          <el-descriptions-item label="平均相似度"
            >{{
              (selectedCluster.avgSimilarity * 100).toFixed(2)
            }}%</el-descriptions-item
          >
          <el-descriptions-item label="创建时间">{{
            selectedCluster.createTime
          }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 20px">包含的文档</h4>
        <el-table :data="selectedCluster.documents" style="width: 100%">
          <el-table-column
            prop="title"
            label="标题"
            min-width="200"
          ></el-table-column>
          <el-table-column
            prop="contentType"
            label="类型"
            width="100"
          ></el-table-column>
          <el-table-column prop="similarity" label="相似度" width="100">
            <template slot-scope="scope">
              {{ (scope.row.similarity * 100).toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column prop="tags" label="标签" min-width="150">
            <template slot-scope="scope">
              <el-tag
                v-for="tag in scope.row.tags"
                :key="tag"
                size="mini"
                style="margin-right: 3px"
              >
                {{ tag }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from "echarts";
import "echarts-wordcloud";
import {
  getClusterAnalysis,
  getClusterStats,
  getWordCloudData,
  getContentTypeDistribution,
  getTagAnalysis,
  getTimeTrendAnalysis,
} from "@/api/knowledge/cluster";

export default {
  name: "KnowledgeCluster",
  data() {
    return {
      // 统计数据
      clusterStats: {},

      // 图表实例
      clusterChart: null,
      typeChart: null,
      wordCloudChart: null,
      trendChart: null,

      // 加载状态
      clusterLoading: false,
      typeLoading: false,
      wordCloudLoading: false,
      trendLoading: false,
      tableLoading: false,

      // 聚类详情数据
      clusterDetails: [],

      // 时间周期选择
      timePeriod: "30d",

      // 对话框
      detailDialogVisible: false,
      selectedCluster: {},
    };
  },

  mounted() {
    this.initPage();
  },

  beforeDestroy() {
    // 销毁图表实例
    if (this.clusterChart) {
      this.clusterChart.dispose();
    }
    if (this.typeChart) {
      this.typeChart.dispose();
    }
    if (this.wordCloudChart) {
      this.wordCloudChart.dispose();
    }
    if (this.trendChart) {
      this.trendChart.dispose();
    }
  },

  methods: {
    // 初始化页面
    async initPage() {
      await this.loadClusterStats();
      await this.loadClusterAnalysis();
      await this.loadContentTypeDistribution();
      await this.loadWordCloudData();
      await this.loadTimeTrendAnalysis();
    },

    // 加载聚类统计信息
    async loadClusterStats() {
      try {
        const response = await getClusterStats();
        this.clusterStats = response.data || {};
      } catch (error) {
        console.error("加载聚类统计失败:", error);
        this.$message.error("加载统计信息失败");
      }
    },

    // 加载聚类分析数据
    async loadClusterAnalysis() {
      this.clusterLoading = true;
      this.tableLoading = true;
      try {
        const response = await getClusterAnalysis();
        const data = response.data || {};

        // 更新表格数据
        this.clusterDetails = data.clusters || [];

        // 初始化聚类散点图
        this.initClusterChart(data.scatterData || []);
      } catch (error) {
        console.error("加载聚类分析失败:", error);
        this.$message.error("加载聚类分析失败");
      } finally {
        this.clusterLoading = false;
        this.tableLoading = false;
      }
    },

    // 加载内容类型分布
    async loadContentTypeDistribution() {
      this.typeLoading = true;
      try {
        const response = await getContentTypeDistribution();
        const data = response.data || [];
        this.initTypeChart(data);
      } catch (error) {
        console.error("加载类型分布失败:", error);
        this.$message.error("加载类型分布失败");
      } finally {
        this.typeLoading = false;
      }
    },

    // 加载词云数据
    async loadWordCloudData() {
      this.wordCloudLoading = true;
      try {
        const response = await getWordCloudData();
        const data = response.data || [];
        this.initWordCloudChart(data);
      } catch (error) {
        console.error("加载词云数据失败:", error);
        this.$message.error("加载词云数据失败");
      } finally {
        this.wordCloudLoading = false;
      }
    },

    // 加载时间趋势分析
    async loadTimeTrendAnalysis() {
      this.trendLoading = true;
      try {
        const response = await getTimeTrendAnalysis({
          period: this.timePeriod,
        });
        const data = response.data || {};
        this.initTrendChart(data);
      } catch (error) {
        console.error("加载趋势分析失败:", error);
        this.$message.error("加载趋势分析失败");
      } finally {
        this.trendLoading = false;
      }
    },

    // 初始化聚类散点图
    initClusterChart(data) {
      if (this.clusterChart) {
        this.clusterChart.dispose();
      }

      this.clusterChart = echarts.init(this.$refs.clusterChart);

      const option = {
        title: {
          text: "知识库内容聚类分布",
          left: "center",
          textStyle: {
            fontSize: 14,
          },
        },
        tooltip: {
          trigger: "item",
          formatter: function (params) {
            return `
              <div>
                <strong>${params.data.title}</strong><br/>
                聚类: ${params.data.cluster}<br/>
                相似度: ${(params.data.similarity * 100).toFixed(2)}%<br/>
                类型: ${params.data.contentType}
              </div>
            `;
          },
        },
        xAxis: {
          type: "value",
          name: "X 坐标",
          nameLocation: "middle",
          nameGap: 30,
        },
        yAxis: {
          type: "value",
          name: "Y 坐标",
          nameLocation: "middle",
          nameGap: 40,
        },
        series: [
          {
            type: "scatter",
            data: data,
            symbolSize: function (data) {
              return Math.max(data[3] * 20, 8); // 根据相似度调整点的大小
            },
            itemStyle: {
              color: function (params) {
                const colors = [
                  "#5470c6",
                  "#91cc75",
                  "#fac858",
                  "#ee6666",
                  "#73c0de",
                  "#3ba272",
                  "#fc8452",
                  "#9a60b4",
                  "#ea7ccc",
                ];
                return colors[params.data[2] % colors.length]; // 根据聚类ID着色
              },
              opacity: 0.8,
            },
          },
        ],
      };

      this.clusterChart.setOption(option);
    },

    // 初始化类型分布饼图
    initTypeChart(data) {
      if (this.typeChart) {
        this.typeChart.dispose();
      }

      this.typeChart = echarts.init(this.$refs.typeChart);

      const option = {
        title: {
          text: "内容类型分布",
          left: "center",
          textStyle: {
            fontSize: 14,
          },
        },
        tooltip: {
          trigger: "item",
          formatter: "{a} <br/>{b}: {c} ({d}%)",
        },
        legend: {
          orient: "vertical",
          left: "left",
        },
        series: [
          {
            name: "内容类型",
            type: "pie",
            radius: "70%",
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: "rgba(0, 0, 0, 0.5)",
              },
            },
          },
        ],
      };

      this.typeChart.setOption(option);
    },

    // 初始化词云图
    initWordCloudChart(data) {
      if (this.wordCloudChart) {
        this.wordCloudChart.dispose();
      }

      this.wordCloudChart = echarts.init(this.$refs.wordCloudChart);

      const option = {
        title: {
          text: "关键词云图",
          left: "center",
          textStyle: {
            fontSize: 14,
          },
        },
        series: [
          {
            type: "wordCloud",
            gridSize: 2,
            sizeRange: [12, 50],
            rotationRange: [-90, 90],
            shape: "pentagon",
            width: "100%",
            height: "100%",
            drawOutOfBound: true,
            textStyle: {
              fontFamily: "sans-serif",
              fontWeight: "bold",
              color: function () {
                return (
                  "rgb(" +
                  [
                    Math.round(Math.random() * 160),
                    Math.round(Math.random() * 160),
                    Math.round(Math.random() * 160),
                  ].join(",") +
                  ")"
                );
              },
            },
            emphasis: {
              textStyle: {
                shadowBlur: 10,
                shadowColor: "#333",
              },
            },
            data: data,
          },
        ],
      };

      this.wordCloudChart.setOption(option);
    },

    // 初始化趋势图
    initTrendChart(data) {
      if (this.trendChart) {
        this.trendChart.dispose();
      }

      this.trendChart = echarts.init(this.$refs.trendChart);

      const option = {
        title: {
          text: "内容增长趋势",
          left: "center",
          textStyle: {
            fontSize: 14,
          },
        },
        tooltip: {
          trigger: "axis",
        },
        xAxis: {
          type: "category",
          data: data.dates || [],
        },
        yAxis: {
          type: "value",
          name: "文档数量",
        },
        series: [
          {
            name: "新增文档",
            type: "line",
            data: data.counts || [],
            smooth: true,
            areaStyle: {
              opacity: 0.3,
            },
          },
        ],
      };

      this.trendChart.setOption(option);
    },

    // 刷新图表
    refreshClusterChart() {
      this.loadClusterAnalysis();
    },

    refreshTypeChart() {
      this.loadContentTypeDistribution();
    },

    refreshWordCloud() {
      this.loadWordCloudData();
    },

    refreshTrendChart() {
      this.loadTimeTrendAnalysis();
    },

    // 获取聚类标签类型
    getClusterTagType(clusterId) {
      const types = ["primary", "success", "info", "warning", "danger"];
      return types[clusterId % types.length];
    },

    // 获取相似度颜色
    getSimilarityColor(similarity) {
      if (similarity >= 0.8) return "#67c23a";
      if (similarity >= 0.6) return "#e6a23c";
      return "#f56c6c";
    },

    // 查看聚类详情
    async viewClusterDetails(cluster) {
      this.selectedCluster = { ...cluster };

      // 模拟加载聚类中的文档详情
      this.selectedCluster.documents = [
        {
          title: "示例文档1",
          contentType: "Web",
          similarity: 0.85,
          tags: ["技术", "Vue.js"],
        },
        {
          title: "示例文档2",
          contentType: "Manual",
          similarity: 0.78,
          tags: ["开发", "前端"],
        },
      ];
      this.selectedCluster.createTime = "2025-09-28 19:30:00";

      this.detailDialogVisible = true;
    },

    // 关闭详情对话框
    handleDetailClose() {
      this.detailDialogVisible = false;
      this.selectedCluster = {};
    },

    // 导出报告
    exportReport() {
      this.$message.success("报告导出功能开发中...");
    },
  },
};
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.page-description {
  color: #909399;
  margin: 0;
}

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 40px;
  color: #409eff;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.cluster-details {
  margin-top: 20px;
}

.cluster-name {
  font-weight: bold;
  color: #409eff;
}

.content-types {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.type-badge {
  background: #f0f9ff;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  display: inline-block;
}

.dialog-footer {
  text-align: right;
}
</style>
