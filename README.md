# 员工信息管理系统（Employee Management System）

> **Java 后端实战项目 · 全程手写** —— 从需求分析、数据库设计到接口开发、部署，完整走一遍企业级开发流程。
> 目标：掌握 `SpringBoot + MyBatis + MySQL` 企业级开发，形成可写进简历、可当场讲解的真实项目。

---

## 📖 项目简介

面向中小企业的员工与部门信息管理平台，解决"员工信息分散在 Excel、部门人员统计困难、修改无记录"的问题。

**核心功能**
- 部门管理：新增 / 修改 / 删除 / 查询（部门下有员工时禁止删除）
- 员工管理：新增 / 修改 / 删除 / 分页 + 多条件查询（姓名、部门、入职时间）
- 登录认证：账号密码登录，令牌校验，未登录不可访问业务接口
- 操作日志：自动记录"谁在什么时间做了什么操作"

---

## 🛠 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Java 17 · SpringBoot 3 · MyBatis · MySQL 8 · HikariCP |
| 构建/工具 | Maven · Git · IDEA · Apifox / Postman |
| 前端（极简） | Vue 3 · Element Plus（登录页 + 员工列表页） |

---

## 📁 目录结构

```
employee-system/
├── docs/
│   ├── requirements.md    # 需求分析
│   ├── database.md        # ER 图 + 表结构设计（含设计决策）
│   ├── api.md             # 接口设计文档
│   └── devlog.md          # 开发日志（踩坑与解决记录）
├── sql/
│   ├── schema.sql         # 建表脚本
│   └── data.sql           # 测试数据
├── src/main/java/com/xiaoxue/employee/    # 后端源码（当前仅 IDEA 生成的 Main.java 脚手架）
└── pom.xml                # Maven 配置（依赖待补齐）
```

> 说明：文档早期规划过 `backend-mybatis/`、`backend/`、`frontend/` 三个独立模块，
> 实际采用**单模块扁平结构**，各阶段在原项目内逐步演进（M2 纯 MyBatis → M3 原地升级 SpringBoot），
> 避免中途搬迁目录、破坏 Git 历史。

---

## 📊 开发进度

| 里程碑 | 内容 | 状态 |
|---|---|---|
| M1 | 需求分析 + 数据库设计（三表 + 索引 + 外键约束验证） | ✅ 已完成（2026-09-13） |
| M2 | 纯 MyBatis 实现部门/员工 CRUD | ⏳ 待开始 |
| M3 | 迁移 SpringBoot，提供 REST 接口 + 分页 + 统一响应 | ⏳ 待开始 |
| M4 | 登录认证（令牌 + 拦截器）+ AOP 操作日志 + 文件上传 | ⏳ 待开始 |
| M5 | 极简前端联调 + 部署 + README 完善 | ⏳ 待开始 |

---

## 🔑 核心设计要点

1. **部门 1 : N 员工**：`emp.dept_id` 外键关联 `dept.id`
2. **删除策略 RESTRICT**：有员工的部门禁止删除，从数据库层防止误删（实测报错 `1451`）
3. **唯一约束**：部门名、员工登录名、账号均加 UNIQUE 索引
4. **公共字段由数据库维护**：`create_time` 默认当前时间，`update_time` 更新时自动刷新
5. **统计口径**：用 `COUNT(e.id)` 而非 `COUNT(*)`，保证无员工的部门正确显示为 0

> 详细设计、ER 图与设计决策见 [`docs/database.md`](docs/database.md)

---

## 🔌 接口文档

全部接口清单、请求响应示例、认证约定见 [`docs/api.md`](docs/api.md)

---

## 🚀 本地运行（随开发进度补充）

> 当前阶段：M1 完成（数据库已就绪），后端代码待开发。

```bash
# 1. 创建数据库并导入结构与数据
mysql -u root -p < sql/schema.sql
mysql -u root -p employee_system < sql/data.sql

# 2. 后端启动（M3 完成后补充）
# cd backend && mvn spring-boot:run

# 3. 前端启动（M5 完成后补充）
# cd frontend && npm install && npm run dev
```

---

## 📝 开发日志

每日的开发记录、踩坑与解决过程见 [`docs/devlog.md`](docs/devlog.md)。
已记录的典型问题：`1064`（漏分号 / 半截选区执行）、`1062`（唯一索引重复插入）、`3780`（外键两边类型不一致）、`1451`（RESTRICT 生效验证）。

---

## 👤 作者

**xiaoxue-ixue** · 软件工程 · 2027 届 · 昆明
本项目为个人学习项目，代码全程手写，欢迎交流。
