-- ----------------------------
-- 知识库模块菜单配置
-- ----------------------------

-- 一级菜单：知识库管理
INSERT INTO sys_menu VALUES('2000', '知识库管理', '0', '5', 'knowledge', NULL, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '', NULL, '知识库管理目录');

-- 二级菜单：知识库数据管理
INSERT INTO sys_menu VALUES('2001', '知识库数据', '2000', '1', 'data', 'knowledge/data/index', '', '', 1, 0, 'C', '0', '0', 'knowledge:data:list', 'table', 'admin', sysdate(), '', NULL, '知识库数据管理菜单');

-- 二级菜单：知识库加载
INSERT INTO sys_menu VALUES('2002', '知识库加载', '2000', '2', 'load', 'knowledge/load/index', '', '', 1, 0, 'C', '0', '0', 'knowledge:load:list', 'upload', 'admin', sysdate(), '', NULL, '知识库加载菜单');

-- 二级菜单：语义搜索
INSERT INTO sys_menu VALUES('2003', '语义搜索', '2000', '3', 'search', 'knowledge/search/index', '', '', 1, 0, 'C', '0', '0', 'knowledge:search:list', 'search', 'admin', sysdate(), '', NULL, '语义搜索菜单');

-- 二级菜单：聚类分析
INSERT INTO sys_menu VALUES('2004', '聚类分析', '2000', '4', 'cluster', 'knowledge/cluster/index', '', '', 1, 0, 'C', '0', '0', 'knowledge:cluster:list', 'data-analysis', 'admin', sysdate(), '', NULL, '知识库聚类分析菜单');

-- 知识库数据管理按钮权限
INSERT INTO sys_menu VALUES('2010', '知识库数据查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:data:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2011', '知识库数据新增', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:data:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2012', '知识库数据修改', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:data:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2013', '知识库数据删除', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:data:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2014', '知识库数据导出', '2001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:data:export', '#', 'admin', sysdate(), '', NULL, '');

-- 知识库加载按钮权限
INSERT INTO sys_menu VALUES('2020', '知识库加载查询', '2002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:load:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2021', '知识库加载新增', '2002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:load:add', '#', 'admin', sysdate(), '', NULL, '');

-- 语义搜索按钮权限
INSERT INTO sys_menu VALUES('2030', '语义搜索查询', '2003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:search:query', '#', 'admin', sysdate(), '', NULL, '');

-- 聚类分析按钮权限
INSERT INTO sys_menu VALUES('2040', '聚类分析查询', '2004', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:cluster:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2041', '聚类分析导出', '2004', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'knowledge:cluster:export', '#', 'admin', sysdate(), '', NULL, '');
