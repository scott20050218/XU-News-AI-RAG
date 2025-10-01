import React, { useState } from "react";
import { Table, Input, Button, Space, DatePicker, Select, Tag } from "antd";
import { SearchOutlined, ReloadOutlined } from "@ant-design/icons";
import "../App.css"; // 确保引入了你的通用CSS文件

const { RangePicker } = DatePicker;
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const { Option } = Select;

interface ReportItem {
  key: string;
  id: string;
  name: string;
  description: string;
  type: string;
  generateTime: string;
  updateTime: string;
}

const ReportGeneration: React.FC = () => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [data, setData] = useState<ReportItem[]>([
    {
      key: "1",
      id: "1",
      name: "知识库聚类分析报告1",
      description: "对知识库内容进行聚类分析的第一份报告",
      type: "文档聚类",
      generateTime: "2025-09-26 11:15:00",
      updateTime: "-",
    },
    {
      key: "2",
      id: "2",
      name: "RSS数据趋势报告",
      description: "分析RSS数据的导入趋势",
      type: "数据趋势",
      generateTime: "2025-09-25 18:49:00",
      updateTime: "-",
    },
    {
      key: "3",
      id: "3",
      name: "网页抓取效率报告",
      description: "评估网页抓取的成功率和效率",
      type: "效率分析",
      generateTime: "2025-09-24 14:34:00",
      updateTime: "2025-09-26 09:30:00",
    },
  ]);

  const columns = [
    {
      title: "序号",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "报告名称",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "报告描述",
      dataIndex: "description",
      key: "description",
    },
    {
      title: "报告类型",
      dataIndex: "type",
      key: "type",
      render: (type: string) => {
        let color = type.length > 5 ? "geekblue" : "green";
        if (type === "数据趋势") {
          color = "volcano";
        }
        return (
          <Tag color={color} key={type}>
            {type.toUpperCase()}
          </Tag>
        );
      },
    },
    {
      title: "生成时间",
      dataIndex: "generateTime",
      key: "generateTime",
    },
    {
      title: "更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
    },
    {
      title: "操作",
      key: "action",
      render: (_: any, record: ReportItem) => (
        <Space size="middle">
          <Button type="link" size="small">
            预览
          </Button>
          <Button type="link" size="small">
            编辑
          </Button>
          <Button type="link" danger size="small">
            删除
          </Button>
          <Button type="link" size="small">
            下载
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="report-generation-container">
      {/* 搜索/筛选表单 */}
      <div className="search-form-container">
        <Space>
          报告名称:{" "}
          <Input placeholder="请输入报告名称" style={{ width: 180 }} />
          报告描述:{" "}
          <Input placeholder="请输入报告描述" style={{ width: 180 }} />
          生成时间: <RangePicker />
          <Button type="primary" icon={<SearchOutlined />}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />}>重置</Button>
        </Space>
      </div>

      {/* 操作按钮组 */}
      <div className="action-buttons-container">
        <Space>
          <Button type="primary">↑ 生成</Button>
          <Button type="primary">+ 创建</Button>
          <Button type="primary">↓ 导入</Button>
          <Button>修改</Button>
          <Button danger>× 删除</Button>
        </Space>
      </div>

      {/* 数据表格 */}
      <Table
        columns={columns}
        dataSource={data}
        pagination={{
          total: data.length,
          showTotal: (total, range) =>
            `显示第 ${range[0]} 到第 ${range[1]} 条记录, 总共 ${total} 条记录`,
        }}
      />
    </div>
  );
};

export default ReportGeneration;
