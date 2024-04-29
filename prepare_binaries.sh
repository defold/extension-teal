#!/usr/bin/env bash

set -o errexit

rm -r bin

mkdir -p bin/arm64-macos/bin/arm64-macos
mkdir -p bin/x86_64-macos/bin/x86_64-macos
mkdir -p bin/x86_64-linux/bin/x86_64-linux
mkdir -p bin/x86_64-win32/bin/x86_64-win32

unzip -o -q macOS-ARM64-cyan.zip -d bin/arm64-macos/bin/arm64-macos
unzip -o -q macOS-X64-cyan.zip -d bin/x86_64-macos/bin/x86_64-macos
unzip -o -q Linux-X64-cyan.zip -d bin/x86_64-linux/bin/x86_64-linux
unzip -o -q Windows-X64-cyan.zip -d bin/x86_64-win32/bin/x86_64-win32

find bin -type f -name "*.tl" -delete

cp cyan bin/arm64-macos/bin/arm64-macos/bin
cp cyan bin/x86_64-macos/bin/x86_64-macos/bin
cp cyan bin/x86_64-linux/bin/x86_64-linux/bin
cp cyan.bat bin/x86_64-linux/bin/x86_64-win32/bin

cp ../defold/com.dynamo.cr/com.dynamo.cr.bob/libexec/arm64-macos/luajit-64 bin/arm64-macos/bin/arm64-macos/bin
cp ../defold/com.dynamo.cr/com.dynamo.cr.bob/libexec/x86_64-macos/luajit-64 bin/x86_64-macos/bin/x86_64-macos/bin
cp ../defold/com.dynamo.cr/com.dynamo.cr.bob/libexec/x86_64-linux/luajit-64 bin/x86_64-linux/bin/x86_64-linux/bin
cp ../defold/com.dynamo.cr/com.dynamo.cr.bob/libexec/x86_64-win32/luajit-64.exe bin/x86_64-win32/bin/x86_64-win32/bin

# assemble zips

rm teal/plugins/arm64-macos.zip
pushd bin/arm64-macos > /dev/null
zip -r -q ../../teal/plugins/arm64-macos.zip bin
popd > /dev/null

rm teal/plugins/x86_64-linux.zip
pushd bin/x86_64-linux > /dev/null
zip -r -q ../../teal/plugins/x86_64-linux.zip bin
popd > /dev/null

rm teal/plugins/x86_64-macos.zip
pushd bin/x86_64-macos > /dev/null
zip -r -q ../../teal/plugins/x86_64-macos.zip bin
popd > /dev/null

pushd bin/x86_64-win32 > /dev/null
zip -r -q ../../teal/plugins/x86_64-win32.zip bin
popd > /dev/null

echo "Done"
