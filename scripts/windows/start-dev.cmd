@echo off
setlocal

set ROOT=%~dp0..\..

echo Starting backend: http://localhost:8080
start "daily-finance-backend" cmd /k "cd /d %ROOT%\backend && if exist mvnw.cmd (call mvnw.cmd clean package -DskipTests) else (call mvn clean package -DskipTests) && java -jar target\daily-finance-backend-1.0.0.jar"

echo Starting frontend: http://localhost:5173
start "daily-finance-frontend" cmd /k "cd /d %ROOT%\frontend && npm install && npm run dev"

echo Development windows started. Close the windows or run stop-dev.cmd to stop services.
endlocal
