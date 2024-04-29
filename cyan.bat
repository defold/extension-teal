@echo off
setlocal
set SCRIPT_DIR=%~dp0..
set LUAROCKS_SYSCONFDIR=%SCRIPT_DIR%
"%SCRIPT_DIR%\bin\luajit-64.exe" -e "package.path=\"%SCRIPT_DIR:\=\\%/share/lua/5.1/?.lua;%SCRIPT_DIR:\=\\%/share/lua/5.1/?/init.lua;\"..package.path;package.cpath=\"%SCRIPT_DIR:\=\\%/lib/lua/5.1/?.dll;\"..package.cpath;local k,l,_=pcall(require,'luarocks.loader') _=k and l.add_context('cyan','0.3.0-1')" "%SCRIPT_DIR%\lib\luarocks\rocks-5.1\cyan\0.3.0-1\bin\cyan" %*
exit /b %ERRORLEVEL%
