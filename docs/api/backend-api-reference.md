<!--
用途: 后端 API 的统一接口参考文档（供前端联调、测试脚本和 AI 生成调用时使用）。
使用方法:
1) 新增/修改接口后，同步更新本文件的路径、参数、返回结构与示例。
2) 前端或脚本调用时，按 ApiResponse<T> 解析并处理 code != 200 的业务失败。
-->

# 后端接口参考（前端联调 + AI 调用）

本文档基于当前 `src/main/java/com/example/publicordercasemanagementsystem` 控制器与 DTO 实现整理。

## 1. 基础约定

- 基础路径：`/api`
- 内容类型：`application/json`
- 受保护接口请求头：

```http
Authorization: Bearer <access_token>
```

- 统一响应结构（`ApiResponse<T>`）：

```json
{
  "code": 200,
  "message": "OK",
  "data": {}
}
```

- 参数校验失败的统一返回：

```json
{
  "code": 400,
  "message": "Validation failed",
  "data": null
}
```

## 2. 认证模型

- 公共接口：
  - `/api/auth/**`
  - `/api/dashscope/**`
- 其余接口都需要 JWT（见 `SecurityConfig`）。
- JWT 主体（`sub`）为 `userId`；`name` 是展示字段，不用于身份判定。

## 3. 常见错误码

- `400`：参数校验失败或业务规则不满足
- `401`：未认证、令牌无效、用户不存在
- `404`：资源不存在（如 case id）
- `429`：登录锁定
- `5xx`：系统内部错误或上游错误

---

## 4. 认证接口（`/api/auth`）

### POST `/api/auth/login`
- 鉴权：无需登录
- 请求体（`LoginRequest`）：
  - `name`（字符串，必填）
  - `password`（字符串，必填）
- 响应：`ApiResponse<AuthResponse>`
- 成功提示：`Login successful`

### POST `/api/auth/refresh`
- 鉴权：需要 `Authorization` 请求头
- 请求体（`RefreshRequest`）：
  - `refreshToken`（字符串，必填）
- 响应：`ApiResponse<AuthResponse>`
- 成功提示：`Token refreshed successfully`
- 约束：`Authorization` 中 access token 与 `refreshToken` 必须属于同一 `userId`，否则返回 `401`。

### POST `/api/auth/logout`
- 鉴权：需要 `Authorization` 请求头
- 请求体（`LogoutRequest`）：
  - `refreshToken`（字符串，必填）
- 响应：`ApiResponse<Void>`
- 成功提示：`Logout successful`
- 行为：先校验 `refreshToken` 属于当前 `Authorization` 用户，再撤销该用户全部 `refreshToken`（账号级退出）。

### POST `/api/auth/register`
- 鉴权：无需登录
- 请求体（`RegisterRequest`）：
  - `name`（字符串，必填）
  - `password`（字符串，必填）
  - `confirmPassword`（字符串，必填）
  - `role`（字符串，可选）
  - `roleName`（字符串，可选）
  - `department`（字符串，可选）
  - `departmentId`（数字，可选）
- 响应：`ApiResponse<UserInfo>`
- 成功提示：`Register successful`

---

## 5. 用户接口（`/api/users`）

### GET `/api/users`
- 鉴权：需要登录
- 查询参数（全部可选）：
  - `name`（字符串）
  - `role`（字符串）
  - `department`（字符串）
  - `departmentId`（数字）
  - `isActive`（布尔）
  - `page`（数字）
  - `size`（数字）
- 响应：`ApiResponse<PageResult<UserListItem>>`

### GET `/api/users/me`
- 鉴权：需要登录
- 查询参数：无
- 响应：`ApiResponse<UserInfo>`
- 说明：当前用户身份来自 JWT 主体（`authentication.getName()` -> `userId` 字符串）。

### PUT `/api/users/{id}/name`
- 鉴权：需要登录（所有角色可修改本人，`admin` 可修改任意用户）
- 路径参数：`id`（数字）
- 请求体（`UpdateUserNameRequest`）：
  - `name`（字符串，必填）
- 响应：`ApiResponse<UserInfo>`
- 成功提示：`User name updated successfully`

### PUT `/api/users/{id}/password`
- 鉴权：需要登录（所有角色可修改本人，`admin` 可修改任意用户）
- 路径参数：`id`（数字）
- 请求体（`ChangePasswordRequest`）：
  - `password`（字符串，必填）
  - `confirmPassword`（字符串，必填）
