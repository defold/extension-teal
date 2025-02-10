# CHANGELOG

## Versioning

This library is versioned based on Semantic Versioning ([SemVer](https://semver.org/)).

#### Version scoping

The scope of what is covered by the version number excludes:

- error messages; the text of the messages can change, unless specifically documented.

#### Releasing new versions

- create a release branch
- update the changelog below
- update version and copyright-years in `./LICENSE.md` and `./src/core.c` (in module constants)
- create a new rockspec and update the version inside the new rockspec:<br/>
  `cp luasystem-scm-0.rockspec ./rockspecs/luasystem-X.Y.Z-1.rockspec`
- clean and render the docs: run `ldoc .`
- commit the changes as `Release vX.Y.Z`
- push the commit, and create a release PR
- after merging tag the release commit with `vX.Y.Z`
- upload to LuaRocks:<br/>
  `luarocks upload ./rockspecs/luasystem-X.Y.Z-1.rockspec --api-key=ABCDEFGH`
- test the newly created rock:<br/>
  `luarocks install luasystem`

## Version history

### version 0.4.5, released 18-Dec-2024

- Fix: suppress a warning when building with clang
- Fix: do not rely on luaconf.h to include limits.h, fixes builds with latest LuaJIT (#38).

### version 0.4.4, released 03-Sep-2024

- Fix: include all objects in Makefile

### version 0.4.3, released 28-Aug-2024

- Chore: add compiler error on Windows if Virtual Terminal Processing is unavailable.
- Fix: fix the freebsd build

### Version 0.4.2, released 25-Jun-2024

- Fix: include additional headers for some MinGW installations

### Version 0.4.1, released 25-Jun-2024

- Fix: when compiling with `msys2` the `conio.h` header is required

### Version 0.4.0, released 20-Jun-2024

- Feat: `getconsoleflags` and `setconsoleflags` for getting/setting the current console configuration flags on Windows
- Feat: `getconsolecp` and `setconsolecp` for getting/setting the console codepage on Windows
- Feat: `getconsoleoutputcp` and `setconsoleoutputcp` for getting/setting the console output codepage on Windows
- Feat: `tcgetattr` and `tcsetattr` for getting/setting the current console configuration flags on Posix
- Feat: `getnonblock` and `setnonblock` for getting/setting the non-blocking flag on Posix
- Feat: `bitflags`: a support feature for the above flag type controls to facilitate bit manipulation without resorting to binary operations (to also support PuC Lua 5.1)
- Feat: `readkey` reads a keyboard input from `stdin` in a non-blocking way (utf8, also on Windows)
- Feat: `readansi` reads a keyboard input from `stdin` in a non-blocking way, parses ansi and utf8 sequences
- Feat: `termsize` gets the current terminal size in rows and columns
- Feat: `utf8cwidth` and `utf8swidth` for getting the display width (in columns) of respectively a single utf8 character, or a utf8 string
- Feat: helpers; `termbackup`, `termrestore`, `autotermrestore`, and `termwrap` for managing the many terminal settings on all platforms.

### Version 0.3.0, released 15-Dec-2023

- Feat: on Windows `sleep` now has a precision parameter
- Feat: `setenv` added to set environment variables.
- Feat: `getenvs` added to list environment variables.
- Feat: `getenv` added to get environment variable previously set (Windows).
- Feat: `random` added to return high-quality random bytes
- Feat: `isatty` added to check if a file-handle is a tty

### Version 0.2.1, released 02-Oct-2016

### Version 0.2.0, released 08-May-2016

### Version 0.1.1, released 10-Apr-2016

### Version 0.1.0, released 11-Feb-2016

- initial release
