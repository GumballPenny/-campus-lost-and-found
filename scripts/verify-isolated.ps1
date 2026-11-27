$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot
$pgBin='C:/Program Files/PostgreSQL/18/bin'
if (-not (Test-Path "$pgBin/initdb.exe")) { throw 'PostgreSQL 18 command-line tools are required.' }
$data=Join-Path $projectRoot '.local/test-postgres'
New-Item -ItemType Directory -Path (Split-Path $data -Parent) -Force | Out-Null
if (-not (Test-Path "$data/PG_VERSION")) {
    & "$pgBin/initdb.exe" -D $data -U css -A trust --encoding=UTF8 --no-locale
    if ($LASTEXITCODE) { throw 'Cannot initialize isolated test PostgreSQL.' }
}
$listener=[System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback,0)
$listener.Start(); $port=$listener.LocalEndpoint.Port; $listener.Stop()
$started=$false
try {
    & "$pgBin/pg_ctl.exe" -D $data -l "$data/server.log" -o "-p $port -h 127.0.0.1 -c max_connections=100" -w start
    if ($LASTEXITCODE) { throw 'Cannot start isolated test PostgreSQL.' }
    $started=$true
    foreach ($dbName in @('campus_items')) {
        $exists=& "$pgBin/psql.exe" -h 127.0.0.1 -p $port -U css -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$dbName'"
        if ($exists -ne '1') {
            & "$pgBin/createdb.exe" -h 127.0.0.1 -p $port -U css $dbName
            if ($LASTEXITCODE) { throw "Cannot create test database $dbName" }
        }
    }
    $env:SPRING_PROFILES_ACTIVE='dev'
    $env:DB_URL="jdbc:postgresql://127.0.0.1:$port/campus_items"
    $env:DB_USERNAME='css'; $env:DB_PASSWORD='css'
    & "$PSScriptRoot/maven.ps1" '-q' 'verify'
    Write-Output 'PASS: items service tests passed in isolated PostgreSQL. Existing css database was not used.'
} finally {
    if ($started) { & "$pgBin/pg_ctl.exe" -D $data -m fast -w stop }
}