- 响应：`ApiResponse<Void>`
- 成功提示：`User password updated successfully`

### PUT `/api/users/{id}/role`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`id`（数字）
- 请求体（`UpdateUserRoleRequest`）：
  - `role`（字符串，必填，角色编码）
- 响应：`ApiResponse<UserInfo>`
- 成功提示：`User role updated successfully`

### PUT `/api/users/{id}/status`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`id`（数字）
- 请求体（`UpdateUserStatusRequest`）：
  - `isActive`（布尔，必填）
- 响应：`ApiResponse<UserInfo>`
- 成功提示：`User status updated successfully`

### DELETE `/api/users/{id}`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`id`（数字）
- 请求体：无
- 响应：`ApiResponse<Void>`
- 成功提示：`User deleted successfully`

### DELETE `/api/users/batch`
- 鉴权：需要登录（仅 `admin`）
- 请求体（`BatchDeleteRequest`）：
  - `ids`（数组，必填）
- 响应：`ApiResponse<Void>`
- 成功提示：`Users deleted successfully`

---

## 5.1 部门接口（`/api/departments`）

### GET `/api/departments`
- 鉴权：需要登录
- 查询参数（全部可选）：`name`、`isActive`、`parentId`
- 响应：`ApiResponse<List<DepartmentItem>>`

### GET `/api/departments/{id}`
- 鉴权：需要登录
- 路径参数：`id`（数字）
- 响应：`ApiResponse<DepartmentItem>`

### POST `/api/departments`
- 鉴权：需要登录（仅 `admin`）
- 请求体（`CreateDepartmentRequest`）：`name`（必填）、`parentId`（可选）
- 响应：`ApiResponse<DepartmentItem>`
- 成功提示：`Department created successfully`

### PUT `/api/departments/{id}`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`id`（数字）
- 请求体（`UpdateDepartmentRequest`）：`name`（必填）、`parentId`（可选）
- 响应：`ApiResponse<DepartmentItem>`
- 成功提示：`Department updated successfully`

### PUT `/api/departments/{id}/status`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`id`（数字）
- 请求体（`UpdateDepartmentStatusRequest`）：`isActive`（必填）
- 响应：`ApiResponse<DepartmentItem>`
- 成功提示：`Department status updated successfully`

### DELETE `/api/departments/{id}`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`id`（数字）
- 请求体：无
- 响应：`ApiResponse<Void>`
- 成功提示：`Department deleted successfully`

---

## 5.2 角色接口（`/api/roles`）

### GET `/api/roles`
- 鉴权：需要登录
- 查询参数（可选）：`isActive`
- 响应：`ApiResponse<List<RoleItem>>`

### GET `/api/roles/{code}`
- 鉴权：需要登录
- 路径参数：`code`（字符串）
- 响应：`ApiResponse<RoleItem>`

### 单字段读取接口（新增）
- `GET /api/roles/{code}/code` -> `ApiResponse<String>`
- `GET /api/roles/{code}/name` -> `ApiResponse<String>`
- `GET /api/roles/{code}/sort-order` -> `ApiResponse<Integer>`
- `GET /api/roles/{code}/is-active` -> `ApiResponse<Boolean>`
- `GET /api/roles/{code}/created-at` -> `ApiResponse<String>`
- `GET /api/roles/{code}/updated-at` -> `ApiResponse<String>`

### POST `/api/roles`
- 鉴权：需要登录（仅 `admin`）
- 请求体（`CreateRoleRequest`）：`code`、`name` 必填，`sortOrder`、`isActive` 可选
- 响应：`ApiResponse<RoleItem>`
- 成功提示：`Role created successfully`

### PUT `/api/roles/{code}`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`code`（字符串）
- 请求体（`UpdateRoleRequest`）：`name` 必填，`sortOrder` 可选
- 响应：`ApiResponse<RoleItem>`
- 成功提示：`Role updated successfully`

### PUT `/api/roles/{code}/status`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`code`（字符串）
- 请求体（`UpdateRoleStatusRequest`）：`isActive` 必填
- 响应：`ApiResponse<RoleItem>`
- 成功提示：`Role status updated successfully`

