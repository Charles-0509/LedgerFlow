$ErrorActionPreference = "Stop"

$Root = Resolve-Path "$PSScriptRoot\..\.."
$Backend = Join-Path $Root "backend"
$Frontend = Join-Path $Root "frontend"

Write-Host "Starting backend: http://localhost:8080"
$backendCommand = "cd /d `"$Backend`" && if exist mvnw.cmd (mvnw.cmd spring-boot:run) else (mvn spring-boot:run)"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $backendCommand -WindowStyle Normal

Write-Host "Starting frontend: http://localhost:5173"
$frontendCommand = "cd /d `"$Frontend`" && npm install && npm run dev"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $frontendCommand -WindowStyle Normal

Write-Host "Development windows started. Close the windows or run stop-dev.ps1 to stop services."
