import request from "@/utils/request";

// 查询列表
export function listData(query) {
  return request({
    url: "/api/knowledge-content",
    method: "get",
    params: query,
  });
}

// 删除
export function delData(id) {
  return request({
    url: "/api/knowledge-content/" + id,
    method: "delete",
  });
}

// 查询详细
export function getData(id) {
  return request({
    url: "/api/knowledge-content/" + id,
    method: "get",
  });
}

// 修改数据
export function updateData(id, data) {
  return request({
    url: "/api/knowledge-content/" + id,
    method: "put",
    data: data,
  });
}

// 新增数据（通过URL加载）
export function addData(data) {
  return request({
    url: "/api/knowledge-content",
    method: "post",
    data: data,
  });
}
