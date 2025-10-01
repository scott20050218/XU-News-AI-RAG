<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="类型" prop="contentType">
        <el-select
          v-model="queryParams.contentType"
          placeholder="“数据类型”"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in dict.type.data_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
        >
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <!-- <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:notice:add']"
          >新增</el-button
        >
      </el-col> -->
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:notice:edit']"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:notice:remove']"
          >删除</el-button
        >
      </el-col>
      <!-- <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar> -->
    </el-row>

    <el-table
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="knowId" width="100" />
      <el-table-column
        label="标题"
        align="center"
        prop="title"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="类型"
        align="center"
        prop="contentType"
        width="100"
      >
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.data_status"
            :value="scope.row.contentType"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="摘要"
        align="center"
        prop="summary"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="标签" align="center" prop="primaryTags">
        <template #default="scope">
          <el-tag
            v-for="(tag, index) in scope.row.primaryTags"
            :key="index"
            size="small"
            style="margin-right: 5px"
          >
            {{ tag }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="URL"
        align="center"
        prop="sourceUrl"
        width="100"
      />
      <el-table-column
        label="创建时间"
        align="center"
        prop="acquisitionTime"
        width="100"
      >
        <template slot-scope="scope">
          <span>{{ formatAcquisitionTime(scope.row.acquisitionTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:notice:edit']"
            >修改</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:notice:remove']"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改公告对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="ID" prop="knowId">
              <el-input v-model="form.knowId" placeholder="请输入Id" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入标题"
                readonly
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="contentType">
              <el-select v-model="form.contentType" placeholder="请选择类型">
                <el-option
                  v-for="dict in dict.type.data_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标签">
              <el-input v-model="form.tags" :min-height="192" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listData, delData, getData, updateData } from "@/api/knowledge/data";

export default {
  name: "KnowledgeData",
  dicts: ["data_status"],
  data() {
    return {
      // 遮罩层
      loading: false,
      // 选中数组
      ids: [],
      // 日期范围
      dateRange: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      dataList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 0,
        pageSize: 10,
        noticeTitle: undefined,
        createBy: undefined,
        status: undefined,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        noticeTitle: [
          { required: true, message: "标题不能为空", trigger: "blur" },
        ],
        noticeType: [
          { required: true, message: "类型不能为空", trigger: "change" },
        ],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 格式化获取时间，避免时区问题 */
    formatAcquisitionTime(timeString) {
      if (!timeString) return "";

      // 如果是ISO格式的字符串，提取日期部分
      if (timeString.includes("T")) {
        return timeString.split("T")[0]; // 只返回日期部分 YYYY-MM-DD
      }

      // 如果已经是Date对象，格式化为YYYY-MM-DD
      const date = new Date(timeString);
      if (isNaN(date.getTime())) return timeString; // 如果无法解析，返回原字符串

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    },
    /** 查询公告列表 */
    getList() {
      this.loading = true;
      const params = {
        ...this.queryParams,
        // 添加日期范围参数
        beginTime: this.dateRange ? this.dateRange[0] : undefined,
        endTime: this.dateRange ? this.dateRange[1] : undefined,
      };
      listData(params).then((response) => {
        console.log(response);
        this.dataList = response.data.content;
        this.total = response.data.totalElements;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        noticeId: undefined,
        noticeTitle: undefined,
        noticeType: undefined,
        noticeContent: undefined,
        status: "0",
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 0;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.knowId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加公告";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const knowId = row.knowId || this.ids;
      // TODO: 实现知识内容的获取和修改功能
      // console.log("编辑知识内容:", knowId);
      getData(knowId).then((response) => {
        // console.log(response);
        this.form = response.data;
        this.open = true;
        this.title = "修改知识内容";
      });
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.knowId != undefined) {
            updateData(this.form.knowId, this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addNotice(this.form).then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              // this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      // 获取要删除的ID数组
      const noticeIds = row.knowId ? [row.knowId] : this.ids;

      if (!noticeIds || noticeIds.length === 0) {
        this.$modal.msgWarning("请选择要删除的数据项");
        return;
      }

      const confirmMessage =
        noticeIds.length === 1
          ? `是否确认删除编号为"${noticeIds[0]}"的数据项？`
          : `是否确认删除选中的${noticeIds.length}个数据项？`;

      this.$modal
        .confirm(confirmMessage)
        .then(() => {
          // 循环删除每个ID
          const deletePromises = noticeIds.map((id) => delData(id));
          return Promise.all(deletePromises);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess(`成功删除${noticeIds.length}个数据项`);
        })
        .catch((error) => {
          console.error("删除失败:", error);
          this.$modal.msgError("删除失败，请重试");
        });
    },
  },
};
</script>
