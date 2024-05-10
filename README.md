# Using [Teal](https://github.com/teal-language/tl) in your project

1. Add a dependency on a latest release of this extension: https://github.com/defold/extension-teal/tags
   
   <img width="401" alt="image" src="https://user-images.githubusercontent.com/2209596/202223571-c77f0304-5202-4314-869d-7a90bbeec5ec.png">
2. Create `tlconfig.lua` file in the project root with the following content:
   ```lua
   return {
	    gen_target = "5.1",
	    gen_compat = "off",
	    include = {"**/*.tl"}
   }
   ```
3. Create `.tl` files — those can be required from scripts and lua modules as if they were `.lua` files 

# Developing this extension

If you modify teal executable, re-assemble the bin zips:
```shell
./make-binaries.sh
```

If you modify extension code (Java), recompile the jar before commit:
```shell
./build_plugin.sh
```