# 若依靠拢任务清单

## 状态说明
- ⬜ 待开发
- ⏳ 开发中
- ✅ 已完成

---

## 阶段一：核心功能补齐（必须）

### 1. 日志查询接口 ✅
- [x] SysLoginLogController（登录日志分页查询/列表）
- [x] SysOperLogController（操作日志分页查询/列表）

### 2. 岗位管理 ✅
- [x] sys_post 表 + 初始数据
- [x] sys_user_post 关联表
- [x] SysPost 实体、Mapper、Service
- [x] SysPostController（CRUD + 分页 + 树形列表）
- [x] SysUserService 关联岗位逻辑（用户保存时同步岗位关系）

### 3. 参数配置 ✅
- [x] sys_config 表 + 初始数据
- [x] SysConfig 实体、Mapper、Service（含缓存）
- [x] SysConfigController（CRUD + 按参数名获取）

### 4. 文件上传 ✅
- [x] 文件上传配置类（上传路径/大小限制/允许类型）
- [x] 文件上传服务（本地存储）
- [x] SysFileController（上传/下载/删除）

---

## 阶段二：常用功能

### 5. 通知公告
- [ ] sys_notice 表 + 初始数据
- [ ] SysNotice 实体、Mapper、Service
- [ ] SysNoticeController（CRUD + 分页）

### 6. 系统监控
- [ ] 在线用户管理（Sa-Token 内置会话查询）
- [ ] 缓存监控（Redis 信息查看）
- [ ] 服务监控（CPU/内存/磁盘/Java VM）

---

## 阶段三：开发提效

### 7. Excel 导入导出
- [ ] 引入 EasyExcel 依赖
- [ ] 通用 Excel 工具类
- [ ] 用户/角色列表导出支持

### 8. 代码生成
- [ ] 表结构查询
- [ ] 后端代码模板（Entity/Mapper/Service/Controller）
- [ ] 前端代码模板（Vue 页面 + API）

### 9. 定时任务
- [ ] Quartz 依赖引入
- [ ] sys_job / sys_job_log 表
- [ ] 任务 CRUD + 执行/暂停/恢复/删除
- [ ] 定时任务日志查询

---

## 前端对应（按需配合后端进度）

### 日志查询页面
- [ ] 登录日志列表页
- [ ] 操作日志列表页

### 岗位管理页面
- [ ] 岗位列表/新增/编辑

### 参数配置页面
- [ ] 参数列表/新增/编辑

### 文件上传组件
- [ ] 图片/文件上传组件封装

### 其他
- [ ] 通知公告页面
- [ ] 系统监控页面
