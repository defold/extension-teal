3c\
set SCRIPT_DIR=%~dp0..\
set "SCRIPT_DIR_ESC=%SCRIPT_DIR:\\=\\\\%"
s@[A-Z]:\\\\([^; ]*\\\\)?Roaming\\\\luarocks\\\\@%SCRIPT_DIR_ESC%\\\\@g;
s@[A-Z]:\\([^; ]*\\)?Roaming\\luarocks\\@%SCRIPT_DIR%\\@g;
s@[A-Z]:\\([^; ]*\\)?.lua\\@%SCRIPT_DIR%\\@g;