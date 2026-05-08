$ErrorActionPreference = "Stop"

$Root = Resolve-Path "$PSScriptRoot\..\.."
$DbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$DbName = if ($env:DB_NAME) { $env:DB_NAME } else { "daily_finance" }
$DbUser = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "root" }

Write-Host "Initializing database: $DbName"
cmd.exe /c "mysql -h $DbHost -P $DbPort -u $DbUser -p < `"$Root\database\schema.sql`""
cmd.exe /c "mysql -h $DbHost -P $DbPort -u $DbUser -p $DbName < `"$Root\database\init-data.sql`""
Write-Host "Database initialization completed."
