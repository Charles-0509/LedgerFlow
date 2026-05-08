$ErrorActionPreference = "SilentlyContinue"

Get-NetTCPConnection -LocalPort 8080,5173 |
  Select-Object -ExpandProperty OwningProcess -Unique |
  ForEach-Object { Stop-Process -Id $_ -Force }

Write-Host "Stopped processes listening on ports 8080 and 5173 when found."
