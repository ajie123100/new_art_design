CREATE TABLE IF NOT EXISTS sys_user (
  user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  dept_id BIGINT NULL COMMENT '部门ID',
  user_name VARCHAR(30) NOT NULL COMMENT '用户账号',
  nick_name VARCHAR(30) NOT NULL COMMENT '用户昵称',
  user_type VARCHAR(2) DEFAULT '00' COMMENT '用户类型',
  email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
  phonenumber VARCHAR(20) DEFAULT '' COMMENT '手机号',
  sex CHAR(1) DEFAULT '0' COMMENT '性别',
  avatar VARCHAR(255) DEFAULT '' COMMENT '头像',
  password VARCHAR(100) DEFAULT '' COMMENT '密码',
  status CHAR(1) DEFAULT '1' COMMENT '状态：1正常 2停用',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志',
  login_ip VARCHAR(128) DEFAULT '' COMMENT '最后登录IP',
  login_date DATETIME NULL COMMENT '最后登录时间',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_sys_user_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

CREATE TABLE IF NOT EXISTS sys_role (
  role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(30) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(100) NOT NULL COMMENT '角色权限字符',
  role_sort INT DEFAULT 0 COMMENT '显示顺序',
  data_scope CHAR(1) DEFAULT '1' COMMENT '数据范围',
  status CHAR(1) DEFAULT '1' COMMENT '状态：1正常 2停用',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';

CREATE TABLE IF NOT EXISTS sys_menu (
  menu_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  menu_name VARCHAR(80) NOT NULL COMMENT '菜单名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
  order_num INT DEFAULT 0 COMMENT '显示顺序',
  path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
  component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
  query_param VARCHAR(255) DEFAULT NULL COMMENT '路由参数',
  route_name VARCHAR(100) DEFAULT '' COMMENT '路由名称',
  is_frame CHAR(1) DEFAULT '1' COMMENT '是否外链',
  is_cache CHAR(1) DEFAULT '0' COMMENT '是否缓存',
  menu_type CHAR(1) DEFAULT 'M' COMMENT '菜单类型：M目录 C菜单 F按钮',
  visible CHAR(1) DEFAULT '1' COMMENT '显示状态：1显示 2隐藏',
  status CHAR(1) DEFAULT '1' COMMENT '菜单状态：1正常 2停用',
  perms VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  icon VARCHAR(100) DEFAULT '' COMMENT '菜单图标',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

CREATE TABLE IF NOT EXISTS sys_dept (
  dept_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
  ancestors VARCHAR(50) DEFAULT '' COMMENT '祖级列表',
  dept_name VARCHAR(30) DEFAULT '' COMMENT '部门名称',
  order_num INT DEFAULT 0 COMMENT '显示顺序',
  leader VARCHAR(20) DEFAULT NULL COMMENT '负责人',
  phone VARCHAR(11) DEFAULT NULL COMMENT '联系电话',
  email VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
  status CHAR(1) DEFAULT '1' COMMENT '部门状态',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE IF NOT EXISTS sys_dict_type (
  dict_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  dict_name VARCHAR(100) DEFAULT '' COMMENT '字典名称',
  dict_type VARCHAR(100) DEFAULT '' COMMENT '字典类型',
  status CHAR(1) DEFAULT '1' COMMENT '状态',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (dict_id),
  UNIQUE KEY uk_sys_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_data (
  dict_code BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  dict_sort INT DEFAULT 0 COMMENT '字典排序',
  dict_label VARCHAR(100) DEFAULT '' COMMENT '字典标签',
  dict_value VARCHAR(100) DEFAULT '' COMMENT '字典键值',
  dict_type VARCHAR(100) DEFAULT '' COMMENT '字典类型',
  css_class VARCHAR(100) DEFAULT NULL COMMENT '样式属性',
  list_class VARCHAR(100) DEFAULT NULL COMMENT '表格回显样式',
  is_default CHAR(1) DEFAULT 'N' COMMENT '是否默认',
  status CHAR(1) DEFAULT '1' COMMENT '状态',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

CREATE TABLE IF NOT EXISTS sys_login_log (
  info_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  user_name VARCHAR(50) DEFAULT '' COMMENT '用户账号',
  ipaddr VARCHAR(128) DEFAULT '' COMMENT '登录IP',
  login_location VARCHAR(255) DEFAULT '' COMMENT '登录地点',
  browser VARCHAR(50) DEFAULT '' COMMENT '浏览器',
  os VARCHAR(50) DEFAULT '' COMMENT '操作系统',
  status CHAR(1) DEFAULT '1' COMMENT '登录状态',
  msg VARCHAR(255) DEFAULT '' COMMENT '提示消息',
  login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  PRIMARY KEY (info_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统访问记录';

CREATE TABLE IF NOT EXISTS sys_oper_log (
  oper_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  title VARCHAR(50) DEFAULT '' COMMENT '模块标题',
  business_type INT DEFAULT 0 COMMENT '业务类型',
  method VARCHAR(200) DEFAULT '' COMMENT '方法名称',
  request_method VARCHAR(10) DEFAULT '' COMMENT '请求方式',
  oper_name VARCHAR(50) DEFAULT '' COMMENT '操作人员',
  oper_url VARCHAR(255) DEFAULT '' COMMENT '请求URL',
  oper_ip VARCHAR(128) DEFAULT '' COMMENT '主机地址',
  oper_location VARCHAR(255) DEFAULT '' COMMENT '操作地点',
  oper_param TEXT COMMENT '请求参数',
  json_result TEXT COMMENT '返回参数',
  status CHAR(1) DEFAULT '1' COMMENT '操作状态',
  error_msg TEXT COMMENT '错误消息',
  oper_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  cost_time BIGINT DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (oper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

INSERT IGNORE INTO sys_dept(dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, create_by)
VALUES (100, 0, '0', 'Art Design', 1, 'Super', '13800000000', 'admin@artd.local', '1', 'system');

INSERT IGNORE INTO sys_user(user_id, dept_id, user_name, nick_name, email, phonenumber, sex, avatar, password, status, create_by)
VALUES
  (1, 100, 'Super', '超级管理员', 'super@artd.local', '13800000001', '0', '', '123456', '1', 'system'),
  (2, 100, 'Admin', '系统管理员', 'admin@artd.local', '13800000002', '0', '', '123456', '1', 'system'),
  (3, 100, 'User', '普通用户', 'user@artd.local', '13800000003', '1', '', '123456', '1', 'system');

INSERT IGNORE INTO sys_role(role_id, role_name, role_code, role_sort, status, create_by, remark)
VALUES
  (1, '超级管理员', 'R_SUPER', 1, '1', 'system', '拥有全部系统权限'),
  (2, '系统管理员', 'R_ADMIN', 2, '1', 'system', '拥有常用系统管理权限'),
  (3, '普通用户', 'R_USER', 3, '1', 'system', '仅拥有基础访问权限');

INSERT IGNORE INTO sys_user_role(user_id, role_id)
VALUES (1, 1), (2, 2), (3, 3);

INSERT IGNORE INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, route_name, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES
  (100, 'menus.dashboard.title', 0, 1, '/dashboard', '/index/index', 'Dashboard', '0', 'M', '1', '1', '', 'ri:pie-chart-line', 'system'),
  (101, 'menus.dashboard.console', 100, 1, 'console', '/dashboard/console', 'Console', '1', 'C', '1', '1', 'dashboard:console:view', 'ri:home-smile-2-line', 'system'),
  (102, 'menus.dashboard.analysis', 100, 2, 'analysis', '/dashboard/analysis', 'Analysis', '1', 'C', '1', '1', 'dashboard:analysis:view', 'ri:align-item-bottom-line', 'system'),
  (103, 'menus.dashboard.ecommerce', 100, 3, 'ecommerce', '/dashboard/ecommerce', 'Ecommerce', '1', 'C', '1', '1', 'dashboard:ecommerce:view', 'ri:bar-chart-box-line', 'system'),
  (200, 'menus.system.title', 0, 2, '/system', '/index/index', 'System', '0', 'M', '1', '1', '', 'ri:user-3-line', 'system'),
  (201, 'menus.system.user', 200, 1, 'user', '/system/user', 'User', '0', 'C', '1', '1', 'system:user:list', 'ri:user-line', 'system'),
  (202, 'menus.system.role', 200, 2, 'role', '/system/role', 'Role', '0', 'C', '1', '1', 'system:role:list', 'ri:user-settings-line', 'system'),
  (203, 'menus.system.dept', 200, 3, 'dept', '/system/dept', 'Dept', '0', 'C', '1', '1', 'system:dept:list', 'ri:organization-chart', 'system'),
  (204, 'menus.system.userCenter', 200, 4, 'user-center', '/system/user-center', 'UserCenter', '0', 'C', '2', '1', 'system:user:center', 'ri:user-line', 'system'),
  (216, 'menus.system.menu', 200, 5, 'menu', '/system/menu', 'Menus', '0', 'C', '1', '1', 'system:menu:list', 'ri:menu-line', 'system'),
  (205, '新增用户', 201, 1, '', '', '', '1', 'F', '1', '1', 'system:user:add', '', 'system'),
  (206, '编辑用户', 201, 2, '', '', '', '1', 'F', '1', '1', 'system:user:edit', '', 'system'),
  (207, '删除用户', 201, 3, '', '', '', '1', 'F', '1', '1', 'system:user:delete', '', 'system'),
  (208, '重置密码', 201, 4, '', '', '', '1', 'F', '1', '1', 'system:user:resetPwd', '', 'system'),
  (209, '新增角色', 202, 1, '', '', '', '1', 'F', '1', '1', 'system:role:add', '', 'system'),
  (210, '编辑角色', 202, 2, '', '', '', '1', 'F', '1', '1', 'system:role:edit', '', 'system'),
  (211, '删除角色', 202, 3, '', '', '', '1', 'F', '1', '1', 'system:role:delete', '', 'system'),
  (212, '角色授权', 202, 4, '', '', '', '1', 'F', '1', '1', 'system:role:grant', '', 'system'),
  (213, '新增部门', 203, 1, '', '', '', '1', 'F', '1', '1', 'system:dept:add', '', 'system'),
  (214, '编辑部门', 203, 2, '', '', '', '1', 'F', '1', '1', 'system:dept:edit', '', 'system'),
  (215, '删除部门', 203, 3, '', '', '', '1', 'F', '1', '1', 'system:dept:delete', '', 'system'),
  (217, '新增菜单', 216, 1, '', '', '', '1', 'F', '1', '1', 'system:menu:add', '', 'system'),
  (218, '编辑菜单', 216, 2, '', '', '', '1', 'F', '1', '1', 'system:menu:edit', '', 'system'),
  (219, '删除菜单', 216, 3, '', '', '', '1', 'F', '1', '1', 'system:menu:delete', '', 'system'),
  (300, 'menus.examples.title', 0, 3, '/examples', '/index/index', 'Examples', '0', 'M', '1', '1', '', 'ri:sparkling-line', 'system'),
  (301, 'menus.examples.tables', 300, 1, 'tables', '/examples/tables', 'Tables', '0', 'C', '1', '1', 'examples:tables:view', 'ri:table-3', 'system'),
  (302, 'menus.examples.forms', 300, 2, 'forms', '/examples/forms', 'Forms', '0', 'C', '1', '1', 'examples:forms:view', 'ri:table-view', 'system');

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
VALUES
  (1, 100), (1, 101), (1, 102), (1, 103), (1, 200), (1, 201), (1, 202), (1, 203), (1, 204), (1, 205), (1, 206), (1, 207), (1, 208), (1, 209), (1, 210), (1, 211), (1, 212), (1, 213), (1, 214), (1, 215), (1, 216), (1, 217), (1, 218), (1, 219), (1, 300), (1, 301), (1, 302),
  (2, 100), (2, 101), (2, 102), (2, 103), (2, 200), (2, 201), (2, 202), (2, 203), (2, 204), (2, 205), (2, 206), (2, 207), (2, 208), (2, 209), (2, 210), (2, 211), (2, 212), (2, 213), (2, 214), (2, 215), (2, 216), (2, 217), (2, 218), (2, 219), (2, 300), (2, 301), (2, 302),
  (3, 100), (3, 101);
