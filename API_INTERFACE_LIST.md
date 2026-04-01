# Public Order Case Management System - API Interface List

Last updated: 2026-04-01
Version: v0.1 (inventory + implementation roadmap)

## 1. Global Conventions

- Base path: `/api`
- Response envelope: `ResponseEntity<ApiResponse<T>>`
- Validation: `@Valid` at controller boundary, field-level constraints in DTO
- Validation error shape: `code=400, message="Validation failed"`
- Business/auth errors: `AuthException(status, message, data)`

### 1.1 Auth Rules

- Public (no JWT): `/api/auth/**`, `/api/dashscope/**`
- JWT required: all other endpoints
- JWT principal: claim `name` (used by `/api/users/me`)

---

## 2. Implemented Endpoints

| Module | Method | Path | Purpose | Auth | Status |
|---|---|---|---|---|---|
| Auth | POST | `/api/auth/register` | Register user | Public | Implemented |
| Auth | POST | `/api/auth/login` | Login, issue access+refresh tokens | Public | Implemented |
| Auth | POST | `/api/auth/refresh` | Rotate refresh token and issue new pair | Public (requires Authorization header) | Implemented |
| Auth | POST | `/api/auth/logout` | Revoke refresh tokens | Public (requires Authorization header) | Implemented |
| Users | GET | `/api/users` | Paged user list with filters | JWT | Implemented |
| Users | GET | `/api/users/me` | Current user profile by JWT `name` | JWT | Implemented |
| AI | POST | `/api/dashscope/chat` | Chat completion proxy | Public | Implemented |
| AI | POST | `/api/dashscope/prompt` | Prompt-to-chat proxy | Public | Implemented |
| Cases | POST | `/api/cases` | Create case (report registration/intake) | JWT | Implemented |
| Cases | GET | `/api/cases` | Paged case search with filters | JWT | Implemented |
| Cases | GET | `/api/cases/{id}` | Case detail | JWT | Implemented |
| Cases | PUT | `/api/cases/{id}` | Update case basic info | JWT | Implemented |
| Cases | POST | `/api/cases/{id}/accept` | Accept case after review | JWT | Implemented |
| Cases | POST | `/api/cases/{id}/assign` | Assign handling officer | JWT | Implemented |
| Evidences | POST | `/api/cases/{id}/evidences` | Upload/link evidence to case | JWT | Implemented |
| Evidences | GET | `/api/cases/{id}/evidences` | List evidences by case | JWT | Implemented |
| Processes | POST | `/api/cases/{id}/status-transitions` | Move case to next status | JWT | Implemented |
| Processes | GET | `/api/cases/{id}/processes` | Case process history | JWT | Implemented |
| Review | POST | `/api/cases/{id}/legal-review/submit` | Submit for legal review | JWT | Implemented |
| Review | POST | `/api/cases/{id}/legal-review/approve` | Approve legal review | JWT | Implemented |
| Review | POST | `/api/cases/{id}/legal-review/reject` | Reject legal review with reason | JWT | Implemented |
| Decision | POST | `/api/cases/{id}/decision` | Save decision result | JWT | Implemented |
| Execution | POST | `/api/cases/{id}/execution` | Record decision execution | JWT | Implemented |
| Archive | POST | `/api/cases/{id}/archive` | Archive case | JWT | Implemented |
| Archive | POST | `/api/cases/{id}/unarchive` | Unarchive case (admin) | JWT | Implemented |
| Archive | GET | `/api/cases/archived` | Paged archived cases | JWT | Implemented |
| Export | GET | `/api/cases/{id}/export` | Export dossier (PDF/ZIP) | JWT | Implemented |
| Dictionary | GET | `/api/dictionaries/case-types` | List case type dictionary | JWT | Implemented |
| Deadline | GET | `/api/cases/deadline-warnings` | Cases nearing deadline | JWT | Implemented |
| Deadline | GET | `/api/cases/overdue` | Overdue cases | JWT | Implemented |
| Statistics | GET | `/api/statistics/cases-overview` | Case count by status/type/time | JWT | Implemented |
| Statistics | GET | `/api/statistics/region-hotspots` | High-frequency region report | JWT | Implemented |
| Statistics | GET | `/api/statistics/officer-efficiency` | Officer efficiency ranking | JWT | Implemented |
| Statistics | GET | `/api/statistics/review-pass-rate` | Legal review pass-rate trend | JWT | Implemented |

---

## 3. Full Lifecycle API List (To Build)

### 3.1 Case Intake and Filing (MVP)

| Module | Method | Path | Purpose | Auth | Priority | Status |
|---|---|---|---|---|---|---|
| Cases | POST | `/api/cases` | Create case (report registration/intake) | JWT | P0 | Implemented |
| Cases | GET | `/api/cases` | Paged case search with filters | JWT | P0 | Implemented |
| Cases | GET | `/api/cases/{id}` | Case detail | JWT | P0 | Implemented |
| Cases | PUT | `/api/cases/{id}` | Update case basic info | JWT | P0 | Implemented |
| Cases | POST | `/api/cases/{id}/accept` | Accept case after review | JWT | P0 | Implemented |
| Cases | POST | `/api/cases/{id}/assign` | Assign handling officer | JWT | P0 | Implemented |

Suggested query filters for `GET /api/cases`:
- `caseNumber`, `title`, `typeCode`, `status`, `departmentId`, `handlingOfficerId`
- `incidentStart`, `incidentEnd`, `isOverdue`, `page`, `size`

### 3.2 Investigation and Evidence (MVP)

