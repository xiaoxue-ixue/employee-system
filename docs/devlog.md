# 开发日志 · 员工信息管理系统

> 记录每天做了什么、踩了什么坑、怎么解决的。
> 这份日志是我面试时的"项目故事库"——别人背话术，我讲真事。

## 2026-09-13 · 数据库设计（M1 完成）

**做了什么**
- 设计并创建三张表：`dept`（部门）、`emp`（员工）、`sys_user`（用户）
- 关系：部门 1 : N 员工（`emp.dept_id` 外键关联 `dept.id`）
- 建索引：`uk_dept_name`（部门名唯一）、`uk_emp_username`（登录名唯一）、`uk_user_username`、`idx_dept_id`（外键字段）
- 外键策略：`ON DELETE RESTRICT`（有员工的部门不许删）、`ON UPDATE CASCADE`
- 公共字段：`create_time`（DEFAULT CURRENT_TIMESTAMP）、`update_time`（DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP）
- 插入测试数据：部门 3 条、员工 15 条、用户 2 条

**踩的坑与解决**

| # | 报错 | 原因 | 解决 |
|---|---|---|---|
| 1 | `1064 SQL syntax error` | `USE employee_system` 漏了分号，且编辑器只选中了半截语句执行 | 补分号；运行前确认选区完整（全选或光标放语句内） |
| 2 | `1062 Duplicate entry '技术部' for key 'dept.uk_dept_name'` | 重复执行 INSERT，唯一索引生效 | 确认数据已存在即可；重跑前先清空或用 INSERT IGNORE |
| 3 | `3780 ... foreign key constraint are incompatible` | `emp.dept_id` 与 `dept.id` 的**符号属性不一致**（一边勾了无符号） | 两边统一为有符号 BIGINT（外键要求类型完全一致） |

**关键验证（面试可讲）**
- 执行 `DELETE FROM dept WHERE id = 1;` → 报 `1451 Cannot delete or update a parent row`，
  说明 `ON DELETE RESTRICT` 生效，**数据库层面防止了误删有员工的部门**
- `SELECT d.name, COUNT(e.id) FROM dept d LEFT JOIN emp e ON d.id = e.dept_id GROUP BY d.id, d.name;`
  → 技术部 6、人事部 4、市场部 4（用 `COUNT(e.id)` 而非 `COUNT(*)`，才能正确显示没员工的部门为 0）

**学到什么**
- 外键不只是"字段关联"，还能在数据库层做数据一致性保护
- UNIQUE 索引、外键约束的报错信息本身就是在"告诉你设计起作用了"
- 表设计要考虑**删除场景**（RESTRICT / CASCADE / SET NULL 怎么选）

**明天计划**
- 开始 MyBatis：用纯 Maven 项目手写部门/员工 CRUD（阶段一 backend-mybatis）