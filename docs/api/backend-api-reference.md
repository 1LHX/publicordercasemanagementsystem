<!--
用途: 后端 API 的统一接口参考文档（供前端联调、测试脚本和 AI 生成调用时使用）。
使用方法:
1) 新增/修改接口后，同步更新本文件的路径、参数、返回结构与示例。
2) 前端或脚本调用时，按 ApiResponse<T> 解析并处理 code != 200 的业务失败。
-->

# Backend API Reference (Frontend + AI)

This document is generated from the current controller and DTO implementation in `src/main/java/com/example/publicordercasemanagementsystem`.

## 1. Base conventions

- Base path: `/api`
- Content type: `application/json`
- Protected APIs require header:

```http
Authorization: Bearer <access_token>
```

- Unified response envelope (`ApiResponse<T>`):

```json
{
  "code": 200,
  "message": "OK",
  "data": {}
}
```

- Validation failures are flattened by global handler:

```json
{
  "code": 400,
  "message": "Validation failed",
  "data": null
}
```

## 2. Auth model

- Public endpoints:
  - `/api/auth/**`
  - `/api/dashscope/**`
- All other routes require JWT (configured in `SecurityConfig`).
- JWT principal is the user `name` claim. Endpoints like `/api/users/me` and case operation audit use that value.

## 3. Common error codes

- `400`: validation/business rule failure
- `401`: unauthenticated, invalid token, or user not found
- `404`: resource not found (for example case id)
- `429`: login lockout
- `5xx`: internal or upstream service error

---

## 4. Auth APIs (`/api/auth`)

### POST `/api/auth/login`
- Auth: public
- Body (`LoginRequest`):
  - `name` (string, required)
  - `password` (string, required)
- Response: `ApiResponse<AuthResponse>`
- Success message: `Login successful`

### POST `/api/auth/refresh`
- Auth: requires header `Authorization`
- Body (`RefreshRequest`):
  - `refreshToken` (string, required)
- Response: `ApiResponse<AuthResponse>`
- Success message: `Token refreshed successfully`

### POST `/api/auth/logout`
- Auth: requires header `Authorization`
- Body (`LogoutRequest`):
  - `refreshToken` (string, required)
- Response: `ApiResponse<Void>`
- Success message: `Logout successful`

### POST `/api/auth/register`
- Auth: public
- Body (`RegisterRequest`):
  - `name` (string, required)
  - `password` (string, required)
  - `confirmPassword` (string, required)
  - `role` (string, optional)
  - `roleName` (string, optional)
  - `department` (string, optional)
  - `departmentId` (number, optional)
- Response: `ApiResponse<UserInfo>`
- Success message: `Register successful`

---

## 5. User APIs (`/api/users`)

### GET `/api/users`
- Auth: required
- Query (all optional):
  - `name` (string)
  - `role` (string)
  - `department` (string)
  - `departmentId` (number)
  - `isActive` (boolean)
  - `page` (number)
  - `size` (number)
- Response: `ApiResponse<PageResult<UserListItem>>`

### GET `/api/users/me`
- Auth: required
- Query: none
- Response: `ApiResponse<UserInfo>`
- Notes:
  - User identity comes from current JWT principal (`authentication.getName()`).

### PUT `/api/users/{id}/name`
- Auth: required (`admin` only)
- Path: `id` (number)
- Body (`UpdateUserNameRequest`):
  - `name` (string, required)
- Response: `ApiResponse<UserInfo>`
- Success message: `User name updated successfully`
- Notes:
  - Duplicate name will fail with `code=400`.
  - Target user's refresh tokens are revoked when name changes.

### PUT `/api/users/{id}/password`
- Auth: required (`admin` only)
- Path: `id` (number)
- Body (`ChangePasswordRequest`):
  - `password` (string, required)
  - `confirmPassword` (string, required)
- Response: `ApiResponse<Void>`
- Success message: `User password updated successfully`
- Notes:
  - Passwords must match.
  - Target user's refresh tokens are revoked after password update.

### PUT `/api/users/{id}/role`
- Auth: required (`admin` only)
- Path: `id` (number)
- Body (`UpdateUserRoleRequest`):
  - `role` (string, required, role code)
- Response: `ApiResponse<UserInfo>`
- Success message: `User role updated successfully`
- Notes:
  - Role code must exist and be active.
  - Target user's refresh tokens are revoked after role update.

### PUT `/api/users/{id}/status`
- Auth: required (`admin` only)
- Path: `id` (number)
- Body (`UpdateUserStatusRequest`):
  - `isActive` (boolean, required)
- Response: `ApiResponse<UserInfo>`
- Success message: `User status updated successfully`
- Notes:
  - `admin` cannot disable itself.
  - Target user's refresh tokens are revoked when disabled.

