# Database Field Dictionary

This document describes the MySQL schema used by the backend in `PublicOrderCaseManagementSystem`.

- Schema source: `src/main/resources/db/schema.sql`
- SQL callers: `src/main/resources/mapping/*.xml`
- Service behavior reference: `src/main/java/com/example/publicordercasemanagementsystem/service/impl/`

## 1) `roles`
Purpose: role dictionary joined by `users.role`.

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `code` | varchar(64) | N | - | PK, role code (`admin`, `police_officer`...) |
| `name` | varchar(128) | N | - | Unique display name |
| `sort_order` | int | N | `0` | Ordering |
| `is_active` | tinyint(1) | N | `1` | Active flag |
| `created_at` | datetime | N | `CURRENT_TIMESTAMP` | Created time |
| `updated_at` | datetime | N | auto update | Updated time |

Used by: `UserMapper.xml` joins `roles r on r.code = u.role`.

## 2) `departments`
Purpose: department dictionary for users and cases.

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `name` | varchar(128) | N | - | Unique department name |
| `parent_id` | bigint | Y | `NULL` | Self-reference for hierarchy |
| `is_active` | tinyint(1) | N | `1` | Active flag |
| `created_at` | datetime | N | `CURRENT_TIMESTAMP` | Created time |
| `updated_at` | datetime | N | auto update | Updated time |

Used by: `UserMapper.xml`, `CaseMapper.xml`, `StatisticsMapper.xml`.

## 3) `dict_case_types`
Purpose: case type dictionary.

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `code` | varchar(64) | N | - | PK, case type code |
| `name` | varchar(128) | N | - | Type display name |
| `sort_order` | int | N | `0` | Ordering |
| `is_active` | tinyint(1) | N | `1` | Active flag |

Used by: `DictionaryMapper.xml#findActiveCaseTypes`, joins in `CaseMapper.xml` and `StatisticsMapper.xml`.

## 4) `users`
Purpose: account table for auth and case operator/officer references.

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `password` | varchar(255) | N | - | BCrypt hash |
| `name` | varchar(64) | N | - | Unique login name |
| `role` | varchar(64) | N | - | FK -> `roles.code` |
| `department_id` | bigint | Y | `NULL` | FK -> `departments.id` |
| `is_active` | tinyint(1) | N | `1` | User enabled flag |
| `last_login` | datetime | Y | `NULL` | Last successful login |
| `login_attempts` | int | N | `0` | Failed login counter |
| `locked_until` | datetime | Y | `NULL` | Lockout end time |
| `created_at` | datetime | N | `CURRENT_TIMESTAMP` | Created time |
| `updated_at` | datetime | N | auto update | Updated time |

Used by: `AuthServiceImpl` login/register/lockout flow, `UserMapper.xml`.

## 5) `refresh_tokens`
Purpose: refresh token persistence (hashed token, rotation, revoke).

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `user_id` | bigint | N | - | FK -> `users.id` |
| `token_hash` | char(64) | N | - | SHA-256 hex of refresh token |
| `expires_at` | datetime | N | - | Expiration time |
| `revoked` | tinyint(1) | N | `0` | Revoked flag |
| `created_at` | datetime | N | `CURRENT_TIMESTAMP` | Created time |
| `last_used_at` | datetime | Y | `NULL` | Last refresh usage time |

Used by: `RefreshTokenMapper.xml`, `AuthServiceImpl#refresh/#logout`.

## 6) `login_logs`
Purpose: login audit records (success/failure, IP, UA parse result).

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `user_id` | bigint | Y | `NULL` | FK -> `users.id`, nullable for unknown user |
| `name` | varchar(64) | Y | `NULL` | Login name snapshot |
| `ip` | varchar(45) | Y | `NULL` | IPv4/IPv6 text |
| `login_time` | datetime | N | `CURRENT_TIMESTAMP` | Login timestamp |
| `login_result` | tinyint | N | - | `1` success, `0` fail |
| `device_type` | varchar(64) | Y | `NULL` | Parsed User-Agent |
| `browser` | varchar(128) | Y | `NULL` | Parsed User-Agent |
| `os` | varchar(128) | Y | `NULL` | Parsed User-Agent |

Used by: `LoginLogMapper.xml`, `AuthServiceImpl#recordLogin`.

## 7) `cases`
Purpose: main case entity.

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `case_number` | varchar(64) | N | - | Unique business identifier |
| `title` | varchar(255) | N | - | Case title |
| `type_code` | varchar(64) | N | - | FK -> `dict_case_types.code` |
| `status` | varchar(32) | N | - | Workflow status |
| `reporter_name` | varchar(64) | N | - | Reporter |
| `reporter_contact` | varchar(64) | Y | `NULL` | Reporter contact |
| `incident_time` | datetime | Y | `NULL` | Incident time |
| `incident_location` | varchar(255) | Y | `NULL` | Incident location |
| `brief_description` | text | Y | `NULL` | Description |
| `handling_officer_id` | bigint | Y | `NULL` | FK -> `users.id` |
| `department_id` | bigint | Y | `NULL` | FK -> `departments.id` |
| `acceptance_time` | datetime | Y | `NULL` | Accept timestamp |
| `deadline_time` | datetime | Y | `NULL` | Deadline |
| `is_overdue` | tinyint(1) | N | `0` | Overdue flag maintained by service/SQL |
| `created_at` | datetime | N | `CURRENT_TIMESTAMP` | Created time |
| `updated_at` | datetime | N | auto update | Updated time |

