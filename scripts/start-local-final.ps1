$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot
$pgBin='C:/Program Files/PostgreSQL/18/bin'
if (-not (Test-Path "$pgBin/initdb.exe")) { throw 'Install PostgreSQL 18 command-line tools.' }
if (-not (Test-Path '.env')) { & "$PSScriptRoot/setup-env.ps1" }
$settings=@{}
foreach ($line in Get-Content '.env') {
    if ($line -match '^([A-Z_]+)=(.*)$') { $settings[$matches[1]]=$matches[2] }
}
$appPort=if ($settings.APP_PORT) { [int]$settings.APP_PORT } else { 8080 }
$pgPort=55440; $accountsPort=18081
$busy=[System.Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners().Port
foreach ($port in @($appPort,$pgPort,$accountsPort)) {
    if ($busy -contains $port) { throw "Port $port is busy. Stop the previous project run first." }
}
Write-Output 'Building two services. Their tests are run separately by verify-isolated.ps1.'
& "$PSScriptRoot/maven.ps1" '-q' '-DskipTests' 'package'
& "$PSScriptRoot/maven.ps1" '-q' '-f' 'services/accounts-service/pom.xml' '-DskipTests' 'package'
$data=Join-Path $projectRoot '.local/demo-postgres'
$logs=Join-Path $projectRoot '.local/logs'
New-Item -ItemType Directory -Path $logs -Force | Out-Null
if (-not (Test-Path "$data/PG_VERSION")) {
    $passwordFile=Join-Path $logs 'pg-init-password.tmp'
    try {
        [System.IO.File]::WriteAllText($passwordFile,$settings.ITEMS_DB_PASSWORD)
        & "$pgBin/initdb.exe" -D $data -U campus -A scram-sha-256 --pwfile=$passwordFile --encoding=UTF8 --no-locale
        if ($LASTEXITCODE) { throw 'Cannot initialize demo database.' }
    } finally { Remove-Item -LiteralPath $passwordFile -ErrorAction SilentlyContinue }
}
$started=$false; $accountsProcess=$null; $itemsProcess=$null
try {
    & "$pgBin/pg_ctl.exe" -D $data -l "$logs/postgres.log" -o "-p $pgPort -h 127.0.0.1 -c max_connections=100" -w start
    if ($LASTEXITCODE) { throw 'Cannot start demo database.' }
    $started=$true
    $env:PGPASSWORD=$settings.ITEMS_DB_PASSWORD
    foreach ($dbName in @('campus_items','campus_accounts')) {
        $exists=& "$pgBin/psql.exe" -h 127.0.0.1 -p $pgPort -U campus -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$dbName'"
        if ($LASTEXITCODE) { throw 'Cannot connect to demo database. Keep the original .env for this demo cluster.' }
        if ($exists -ne '1') {
            & "$pgBin/createdb.exe" -h 127.0.0.1 -p $pgPort -U campus $dbName
            if ($LASTEXITCODE) { throw "Cannot create database $dbName" }
        }
    }
    Remove-Item Env:PGPASSWORD
    $env:CSS_JWT_SECRET=$settings.CSS_JWT_SECRET
    $env:CSS_CUSTODIAN_INVITE=$settings.CSS_CUSTODIAN_INVITE
    $env:ACCOUNTS_DB_URL="jdbc:postgresql://127.0.0.1:$pgPort/campus_accounts"
    $env:ACCOUNTS_DB_USER='campus'; $env:ACCOUNTS_DB_PASSWORD=$settings.ITEMS_DB_PASSWORD
    $env:ACCOUNTS_PORT="$accountsPort"; $env:SPRING_PROFILES_ACTIVE='final'
    $java=Join-Path $env:JAVA_HOME 'bin/java.exe'
    $accountsJar=Join-Path $projectRoot 'services/accounts-service/target/campus-accounts-1.0-SNAPSHOT.jar'
    $itemsJar=Join-Path $projectRoot 'target/campus-lost-and-found-1.0-SNAPSHOT.jar'
    $accountsProcess=Start-Process -FilePath $java -ArgumentList @('-Dserver.address=127.0.0.1','-jar',('"'+$accountsJar+'"')) -WorkingDirectory $projectRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput "$logs/accounts.log" -RedirectStandardError "$logs/accounts-error.log"
    $env:CSS_DB_URL="jdbc:postgresql://127.0.0.1:$pgPort/campus_items"
    $env:CSS_DB_USER='campus'; $env:CSS_DB_PASSWORD=$settings.ITEMS_DB_PASSWORD
    $env:CSS_ACCOUNTS_URL="http://127.0.0.1:$accountsPort"
    $itemsProcess=Start-Process -FilePath $java -ArgumentList @('-Dserver.address=127.0.0.1',"-Dserver.port=$appPort",'-jar',('"'+$itemsJar+'"')) -WorkingDirectory $projectRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput "$logs/items.log" -RedirectStandardError "$logs/items-error.log"
    Write-Output "Open http://localhost:$appPort/ after startup (about 15 seconds). Register your own account."
    Write-Output 'Senior invitation: CSS_CUSTODIAN_INVITE in your local .env. Logs: .local/logs. Ctrl+C stops all three processes.'
    Write-Output 'This is local final-mode demonstration. Docker deployment uses separate PostgreSQL containers.'
    $itemsProcess.WaitForExit()
    if ($itemsProcess.ExitCode -ne 0) { throw 'Items failed. Open .local/logs/items.log.' }
} finally {
    foreach ($child in @($itemsProcess,$accountsProcess)) {
        if ($child -and -not $child.HasExited) { Stop-Process -Id $child.Id -ErrorAction SilentlyContinue }
    }
    if ($started) { & "$pgBin/pg_ctl.exe" -D $data -m fast -w stop }
}
