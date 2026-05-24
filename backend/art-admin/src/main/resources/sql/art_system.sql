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

CREATE TABLE IF NOT EXISTS sys_role_dept (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  dept_id BIGINT NOT NULL COMMENT '部门ID',
  PRIMARY KEY (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和部门关联表';

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

CREATE TABLE IF NOT EXISTS sys_post (
  post_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
  post_name VARCHAR(50) NOT NULL COMMENT '岗位名称',
  post_sort INT DEFAULT 0 COMMENT '显示顺序',
  status CHAR(1) DEFAULT '1' COMMENT '状态：1正常 2停用',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (post_id),
  UNIQUE KEY uk_sys_post_code (post_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位信息表';

CREATE TABLE IF NOT EXISTS sys_user_post (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  post_id BIGINT NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和岗位关联表';

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

CREATE TABLE IF NOT EXISTS sys_config (
  config_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  config_name VARCHAR(100) DEFAULT '' COMMENT '参数名称',
  config_key VARCHAR(100) DEFAULT '' COMMENT '参数键名',
  config_value VARCHAR(500) DEFAULT '' COMMENT '参数键值',
  config_type CHAR(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_sys_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

CREATE TABLE IF NOT EXISTS sys_notice (
  notice_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  notice_title VARCHAR(50) NOT NULL COMMENT '公告标题',
  notice_type CHAR(1) NOT NULL COMMENT '公告类型（1通知 2公告）',
  notice_content LONGTEXT COMMENT '公告内容',
  status CHAR(1) DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

CREATE TABLE IF NOT EXISTS sys_job (
  job_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  job_name VARCHAR(64) DEFAULT '' COMMENT '任务名称',
  job_group VARCHAR(64) DEFAULT 'DEFAULT' COMMENT '任务组名',
  invoke_target VARCHAR(500) NOT NULL COMMENT '调用目标字符串',
  cron_expression VARCHAR(255) DEFAULT '' COMMENT 'cron执行表达式',
  misfire_policy VARCHAR(20) DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  concurrent CHAR(1) DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  status CHAR(1) DEFAULT '1' COMMENT '状态（0正常 1暂停）',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';

CREATE TABLE IF NOT EXISTS sys_job_log (
  job_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  job_name VARCHAR(64) NOT NULL COMMENT '任务名称',
  job_group VARCHAR(64) NOT NULL COMMENT '任务组名',
  invoke_target VARCHAR(500) NOT NULL COMMENT '调用目标字符串',
  job_message VARCHAR(500) DEFAULT '' COMMENT '日志信息',
  status CHAR(1) DEFAULT '1' COMMENT '执行状态（0正常 1失败）',
  exception_info TEXT COMMENT '异常信息',
  elapsed_ms BIGINT DEFAULT 0 COMMENT '执行耗时（毫秒）',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (job_log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度日志表';

INSERT IGNORE INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by)
VALUES
  (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'system'),
  (2, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'true', 'Y', 'system'),
  (3, '账号自助-是否开启用户忘记密码功能', 'sys.account.passwordReset', 'true', 'Y', 'system'),
  (4, '用户登录-账号最大错误次数', 'sys.account.passwordRetryCount', '5', 'Y', 'system'),
  (5, '用户登录-密码锁定时间（默认10分钟）', 'sys.account.passwordLockTime', '10', 'Y', 'system'),
  (6, '用户登录-是否开启登录验证码', 'sys.account.captcha', 'true', 'Y', 'system'),
  (7, '用户登录-每分钟最大登录请求数', 'sys.account.loginRateLimit', '30', 'Y', 'system');

INSERT IGNORE INTO sys_dept(dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, create_by)
VALUES (100, 0, '0', 'Art Design', 1, 'Super', '13800000000', 'admin@artd.local', '1', 'system');

INSERT IGNORE INTO sys_user(user_id, dept_id, user_name, nick_name, email, phonenumber, sex, avatar, password, status, create_by)
VALUES
  (1, 100, 'Super', '超级管理员', 'super@artd.local', '13800000001', '0', '', '$2a$10$y.mNWkep2Gv6TdUfMkOalOMpdSm/9FYo25yXEflcQXm2KmX82bFAq', '1', 'system'),
  (2, 100, 'Admin', '系统管理员', 'admin@artd.local', '13800000002', '0', '', '$2a$10$y.mNWkep2Gv6TdUfMkOalOMpdSm/9FYo25yXEflcQXm2KmX82bFAq', '1', 'system'),
  (3, 100, 'User', '普通用户', 'user@artd.local', '13800000003', '1', '', '$2a$10$y.mNWkep2Gv6TdUfMkOalOMpdSm/9FYo25yXEflcQXm2KmX82bFAq', '1', 'system');

UPDATE sys_user
SET password = '$2a$10$y.mNWkep2Gv6TdUfMkOalOMpdSm/9FYo25yXEflcQXm2KmX82bFAq'
WHERE user_name IN ('Super', 'Admin', 'User')
  AND password = '123456';

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
  (220, '字典管理', 200, 6, 'dict', '/system/dict', 'Dict', '0', 'C', '1', '1', 'system:dict:list', 'ri:book-2-line', 'system'),
  (230, '参数管理', 200, 7, 'config', '/system/config', 'Config', '0', 'C', '1', '1', 'system:config:list', 'ri:settings-3-line', 'system'),
  (240, '通知公告', 200, 8, 'notice', '/system/notice', 'Notice', '0', 'C', '1', '1', 'system:notice:list', 'ri:notification-3-line', 'system'),
  (250, '岗位管理', 200, 9, 'post', '/system/post', 'Post', '0', 'C', '1', '1', 'system:post:list', 'ri:id-card-line', 'system'),
  (260, '文件管理', 200, 10, 'file', '/system/file', 'File', '0', 'C', '1', '1', 'system:file:list', 'ri:file-line', 'system'),
  (205, '新增用户', 201, 1, '', '', '', '1', 'F', '1', '1', 'system:user:add', '', 'system'),
  (206, '编辑用户', 201, 2, '', '', '', '1', 'F', '1', '1', 'system:user:edit', '', 'system'),
  (207, '删除用户', 201, 3, '', '', '', '1', 'F', '1', '1', 'system:user:delete', '', 'system'),
  (208, '重置密码', 201, 4, '', '', '', '1', 'F', '1', '1', 'system:user:resetPwd', '', 'system'),
  (264, '导出用户', 201, 5, '', '', '', '1', 'F', '1', '1', 'system:user:export', '', 'system'),
  (265, '导入用户', 201, 6, '', '', '', '1', 'F', '1', '1', 'system:user:import', '', 'system'),
  (209, '新增角色', 202, 1, '', '', '', '1', 'F', '1', '1', 'system:role:add', '', 'system'),
  (210, '编辑角色', 202, 2, '', '', '', '1', 'F', '1', '1', 'system:role:edit', '', 'system'),
  (211, '删除角色', 202, 3, '', '', '', '1', 'F', '1', '1', 'system:role:delete', '', 'system'),
  (212, '角色授权', 202, 4, '', '', '', '1', 'F', '1', '1', 'system:role:grant', '', 'system'),
  (266, '导出角色', 202, 5, '', '', '', '1', 'F', '1', '1', 'system:role:export', '', 'system'),
  (267, '导入角色', 202, 6, '', '', '', '1', 'F', '1', '1', 'system:role:import', '', 'system'),
  (213, '新增部门', 203, 1, '', '', '', '1', 'F', '1', '1', 'system:dept:add', '', 'system'),
  (214, '编辑部门', 203, 2, '', '', '', '1', 'F', '1', '1', 'system:dept:edit', '', 'system'),
  (215, '删除部门', 203, 3, '', '', '', '1', 'F', '1', '1', 'system:dept:delete', '', 'system'),
  (217, '新增菜单', 216, 1, '', '', '', '1', 'F', '1', '1', 'system:menu:add', '', 'system'),
  (218, '编辑菜单', 216, 2, '', '', '', '1', 'F', '1', '1', 'system:menu:edit', '', 'system'),
  (219, '删除菜单', 216, 3, '', '', '', '1', 'F', '1', '1', 'system:menu:delete', '', 'system'),
  (268, '导出菜单', 216, 4, '', '', '', '1', 'F', '1', '1', 'system:menu:export', '', 'system'),
  (269, '导入菜单', 216, 5, '', '', '', '1', 'F', '1', '1', 'system:menu:import', '', 'system'),
  (221, '新增字典', 220, 1, '', '', '', '1', 'F', '1', '1', 'system:dict:add', '', 'system'),
  (222, '编辑字典', 220, 2, '', '', '', '1', 'F', '1', '1', 'system:dict:edit', '', 'system'),
  (223, '删除字典', 220, 3, '', '', '', '1', 'F', '1', '1', 'system:dict:delete', '', 'system'),
  (270, '导出字典', 220, 4, '', '', '', '1', 'F', '1', '1', 'system:dict:export', '', 'system'),
  (271, '导入字典', 220, 5, '', '', '', '1', 'F', '1', '1', 'system:dict:import', '', 'system'),
  (231, '新增参数', 230, 1, '', '', '', '1', 'F', '1', '1', 'system:config:add', '', 'system'),
  (232, '编辑参数', 230, 2, '', '', '', '1', 'F', '1', '1', 'system:config:edit', '', 'system'),
  (233, '删除参数', 230, 3, '', '', '', '1', 'F', '1', '1', 'system:config:delete', '', 'system'),
  (272, '导出参数', 230, 4, '', '', '', '1', 'F', '1', '1', 'system:config:export', '', 'system'),
  (273, '导入参数', 230, 5, '', '', '', '1', 'F', '1', '1', 'system:config:import', '', 'system'),
  (241, '新增通知', 240, 1, '', '', '', '1', 'F', '1', '1', 'system:notice:add', '', 'system'),
  (242, '编辑通知', 240, 2, '', '', '', '1', 'F', '1', '1', 'system:notice:edit', '', 'system'),
  (243, '删除通知', 240, 3, '', '', '', '1', 'F', '1', '1', 'system:notice:delete', '', 'system'),
  (251, '新增岗位', 250, 1, '', '', '', '1', 'F', '1', '1', 'system:post:add', '', 'system'),
  (252, '编辑岗位', 250, 2, '', '', '', '1', 'F', '1', '1', 'system:post:edit', '', 'system'),
  (253, '删除岗位', 250, 3, '', '', '', '1', 'F', '1', '1', 'system:post:delete', '', 'system'),
  (261, '上传文件', 260, 1, '', '', '', '1', 'F', '1', '1', 'system:file:upload', '', 'system'),
  (262, '下载文件', 260, 2, '', '', '', '1', 'F', '1', '1', 'system:file:download', '', 'system'),
  (263, '删除文件', 260, 3, '', '', '', '1', 'F', '1', '1', 'system:file:delete', '', 'system'),
  (400, '系统监控', 0, 4, '/monitor', '/index/index', 'Monitor', '0', 'M', '1', '1', '', 'ri:dashboard-3-line', 'system'),
  (401, '服务监控', 400, 1, 'server', '/safeguard/server', 'Server', '0', 'C', '1', '1', 'monitor:server:view', 'ri:server-line', 'system'),
  (402, '在线用户', 400, 2, 'online', '/monitor/online', 'Online', '0', 'C', '1', '1', 'monitor:online:list', 'ri:user-shared-line', 'system'),
  (403, '缓存监控', 400, 3, 'cache', '/monitor/cache', 'Cache', '0', 'C', '1', '1', 'monitor:cache:view', 'ri:database-2-line', 'system'),
  (404, '定时任务', 400, 4, 'job', '/monitor/job', 'Job', '0', 'C', '1', '1', 'monitor:job:list', 'ri:timer-line', 'system'),
  (405, '数据库监控', 400, 5, 'database', '/monitor/database', 'Database', '0', 'C', '1', '1', 'monitor:database:view', 'ri:database-line', 'system'),
  (406, '登录日志', 400, 6, 'logininfor', '/monitor/logininfor', 'Logininfor', '0', 'C', '1', '1', 'monitor:logininfor:list', 'ri:login-box-line', 'system'),
  (407, '操作日志', 400, 7, 'operlog', '/monitor/operlog', 'Operlog', '0', 'C', '1', '1', 'monitor:operlog:list', 'ri:file-list-3-line', 'system'),
  (408, '强退用户', 402, 1, '', '', '', '1', 'F', '1', '1', 'monitor:online:forceLogout', '', 'system'),
  (409, '清理缓存', 403, 1, '', '', '', '1', 'F', '1', '1', 'monitor:cache:edit', '', 'system'),
  (410, '新增任务', 404, 1, '', '', '', '1', 'F', '1', '1', 'monitor:job:add', '', 'system'),
  (411, '编辑任务', 404, 2, '', '', '', '1', 'F', '1', '1', 'monitor:job:edit', '', 'system'),
  (412, '删除任务', 404, 3, '', '', '', '1', 'F', '1', '1', 'monitor:job:delete', '', 'system'),
  (413, '导出任务日志', 404, 4, '', '', '', '1', 'F', '1', '1', 'monitor:job:export', '', 'system'),
  (414, '登录日志清空', 406, 1, '', '', '', '1', 'F', '1', '1', 'monitor:logininfor:delete', '', 'system'),
  (415, '登录日志导出', 406, 2, '', '', '', '1', 'F', '1', '1', 'monitor:logininfor:export', '', 'system'),
  (416, '操作日志清空', 407, 1, '', '', '', '1', 'F', '1', '1', 'monitor:operlog:delete', '', 'system'),
  (417, '操作日志导出', 407, 2, '', '', '', '1', 'F', '1', '1', 'monitor:operlog:export', '', 'system'),
  (300, 'menus.examples.title', 0, 3, '/examples', '/index/index', 'Examples', '0', 'M', '1', '1', '', 'ri:sparkling-line', 'system'),
  (301, 'menus.examples.tables', 300, 1, 'tables', '/examples/tables', 'Tables', '0', 'C', '1', '1', 'examples:tables:view', 'ri:table-3', 'system'),
  (302, 'menus.examples.forms', 300, 2, 'forms', '/examples/forms', 'Forms', '0', 'C', '1', '1', 'examples:forms:view', 'ri:table-view', 'system');

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
VALUES
  (1, 100), (1, 101), (1, 102), (1, 103), (1, 200), (1, 201), (1, 202), (1, 203), (1, 204), (1, 205), (1, 206), (1, 207), (1, 208), (1, 209), (1, 210), (1, 211), (1, 212), (1, 213), (1, 214), (1, 215), (1, 216), (1, 217), (1, 218), (1, 219), (1, 220), (1, 221), (1, 222), (1, 223), (1, 230), (1, 231), (1, 232), (1, 233), (1, 240), (1, 241), (1, 242), (1, 243), (1, 250), (1, 251), (1, 252), (1, 253), (1, 260), (1, 261), (1, 262), (1, 263), (1, 264), (1, 265), (1, 266), (1, 267), (1, 268), (1, 269), (1, 270), (1, 271), (1, 272), (1, 273), (1, 300), (1, 301), (1, 302), (1, 400), (1, 401), (1, 402), (1, 403), (1, 404), (1, 405), (1, 406), (1, 407), (1, 408), (1, 409), (1, 410), (1, 411), (1, 412), (1, 413), (1, 414), (1, 415), (1, 416), (1, 417),
  (2, 100), (2, 101), (2, 102), (2, 103), (2, 200), (2, 201), (2, 202), (2, 203), (2, 204), (2, 205), (2, 206), (2, 207), (2, 208), (2, 209), (2, 210), (2, 211), (2, 212), (2, 213), (2, 214), (2, 215), (2, 216), (2, 217), (2, 218), (2, 219), (2, 220), (2, 221), (2, 222), (2, 223), (2, 230), (2, 231), (2, 232), (2, 233), (2, 240), (2, 241), (2, 242), (2, 243), (2, 250), (2, 251), (2, 252), (2, 253), (2, 260), (2, 261), (2, 262), (2, 263), (2, 264), (2, 265), (2, 266), (2, 267), (2, 268), (2, 269), (2, 270), (2, 271), (2, 272), (2, 273), (2, 300), (2, 301), (2, 302), (2, 400), (2, 401), (2, 402), (2, 403), (2, 404), (2, 405), (2, 406), (2, 407), (2, 408), (2, 409), (2, 410), (2, 411), (2, 412), (2, 413), (2, 414), (2, 415), (2, 416), (2, 417),
  (3, 100), (3, 101);
