#!/usr/bin/env sh

set -o errexit

wget -q https://github.com/defold/lua-language-server/releases/download/v0.0.5/release.zip

mkdir -p dist
unzip -o -q release.zip "lsp-lua-language-server/plugins/*.zip" -d dist

pairs="Linux-X64-cyan:x86_64-linux macOS-ARM64-cyan:arm64-macos macOS-X64-cyan:x86_64-macos Windows-X64-cyan:x86_64-win32"

for pair in $pairs
do
   cyan_id="${pair%%:*}"
   platform_id="${pair##*:}"
   case "$platform_id" in
     *win32*) script_ext=".bat" ;;
     *) script_ext="" ;;
   esac
   mkdir -p "dist/$platform_id/bin/$platform_id"
   unzip -o -q "$cyan_id.zip" "share/lua/5.1/*.lua" -d "dist/$platform_id/bin/$platform_id"
   mkdir -p "dist/$platform_id/bin/$platform_id/script"

   ls -1 "dist/$platform_id/bin/$platform_id/share/lua/5.1/" | xargs -I '{}' mv "dist/$platform_id/bin/$platform_id/share/lua/5.1/{}" "dist/$platform_id/bin/$platform_id/script/"
   rm -r "dist/$platform_id/bin/$platform_id/share"
   unzip -o -q "dist/lsp-lua-language-server/plugins/$platform_id.zip" "bin/$platform_id/bin/*" -d "dist/$platform_id"
   cp "cyan$script_ext" "dist/$platform_id/bin/$platform_id/bin"
   echo "require(\"cyan.cli\")" > "dist/$platform_id/bin/$platform_id/main.lua"
   chmod +x "dist/$platform_id/bin/$platform_id/bin/cyan$script_ext"

   rm "teal/plugins/$platform_id.zip"
   cd "dist/$platform_id"
   zip -r -q "../../teal/plugins/$platform_id.zip" "bin"
   cd "../.."
done

rm release.zip
rm -r dist

echo "Making binaries: done"