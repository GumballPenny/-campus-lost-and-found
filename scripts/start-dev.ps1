$ErrorActionPreference='Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
if (-not $env:DB_USERNAME) {
    $dbUser=Read-Host 'PostgreSQL username (Enter = postgres)'
    $env:DB_USERNAME=if ($dbUser) { $dbUser } else { 'postgres' }
}
if (-not $env:DB_PASSWORD) {
    $secure=Read-Host 'PostgreSQL password (input is hidden)' -AsSecureString
    $env:DB_PASSWORD=[System.Net.NetworkCredential]::new('', $secure).Password
}
if (-not $env:DB_URL) { $env:DB_URL='jdbc:postgresql://localhost:5432/css' }
$env:SPRING_PROFILES_ACTIVE='dev'
Write-Output 'Starting Lab 5. Open http://localhost:8080/ when Started Application appears. Ctrl+C stops it.'
& "$PSScriptRoot/maven.ps1" 'spring-boot:run'
