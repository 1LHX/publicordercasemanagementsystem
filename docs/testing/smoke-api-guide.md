<!--
用途: 说明如何执行 API 冒烟脚本，用于快速验证认证与基础受保护接口链路。
使用方法:
1) 先启动后端服务，再按文档命令运行脚本。
2) 可通过参数切换 BaseUrl、账号与密码，适配不同本地环境。
-->

# API Smoke Test Guide

## `smoke-auth-user-dictionary.ps1`

该脚本用于快速校验认证链路与核心受保护接口可用性（登录、用户信息、字典查询、刷新、登出）。

接口字段与返回结构请以 `docs/api/backend-api-reference.md` 为准。

## Usage (PowerShell)

```powershell
cd "E:\@Study documents\End\Code\Public Order Case Management System\PublicOrderCaseManagementSystem"
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-auth-user-dictionary.ps1 -TryRegister
```

Optional parameters:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-auth-user-dictionary.ps1 -BaseUrl "http://localhost:8080" -UserName "smoke_user" -Password "P@ssw0rd123"
```


