# 接口设计文档 · 员工信息管理系统

> 状态：**规划中（M2 起逐步实现）** —— 每实现一个接口，把「状态」改成 ✅ 并补详细参数
> 服务地址（本地）：`http://localhost:8080`

---

## 1. 全局约定

| 项 | 约定 |
|---|---|
| 路径前缀 | `/api`（示例：`/api/emps`） |
| 请求体格式 | JSON（`Content-Type: application/json`） |
| 认证方式 | 请求头 `Authorization: Bearer <token>`（登录接口除外） |
| 统一响应结构 | `{ "code": 200, "msg": "成功", "data": ... }` |
| 状态码 | 200 成功 / 400 参数错误 / 401 未登录或令牌失效 / 403 无权限 / 500 服务器异常 |

**统一响应示例**
```json
{ "code": 200, "msg": "成功", "data": { "id": 1, "name": "技术部" } }
```

**分页响应示例**
```json
{ "code": 200, "msg": "成功", "data": { "total": 15, "rows": [ /* 数据列表 */ ] } }
```

---

## 2. 接口清单

### 2.1 登录认证

| 方法 | 路径 | 说明 | 状态 |
|---|---|---|---|
| POST | `/api/login` | 登录，校验账号密码，返回令牌 | ⏳ 待开发 |
| POST | `/api/logout` | 退出登录（清除令牌） | ⏳ 待开发 |
| GET | `/api/me` | 获取当前登录用户信息 | ⏳ 待开发 |

**POST /api/login**
```json
// 请求
{ "username": "admin", "password": "123456" }

// 响应
{ "code": 200, "msg": "成功", "data": { "token": "eyJhbGciOi...", "username": "admin" } }
```

### 2.2 部门管理

| 方法 | 路径 | 说明 | 状态 |
|---|---|---|---|
| GET | `/api/depts` | 查询全部部门 | ⏳ 待开发 |
| GET | `/api/depts/{id}` | 根据 id 查询部门 | ⏳ 待开发 |
| POST | `/api/depts` | 新增部门 | ⏳ 待开发 |
| PUT | `/api/depts/{id}` | 修改部门 | ⏳ 待开发 |
| DELETE | `/api/depts/{id}` | 删除部门（有员工时返回错误） | ⏳ 待开发 |

**新增/修改部门请求体**
```json
{ "name": "财务部" }
```

### 2.3 员工管理

| 方法 | 路径 | 说明 | 状态 |
|---|---|---|---|
| GET | `/api/emps` | 员工分页 + 多条件查询 | ⏳ 待开发 |
| GET | `/api/emps/{id}` | 根据 id 查询员工（回显） | ⏳ 待开发 |
| POST | `/api/emps` | 新增员工 | ⏳ 待开发 |
| PUT | `/api/emps/{id}` | 修改员工 | ⏳ 待开发 |
| DELETE | `/api/emps/{id}` | 删除单个员工 | ⏳ 待开发 |
| DELETE | `/api/emps` | 批量删除（`?ids=1,2,3`） | ⏳ 待开发 |

**GET /api/emps 查询参数**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 10 |
| name | string | 否 | 姓名模糊查询 |
| deptId | long | 否 | 部门 id |
| entryDateBegin | date | 否 | 入职日期起（yyyy-MM-dd） |
| entryDateEnd | date | 否 | 入职日期止（yyyy-MM-dd） |

**新增/修改员工请求体**
```json
{
  "username": "zhangsan",
  "name": "张三",
  "deptId": 1,
  "job": "Java开发",
  "entryDate": "2024-03-01",
  "image": "/upload/1.jpg"
}
```

### 2.4 文件上传

| 方法 | 路径 | 说明 | 状态 |
|---|---|---|---|
| POST | `/api/upload` | 上传图片（返回可访问的 URL） | ⏳ 待开发 |

> 请求：`multipart/form-data`，字段名 `file`
> 响应：`{ "code": 200, "msg": "成功", "data": "http://localhost:8080/images/xxx.jpg" }`

### 2.5 操作日志

| 方法 | 路径 | 说明 | 状态 |
|---|---|---|---|
| GET | `/api/logs` | 操作日志分页查询 | ⏳ 待开发 |

---

## 3. 开发时的补充规则

1. **接口返回统一用 `Result` 封装类**（`code` / `msg` / `data`），错误统一由 `GlobalExceptionHandler` 处理；
2. **所有写操作入参做校验**（`@Validated` + `@NotBlank` / `@Size` / `@NotNull`），校验失败返回 400；
3. **DELETE 接口只接受 id 路径参数或 ids 查询参数**，不用请求体传参；
4. 每实现完一个接口，在 Apifox/Postman 里保存请求示例，并回来把本文档「状态」改成 ✅。

---

## 4. 里程碑对应关系

| 里程碑 | 覆盖接口 |
|---|---|
| M2（MyBatis 阶段） | 部门/员工 CRUD（不含认证，Main 方法或单元测试验证） |
| M3（SpringBoot 阶段） | 2.2 + 2.3 全部接口（REST 风格 + 分页 + 统一响应） |
| M4（认证与增强） | 2.1 登录认证 + 2.4 文件上传 + 2.5 操作日志 |
| M5（前端联调） | 前端页面调用 2.1 / 2.2 / 2.3 |
