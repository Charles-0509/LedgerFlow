@echo off
setlocal

set ROOT=%~dp0..\..

echo Starting backend: http://localhost:8080
start "daily-finance-backend" cmd /k "cd /d %ROOT%\backend && if exist mvnw.cmd (mvnw.cmd spring-boot:run) else (mvn spring-boot:run)"

echo Starting frontend: http://localhost:5173
start "daily-finance-frontend" cmd /k "cd /d %ROOT%\frontend && npm install && npm run dev"

echo Development windows started. Close the windows or run stop-dev.cmd to stop services.
endlocal
