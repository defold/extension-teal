@echo off
setlocal
set SCRIPT_DIR=%~dp0..
"%SCRIPT_DIR%\bin\lua-language-server.exe" "%SCRIPT_DIR%\main.lua" %*
exit /b %ERRORLEVEL%
