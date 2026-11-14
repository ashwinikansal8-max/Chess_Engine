$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $repositoryRoot

$wixTools = Join-Path $env:LOCALAPPDATA "ChessEngine\build-tools\wix314"
if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or
    -not (Get-Command light.exe -ErrorAction SilentlyContinue)) {
    $candle = Join-Path $wixTools "candle.exe"
    $light = Join-Path $wixTools "light.exe"
    if (-not (Test-Path $candle) -or -not (Test-Path $light)) {
        $archive = Join-Path $env:LOCALAPPDATA "ChessEngine\build-tools\wix314-binaries.zip"
        New-Item -ItemType Directory -Force -Path (Split-Path $archive -Parent) | Out-Null
        $downloadUrl = "https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip"
        Invoke-WebRequest -Uri $downloadUrl -OutFile $archive

        $expectedHash = "6AC824E1642D6F7277D0ED7EA09411A508F6116BA6FAE0AA5F2C7DAA2FF43D31"
        $actualHash = (Get-FileHash -Path $archive -Algorithm SHA256).Hash
        if ($actualHash -ne $expectedHash) {
            Remove-Item -LiteralPath $archive -Force
            throw "WiX Toolset archive failed SHA-256 verification."
        }

        if (Test-Path $wixTools) {
            Remove-Item -LiteralPath $wixTools -Recurse -Force
        }
        Expand-Archive -Path $archive -DestinationPath $wixTools -Force
    }

    $env:PATH = "$wixTools;$env:PATH"
}

if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or
    -not (Get-Command light.exe -ErrorAction SilentlyContinue)) {
    throw "WiX Toolset v3 is required to create the Windows installer."
}

mvn clean test package javafx:jlink
if ($LASTEXITCODE -ne 0) {
    throw "Maven build failed with exit code $LASTEXITCODE"
}

$runtimeImage = Join-Path $PSScriptRoot "..\target\runtime-image"
if (-not (Test-Path $runtimeImage)) {
    throw "The JavaFX runtime image was not created at $runtimeImage"
}

$installerDirectory = Join-Path $repositoryRoot "target\installer"
New-Item -ItemType Directory -Force -Path $installerDirectory | Out-Null
$icon = Join-Path $repositoryRoot "src\main\resources\chess\engine\gui\chess-engine.ico"
jpackage --type msi --name "Chess Engine" --app-version "1.0.0" `
    --vendor "Chess Engine Project" --dest $installerDirectory `
    --module-path $runtimeImage --module "chess.engine/chess.engine.gui.ChessApplication" `
    --runtime-image $runtimeImage --icon $icon `
    --win-per-user-install --win-menu --win-menu-group "Chess Engine" `
    --win-shortcut --win-dir-chooser
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

$installer = Get-ChildItem -Path $installerDirectory -Filter "*.msi" -File | Select-Object -First 1
if (-not $installer) {
    throw "jpackage completed without producing a Windows installer in $installerDirectory"
}

Write-Output "Windows installer: $($installer.FullName)"
