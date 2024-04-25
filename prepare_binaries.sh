#!/usr/bin/env bash

set -o errexit

## arm64-macos
unzip -o -q macOS-ARM64-cyan.zip -d arm64-macos
cp ../defold/com.dynamo.cr/com.dynamo.cr.bob/libexec/arm64-macos/luajit-64 arm64-macos/bin
sed -i '' 's!/Users/runner/work/cyan-builder/cyan-builder/.luarocks!build/plugins/teal/plugins/bin/arm64-macos!g' arm64-macos/bin/cyan
sed -i '' 's!/Users/runner/work/cyan-builder/cyan-builder/.lua/bin/lua!build/plugins/teal/plugins/bin/arm64-macos/bin/luajit-64!g' arm64-macos/bin/cyan
rm teal/plugins/arm64-macos.zip
zip -r -q teal/plugins/arm64-macos.zip arm64-macos/*
echo "Done"