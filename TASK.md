# 若依靠拢任务清单

## 阶段一：核心补齐（高优先级）

### 1. 日志查询接口 ✅
- [x] 登录日志查询 Controller（分页/列表）
- [x] 操作日志查询 Controller（分页/列表）

### 2. 岗位管理 ✅
- [x] sys_post 表 + sys_user_post 关联表
- [x] SysPost 实体 / Mapper / Service / Controller

### 3. 系统参数配置 ✅
- [x] sys_config 表 + 初始数据
- [x] SysConfig 实体 / Mapper / Service / Controller
- [x] Redis 缓存（30分钟TTL）

### 4. 文件上传 ✅
- [x] 文件上传配置类 FileConfig
- [x] 本地文件上传 Service + Controller

---

## 阶段二：常用功能

### 5. 通知公告 ✅
- [x] sys_notice 表
- [x] SysNotice 实体 / Mapper / Service / Controller

### 6. 系统监控
- [ ] 在线用户查询
- [ ] 服务器监控（CPU/内存/磁盘/JVM）
- [ ] 缓存监控（Redis）

---

## 阶段三：开发提效

### 7. Excel 导入导出
- [ ] 引入 EasyExcel
- [ ] 通用 Excel 工具类

### 8. 代码生成
- [ ] 代码生成模板

### 9. 定时任务
- [ ] Quartz 集成
- [ ] sys_job / sys_job_log 表

---

## 前端对应页面
- [ ] 日志查询页面
- [ ] 岗位管理页面
- [ ] 参数配置页面
- [ ] 文件上传组件
- [ ] 系统监控页面
