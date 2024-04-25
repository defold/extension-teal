-- LuaRocks configuration

rocks_trees = {
   { name = "user", root = home .. "/.luarocks" };
   { name = "system", root = "/home/runner/work/cyan-builder/cyan-builder/.luarocks" };
}
lua_interpreter = "lua";
variables = {
   LUA_DIR = "/home/runner/work/cyan-builder/cyan-builder/.lua";
   LUA_BINDIR = "/home/runner/work/cyan-builder/cyan-builder/.lua/bin";
}
