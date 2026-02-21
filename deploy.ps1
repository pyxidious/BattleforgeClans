$ErrorActionPreference = "Stop"

# --- Paths ---
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$serverDir = "C:\Users\emima\Desktop\Dev\TestServer"
$pluginsDir = Join-Path $serverDir "plugins"

$javaExe = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot\bin\java.exe"
$paperJar = "paper-1.21.11-117.jar"

# --- Build ---
Write-Host "==> Building plugin..." -ForegroundColor Cyan
Push-Location $projectDir
& .\gradlew.bat clean build
Pop-Location

# --- Find latest jar in build/libs ---
$libsDir = Join-Path $projectDir "build\libs"
$jar = Get-ChildItem -Path $libsDir -Filter "*.jar" |
Where-Object { $_.Name -notmatch "(-sources|-javadoc)\.jar$" } |
Sort-Object LastWriteTime -Descending |
Select-Object -First 1

if (-not $jar) {
    throw "No plugin jar found in $libsDir"
}

# --- Copy to server plugins ---
Write-Host "==> Copying $($jar.Name) to plugins..." -ForegroundColor Cyan
Copy-Item -Path $jar.FullName -Destination $pluginsDir -Force

# --- Start server ---
Write-Host "==> Starting Paper server..." -ForegroundColor Cyan
Push-Location $serverDir
& $javaExe -Xms2G -Xmx2G -jar $paperJar nogui
Pop-Location