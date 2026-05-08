@echo off
setlocal

powershell -ExecutionPolicy Bypass -NoProfile -Command "Get-NetTCPConnection -LocalPort 8080,5173 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }"
echo Stopped processes listening on ports 8080 and 5173 when found.
endlocal
