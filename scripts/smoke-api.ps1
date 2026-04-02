param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$UserName = "smoke_user",
    [string]$Password = "P@ssw0rd123",
    [switch]$TryRegister
)

$ErrorActionPreference = "Stop"

function Write-Step($message) {
    Write-Host "`n==> $message" -ForegroundColor Cyan
}

function Assert-ApiResponse($response, $stepName) {
    if ($null -eq $response.code -or $null -eq $response.message) {
        throw "[$stepName] Response is not ApiResponse envelope."
    }
}

$headers = @{ "Content-Type" = "application/json" }

if ($TryRegister) {
    Write-Step "Register test user (ignore duplicate error)"
    $registerBody = @{
        name = $UserName
        password = $Password
        confirmPassword = $Password
    } | ConvertTo-Json

    try {
        $registerResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/register" -Headers $headers -Body $registerBody
        Assert-ApiResponse $registerResp "register"
        Write-Host "Register result: $($registerResp.message)"
    }
    catch {
        Write-Host "Register skipped or failed (possible duplicate user): $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Step "Login"
$loginBody = @{
    name = $UserName
    password = $Password
} | ConvertTo-Json

$loginResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/login" -Headers $headers -Body $loginBody
Assert-ApiResponse $loginResp "login"
if (-not $loginResp.data.token) {
    throw "[login] token is missing."
}
if (-not $loginResp.data.refreshToken) {
    throw "[login] refreshToken is missing."
}

$token = $loginResp.data.token
$refreshToken = $loginResp.data.refreshToken
$authHeaders = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Step "Get current user (/api/users/me)"
$meResp = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/users/me" -Headers $authHeaders
Assert-ApiResponse $meResp "users/me"
Write-Host "Current user: $($meResp.data.name)"

Write-Step "List users (/api/users?page=1&size=5)"
$usersResp = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/users?page=1&size=5" -Headers $authHeaders
Assert-ApiResponse $usersResp "users"

Write-Step "List case types dictionary"
$dictResp = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/dictionaries/case-types" -Headers $authHeaders
Assert-ApiResponse $dictResp "dict-case-types"

Write-Step "Refresh token"
$refreshBody = @{ refreshToken = $refreshToken } | ConvertTo-Json
$refreshResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/refresh" -Headers $authHeaders -Body $refreshBody
Assert-ApiResponse $refreshResp "refresh"

Write-Step "Logout"
$logoutBody = @{ refreshToken = $refreshResp.data.refreshToken } | ConvertTo-Json
$logoutResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/logout" -Headers @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $($refreshResp.data.token)"
} -Body $logoutBody
Assert-ApiResponse $logoutResp "logout"

Write-Host "`nSmoke API checks completed successfully." -ForegroundColor Green

