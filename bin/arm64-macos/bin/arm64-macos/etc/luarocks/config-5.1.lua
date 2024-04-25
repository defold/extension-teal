-- LuaRocks configuration

rocks_trees = {
   { name = "user", root = home .. "/.luarocks" };
   { name = "system", root = "/Users/runner/work/cyan-builder/cyan-builder/.luarocks" };
}
lua_interpreter = "lua";
variables = {
   LUA_DIR = "/Users/runner/work/cyan-builder/cyan-builder/.lua";
   LUA_BINDIR = "/Users/runner/work/cyan-builder/cyan-builder/.lua/bin";
}
