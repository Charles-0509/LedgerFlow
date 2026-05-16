$ErrorActionPreference = "Stop"

$Root = Resolve-Path "$PSScriptRoot\..\.."
$Backend = Join-Path $Root "backend"
$Frontend = Join-Path $Root "frontend"
$DbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$DbName = if ($env:DB_NAME) { $env:DB_NAME } else { "daily_finance" }
$DbUser = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "root" }
$DbPassword = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { $null }

function Stop-ExistingDevProcesses {
  Write-Host "Stopping existing services on ports 8080 and 5173 when found..."
  Get-NetTCPConnection -LocalPort 8080,5173 -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object {
      Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
    }
}

function Read-PlainPassword {
  if ($DbPassword) {
    return $DbPassword
  }

  $secure = Read-Host "Enter MySQL password for user $DbUser" -AsSecureString
  $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
  try {
    return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
  } finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
  }
}

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

function Ensure-MySqlServerRunning {
  if ($DbHost -notin @("localhost", "127.0.0.1", "::1")) {
    return
  }

  $portOpen = Get-NetTCPConnection -LocalPort $DbPort -ErrorAction SilentlyContinue
  if ($portOpen) {
    return
  }

  $services = Get-Service -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match "mysql" -or $_.DisplayName -match "mysql" } |
    Sort-Object @{ Expression = { if ($_.Name -match "9\\.7|97" -or $_.DisplayName -match "9\\.7|97") { 0 } else { 1 } } }, Name

  $service = $services | Select-Object -First 1
  if (-not $service) {
    throw "MySQL server is not listening on $DbHost`:$DbPort and no local MySQL Windows service was found."
  }

  if ($service.Status -ne "Running") {
    Write-Host "Starting MySQL service: $($service.Name)"
    try {
      Start-Service -Name $service.Name -ErrorAction Stop
    } catch {
      throw "MySQL service '$($service.Name)' is stopped and could not be started. Start it manually or run PowerShell as Administrator."
    }
  }

  for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 1
    $portOpen = Get-NetTCPConnection -LocalPort $DbPort -ErrorAction SilentlyContinue
    if ($portOpen) {
      Write-Host "MySQL server is listening on $DbHost`:$DbPort"
      return
    }
  }

  throw "MySQL service '$($service.Name)' started, but port $DbPort did not become ready in time."
}

function Invoke-MySqlText {
  param(
    [string] $MySqlExe,
    [string] $SqlText,
    [string] $Database = ""
  )

  $databaseArg = if ($Database) { " $Database" } else { "" }
  $command = "`"$MySqlExe`" --default-character-set=utf8mb4 -h $DbHost -P $DbPort -u $DbUser$databaseArg -N -B -e `"$SqlText`""
  $output = cmd.exe /c $command
  if ($LASTEXITCODE -ne 0) {
    throw "mysql command failed: $SqlText"
  }
  return ($output | Out-String).Trim()
}

function Invoke-MySqlFile {
  param(
    [string] $MySqlExe,
    [string] $Database,
    [string] $SqlFile
  )

  $databaseArg = if ($Database) { " $Database" } else { "" }
  $command = "`"$MySqlExe`" --default-character-set=utf8mb4 -h $DbHost -P $DbPort -u $DbUser$databaseArg < `"$SqlFile`""
  cmd.exe /c $command
  if ($LASTEXITCODE -ne 0) {
    throw "mysql command failed for $SqlFile"
  }
}

function Initialize-DatabaseIfNeeded {
  param(
    [string] $MySqlExe
  )

  $mySqlExe = $MySqlExe
  Write-Host "Using MySQL client: $mySqlExe"
  Write-Host "Checking database: $DbName"

  $escapedDbName = $DbName.Replace("'", "''")
  $exists = Invoke-MySqlText -MySqlExe $mySqlExe -SqlText "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '$escapedDbName';"

  if ($exists -ne $DbName) {
    Write-Host "Database not found. Creating schema: $DbName"
    Invoke-MySqlFile -MySqlExe $mySqlExe -Database "" -SqlFile "$Root\database\schema.sql"
    Invoke-MySqlFile -MySqlExe $mySqlExe -Database $DbName -SqlFile "$Root\database\init-data.sql"
    Write-Host "Database initialization completed."
    return
  }

  $tableCount = Invoke-MySqlText -MySqlExe $mySqlExe -Database $DbName -SqlText "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '$escapedDbName' AND TABLE_NAME = 'user';"
  if ($tableCount -eq "0") {
    Write-Host "Database exists but tables are missing. Creating schema and seed data."
    Invoke-MySqlFile -MySqlExe $mySqlExe -Database "" -SqlFile "$Root\database\schema.sql"
    Invoke-MySqlFile -MySqlExe $mySqlExe -Database $DbName -SqlFile "$Root\database\init-data.sql"
    Write-Host "Database initialization completed."
    return
  }

  $seedReady = Invoke-MySqlText -MySqlExe $mySqlExe -Database $DbName -SqlText "SELECT CASE WHEN (SELECT COUNT(*) FROM ``user`` WHERE username = 'demo') > 0 AND (SELECT COUNT(*) FROM category WHERE user_id = 1) >= 11 AND (SELECT COUNT(*) FROM transaction_record WHERE user_id = 1) >= 5 THEN 1 ELSE 0 END;"
  if ($seedReady -ne "1") {
    Write-Host "Database exists but seed data is incomplete. Importing seed data."
    Invoke-MySqlFile -MySqlExe $mySqlExe -Database $DbName -SqlFile "$Root\database\init-data.sql"
    Write-Host "Seed data import completed."
    return
  }

  Write-Host "Database already initialized. Skipping initialization."
}

$mySqlExe = Resolve-MySqlClient
Write-Host "Using MySQL client: $mySqlExe"
Ensure-MySqlServerRunning

$clearDbPassword = -not $env:DB_PASSWORD
$plainPassword = Read-PlainPassword
try {
  $env:MYSQL_PWD = $plainPassword
  if ($clearDbPassword) {
    $env:DB_PASSWORD = $plainPassword
  }
  Initialize-DatabaseIfNeeded -MySqlExe $mySqlExe
} finally {
  Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}

Stop-ExistingDevProcesses

Write-Host "Starting backend: http://localhost:8080"
$backendCommand = "cd /d `"$Backend`" && if exist mvnw.cmd (call mvnw.cmd clean package -DskipTests) else (call mvn clean package -DskipTests) && java -jar target\daily-finance-backend-1.0.0.jar"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $backendCommand -WindowStyle Normal
if ($clearDbPassword) {
  Remove-Item Env:\DB_PASSWORD -ErrorAction SilentlyContinue
}

Write-Host "Starting frontend: http://localhost:5173"
$frontendCommand = "cd /d `"$Frontend`" && npm install && npm run dev"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $frontendCommand -WindowStyle Normal

Write-Host "Development windows started. Close the windows or run stop.ps1 to stop services."