Used by: all list/detail/search/deadline/statistics queries in `CaseMapper.xml`, `StatisticsMapper.xml`.

## 8) `case_processes`
Purpose: immutable process history for status changes and key operations.

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `case_id` | bigint | N | - | FK -> `cases.id` |
| `from_status` | varchar(32) | Y | `NULL` | Previous status |
| `to_status` | varchar(32) | N | - | New status |
| `operator_id` | bigint | Y | `NULL` | FK -> `users.id` |
| `operation_time` | datetime | N | `CURRENT_TIMESTAMP` | Operation time |
| `comment` | varchar(500) | Y | `NULL` | Operation remark |
| `ip_address` | varchar(45) | Y | `NULL` | Client IP |

Used by: `CaseMapper.xml#insertProcess/#findProcessesByCaseId/#findLatestArchiveProcessByCaseId`.

## 9) `case_evidences`
Purpose: evidence metadata linked to case.

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `case_id` | bigint | N | - | FK -> `cases.id` |
| `file_name` | varchar(255) | N | - | Evidence filename |
| `file_path` | varchar(512) | N | - | Storage path/link |
| `file_type` | varchar(64) | Y | `NULL` | MIME/type tag |
| `file_size` | bigint | Y | `NULL` | Bytes |
| `upload_user_id` | bigint | Y | `NULL` | FK -> `users.id` |
| `description` | varchar(500) | Y | `NULL` | Evidence note |
| `uploaded_at` | datetime | N | `CURRENT_TIMESTAMP` | Upload time |

Used by: `CaseMapper.xml#insertEvidence/#findEvidencesByCaseId`.

## 10) `case_legal_reviews`
Purpose: per-case legal review snapshot (upsert by `case_id`).

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `case_id` | bigint | N | - | Unique FK -> `cases.id` |
| `review_status` | varchar(32) | N | - | `SUBMITTED/APPROVED/REJECTED` |
| `review_comment` | varchar(1000) | Y | `NULL` | Review note |
| `reviewer_id` | bigint | Y | `NULL` | FK -> `users.id` |
| `reviewed_at` | datetime | Y | `NULL` | Review time |
| `created_at` | datetime | N | `CURRENT_TIMESTAMP` | Created time |
| `updated_at` | datetime | N | auto update | Updated time |

Used by: legal review APIs and pass-rate stats in `StatisticsMapper.xml`.

## 11) `case_decisions`
Purpose: per-case decision snapshot (upsert by `case_id`).

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `case_id` | bigint | N | - | Unique FK -> `cases.id` |
| `decision_result` | varchar(64) | N | - | Decision result code |
| `decision_content` | varchar(2000) | Y | `NULL` | Decision content |
| `coercive_measure_code` | varchar(64) | Y | `NULL` | Measure code |
| `decided_by` | bigint | Y | `NULL` | FK -> `users.id` |
| `decided_at` | datetime | Y | `NULL` | Decision time |
| `created_at` | datetime | N | `CURRENT_TIMESTAMP` | Created time |
| `updated_at` | datetime | N | auto update | Updated time |

Used by: `CaseMapper.xml#upsertDecision`.

## 12) `case_executions`
Purpose: per-case execution snapshot (upsert by `case_id`).

| Field | Type | Null | Default | Notes |
|---|---|---|---|---|
| `id` | bigint | N | auto inc | PK |
| `case_id` | bigint | N | - | Unique FK -> `cases.id` |
| `execution_result` | varchar(64) | N | - | Execution result |
| `execution_note` | varchar(1000) | Y | `NULL` | Execution note |
| `executed_by` | bigint | Y | `NULL` | FK -> `users.id` |
| `executed_at` | datetime | Y | `NULL` | Execution time |
| `created_at` | datetime | N | `CURRENT_TIMESTAMP` | Created time |
| `updated_at` | datetime | N | auto update | Updated time |

Used by: `CaseMapper.xml#upsertExecution`.

## Workflow Value Conventions (current backend)
- Case status baseline: `REGISTERED -> ACCEPTED -> INVESTIGATING -> LEGAL_REVIEW -> DECIDED -> EXECUTED -> ARCHIVED`
- Legal review status: `SUBMITTED`, `APPROVED`, `REJECTED`
- Login result: `1` success, `0` failure

## Index Design Notes
- `refresh_tokens(token_hash, revoked)`: fast token validation and revoke check.
- `cases(deadline_time, status)` and `cases(is_overdue, status)`: deadline warning/overdue scans.
- `case_processes(case_id, to_status, id)`: fetch latest archive operation quickly.
- `dict_case_types(is_active, sort_order, code)`: dictionary endpoint sorted access.

