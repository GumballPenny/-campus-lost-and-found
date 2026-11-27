param([Parameter(ValueFromRemainingArguments=$true)][string[]]$MavenArgs)
$ErrorActionPreference='Stop'
$isJava21=$env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME/release") -and
    ((Get-Content "$env:JAVA_HOME/release" -Raw) -match 'JAVA_VERSION="21[.\"]')
if (-not $isJava21) {
    $jdk=Get-ChildItem 'C:/Program Files/Java' -Directory -Filter 'jdk-21*' -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $jdk) { throw 'Install Java 21 or set JAVA_HOME to its folder.' }
    $env:JAVA_HOME=$jdk.FullName
}
$mavenCommand=Get-Command mvn.cmd -ErrorAction SilentlyContinue
$mavenPath=if ($mavenCommand) { $mavenCommand.Source } else {
    'C:/Program Files/JetBrains/IntelliJ IDEA Community Edition 2024.3.3/plugins/maven/lib/maven3/bin/mvn.cmd'
}
if (-not (Test-Path -LiteralPath $mavenPath)) { throw 'Maven not found. Run the goal in the IntelliJ Maven panel or install Maven.' }
if (-not $MavenArgs) { $MavenArgs=@('verify') }
& $mavenPath @MavenArgs
if ($LASTEXITCODE -ne 0) { throw "Maven failed (exit $LASTEXITCODE). Read the first ERROR above." }