### DELETE `/api/users/{id}`
- Auth: required (`admin` only)
- Path: `id` (number)
- Body: none
- Response: `ApiResponse<Void>`
- Success message: `User deleted successfully`
- Notes:
  - `admin` cannot delete itself.
  - This is account deletion, not token logout.

---

## 5.1 Role APIs (`/api/roles`)

### GET `/api/roles`
- Auth: required
- Query (optional):
  - `isActive` (boolean)
- Response: `ApiResponse<List<RoleItem>>`

### GET `/api/roles/{code}`
- Auth: required
- Path: `code` (string)
- Response: `ApiResponse<RoleItem>`

### POST `/api/roles`
- Auth: required (`admin` only)
- Body (`CreateRoleRequest`):
  - `code` (string, required)
  - `name` (string, required)
  - `sortOrder` (number, optional, default `0`)
  - `isActive` (boolean, optional, default `true`)
- Response: `ApiResponse<RoleItem>`
- Success message: `Role created successfully`

### PUT `/api/roles/{code}`
- Auth: required (`admin` only)
- Path: `code` (string)
- Body (`UpdateRoleRequest`):
  - `name` (string, required)
  - `sortOrder` (number, optional)
- Response: `ApiResponse<RoleItem>`
- Success message: `Role updated successfully`

### PUT `/api/roles/{code}/status`
- Auth: required (`admin` only)
- Path: `code` (string)
- Body (`UpdateRoleStatusRequest`):
  - `isActive` (boolean, required)
- Response: `ApiResponse<RoleItem>`
- Success message: `Role status updated successfully`
- Notes:
  - `admin` role cannot be disabled.
  - Role in use by users cannot be disabled.

### DELETE `/api/roles/{code}`
- Auth: required (`admin` only)
- Path: `code` (string)
- Body: none
- Response: `ApiResponse<Void>`
- Success message: `Role deleted successfully`
- Notes:
  - `admin` role cannot be deleted.
  - Role in use by users cannot be deleted.

---

## 6. Case APIs (`/api/cases`)

### POST `/api/cases`
- Auth: required
- Body (`CreateCaseRequest`):
  - `caseNumber` (string, required)
  - `title` (string, required)
  - `typeCode` (string, required)
  - `departmentId` (number, required)
  - `reporterName` (string, optional)
  - `reporterContact` (string, optional)
  - `incidentTime` (string datetime, optional)
  - `incidentLocation` (string, optional)
  - `briefDescription` (string, optional)
  - `deadlineTime` (string datetime, optional)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Case created successfully`

### GET `/api/cases`
- Auth: required
- Query (all optional):
  - `caseNumber` (string)
  - `title` (string)
  - `typeCode` (string)
  - `status` (string)
  - `departmentId` (number)
  - `handlingOfficerId` (number)
  - `isOverdue` (boolean)
  - `page` (number)
  - `size` (number)
- Response: `ApiResponse<PageResult<CaseListItem>>`

### GET `/api/cases/archived`
- Auth: required
- Query: `page` (optional), `size` (optional)
- Response: `ApiResponse<PageResult<CaseListItem>>`

### GET `/api/cases/deadline-warnings`
- Auth: required
- Query: `withinDays` (optional), `page` (optional), `size` (optional)
- Response: `ApiResponse<PageResult<CaseListItem>>`

### GET `/api/cases/overdue`
- Auth: required
- Query: `page` (optional), `size` (optional)
- Response: `ApiResponse<PageResult<CaseListItem>>`

### GET `/api/cases/{id}`
- Auth: required
- Path: `id` (number)
- Response: `ApiResponse<CaseDetailResponse>`

### GET `/api/cases/{id}/export`
- Auth: required
- Path: `id` (number)
- Response: `ApiResponse<CaseExportResponse>`
- Success message: `Case dossier exported`

### PUT `/api/cases/{id}`
- Auth: required
- Path: `id` (number)
- Body (`UpdateCaseRequest`):
  - `title` (string, required)
  - `typeCode` (string, required)
  - `departmentId` (number, required)
  - `reporterName` (optional)
  - `reporterContact` (optional)
  - `incidentTime` (optional)
  - `incidentLocation` (optional)
  - `briefDescription` (optional)
  - `deadlineTime` (optional)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Case updated successfully`

### POST `/api/cases/{id}/accept`
- Auth: required
- Path: `id` (number)
- Body: none
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Case accepted successfully`

### POST `/api/cases/{id}/assign`
- Auth: required
- Path: `id` (number)
- Body (`AssignCaseRequest`):
  - `handlingOfficerId` (number, required)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Case assigned successfully`

