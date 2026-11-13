$ErrorActionPreference = "Stop"

mvn clean test package javafx:jlink
if ($LASTEXITCODE -ne 0) {
    throw "Maven build failed with exit code $LASTEXITCODE"
}

$runtimeImage = Join-Path $PSScriptRoot "..\target\runtime-image"
$distribution = Join-Path $PSScriptRoot "..\target\dist"
if (-not (Test-Path $runtimeImage)) {
    throw "The JavaFX runtime image was not created at $runtimeImage"
}

New-Item -ItemType Directory -Force -Path $distribution | Out-Null
jpackage --type app-image --name ChessEngine --dest $distribution `
    --module-path $runtimeImage --module chess.engine/chess.engine.gui.ChessApplication `
    --runtime-image $runtimeImage
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

$appImage = Join-Path $distribution "ChessEngine"
$archive = Join-Path (Split-Path $distribution -Parent) "Chess-Engine-windows-x64.zip"
Compress-Archive -Path $appImage -DestinationPath $archive -Force
Write-Output "Portable application: $archive"
