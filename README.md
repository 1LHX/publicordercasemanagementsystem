# Public Order Case Management System - Auth

This module provides registration, login, refresh token, and logout APIs backed by MySQL and JWT.

## Quick Start

1. Create database tables using `src/main/resources/db/schema.sql`.
2. Update `src/main/resources/application.properties` with your MySQL credentials and JWT secret.
3. Run the application with Maven.

## Auth Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
