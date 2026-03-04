param(
    [switch]$NoClean
)

$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')

Push-Location $root
try {
    if ($NoClean) {
        & .\gradlew.bat testWorkflow
    } else {
        & .\gradlew.bat clean testWorkflow
    }

    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}
finally {
    Pop-Location
}