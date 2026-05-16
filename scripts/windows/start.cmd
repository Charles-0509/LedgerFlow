@echo off
setlocal

set ROOT=%~dp0..\..
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306
if "%DB_NAME%"=="" set DB_NAME=daily_finance
if "%DB_USERNAME%"=="" set DB_USERNAME=root

echo Stopping existing services on ports 8080 and 5173 when found...
powershell -ExecutionPolicy Bypass -NoProfile -Command "Get-NetTCPConnection -LocalPort 8080,5173 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }"

echo Checking local MySQL server on port %DB_PORT%...
powershell -ExecutionPolicy Bypass -NoProfile -Command "$port='%DB_PORT%'; if (-not (Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue)) { $svc = Get-Service | Where-Object { $_.Name -match 'mysql' -or $_.DisplayName -match 'mysql' } | Sort-Object @{ Expression = { if ($_.Name -match '9\.7|97' -or $_.DisplayName -match '9\.7|97') { 0 } else { 1 } } }, Name | Select-Object -First 1; if (-not $svc) { throw 'MySQL server is not listening and no local MySQL service was found.' }; if ($svc.Status -ne 'Running') { Write-Host ('Starting MySQL service: ' + $svc.Name); Start-Service -Name $svc.Name }; for ($i=0; $i -lt 30; $i++) { Start-Sleep -Seconds 1; if (Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue) { Write-Host 'MySQL server is ready.'; exit 0 } }; throw 'MySQL service started, but port did not become ready in time.' }"
if errorlevel 1 exit /b 1

if "%DB_PASSWORD%"=="" set /p DB_PASSWORD=Enter MySQL password for user %DB_USERNAME%: 
set MYSQL_PWD=%DB_PASSWORD%

set MYSQL_CMD=mysql
if not "%MYSQL_EXE%"=="" if exist "%MYSQL_EXE%" set MYSQL_CMD=%MYSQL_EXE%
if "%MYSQL_CMD%"=="mysql" if exist "C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe" set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe
if "%MYSQL_CMD%"=="mysql" if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe

echo Using MySQL client: %MYSQL_CMD%
echo Checking database: %DB_NAME%
"%MYSQL_CMD%" --default-character-set=utf8mb4 -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -N -B -e "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '%DB_NAME%';" > "%TEMP%\ledgerflow_db_check.txt"
if errorlevel 1 exit /b 1

set /p DB_EXISTS=<"%TEMP%\ledgerflow_db_check.txt"
del "%TEMP%\ledgerflow_db_check.txt" >nul 2>nul

if "%DB_EXISTS%"=="%DB_NAME%" (
    "%MYSQL_CMD%" --default-character-set=utf8mb4 -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% %DB_NAME% -N -B -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%DB_NAME%' AND TABLE_NAME = 'user';" > "%TEMP%\ledgerflow_table_check.txt"
    if errorlevel 1 exit /b 1
    set /p TABLE_EXISTS=<"%TEMP%\ledgerflow_table_check.txt"
    del "%TEMP%\ledgerflow_table_check.txt" >nul 2>nul
    if "%TABLE_EXISTS%"=="0" (
        echo Database exists but tables are missing. Creating schema and seed data.
        "%MYSQL_CMD%" --default-character-set=utf8mb4 -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% < "%ROOT%\database\schema.sql"
        if errorlevel 1 exit /b 1
        "%MYSQL_CMD%" --default-character-set=utf8mb4 -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% %DB_NAME% < "%ROOT%\database\init-data.sql"
        if errorlevel 1 exit /b 1
    ) else (
        "%MYSQL_CMD%" --default-character-set=utf8mb4 -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% %DB_NAME% -N -B -e "SELECT CASE WHEN (SELECT COUNT(*) FROM `user` WHERE username = 'demo') > 0 AND (SELECT COUNT(*) FROM category WHERE user_id = 1) >= 11 AND (SELECT COUNT(*) FROM transaction_record WHERE user_id = 1) >= 5 THEN 1 ELSE 0 END;" > "%TEMP%\ledgerflow_seed_check.txt"
        if errorlevel 1 exit /b 1
        set /p SEED_EXISTS=<"%TEMP%\ledgerflow_seed_check.txt"
        del "%TEMP%\ledgerflow_seed_check.txt" >nul 2>nul
        if not "%SEED_EXISTS%"=="1" (
            echo Database exists but seed data is incomplete. Importing seed data.
            "%MYSQL_CMD%" --default-character-set=utf8mb4 -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% %DB_NAME% < "%ROOT%\database\init-data.sql"
            if errorlevel 1 exit /b 1
        ) else (
            echo Database already initialized. Skipping initialization.
        )
    )
) else (
    echo Database not found. Initializing database: %DB_NAME%
    "%MYSQL_CMD%" --default-character-set=utf8mb4 -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% < "%ROOT%\database\schema.sql"
    if errorlevel 1 exit /b 1
    "%MYSQL_CMD%" --default-character-set=utf8mb4 -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% %DB_NAME% < "%ROOT%\database\init-data.sql"
    if errorlevel 1 exit /b 1
    echo Database initialization completed.
)

echo Starting backend: http://localhost:8080
start "daily-finance-backend" cmd /k "cd /d %ROOT%\backend && if exist mvnw.cmd (call mvnw.cmd clean package -DskipTests) else (call mvn clean package -DskipTests) && java -jar target\daily-finance-backend-1.0.0.jar"

echo Starting frontend: http://localhost:5173
start "daily-finance-frontend" cmd /k "cd /d %ROOT%\frontend && npm install && npm run dev"

echo Development windows started. Close the windows or run stop.cmd to stop services.
set MYSQL_PWD=
endlocal
