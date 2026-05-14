<#
.SYNOPSIS
    Installs MySQL Server 8.x (if not present), starts the service,
    creates the labourhand_db database, applies the schema,
    and seeds initial data for the LabourHand project.

.USAGE
    Run from an ELEVATED (Administrator) PowerShell terminal:
    .\setup_db.ps1
#>

$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$SchemaFile = Join-Path $ScriptDir "Backend\src\main\resources\schema.sql"
$DataFile   = Join-Path $ScriptDir "Backend\src\main\resources\data.sql"

$DB_NAME = "labourhand_db"
$DB_USER = "root"
$DB_PASS = "root"
$DB_PORT = 3306

# == Helper: find mysql.exe ====================================================
function Find-MySQLClient {
    # 1. Already on PATH?
    $cli = Get-Command mysql -ErrorAction SilentlyContinue
    if ($cli) { return $cli.Source }

    # 2. Refresh PATH (picks up newly installed tools)
    $env:PATH = [Environment]::GetEnvironmentVariable('PATH','Machine') + ';' +
                [Environment]::GetEnvironmentVariable('PATH','User')
    $cli = Get-Command mysql -ErrorAction SilentlyContinue
    if ($cli) { return $cli.Source }

    # 3. Well-known install locations (MySQL Server + MariaDB)
    $patterns = @(
        "C:\Program Files\MySQL\MySQL Server *\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server*\bin\mysql.exe",
        "C:\Program Files\MariaDB*\bin\mysql.exe",
        "C:\Program Files (x86)\MySQL\*\bin\mysql.exe",
        "C:\Program Files (x86)\MariaDB*\bin\mysql.exe"
    )
    foreach ($p in $patterns) {
        $hit = Get-Item $p -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
        if ($hit) { return $hit.FullName }
    }
    return $null
}

# == Helper: wait for TCP port =================================================
function Wait-ForPort {
    param([int]$Port, [int]$TimeoutSeconds = 90)
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    Write-Host "  Waiting for port $Port" -NoNewline
    while ((Get-Date) -lt $deadline) {
        $tcp = New-Object System.Net.Sockets.TcpClient
        try {
            $tcp.Connect("127.0.0.1", $Port)
            $tcp.Close()
            Write-Host " [OK]" -ForegroundColor Green
            return $true
        } catch { }
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 2
    }
    Write-Host " [TIMEOUT]" -ForegroundColor Red
    return $false
}

# =============================================================================
Write-Host ""
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "  LabourHand - MySQL Database Setup" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Check if running as Admin
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "  [WARN] WARNING: This script is not running as Administrator." -ForegroundColor Yellow
    Write-Host "  If MySQL needs to be installed or initialized, it will likely fail." -ForegroundColor Yellow
}

# == Step 1: Detect existing MySQL/MariaDB service =============================
Write-Host ""
Write-Host "[1/4] Checking for MySQL Server service..." -ForegroundColor Yellow

$dbService = Get-Service -Name "MySQL*","MariaDB" -ErrorAction SilentlyContinue |
             Where-Object { $_.DisplayName -notlike "*Workbench*" -and $_.DisplayName -notlike "*Notifier*" } |
             Select-Object -First 1

if (-not $dbService) {
    Write-Host "  No MySQL Server service found." -ForegroundColor Yellow
    
    $mysqldExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
    if (-not (Test-Path $mysqldExe)) {
        Write-Host "  Installing Oracle.MySQL via winget..." -ForegroundColor Yellow
        winget install Oracle.MySQL --accept-package-agreements --accept-source-agreements --silent
        if ($LASTEXITCODE -ne 0) {
            Write-Host ""
            Write-Host "  [FAIL] winget install failed." -ForegroundColor Red
            Write-Host "  Download MySQL Installer from: https://dev.mysql.com/downloads/installer/" -ForegroundColor Red
            exit 1
        }
        # Refresh PATH
        $env:PATH = [Environment]::GetEnvironmentVariable('PATH','Machine') + ';' +
                    [Environment]::GetEnvironmentVariable('PATH','User')
    }

    if (Test-Path $mysqldExe) {
        Write-Host "  Initializing MySQL Server..." -ForegroundColor Yellow
        $dataDir = "C:\ProgramData\MySQL\MySQL Server 8.4\Data"
        if (-not (Test-Path $dataDir)) {
            New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
        }
        # Initialize insecure so root has no password initially
        & $mysqldExe --initialize-insecure --datadir="$dataDir" --console
        
        # Install service explicitly pointing to the data directory
        & sc.exe create MySQL84 binPath= "`"$mysqldExe`" MySQL84" start= auto | Out-Null
        $regPath = "HKLM:\SYSTEM\CurrentControlSet\Services\MySQL84"
        $fixedBinPath = "`"$mysqldExe`" --datadir=`"$dataDir`" MySQL84"
        Set-ItemProperty -Path $regPath -Name ImagePath -Value $fixedBinPath -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    }

    # Re-check service
    $dbService = Get-Service -Name "MySQL*","MariaDB" -ErrorAction SilentlyContinue |
                 Where-Object { $_.DisplayName -notlike "*Workbench*" } |
                 Select-Object -First 1
    if (-not $dbService) {
        Write-Host "  [FAIL] Service still not found after install. Please reboot and re-run as Administrator." -ForegroundColor Red
        exit 1
    }
} else {
    # Proactively fix the binPath if it was installed previously without the datadir
    $mysqldExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
    $dataDir = "C:\ProgramData\MySQL\MySQL Server 8.4\Data"
    if ((Test-Path $mysqldExe) -and (Test-Path $dataDir)) {
        $regPath = "HKLM:\SYSTEM\CurrentControlSet\Services\$($dbService.Name)"
        $fixedBinPath = "`"$mysqldExe`" --datadir=`"$dataDir`" MySQL84"
        Set-ItemProperty -Path $regPath -Name ImagePath -Value $fixedBinPath -ErrorAction SilentlyContinue
    }
}
Write-Host "  [OK] Found service: $($dbService.DisplayName) [$($dbService.Name)]" -ForegroundColor Green

