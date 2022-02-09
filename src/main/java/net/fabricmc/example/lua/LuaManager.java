package net.fabricmc.example.lua;

import net.fabricmc.example.FiguaMod;
import net.fabricmc.example.lua.api.EventsApi;
import net.fabricmc.example.lua.api.VectorsApi;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaState53;
import org.terasology.jnlua.NativeSupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LuaManager {

    private static final int MEMORY_LIMIT = 1000000; //1 MB

    public static LuaState createLuaState() {
        LuaState luaState = new LuaState53(MEMORY_LIMIT);

        //Open the specified safe libraries
        openLibraries(luaState);

        //Set up our APIs
        (new VectorsApi()).addTo(luaState);
        (new EventsApi()).addTo(luaState);

        //Set up log and print to do nothing
        luaState.pushJavaFunction(DO_NOTHING);
        luaState.pushJavaFunction(DO_NOTHING);
        luaState.setGlobal("log");
        luaState.setGlobal("print");

        //Run sandboxer
        runSandboxer(luaState);

        //Return our completed LuaState
        return luaState;
    }

    public static void runSource(LuaState luaState, String source) {
        //Try call the source code
        //TODO: proper exception handling
        try {
            luaState.load(source, "figua");
            luaState.call(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final JavaFunction DO_NOTHING = s -> 0;

    private static final String LOG_HEADER = "[FIGUA LOG] >>> ";
    public static void setupLog(LuaState luaState) {
        luaState.pushJavaFunction(state -> {
            String message = LOG_HEADER;
            if (state.isString(1))
                message += state.checkString(1, "nil");
            else
                message += "[" + state.typeName(1) + "]";
            //TODO: also send this to the player in game
            FiguaMod.LOGGER.info(message);
            return 0;
        });
        luaState.setGlobal("log");
        luaState.getGlobal("log");
        luaState.setGlobal("print"); //Overwrite print function also, like Figura
    }

    private static void openLibraries(LuaState luaState) {
        //Load up these libraries which we want to include
        luaState.openLib(LuaState.Library.BASE);
        //luaState.openLib(LuaState.Library.BIT32); //For some reason this crashes?????
        luaState.openLib(LuaState.Library.TABLE);
        luaState.openLib(LuaState.Library.STRING);
        luaState.openLib(LuaState.Library.MATH);
        //Then pop the tables off the stack at the end
        luaState.pop(4);
    }

    /**
     * Messy function to run sandboxing lua script, which just kinda
     * yeets things I think might be bad possibly potentially maybe.
     * TODO: Put this in an asset file and load it instead of just having this hardcoded string
     * @param luaState The lua state to run the sandboxer in.
     */
    private static void runSandboxer(LuaState luaState) {
        //Call our function which helps sandbox
        //Just kinda deleting everything which I think might be bad? :p
        //Doesn't need to be anywhere near perfect yet anyway
        //http://lua-users.org/wiki/SandBoxes tried to kinda follow this a little
        String sandboxScript =
                "debug = nil " +
                        //"collectgarbage = nil " +
                        "dofile = nil " +
                        "_G = nil " +
                        "getfenv = nil " +
                        "load = nil " +
                        "loadfile = nil " +
                        "loadstring = nil " +
                        "rawequal = nil " +
                        "rawget = nil " +
                        "rawset = nil " +
                        "setfenv = nil " +
                        "string.dump = nil ";
        luaState.load(sandboxScript, "sandboxer");
        luaState.call(0, 0);
    }

    /**
     * Copies the correct JNLua .dll from the assets to the figua directory.
     * Also sets the value of loadPath in NativeSupport.
     * Most of this code is taken from org.terasology.jnlua.NativeSupport.
     * and from omo.
     */
    public static void setupLuaNatives() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        boolean isMacOS = System.getProperty("os.name").toLowerCase().contains("mac");

        // Generate library name.
        StringBuilder builder = new StringBuilder(isWindows ? "libjnlua-" : "jnlua-");

        builder.append("5.3-");

        if (isWindows) {
            // Windows
            builder.append("windows-");
        } else if (isMacOS) {
            builder.append("mac-");
        } else {
            // Assume Linux
            builder.append("linux-");
        }

        if (System.getProperty("os.arch").endsWith("64")) {
            // Assume x86_64
            builder.append("amd64");
        } else {
            // Assume x86_32
            builder.append("i686");
        }

        String ext;

        if (isWindows) {
            // Windows
            ext = ".dll";
        } else if (isMacOS) {
            ext = ".dylib";
        } else {
            // Assume Linux
            ext = ".so";
        }

        InputStream libStream = null;
        try {
            libStream = Files.newInputStream(FiguaMod.getAssetPath().resolve("lua_natives/" + builder + ext));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f = FiguaMod.getFiguaPath().resolve("nativelua"+ext).toFile();

        try {
            if (libStream != null)
                System.out.println("Loaded native lua - " + builder + ext);
            else
                System.out.println("Failed to load native lua - " + builder + ext);
            Files.copy(libStream, f.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }

        NativeSupport.loadPath = f.getAbsolutePath();
    }
}
