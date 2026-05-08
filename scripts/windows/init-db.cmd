@echo off
setlocal

set ROOT=%~dp0..\..
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306
if "%DB_NAME%"=="" set DB_NAME=daily_finance
if "%DB_USERNAME%"=="" set DB_USERNAME=root

set MYSQL_CMD=mysql
if not "%MYSQL_EXE%"=="" if exist "%MYSQL_EXE%" set MYSQL_CMD=%MYSQL_EXE%
if "%MYSQL_CMD%"=="mysql" if exist "C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe" set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe
if "%MYSQL_CMD%"=="mysql" if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe

echo Using MySQL client: %MYSQL_CMD%
echo Initializing database: %DB_NAME%
"%MYSQL_CMD%" -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p < "%ROOT%\database\schema.sql"
if errorlevel 1 exit /b 1

"%MYSQL_CMD%" -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p %DB_NAME% < "%ROOT%\database\init-data.sql"
if errorlevel 1 exit /b 1

echo Database initialization completed.
endlocal