| Module | Method | Path | Purpose | Auth | Priority | Status |
|---|---|---|---|---|---|---|
| Evidences | POST | `/api/cases/{id}/evidences` | Upload/link evidence to case | JWT | P0 | Implemented |
| Evidences | GET | `/api/cases/{id}/evidences` | List evidences by case | JWT | P0 | Implemented |
| Evidences | GET | `/api/evidences/{evidenceId}` | Evidence detail | JWT | P1 | Planned |
| Evidences | PUT | `/api/evidences/{evidenceId}` | Update evidence metadata/remark | JWT | P1 | Planned |
| Evidences | DELETE | `/api/evidences/{evidenceId}` | Remove evidence | JWT | P1 | Planned |

### 3.3 Process Flow, Review, Decision, Execution (MVP)

| Module | Method | Path | Purpose | Auth | Priority | Status |
|---|---|---|---|---|---|---|
| Processes | POST | `/api/cases/{id}/status-transitions` | Move case to next status | JWT | P0 | Implemented |
| Processes | GET | `/api/cases/{id}/processes` | Case process history | JWT | P0 | Implemented |
| Review | POST | `/api/cases/{id}/legal-review/submit` | Submit for legal review | JWT | P1 | Implemented |
| Review | POST | `/api/cases/{id}/legal-review/approve` | Approve legal review | JWT | P1 | Implemented |
| Review | POST | `/api/cases/{id}/legal-review/reject` | Reject legal review with reason | JWT | P1 | Implemented |
| Decision | POST | `/api/cases/{id}/decision` | Save decision result | JWT | P1 | Implemented |
| Execution | POST | `/api/cases/{id}/execution` | Record decision execution | JWT | P1 | Implemented |

Recommended baseline status machine:
- `REGISTERED -> ACCEPTED -> INVESTIGATING -> LEGAL_REVIEW -> DECIDED -> EXECUTED -> ARCHIVED`

### 3.4 Archiving and Retrieval (MVP)

| Module | Method | Path | Purpose | Auth | Priority | Status |
|---|---|---|---|---|---|---|
| Archive | POST | `/api/cases/{id}/archive` | Archive case | JWT | P0 | Implemented |
| Archive | POST | `/api/cases/{id}/unarchive` | Unarchive case (admin) | JWT | P1 | Implemented |
| Archive | GET | `/api/cases/archived` | Paged archived cases | JWT | P1 | Implemented |
| Export | GET | `/api/cases/{id}/export` | Export dossier (PDF/ZIP) | JWT | P1 | Implemented |

---

## 4. Supporting APIs (Project Requirements)

### 4.1 Dictionaries and Metadata (MVP)

| Module | Method | Path | Purpose | Auth | Priority | Status |
|---|---|---|---|---|---|---|
| Dictionary | GET | `/api/dictionaries/case-types` | List case type dictionary | JWT | P0 | Implemented |
| Dictionary | GET | `/api/dictionaries/coercive-measures` | List coercive measure dictionary | JWT | P1 | Planned |
| Org | GET | `/api/departments` | Department list | JWT | P1 | Planned |
| Org | GET | `/api/roles` | Role list | JWT | P1 | Planned |

### 4.2 Deadline Warning and Timeliness

| Module | Method | Path | Purpose | Auth | Priority | Status |
|---|---|---|---|---|---|---|
| Deadline | GET | `/api/cases/deadline-warnings` | Cases nearing deadline | JWT | P1 | Implemented |
| Deadline | GET | `/api/cases/overdue` | Overdue cases | JWT | P1 | Implemented |
| Deadline | POST | `/api/cases/{id}/deadline/recalculate` | Recompute deadline | JWT | P2 | Planned |

### 4.3 Statistics and Reports

| Module | Method | Path | Purpose | Auth | Priority | Status |
|---|---|---|---|---|---|---|
| Statistics | GET | `/api/statistics/cases-overview` | Case count by status/type/time | JWT | P1 | Implemented |
| Statistics | GET | `/api/statistics/region-hotspots` | High-frequency region report | JWT | P2 | Implemented |
| Statistics | GET | `/api/statistics/officer-efficiency` | Officer efficiency ranking | JWT | P2 | Implemented |
| Statistics | GET | `/api/statistics/review-pass-rate` | Legal review pass-rate trend | JWT | P2 | Implemented |

### 4.4 Documents and Templates

| Module | Method | Path | Purpose | Auth | Priority | Status |
|---|---|---|---|---|---|---|
| Documents | GET | `/api/document-templates` | List document templates | JWT | P2 | Planned |
| Documents | POST | `/api/documents/generate` | Generate document from template | JWT | P1 | Planned |
| Documents | GET | `/api/cases/{id}/documents` | Case document list | JWT | P1 | Planned |
| Documents | GET | `/api/documents/{documentId}/download` | Download generated document | JWT | P2 | Planned |

---

## 5. Recommended Delivery Batches

### Batch 1 (Must-Have, end-to-end runnable)
- `/api/cases` (POST/GET)
- `/api/cases/{id}` (GET/PUT)
- `/api/cases/{id}/accept`
- `/api/cases/{id}/assign`
- `/api/cases/{id}/status-transitions`
- `/api/cases/{id}/processes`
- `/api/cases/{id}/evidences` (POST/GET)
- `/api/cases/{id}/archive`
- `/api/dictionaries/case-types`

### Batch 2 (Operational completeness)
- Legal review + decision + execution APIs
- Archived query + dossier export
- Deadline warnings

### Batch 3 (Enhancement)
- Statistics dashboards
- Document template management and generation enhancements

---

## 6. Notes for Implementation

- Keep strict layering: `controller -> service -> mapper XML SQL -> MySQL`.
- Keep JWT claim contract (`name`, `role`, `departmentId`, etc.) unchanged.
- Sync changes across POJO/DTO, mapper interface, mapper XML, and `schema.sql`.
- Keep error response shape centralized via `ApiExceptionHandler`.

