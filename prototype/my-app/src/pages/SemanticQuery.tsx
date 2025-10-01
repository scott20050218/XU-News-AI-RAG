import React, { useState } from "react";
import {
  Table,
  Input,
  Button,
  Space,
  DatePicker,
  Select,
  Tag,
  Slider,
} from "antd";
import { SearchOutlined, ReloadOutlined } from "@ant-design/icons";
import "../App.css"; // 确保引入了你的通用CSS文件

const { RangePicker } = DatePicker;
const { Option } = Select;

interface SemanticQueryResult {
  key: string;
  id: string;
  result: string;
  matchProbability: number;
  source: string;
  createTime: string;
}

const SemanticQuery: React.FC = () => {
  const [data, setData] = useState<SemanticQueryResult[]>([
    {
      key: "1",
      id: "1",
      result: "关于React Hooks最佳实践的文档",
      matchProbability: 0.95,
      source: "知识库",
      createTime: "2025-09-26 10:00:00",
    },
    {
      key: "2",
      id: "2",
      result: "Spring Boot微服务架构设计指南",
      matchProbability: 0.88,
      source: "外部博客",
      createTime: "2025-09-26 10:15:00",
    },
    {
      key: "3",
      id: "3",
      result: "Python数据分析库Pandas使用教程",
      matchProbability: 0.72,
      source: "网络抓取",
      createTime: "2025-09-26 10:30:00",
    },
  ]);

  const columns = [
    {
      title: "序号",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "查询结果",
      dataIndex: "result",
      key: "result",
    },
    {
      title: "匹配概率",
      dataIndex: "matchProbability",
      key: "matchProbability",
      render: (probability: number) => (
        <Tag
          color={
            probability > 0.9
              ? "green"
              : probability > 0.7
              ? "geekblue"
              : "default"
          }
        >
          {(probability * 100).toFixed(2)}%
        </Tag>
      ),
      sorter: (a: SemanticQueryResult, b: SemanticQueryResult) =>
        a.matchProbability - b.matchProbability,
    },
    {
      title: "来源",
      dataIndex: "source",
      key: "source",
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
    },
    {
      title: "操作",
      key: "action",
      render: (_: any, record: SemanticQueryResult) => (
        <Space size="middle">
          <Button type="link" size="small">
            查看详情
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="semantic-query-container">
      {/* 搜索/筛选表单 */}
      <div className="search-form-container">
        <Space>
          查询条件:{" "}
          <Input placeholder="请输入查询条件" style={{ width: 220 }} />
          匹配概率:
          <Slider
            range
            defaultValue={[0, 100]}
            style={{ width: 180 }}
            tooltip={{ formatter: (value) => `${value}%` }}
          />
          来源:
          <Select defaultValue="all" style={{ width: 120 }}>
            <Option value="all">所有</Option>
            <Option value="知识库">知识库</Option>
            <Option value="外部博客">外部博客</Option>
            <Option value="网络抓取">网络抓取</Option>
          </Select>
          创建时间: <RangePicker />
          <Button type="primary" icon={<SearchOutlined />}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />}>重置</Button>
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

export default SemanticQuery;
