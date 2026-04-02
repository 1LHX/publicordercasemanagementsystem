# API Smoke Scripts

## `smoke-api.ps1`

Quick smoke checks for auth + protected endpoints:

- login
- `/api/users/me`
- `/api/users`
- `/api/dictionaries/case-types`
- refresh token
- logout

## Usage (PowerShell)

```powershell
cd "E:\@Study documents\End\Code\Public Order Case Management System\PublicOrderCaseManagementSystem"
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-api.ps1 -TryRegister
```

Optional parameters:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-api.ps1 -BaseUrl "http://localhost:8080" -UserName "smoke_user" -Password "P@ssw0rd123"
```

