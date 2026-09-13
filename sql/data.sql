-- ============================================================
-- 员工信息管理系统 · 测试数据
-- 执行顺序：dept → emp → sys_user（外键依赖，顺序不能错）
-- ============================================================

USE employee_system;

-- ① 部门 3 条（先插）
INSERT INTO dept (name) VALUES ('技术部'), ('人事部'), ('市场部');

-- ② 员工 15 条（沈六故意不设部门，用于练习 LEFT JOIN / IS NULL）
INSERT INTO emp (username, name, dept_id, job, entry_date) VALUES
                                                               ('zhangsan', '张三', 1, 'Java开发',   '2024-03-01'),
                                                               ('lisi',     '李四', 1, '前端开发',   '2024-05-10'),
                                                               ('wangwu',   '王五', 1, '测试工程师', '2023-11-20'),
                                                               ('zhaoliu',  '赵六', 2, 'HR专员',     '2024-01-15'),
                                                               ('sunqi',    '孙七', 2, '招聘主管',   '2022-08-01'),
                                                               ('zhouba',   '周八', 2, '薪酬专员',   '2024-06-01'),
                                                               ('wujiu',    '吴九', 3, '市场专员',   '2024-02-20'),
                                                               ('zhengshi', '郑十', 3, '品牌经理',   '2023-03-05'),
                                                               ('liuyi',    '刘一', 3, '渠道经理',   '2023-09-12'),
                                                               ('chener',   '陈二', 1, 'Java开发',   '2025-01-06'),
                                                               ('chusan',   '褚三', 1, '运维工程师', '2024-07-15'),
                                                               ('weisi',    '卫四', 2, '培训专员',   '2024-04-08'),
                                                               ('jiangwu',  '蒋五', 3, '商务拓展',   '2024-10-21'),
                                                               ('shenliu',  '沈六', NULL, '实习生',  '2025-06-01'),
                                                               ('hanqi',    '韩七', 1, '架构师',     '2021-05-30');

-- ③ 用户 2 条
INSERT INTO sys_user (username, password) VALUES ('admin', '123456'), ('zhangsan', '123456');

-- ============================================================
-- 附：重置数据（需要重跑时先执行这段，注意先删子表）
-- DELETE FROM emp;
-- DELETE FROM dept;
-- DELETE FROM sys_user;
-- ALTER TABLE emp AUTO_INCREMENT = 1;
-- ALTER TABLE dept AUTO_INCREMENT = 1;
-- ALTER TABLE sys_user AUTO_INCREMENT = 1;
-- ============================================================