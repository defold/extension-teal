@echo off
setlocal
set "LUAROCKS_SYSCONFDIR=C:\Program Files/luarocks"
"D:\a\cyan-builder\cyan-builder\.lua\bin\lua.exe" -e "package.path=\"C:\\Users\\runneradmin\\AppData\\Roaming/luarocks/share/lua/5.1/?.lua;C:\\Users\\runneradmin\\AppData\\Roaming/luarocks/share/lua/5.1/?/init.lua;\"..package.path;package.cpath=\"C:\\Users\\runneradmin\\AppData\\Roaming/luarocks/lib/lua/5.1/?.dll;\"..package.cpath;local k,l,_=pcall(require,'luarocks.loader') _=k and l.add_context('cyan','0.3.0-1')" "C:\Users\runneradmin\AppData\Roaming\luarocks\lib\luarocks\rocks-5.1\cyan\0.3.0-1\bin\cyan" %*
exit /b %ERRORLEVEL%