### DELETE `/api/roles/{code}`
- 鉴权：需要登录（仅 `admin`）
- 路径参数：`code`（字符串）
- 请求体：无
- 响应：`ApiResponse<Void>`
- 成功提示：`Role deleted successfully`

---

## 6. 案件接口（`/api/cases`）

### POST `/api/cases`
- 鉴权：需要登录
- 请求体（`CreateCaseRequest`）：
  - `caseNumber`、`title`、`typeCode`、`departmentId` 必填
  - 其他字段可选：`reporterName`、`reporterContact`、`incidentTime`、`incidentLocation`、`briefDescription`、`deadlineTime`
- 响应：`ApiResponse<CaseDetailResponse>`
- 成功提示：`Case created successfully`

### GET `/api/cases`
- 鉴权：需要登录
- 查询参数：`caseNumber`、`title`、`typeCode`、`status`、`departmentId`、`handlingOfficerId`、`isOverdue`、`page`、`size`（均可选）
- 响应：`ApiResponse<PageResult<CaseListItem>>`

### GET `/api/cases/archived`
### GET `/api/cases/deadline-warnings`
### GET `/api/cases/overdue`
- 鉴权：需要登录
- 响应：`ApiResponse<PageResult<CaseListItem>>`

### GET `/api/cases/{id}`
- 鉴权：需要登录
- 路径参数：`id`
- 响应：`ApiResponse<CaseDetailResponse>`

### GET `/api/cases/{id}/export`
- 鉴权：需要登录
- 路径参数：`id`
- 响应：`ApiResponse<CaseExportResponse>`
- 成功提示：`Case dossier exported`

### PUT `/api/cases/{id}`
- 鉴权：需要登录（权限基于案件状态和用户角色：REGISTERED状态仅创建者可修改，ACCEPTED/INVESTIGATING状态仅受理民警可修改）
- 路径参数：`id`
- 请求体（`UpdateCaseRequest`）：`title`、`typeCode`、`departmentId` 必填，其余可选
- 响应：`ApiResponse<CaseDetailResponse>`
- 成功提示：`Case updated successfully`

### DELETE `/api/cases/{id}`
- 鉴权：需要登录
- 路径参数：`id`
- 响应：`ApiResponse<Void>`
- 成功提示：`Case deleted successfully`

### DELETE `/api/cases/batch`
- 鉴权：需要登录
- 请求体（`BatchDeleteRequest`）：
  - `ids`（数组，必填）
- 响应：`ApiResponse<Void>`
- 成功提示：`Cases deleted successfully`

### POST `/api/cases/{id}/accept`
- 鉴权：需要登录
- 路径参数：`id`
- 请求头（可选）：`Idempotency-Key`
- 响应：`ApiResponse<CaseDetailResponse>`
- 成功提示：`Case accepted successfully`
- 说明：已接入标准审批链（`ACCEPTANCE_REVIEW`）。

### POST `/api/cases/{id}/assign`
- 鉴权：需要登录（仅supervisor或admin角色可调用）
- 路径参数：`id`
- 请求体（`AssignCaseRequest`）：`handlingOfficerId` 必填
- 响应：`ApiResponse<CaseDetailResponse>`

### POST `/api/cases/{id}/status-transitions`
- 鉴权：需要登录
- 请求体（`StatusTransitionRequest`）：`toStatus` 必填，`comment` 可选
- 响应：`ApiResponse<CaseDetailResponse>`

### GET `/api/cases/{id}/processes`
- 鉴权：需要登录
- 响应：`ApiResponse<List<CaseProcessItem>>`

### 证据接口
- `POST /api/cases/{id}/evidences`
- `GET /api/cases/{id}/evidences`
- `PUT /api/cases/{caseId}/evidences/{evidenceId}`
- `DELETE /api/cases/{caseId}/evidences/{evidenceId}`

### 兼容审批入口（已接入工作流）
- `POST /api/cases/{id}/legal-review/submit`
- `POST /api/cases/{id}/legal-review/approve`
- `POST /api/cases/{id}/legal-review/reject`
- `POST /api/cases/{id}/decision`
- `POST /api/cases/{id}/execution`
- `POST /api/cases/{id}/archive`
- `POST /api/cases/{id}/unarchive`

---

## 6.1 前端联调建议

- 角色码：`police_officer`（民警）、`legal_officer`（法制员）、`supervisor`（主管）、`admin`（管理员）
- 对写接口建议统一传 `Idempotency-Key`
- 前端按 `code != 200` 处理业务失败，即使 HTTP 状态码是 200

