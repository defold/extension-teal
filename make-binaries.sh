#!/usr/bin/env sh

set -o errexit
# set -o xtrace

CYAN_BUILDER_VERSION="v1.71"

if [ -d dist ]; then
  rm -r dist
fi
mkdir -p dist

pairs="Windows-X64-cyan:x86_64-win32 macOS-ARM64-cyan:arm64-macos Linux-X64-cyan:x86_64-linux macOS-X64-cyan:x86_64-macos"

for pair in $pairs
do
  cyan_id="${pair%%:*}"
  platform_id="${pair##*:}"
  wget -q -O "dist/$cyan_id.zip" https://github.com/vlaaad/cyan-builder/releases/download/$CYAN_BUILDER_VERSION/$cyan_id.zip
  mkdir -p "dist/$platform_id/bin/$platform_id"
  unzip -o -q "dist/$cyan_id.zip" -d "dist/$platform_id/bin/$platform_id"
  find "dist/$platform_id/bin/$platform_id" -type f -name '*.tl' -delete


  if [ "$platform_id" = "x86_64-win32" ]; then
    sed -Ei '' '
      3c\
set SCRIPT_DIR=%~dp0..\
set "SCRIPT_DIR_ESC=%SCRIPT_DIR:\\=\\\\%"
      s@[A-Z]:\\\\([^; ]*\\\\)?Roaming\\\\luarocks\\\\@%SCRIPT_DIR_ESC%\\\\@g;
      s@[A-Z]:\\([^; ]*\\)?Roaming\\luarocks\\@%SCRIPT_DIR%\\@g;
      s@[A-Z]:\\([^; ]*\\)?.lua\\@%SCRIPT_DIR%\\@g;
    ' "dist/$platform_id/bin/$platform_id/bin/cyan.bat"
  else
    sed -Ei '' '
      s/\"/\\\"/g;
      s/\\\"\$@\\\"/"$@"/g;
      s/'"'"'/"/g;
      s@/[^; ]*\.luarocks/@\$SCRIPT_DIR/@g;
      s@/[^; ]*\.lua/@\$SCRIPT_DIR/@g;
      1a\
SCRIPT_DIR="$(dirname -- "$(readlink -f -- "$0")")/.."
    ' "dist/$platform_id/bin/$platform_id/bin/cyan"
  fi

  rm "teal/plugins/$platform_id.zip"
  cd "dist/$platform_id"
  zip -r -q "../../teal/plugins/$platform_id.zip" "bin"
  cd "../.."
done

echo "Making binaries: done"