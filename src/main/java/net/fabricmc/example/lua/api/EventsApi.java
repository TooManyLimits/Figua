package net.fabricmc.example.lua.api;

import org.terasology.jnlua.*;

public class EventsApi implements LuaApi {

    public void addTo(LuaState luaState) {
        //Push "events" table
        luaState.newTable();

        //Push metatable
        pushMetatable(luaState);

        //Push tick table
        luaState.newTable();
        //Set metatable of tick table
        luaState.pushValue(-2);
        luaState.setMetatable(-2);
        //Add table to "events"
        luaState.setField(-3, "tick");

        //Push render table
        luaState.newTable();
        luaState.pushValue(-2);
        luaState.setMetatable(-2);
        luaState.setField(-3, "render");

        //Pop metatable
        luaState.pop(1);

        //Add events as global
        luaState.setGlobal("events");
    }

    private static void pushMetatable(LuaState luaState) {
        //Push metatable
        luaState.newTable();

        //Prohibit __newindex
        luaState.pushJavaFunction(state -> {
            throw new LuaRuntimeException("Cannot modify values in event!");
        });
        luaState.setField(-2, "__newindex");

        //Hide metatable
        luaState.pushBoolean(false);
        luaState.setField(-2, "__metatable");

        //Create table to use as value for __index
        //This table holds all our functions
        luaState.newTable();

        //Register function
        luaState.pushJavaFunction(state -> {
            state.checkType(1, LuaType.TABLE);
            state.checkType(2, LuaType.FUNCTION);

            //Push a copy of the function
            state.pushValue(2);
            //Store it in the provided table
            state.rawSet(1, state.rawLen(1) + 1);
            return 0;
        });
        luaState.setField(-2, "register");

        //Clear function
        luaState.pushJavaFunction(state -> {
            state.checkType(1, LuaType.TABLE);
            luaState.newTable();
            luaState.replace(1);
            return 0;
        });
        luaState.setField(-2, "clear");

        //Invoke function
        luaState.pushJavaFunction(state -> {
            state.checkType(1, LuaType.TABLE);
            //Get number of arguments and functions
            int numArguments = state.getTop();
            int numFunctions = state.rawLen(1);
            //For each function:
            for (int i = 1; i <= numFunctions; i++) {
                //Push the function key
                state.pushInteger(i);
                //Pop key, push function
                state.getTable(1);
                //Push all arguments
                for (int j = 2; j <= numArguments; j++)
                    state.pushValue(j);
                //Call function with the arguments
                state.call(numArguments - 1, 0); //-1 because the table is an argument
            }
            return 0;
        });
        luaState.setField(-2, "invoke");

        //Set our index table in the metatable
        luaState.setField(-2, "__index");
    }
}
