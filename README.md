# Teal

[Teal](https://github.com/teal-language/tl) is a typed dialect of Lua. To get familiar with Teal, please have a look at 
[Teal Tutorial](https://github.com/teal-language/tl/blob/master/docs/tutorial.md).

# Using teal in your project (Defold 1.8.1+)

1. Add a dependency on a latest release of this extension: https://github.com/defold/extension-teal/tags
   
   <img width="401" alt="image" src="https://user-images.githubusercontent.com/2209596/202223571-c77f0304-5202-4314-869d-7a90bbeec5ec.png">
2. Fetch Libraries (`Project → Fetch Libraries`)
3. Create `tlconfig.lua` file in the project root with the following content:
   ```lua
   return {
	    gen_target = "5.1",
	    gen_compat = "off",
	    include = {"**/*.tl"}
   }
   ```
4Create `.tl` files — those can be required from scripts and lua modules as if they were `.lua` files 

# Examples

## Hello world

With Teal extension added to the project dependency list and `/tlconfig.lua` file in the project root, we can start using 
teal. Assuming you are using an empty project template to try it out, here is how you can give it a try:

1. Create a file in the `main` folder and name it `greeter.tl`, then write the following code:
   ```teal
   -- this is Teal, note the type annotations:
   local template: string = "Hello, %s!"
   
   local M = {}
   
   function M.greet(s: string): string
      return template:format(s)
   end
   
   return M
   ```
2. Create `main.script` file in the main directory, with the following content:
   ```lua
   -- we are importing the Teal file as if it was Lua
   local greeter = require("main.greeter")
   
   function init(self)
       -- use the Teal module
       print(greeter.greet("Teal"))
   end
   ```
3. In `main.collection` file, add a `main.script` Script as a component to some game object.
4. Run the game. The build process will compile and type check all Teal code, and you will 
   see the following line in the output:
   ```
   DEBUG:SCRIPT: Hello, Teal!
   ```

# Developing this extension

If you modify teal executable, re-assemble the bin zips:
```shell
./make-binaries.sh
```

If you modify extension code (Java), recompile the jar before commit:
```shell
./build_plugin.sh
```