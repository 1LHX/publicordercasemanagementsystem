<!--
用途: 模块级入口说明，快速告诉开发者项目做什么、文档在哪、如何启动。
使用方法:
1) 新成员先阅读本文件，再跳转到详细 API 与脚本文档。
2) 若启动方式、文档路径或关键端点变化，请优先更新本文件链接。
-->

# Public Order Case Management System - Module Guide

This module provides registration, login, refresh token, and logout APIs backed by MySQL and JWT.

## Documentation

- Full API reference: `docs/api/backend-api-reference.md`
- Extended project/help doc: `HELP.md`
- Smoke test guide: `docs/testing/smoke-api-guide.md`

## Quick Start

1. Create database tables using `src/main/resources/db/schema.sql`.
2. Update `src/main/resources/application.properties` with your MySQL credentials and JWT secret.
3. Run the application with Maven.

## Auth Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
