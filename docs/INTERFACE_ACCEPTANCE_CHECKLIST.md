# Interface Acceptance Checklist

Last updated: 2026-04-01

## 1. Preconditions

- Service is running on `http://localhost:8080`.
- MySQL schema is initialized (`src/main/resources/db/schema.sql`).
- You can create a temporary test account for smoke checks.
- All responses must keep the envelope: `code`, `message`, `data`.

## 2. Global Assertions (apply to every API)

- Success path: HTTP 200 and `code=200`.
- Validation path: HTTP 400 and `message="Validation failed"`.
- Auth path: unauthorized access returns `401`.
- Business errors remain in `ApiResponse` shape (no mixed error body format).

## 3. Smoke Cases by Module

## 3.1 Auth (`/api/auth`)

- `POST /login`
  - Valid body returns `token` and `refreshToken` in `data`.
  - Missing `name` or `password` returns validation failed.
- `POST /refresh`
  - Valid `Authorization` + `refreshToken` returns new token pair.
  - Missing `Authorization` returns 400/401.
- `POST /logout`
  - Valid request returns `code=200`.
  - Reusing revoked refresh token should fail.
- `POST /register`
  - Valid body creates user.
  - Duplicate name should return business 400.

## 3.2 Users (`/api/users`)

- `GET /api/users/me`
  - With Bearer token returns current user with JWT `name` mapping.
  - Without token returns `401`.
- `GET /api/users`
  - With token returns paged structure: `items`, `total`, `page`, `size`.

## 3.3 Cases (`/api/cases`)

- `POST /api/cases` creates one case and returns case id.
- `GET /api/cases` can find the newly created case by `caseNumber`.
- `POST /api/cases/{id}/accept` updates process state.
- `POST /api/cases/{id}/assign` with `handlingOfficerId` succeeds.
- `POST /api/cases/{id}/status-transitions` rejects invalid transition with 400.
- `GET /api/cases/{id}/processes` returns non-empty process history.
- `POST /api/cases/{id}/archive` then `GET /api/cases/archived` can query it.

## 3.4 Statistics and Dictionary

- `GET /api/dictionaries/case-types` returns active dictionary data.
- `GET /api/statistics/*` endpoints all return `code=200` and non-error envelope.

## 4. Regression Gate (minimum)

- Run controller-level tests: `AuthControllerWebMvcTest`, `UserControllerWebMvcTest`.
- Run all tests before merge: `./mvnw.cmd -q test`.

