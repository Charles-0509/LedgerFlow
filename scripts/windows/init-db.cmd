@echo off
setlocal

set ROOT=%~dp0..\..
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306
if "%DB_NAME%"=="" set DB_NAME=daily_finance
if "%DB_USERNAME%"=="" set DB_USERNAME=root

echo Initializing database: %DB_NAME%
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p < "%ROOT%\database\schema.sql"
if errorlevel 1 exit /b 1

mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p %DB_NAME% < "%ROOT%\database\init-data.sql"
if errorlevel 1 exit /b 1

echo Database initialization completed.
endlocal
