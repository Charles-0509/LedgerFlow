$ErrorActionPreference = "SilentlyContinue"

Get-NetTCPConnection -LocalPort 8080,5173 |
  Select-Object -ExpandProperty OwningProcess -Unique |
  ForEach-Object { Stop-Process -Id $_ -Force }

Get-CimInstance Win32_Process |
  Where-Object { $_.CommandLine -like "*daily-finance-backend-1.0.0.jar*" -or $_.CommandLine -like "*vite*" } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force }

Write-Host "Stopped processes listening on ports 8080 and 5173 when found."
