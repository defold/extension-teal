#!/usr/bin/env bash

set -o errexit

## arm64-macos
mkdir -p bin/arm64-macos
unzip -o -q macOS-ARM64-cyan.zip -d bin/arm64-macos
cp ../defold/com.dynamo.cr/com.dynamo.cr.bob/libexec/arm64-macos/luajit-64 bin/arm64-macos/bin
sed -i '' 's!/Users/runner/work/cyan-builder/cyan-builder/.luarocks!build/plugins/teal/plugins/bin/arm64-macos!g' bin/arm64-macos/bin/cyan
sed -i '' 's!/Users/runner/work/cyan-builder/cyan-builder/.lua/bin/lua!build/plugins/teal/plugins/bin/arm64-macos/bin/luajit-64!g' bin/arm64-macos/bin/cyan
rm teal/plugins/arm64-macos.zip
zip -r -q teal/plugins/arm64-macos.zip bin
rm -r bin
echo "Done"