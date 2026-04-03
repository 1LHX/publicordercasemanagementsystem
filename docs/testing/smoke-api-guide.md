<!--
用途: 说明如何执行 API 冒烟脚本，用于快速验证认证与基础受保护接口链路。
使用方法:
1) 先启动后端服务，再按文档命令运行脚本。
2) 可通过参数切换 BaseUrl、账号与密码，适配不同本地环境。
-->

# API Smoke Test Guide

## `smoke-auth-user-dictionary.ps1`

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
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-auth-user-dictionary.ps1 -TryRegister
```

Optional parameters:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-auth-user-dictionary.ps1 -BaseUrl "http://localhost:8080" -UserName "smoke_user" -Password "P@ssw0rd123"
```


