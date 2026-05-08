$ErrorActionPreference = "Stop"

$Root = Resolve-Path "$PSScriptRoot\..\.."
$DbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$DbName = if ($env:DB_NAME) { $env:DB_NAME } else { "daily_finance" }
$DbUser = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "root" }

function Resolve-MySqlClient {
  if ($env:MYSQL_EXE -and (Test-Path $env:MYSQL_EXE)) {
    return $env:MYSQL_EXE
  }

  $command = Get-Command mysql.exe -ErrorAction SilentlyContinue
  if ($command) {
    return $command.Source
  }

  $candidates = @(
    "C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 9.4\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 9.3\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
  )

  foreach ($candidate in $candidates) {
    if (Test-Path $candidate) {
      return $candidate
    }
  }

  $found = Get-ChildItem "C:\Program Files\MySQL" -Recurse -Filter mysql.exe -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
  if ($found) {
    return $found
  }

  throw "mysql.exe was not found. Add MySQL bin to PATH or set MYSQL_EXE to the full mysql.exe path."
}

function Invoke-MySqlFile {
  param(
    [string] $MySqlExe,
    [string] $Database,
    [string] $SqlFile
  )

  $databaseArg = if ($Database) { " $Database" } else { "" }
  $command = "`"$MySqlExe`" -h $DbHost -P $DbPort -u $DbUser -p$databaseArg < `"$SqlFile`""
  cmd.exe /c $command
  if ($LASTEXITCODE -ne 0) {
    throw "mysql command failed for $SqlFile"
  }
}

$MySqlExe = Resolve-MySqlClient
Write-Host "Using MySQL client: $MySqlExe"
Write-Host "Initializing database: $DbName"

Invoke-MySqlFile -MySqlExe $MySqlExe -Database "" -SqlFile "$Root\database\schema.sql"
Invoke-MySqlFile -MySqlExe $MySqlExe -Database $DbName -SqlFile "$Root\database\init-data.sql"

Write-Host "Database initialization completed."