### POST `/api/cases/{id}/status-transitions`
- Auth: required
- Path: `id` (number)
- Body (`StatusTransitionRequest`):
  - `toStatus` (string, required)
  - `comment` (string, optional)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Case status updated successfully`

### GET `/api/cases/{id}/processes`
- Auth: required
- Path: `id` (number)
- Response: `ApiResponse<List<CaseProcessItem>>`

### POST `/api/cases/{id}/evidences`
- Auth: required
- Path: `id` (number)
- Body (`CreateEvidenceRequest`):
  - `fileName` (string, required)
  - `filePath` (string, required)
  - `fileType` (string, optional)
  - `fileSize` (number, optional)
  - `description` (string, optional)
- Response: `ApiResponse<CaseEvidenceItem>`
- Success message: `Evidence added successfully`

### GET `/api/cases/{id}/evidences`
- Auth: required
- Path: `id` (number)
- Response: `ApiResponse<List<CaseEvidenceItem>>`

### POST `/api/cases/{id}/legal-review/submit`
- Auth: required
- Path: `id` (number)
- Body (`LegalReviewSubmitRequest`):
  - `comment` (string, required)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Legal review submitted successfully`

### POST `/api/cases/{id}/legal-review/approve`
- Auth: required
- Path: `id` (number)
- Body (`LegalReviewApproveRequest`):
  - `comment` (string, required)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Legal review approved successfully`

### POST `/api/cases/{id}/legal-review/reject`
- Auth: required
- Path: `id` (number)
- Body (`LegalReviewRejectRequest`):
  - `reason` (string, required)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Legal review rejected successfully`

### POST `/api/cases/{id}/decision`
- Auth: required
- Path: `id` (number)
- Body (`SaveDecisionRequest`):
  - `decisionResult` (string, required)
  - `decisionContent` (string, required)
  - `coerciveMeasureCode` (string, optional)
  - `decidedAt` (string datetime, optional)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Decision saved successfully`

### POST `/api/cases/{id}/execution`
- Auth: required
- Path: `id` (number)
- Body (`RecordExecutionRequest`):
  - `executionResult` (string, required)
  - `executionNote` (string, optional)
  - `executedAt` (string datetime, optional)
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Execution recorded successfully`

### POST `/api/cases/{id}/archive`
- Auth: required
- Path: `id` (number)
- Body: none
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Case archived successfully`

### POST `/api/cases/{id}/unarchive`
- Auth: required
- Path: `id` (number)
- Body: none
- Response: `ApiResponse<CaseDetailResponse>`
- Success message: `Case unarchived successfully`

---

## 7. Statistics APIs (`/api/statistics`)

All endpoints require auth.

### GET `/api/statistics/cases-overview`
- Query:
  - `startTime` (ISO datetime, optional)
  - `endTime` (ISO datetime, optional)
  - `granularity` (string, optional, default `DAY`)
- Response: `ApiResponse<CasesOverviewResponse>`

### GET `/api/statistics/region-hotspots`
- Query:
  - `startTime` (ISO datetime, optional)
  - `endTime` (ISO datetime, optional)
  - `topN` (number, optional)
- Response: `ApiResponse<List<RegionHotspotItem>>`

### GET `/api/statistics/officer-efficiency`
- Query:
  - `startTime` (ISO datetime, optional)
  - `endTime` (ISO datetime, optional)
  - `topN` (number, optional)
- Response: `ApiResponse<List<OfficerEfficiencyItem>>`

### GET `/api/statistics/review-pass-rate`
- Query:
  - `startTime` (ISO datetime, optional)
  - `endTime` (ISO datetime, optional)
- Response: `ApiResponse<List<ReviewPassRateItem>>`

---

## 8. Dictionary API (`/api/dictionaries`)

### GET `/api/dictionaries/case-types`
- Auth: required
- Query: none
- Response: `ApiResponse<List<CaseTypeItem>>`

---

## 9. DashScope APIs (`/api/dashscope`)

These endpoints are public in current security config.

### POST `/api/dashscope/chat`
- Auth: public
- Body (`ChatCompletionRequest`):
  - `model` (string, optional, default `qwen-plus`)
  - `messages` (array of `ChatMessage`, optional in DTO but required by business usage)
    - `role` (string)
    - `content` (string)
  - `maxTokens` (number, optional)
  - `temperature` (number, optional)
- Response: `ApiResponse<ChatCompletionResponse>`

### POST `/api/dashscope/prompt`
- Auth: public
- Body (`PromptRequest`):
  - `prompt` (string, required)
  - `model` (string, optional)
- Response: `ApiResponse<ChatCompletionResponse>`

---

## 10. Frontend integration quick examples

### Login then call protected API

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

### Typical paged list call

```http
GET /api/cases?page=1&size=10&status=REGISTERED
Authorization: Bearer <token>
```

---

## 11. AI prompt guidance for this API

When asking AI to generate client calls:
- Always use `ApiResponse<T>` envelope parsing.
- For protected APIs include `Authorization: Bearer <token>`.
- Treat `code != 200` as business failure even if HTTP status is 200.
- Use ISO-8601 strings for datetime request fields.
- Keep JWT subject identity aligned to `name` (not user id).


