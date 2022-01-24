package net.fabricmc.example.lua.api;

import net.fabricmc.example.math.Vector3;
import org.terasology.jnlua.LuaState;

public class VectorsApi implements LuaApi {

    public void addTo(LuaState luaState) {
        //Push "vectors" table
        luaState.newTable();

        // vec3() function: creates new Vector3
        // vec3(num): Fills Vector3 with num for all values
        // vec3(x, y, z): Fills Vector3 with x, y, z
        luaState.pushJavaFunction(state -> {
            if (state.isNone(2)) {
                double val = state.checkNumber(1);
                state.pushJavaObject(new Vector3(val, val, val));
            } else {
                double x = state.checkNumber(1);
                double y = state.checkNumber(2);
                double z = state.checkNumber(3);
                state.pushJavaObject(new Vector3(x, y, z));
            }
            return 1;
        });
        luaState.setField(-2, "vec3");

        //Add our table to global values under the name "vectors"
        luaState.setGlobal("vectors");
    }

}
