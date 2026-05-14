<#
.SYNOPSIS
    Starts both the LabourHand Backend (Spring Boot) and Frontend (Vite) in separate console windows.
#>

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Refresh environment variables so newly installed tools like Maven and Node are picked up
$env:PATH = [Environment]::GetEnvironmentVariable('PATH', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('PATH', 'User')

$BackendDir = Join-Path $ScriptDir "Backend"
$FrontendDir = Join-Path $ScriptDir "simple-frontend"

Write-Host "🚀 Starting LabourHand Development Servers..." -ForegroundColor Cyan

# Start Backend 
Write-Host "☕ Starting Spring Boot Backend (Port 8081)..." -ForegroundColor Yellow
Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "cd '$BackendDir'; Write-Host 'Starting Spring Boot...'; mvn spring-boot:run" -WindowStyle Normal

# Start Frontend 
Write-Host "⚛️ Starting Frontend (Port 5173)..." -ForegroundColor Blue
Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "cd '$FrontendDir'; Write-Host 'Starting simple-frontend server...'; npx -y serve -p 5173" -WindowStyle Normal

Write-Host "✅ Both servers are starting up in separate windows!" -ForegroundColor Green
Write-Host "- Backend API: http://localhost:8081/api"
Write-Host "- Frontend UI: http://localhost:5173"