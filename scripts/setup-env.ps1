$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
$envPath=Join-Path $projectRoot '.env'
if(Test-Path -LiteralPath $envPath){ throw '.env already exists; keep your current secrets.' }
function New-CampusSecret {
    $bytes=[byte[]]::new(32)
    $random=[System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $random.GetBytes($bytes) } finally { $random.Dispose() }
    return [BitConverter]::ToString($bytes).Replace('-','').ToLowerInvariant()
}
$contents=@(
    'ITEMS_DB_PASSWORD='+(New-CampusSecret)
    'ACCOUNTS_DB_PASSWORD='+(New-CampusSecret)
    'CSS_JWT_SECRET='+(New-CampusSecret)
    'CSS_CUSTODIAN_INVITE='+(New-CampusSecret)
    'APP_PORT=8080'
    'CAMPUS_DOMAIN=localhost'
)
[System.IO.File]::WriteAllLines($envPath,$contents)
Write-Output 'Created local .env. Keep it private. The custodian invitation is CSS_CUSTODIAN_INVITE inside this file.'
