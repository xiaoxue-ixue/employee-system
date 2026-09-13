-- ============================================================
-- 员工信息管理系统 · 建表脚本
-- 数据库：employee_system   引擎：InnoDB   字符集：utf8mb4
-- 说明：表由 Navicat 图形化创建，本文件为等价 DDL（版本管理/部署用）
-- ============================================================

CREATE DATABASE IF NOT EXISTS employee_system DEFAULT CHARSET utf8mb4;
USE employee_system;

-- 删除顺序：先删有外键的子表，再删父表
DROP TABLE IF EXISTS emp;
DROP TABLE IF EXISTS dept;
DROP TABLE IF EXISTS sys_user;

-- ① 部门表
CREATE TABLE dept (
                      id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                      name        VARCHAR(50)  NOT NULL                COMMENT '部门名称（唯一）',
                      create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      PRIMARY KEY (id),
                      UNIQUE KEY uk_dept_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='部门表';

-- ② 员工表（dept_id 外键 → dept.id，删除策略 RESTRICT 防误删）
CREATE TABLE emp (
                     id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                     username    VARCHAR(20)  NOT NULL                COMMENT '登录名',
                     name        VARCHAR(20)  NOT NULL                COMMENT '姓名',
                     dept_id     BIGINT       NULL                    COMMENT '所属部门',
                     image       VARCHAR(200) NULL                    COMMENT '头像路径',
                     job         VARCHAR(20)  NULL                    COMMENT '职位',
                     entry_date  DATE         NULL                    COMMENT '入职日期',
                     create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                     update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                     PRIMARY KEY (id),
                     UNIQUE KEY uk_emp_username (username),
                     KEY idx_dept_id (dept_id),
                     CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES dept (id)
                         ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='员工表';

-- ③ 用户表
CREATE TABLE sys_user (
                          id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          username    VARCHAR(20)  NOT NULL                COMMENT '账号',
                          password    VARCHAR(100) NOT NULL                COMMENT '密码（加密后存储）',
                          create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';