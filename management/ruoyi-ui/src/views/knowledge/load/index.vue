<template>
  <div class="app-container">
    <!-- 页面标题和说明 -->
    <el-row class="mb8">
      <el-col>
        <h3>知识内容加载</h3>
        <p style="color: #666; margin-bottom: 20px">
          通过输入URL地址，系统将自动抓取并处理内容到知识库中
        </p>
      </el-col>
    </el-row>

    <!-- URL输入表单 -->
    <el-card class="mb8">
      <div slot="header">
        <span>URL 输入</span>
      </div>
      <el-form
        :model="form"
        :rules="rules"
        ref="form"
        label-width="100px"
        size="small"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="URL地址" prop="sourceUrl">
              <el-input
                v-model="form.sourceUrl"
                placeholder="请输入要处理的URL地址，如：https://example.com/article"
                clearable
                :disabled="loading"
              >
                <template slot="prepend">URL</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="内容标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入内容标题（可选，留空将自动从URL提取）"
                clearable
                :disabled="loading"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="内容类型" prop="contentType">
              <el-select
                v-model="form.contentType"
                placeholder="请选择内容类型"
                clearable
                style="width: 100%"
                :disabled="loading"
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
          <el-col :span="12">
            <el-form-item label="标签" prop="tags">
              <el-input
                v-model="form.tags"
                placeholder="请输入标签，多个标签用逗号分隔"
                clearable
                :disabled="loading"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息（可选）"
                :disabled="loading"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24" style="text-align: center">
            <el-button
              type="primary"
              size="small"
              :loading="loading"
              @click="handleSubmit"
              icon="el-icon-upload"
            >
              {{ loading ? "处理中..." : "开始处理" }}
            </el-button>
            <el-button
              size="small"
              @click="handleReset"
              :disabled="loading"
              icon="el-icon-refresh"
            >
              重置
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 处理状态 -->
    <el-card v-show="showProgress">
      <div slot="header">
        <span>处理进度</span>
      </div>
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="验证URL" description="检查URL有效性"></el-step>
        <el-step title="抓取内容" description="获取网页内容"></el-step>
        <el-step title="内容分析" description="分析和提取关键信息"></el-step>
        <el-step title="保存到知识库" description="存储处理结果"></el-step>
      </el-steps>

      <div style="margin-top: 20px; text-align: center">
        <el-progress
          :percentage="progressPercentage"
          :status="progressStatus"
          :stroke-width="6"
        ></el-progress>
        <p style="margin-top: 10px; color: #666">{{ progressText }}</p>
      </div>
    </el-card>

    <!-- 处理结果 -->
    <el-card v-show="showResult">
      <div slot="header">
        <span>处理结果</span>
      </div>
      <el-result
        :icon="resultIcon"
        :title="resultTitle"
        :subTitle="resultSubTitle"
      >
        <template slot="extra">
          <el-button type="primary" size="small" @click="handleViewData">
            查看数据
          </el-button>
          <el-button size="small" @click="handleLoadAnother">
            继续加载
          </el-button>
        </template>
      </el-result>
    </el-card>

    <!-- 最近处理记录 -->
    <el-card class="mt8">
      <div slot="header">
        <span>最近处理记录</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="refreshRecentRecords"
          icon="el-icon-refresh"
        >
          刷新
        </el-button>
      </div>
      <el-table
        :data="recentRecords"
        v-loading="recordsLoading"
        size="small"
        stripe
      >
        <el-table-column
          prop="sourceUrl"
          label="URL地址"
          :show-overflow-tooltip="true"
        />
        <el-table-column
          prop="title"
          label="标题"
          :show-overflow-tooltip="true"
          width="200"
        />
        <el-table-column prop="contentType" label="类型" width="100">
          <template slot-scope="scope">
            <dict-tag
              :options="dict.type.data_status"
              :value="scope.row.contentType"
            />
          </template>
        </el-table-column>
        <el-table-column prop="acquisitionTime" label="处理时间" width="150">
          <template slot-scope="scope">
            <span>{{ formatAcquisitionTime(scope.row.acquisitionTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              @click="handleViewRecord(scope.row)"
              icon="el-icon-view"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { addData, listData } from "@/api/knowledge/data";

export default {
  name: "KnowledgeLoad",
  dicts: ["data_status"],
  data() {
    return {
      // 表单数据
      form: {
        sourceUrl: "",
        title: "",
        contentType: "",
        tags: "",
        remark: "",
      },
      // 表单验证规则
      rules: {
        sourceUrl: [
          { required: true, message: "URL地址不能为空", trigger: "blur" },
          {
            pattern: /^https?:\/\/.+/,
            message: "请输入有效的URL地址（以http://或https://开头）",
            trigger: "blur",
          },
        ],
        contentType: [
          { required: true, message: "请选择内容类型", trigger: "change" },
        ],
      },
      // 加载状态
      loading: false,
      // 进度相关
      showProgress: false,
      currentStep: 0,
      progressPercentage: 0,
      progressStatus: "",
      progressText: "",
      // 结果相关
      showResult: false,
      resultIcon: "success",
      resultTitle: "",
      resultSubTitle: "",
      // 最近记录
      recentRecords: [],
      recordsLoading: false,
    };
  },
  created() {
    this.getRecentRecords();
  },
  methods: {
    /** 格式化获取时间，避免时区问题 */
    formatAcquisitionTime(timeString) {
      if (!timeString) return "";
      if (timeString.includes("T")) {
        return (
          timeString.split("T")[0] +
          " " +
          timeString.split("T")[1].substring(0, 8)
        );
      }
      const date = new Date(timeString);
      if (isNaN(date.getTime())) return timeString;
      return date.toLocaleString();
    },

    /** 提交处理 */
    handleSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.startProcessing();
        }
      });
    },

    /** 开始处理 */
    async startProcessing() {
      this.loading = true;
      this.showProgress = true;
      this.showResult = false;
      this.currentStep = 0;
      this.progressPercentage = 0;
      this.progressStatus = "active";

      try {
        // 步骤1: 验证URL
        this.updateProgress(0, 25, "正在验证URL...");
        await this.sleep(500);

        // 步骤2: 抓取内容
        this.updateProgress(1, 50, "正在抓取网页内容...");
        await this.sleep(800);

        // 步骤3: 内容分析
        this.updateProgress(2, 75, "正在分析内容...");
        await this.sleep(600);

        // 步骤4: 保存到知识库
        this.updateProgress(3, 90, "正在保存到知识库...");

        // 调用API
        const response = await addData({
          sourceUrl: this.form.sourceUrl,
          contentType: this.form.contentType,
          tags: this.form.tags,
          title:
            this.form.title || this.extractTitleFromUrl(this.form.sourceUrl), // 使用用户输入的标题或从URL提取
          content: this.form.remark || "", // 使用备注作为初始内容
          success: true, // 标记为成功处理
          processed: false, // 初始状态未处理
          errorMessage: null, // 无错误信息
        });

        // 完成
        this.updateProgress(4, 100, "处理完成！");
        this.progressStatus = "success";

        setTimeout(() => {
          this.showSuccessResult(response.data);
          this.getRecentRecords(); // 刷新最近记录
        }, 500);
      } catch (error) {
        console.error("处理失败:", error);
        this.progressStatus = "exception";
        this.progressText = "处理失败，请检查URL是否有效或稍后重试";
        this.showErrorResult(error);
      } finally {
        this.loading = false;
      }
    },

    /** 更新进度 */
    updateProgress(step, percentage, text) {
      this.currentStep = step;
      this.progressPercentage = percentage;
      this.progressText = text;
    },

    /** 显示成功结果 */
    showSuccessResult(data) {
      this.showResult = true;
      this.resultIcon = "success";
      this.resultTitle = "内容处理成功";
      this.resultSubTitle = `URL内容已成功添加到知识库，知识内容ID: ${
        data.knowId || "未知"
      }`;
    },

    /** 显示错误结果 */
    showErrorResult(error) {
      this.showResult = true;
      this.resultIcon = "error";
      this.resultTitle = "处理失败";
      this.resultSubTitle = error.message || "请检查URL是否有效，或联系管理员";
    },

    /** 重置表单 */
    handleReset() {
      this.$refs.form.resetFields();
      this.showProgress = false;
      this.showResult = false;
    },

    /** 继续加载 */
    handleLoadAnother() {
      this.handleReset();
    },

    /** 查看数据 */
    handleViewData() {
      this.$router.push("/knowledge/data");
    },

    /** 查看记录详情 */
    handleViewRecord(row) {
      this.$router.push({
        path: "/knowledge/data",
        query: { id: row.knowId },
      });
    },

    /** 获取最近记录 */
    async getRecentRecords() {
      this.recordsLoading = true;
      try {
        const response = await listData({
          pageNum: 0,
          pageSize: 5,
        });
        this.recentRecords = response.data.content || [];
      } catch (error) {
        console.error("获取最近记录失败:", error);
      } finally {
        this.recordsLoading = false;
      }
    },

    /** 刷新最近记录 */
    refreshRecentRecords() {
      this.getRecentRecords();
    },

    /** 从URL提取标题 */
    extractTitleFromUrl(url) {
      try {
        const urlObj = new URL(url);
        const pathname = urlObj.pathname;
        const segments = pathname
          .split("/")
          .filter((segment) => segment.length > 0);

        if (segments.length > 0) {
          // 取最后一个路径段作为标题，去掉文件扩展名
          const lastSegment = segments[segments.length - 1];
          const title = lastSegment.replace(/\.[^/.]+$/, ""); // 移除扩展名
          return title.replace(/[-_]/g, " ") || urlObj.hostname; // 替换连字符为空格
        }

        return urlObj.hostname; // 如果没有路径，使用域名
      } catch (error) {
        // 如果URL解析失败，使用简单的字符串处理
        return url.split("/").pop() || url;
      }
    },

    /** 辅助函数：延时 */
    sleep(ms) {
      return new Promise((resolve) => setTimeout(resolve, ms));
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

.el-card {
  margin-bottom: 20px;
}

.el-steps {
  margin: 20px 0;
}

.el-progress {
  max-width: 600px;
  margin: 0 auto;
}

h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.el-form-item {
  margin-bottom: 18px;
}
</style>