---

## 6.2 工作流接口（`/api/workflows`）

### POST `/api/cases/{id}/workflows/{flowType}/start`
- 鉴权：需要登录
- 请求头（推荐）：`Idempotency-Key`
- `flowType` 可选值：
  - `ACCEPTANCE_REVIEW`
  - `FILING_REVIEW`
  - `LEGAL_AUDIT_REVIEW`
  - `DECISION_REVIEW`
  - `EXECUTION_REVIEW`
  - `ARCHIVE_REVIEW`
- 请求体（`StartCaseWorkflowRequest`）：`comment` 可选
- 响应：`ApiResponse<WorkflowInstanceResponse>`

### GET `/api/cases/{id}/workflows`
### GET `/api/workflows/instances/{instanceId}`
### GET `/api/workflows/tasks/my-pending`
- 鉴权：需要登录

### POST `/api/workflows/tasks/{taskId}/approve`
### POST `/api/workflows/tasks/{taskId}/reject`
- 鉴权：需要登录
- 请求头（推荐）：`Idempotency-Key`
- 请求体（`WorkflowActionRequest`）：`comment`

---

## 7. 统计接口（`/api/statistics`）

- 均需登录鉴权。
- 包含：
  - `GET /api/statistics/cases-overview`
   - `GET /api/statistics/created-cases-trend`
  - `GET /api/statistics/region-hotspots`
  - `GET /api/statistics/officer-efficiency`
  - `GET /api/statistics/review-pass-rate`
  - `GET /api/statistics/current-online-users`
  - `GET /api/statistics/total-users`
  - `GET /api/statistics/police-officers`
  - `GET /api/statistics/total-cases`
  - `GET /api/statistics/open-cases`
  - `GET /api/statistics/closed-cases`
  - `GET /api/statistics/overdue-cases`
  - `GET /api/statistics/near-deadline-cases?withinDays=3`

### 工作台专用计数接口

- 以上 8 个接口均返回 `ApiResponse<Long>`。
- 口径约定：
  - 在线人数：`refresh_tokens` 中未撤销且未过期的去重用户数。
  - 系统总人数：`users.is_active = 1` 的人数。
  - 民警数：`users.role = police_officer` 且 `is_active = 1`。
  - 已结案：状态为 `EXECUTED` 或 `ARCHIVED`。
  - 未结案：状态不在 `EXECUTED`、`ARCHIVED`。
  - 超期/临期：仅统计未结案且存在 `deadline_time` 的案件；临期默认 `withinDays = 3`。

### GET `/api/statistics/created-cases-trend`
- 鉴权：需要登录
- 查询参数（全部可选）：
  - `startTime`（ISO 日期时间）
  - `endTime`（ISO 日期时间）
  - `granularity`（`DAY` / `MONTH`，默认 `DAY`）
- 响应：`ApiResponse<List<TimeCountItem>>`
- 口径：按 `cases.created_at` 分组统计已建立案件数量；`DAY` 按日，`MONTH` 按月。
- 说明：统计范围与 `cases-overview` 的 `byPeriod` 一致。

---

## 8. 字典接口（`/api/dictionaries`）

### GET `/api/dictionaries/case-types`
- 鉴权：需要登录
- 响应：`ApiResponse<List<CaseTypeItem>>`

---

## 9. DashScope 接口（`/api/dashscope`）

- 当前为公开接口（不需要登录）。
- 包含：
  - `POST /api/dashscope/chat`
  - `POST /api/dashscope/prompt`

---

## 10. 联调示例

### 登录后访问受保护接口

```http
POST /api/auth/login
Content-Type: application/json

{
  "name": "alice",
  "password": "P@ssw0rd"
}
```

```http
GET /api/users/me
Authorization: Bearer <token_from_login>
```

### 分页查询案件

```http
GET /api/cases?page=1&size=10&status=REGISTERED
Authorization: Bearer <token>
```

---

## 11. AI 调用提示

- 始终按 `ApiResponse<T>` 包络解析返回。
- 受保护接口必须带 `Authorization: Bearer <token>`。
- 即使 HTTP 200，只要 `code != 200` 也视为业务失败。
- 日期时间字段使用 ISO-8601 字符串。
- JWT 主体身份以 `userId`（`sub`）为准。
