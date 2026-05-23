# 若依靠拢任务清单

## 状态说明
- ⬜ 待开发
- ⏳ 开发中
- ✅ 已完成

---

## 核心功能补齐

### 1. 日志查询接口
- [ ] 登录日志查询 Controller（分页/列表）
- [ ] 操作日志查询 Controller（分页/列表）
- 已有数据表和 Service 记录能力，缺查询 API

### 2. 岗位管理
- [ ] sys_post 数据表
- [ ] sys_user_post 关联表
- [ ] SysPost 实体、Mapper、Service、Controller
- [ ] 用户岗位关联逻辑

### 3. 参数配置
- [ ] sys_config 数据表
- [ ] SysConfig 实体、Mapper、Service、Controller
- [ ] 参数缓存机制（Redis）

### 4. 文件上传
- [ ] 文件上传配置类
- [ ] 文件上传 Service（本地/OSS）
- [ ] 文件上传 Controller
- [ ] 前端文件上传组件

---

## 扩展功能

### 5. 通知公告
- [ ] sys_notice 数据表
- [ ] SysNotice 实体、Mapper、Service、Controller
- [ ] 前端通知页面

### 6. 系统监控
- [ ] 服务监控（CPU/内存/磁盘/JVM）
- [ ] 在线用户管理
- [ ] 缓存监控

### 7. 定时任务
- [ ] Quartz/XXL-Job 集成
- [ ] sys_job / sys_job_log 数据表
- [ ] 定时任务管理 CRUD

### 8. 代码生成
- [ ] 代码生成模板
- [ ] 表结构解析
- [ ] 前后端代码自动生成

---

## 前端补齐

### 对应后端功能的前端页面
- [ ] 日志查询页面（登录日志 + 操作日志）
- [ ] 岗位管理页面
- [ ] 参数配置页面
- [ ] 文件上传组件
- [ ] 通知公告页面
- [ ] 系统监控页面
