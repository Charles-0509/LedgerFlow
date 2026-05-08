$ErrorActionPreference = "Stop"

$Root = Resolve-Path "$PSScriptRoot\..\.."
$Backend = Join-Path $Root "backend"
$Frontend = Join-Path $Root "frontend"

Write-Host "Starting backend: http://localhost:8080"
$backendCommand = "cd /d `"$Backend`" && if exist mvnw.cmd (call mvnw.cmd clean package -DskipTests) else (call mvn clean package -DskipTests) && java -jar target\daily-finance-backend-1.0.0.jar"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $backendCommand -WindowStyle Normal

Write-Host "Starting frontend: http://localhost:5173"
$frontendCommand = "cd /d `"$Frontend`" && npm install && npm run dev"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $frontendCommand -WindowStyle Normal

Write-Host "Development windows started. Close the windows or run stop-dev.ps1 to stop services."