# == Step 2: Start the service =================================================
Write-Host ""
Write-Host "[2/4] Starting MySQL service..." -ForegroundColor Yellow

if ($dbService.Status -ne 'Running') {
    Start-Service $dbService.Name -ErrorAction Stop
    Start-Sleep -Seconds 5
    $dbService.Refresh()
}

if ($dbService.Status -eq 'Running') {
    Write-Host "  [OK] Service is Running." -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Service did not start. Status: $($dbService.Status)" -ForegroundColor Red
    exit 1
}

if (-not (Wait-ForPort -Port $DB_PORT)) {
    Write-Host "  [FAIL] Port $DB_PORT never opened. MySQL may have an error." -ForegroundColor Red
    exit 1
}

# == Step 3: Locate mysql.exe =================================================
Write-Host ""
Write-Host "[3/4] Locating mysql client..." -ForegroundColor Yellow
$mysqlExe = Find-MySQLClient
if (-not $mysqlExe) {
    Write-Host "  [FAIL] mysql.exe not found. Please add MySQL Server bin to PATH." -ForegroundColor Red
    exit 1
}
Write-Host "  [OK] mysql client: $mysqlExe" -ForegroundColor Green

# == Helper: run SQL with optional password =====================================
function Invoke-SQL {
    param([string]$Sql, [string]$Database = "", [string]$Password = $DB_PASS)
    $args_ = @("-u", $DB_USER, "--port=$DB_PORT", "--connect-timeout=10")
    if ($Password) { $args_ += "--password=$Password" }
    if ($Database)  { $args_ += $Database }
    $Sql | & $mysqlExe @args_ 2>&1
    return $LASTEXITCODE
}

# == Step 4: Create DB + apply schema + seed ===================================
Write-Host ""
Write-Host "[4/4] Setting up database..." -ForegroundColor Yellow

# Try connecting - MySQL fresh install may have empty root password OR prompt for one
$testSql = "SELECT 1;"
$exitCode = Invoke-SQL -Sql $testSql -Password $DB_PASS
if ($exitCode -ne 0) {
    Write-Host "  [WARN] Could not connect with password='$DB_PASS'. Trying empty password..." -ForegroundColor Yellow
    $exitCode = Invoke-SQL -Sql $testSql -Password ""
    if ($exitCode -ne 0) {
        Write-Host ""
        Write-Host "  [FAIL] Cannot connect to MySQL as root." -ForegroundColor Red
        Write-Host "  If this is a fresh install, your temp root password is in:" -ForegroundColor Yellow
        Write-Host "    C:\ProgramData\MySQL\MySQL Server 8.0\Data\*.err" -ForegroundColor Yellow
        Write-Host "  Or run: ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';" -ForegroundColor Yellow
        exit 1
    }
    $DB_PASS = ""   # use empty password for the rest of this session
}
Write-Host "  [OK] Connected to MySQL." -ForegroundColor Green

# If password is empty, set it to root so backend doesn't fail
if ($DB_PASS -eq "") {
    Write-Host "  Setting root password to 'root'..." -ForegroundColor Yellow
    $setPasswordSql = "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root'; FLUSH PRIVILEGES;"
    Invoke-SQL -Sql $setPasswordSql -Password "" | Out-Null
    $DB_PASS = "root"
}

# Create database
$createDbSql = "CREATE DATABASE IF NOT EXISTS ``$DB_NAME`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
$exitCode = Invoke-SQL -Sql $createDbSql -Password $DB_PASS
if ($exitCode -eq 0) {
    Write-Host "  [OK] Database '$DB_NAME' ready." -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Failed to create database." -ForegroundColor Red
    exit 1
}

# Apply schema
Write-Host "  Applying schema.sql..." -NoNewline
$content = Get-Content $SchemaFile -Raw
# Remove the CREATE DATABASE + USE statements (already handled above)
$content = $content -replace "(?ms)CREATE DATABASE.*?;\s*", ""
$content = $content -replace "(?ms)USE\s+\w+\s*;\s*", ""
$exitCode = Invoke-SQL -Sql $content -Database $DB_NAME -Password $DB_PASS
if ($exitCode -eq 0) {
    Write-Host " [OK]" -ForegroundColor Green
} else {
    Write-Host " [WARN] Some statements had errors (shown above). May be safe to ignore if tables already exist." -ForegroundColor Yellow
}

# Apply seed data
Write-Host "  Applying data.sql (seed data)..." -NoNewline
$seedContent = Get-Content $DataFile -Raw
$exitCode = Invoke-SQL -Sql $seedContent -Database $DB_NAME -Password $DB_PASS
if ($exitCode -eq 0) {
    Write-Host " [OK]" -ForegroundColor Green
} else {
    Write-Host " [WARN] Some INSERT IGNORE statements skipped (duplicates - safe)." -ForegroundColor Yellow
}

# == Done ======================================================================
Write-Host ""
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "  [SUCCESS] Database setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "  Database : $DB_NAME" -ForegroundColor White
Write-Host "  Host     : localhost:$DB_PORT" -ForegroundColor White
Write-Host "  User     : root / $DB_PASS" -ForegroundColor White
Write-Host ""
Write-Host "  -> Next step: run .\run_dev.ps1 to start the app." -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
