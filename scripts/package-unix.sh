#!/usr/bin/env sh
set -eu

mvn clean test package javafx:jlink

runtime_image="target/runtime-image"
distribution="target/dist"
if [ ! -d "$runtime_image" ]; then
    echo "The JavaFX runtime image was not created at $runtime_image" >&2
    exit 1
fi

mkdir -p "$distribution"
jpackage --type app-image --name ChessEngine --dest "$distribution" \
    --module-path "$runtime_image" \
    --module chess.engine/chess.engine.gui.ChessApplication \
    --runtime-image "$runtime_image"

archive="target/Chess-Engine-$(uname -s | tr '[:upper:]' '[:lower:]')-$(uname -m).zip"
if command -v zip >/dev/null 2>&1; then
    (cd "$distribution" && zip -qr "../$(basename "$archive")" ChessEngine)
else
    echo "The zip utility is required to archive the portable application." >&2
    exit 1
fi
echo "Portable application: $archive"
