import React, { useState } from "react";
import {
  Table,
  Input,
  Button,
  Space,
  DatePicker,
  Select,
  Tag,
  Modal,
} from "antd";
import {
  SearchOutlined,
  ReloadOutlined,
  ExclamationCircleOutlined,
} from "@ant-design/icons";
import "../App.css"; // 确保引入了你的通用CSS文件

const { confirm } = Modal;
const { RangePicker } = DatePicker;
const { Option } = Select;

interface KnowledgeItem {
  key: string;
  id: string;
  name: string;
  tags: string[];
  source: string;
  type: string;
  createTime: string;
}

const KnowledgeBase: React.FC = () => {
  const [searchText, setSearchText] = useState("");
  const [searchedColumn, setSearchedColumn] = useState("");
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [data, setData] = useState<KnowledgeItem[]>([
    {
      key: "1",
      id: "1",
      name: "React Hooks 教程",
      tags: ["前端", "React", "教程"],
      source: "官网",
      type: "文档",
      createTime: "2025-05-26 11:03:50",
    },
    {
      key: "2",
      id: "2",
      name: "Spring Boot 微服务实践",
      tags: ["后端", "Java", "微服务"],
      source: "博客",
      type: "文章",
      createTime: "2025-05-26 11:03:50",
    },
    {
      key: "3",
      id: "3",
      name: "Node.js 异步编程",
      tags: ["后端", "Node.js", "异步"],
      source: "社区",
      type: "博客",
      createTime: "2025-05-27 09:10:00",
    },
  ]);

  const onSelectChange = (newSelectedRowKeys: React.Key[]) => {
    console.log("selectedRowKeys changed: ", newSelectedRowKeys);
    setSelectedRowKeys(newSelectedRowKeys);
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: onSelectChange,
  };

  const handleDelete = (id?: string) => {
    confirm({
      title: id ? "确定删除这条知识内容吗？" : "确定删除选中的知识内容吗？",
      icon: <ExclamationCircleOutlined />,
      content: "删除后将无法恢复！",
      okText: "确定",
      cancelText: "取消",
      onOk() {
        let newData = [...data];
        if (id) {
          newData = newData.filter((item) => item.id !== id);
        } else {
          newData = newData.filter(
            (item) => !selectedRowKeys.includes(item.key)
          );
          setSelectedRowKeys([]); // 清空选中项
        }
        setData(newData);
      },
      onCancel() {
        console.log("Cancel");
      },
    });
  };

  const columns = [
    {
      title: "内容编号",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "内容名称",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "标签",
      dataIndex: "tags",
      key: "tags",
      render: (tags: string[]) => (
        <>
          {tags.map((tag) => {
            let color = tag.length > 5 ? "geekblue" : "green";
            if (tag === "React") {
              color = "volcano";
            }
            return (
              <Tag color={color} key={tag}>
                {tag.toUpperCase()}
              </Tag>
            );
          })}
        </>
      ),
    },
    {
      title: "来源",
      dataIndex: "source",
      key: "source",
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
    },
    {
      title: "操作",
      key: "action",
      render: (_: any, record: KnowledgeItem) => (
        <Space size="middle">
          <Button type="link" size="small">
            编辑
          </Button>
          <Button
            type="link"
            danger
            size="small"
            onClick={() => handleDelete(record.id)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="knowledge-base-container">
      {/* 搜索/筛选表单 */}
      <div className="search-form-container">
        <Space>
          内容名称:{" "}
          <Input placeholder="请输入内容名称" style={{ width: 180 }} />
          标签: <Input placeholder="请输入标签" style={{ width: 180 }} />
          来源:
          <Select defaultValue="all" style={{ width: 120 }}>
            <Option value="all">所有</Option>
            <Option value="官网">官网</Option>
            <Option value="博客">博客</Option>
          </Select>
          创建时间: <RangePicker />
          <Button type="primary" icon={<SearchOutlined />}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />}>重置</Button>
        </Space>
      </div>

      {/* 操作按钮组 */}
      <div className="action-buttons-container">
        <Space>
          <Button type="primary">+ 新增</Button>
          <Button>修改</Button>
          <Button danger onClick={() => handleDelete()}>
            × 删除
          </Button>
          <Button>↓ 导入</Button>
        </Space>
      </div>

      {/* 数据表格 */}
      <Table
        rowSelection={rowSelection}
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

export default KnowledgeBase;
