package net.fabricmc.example.lua.api;

import net.fabricmc.example.math.Vector2;
import net.fabricmc.example.math.Vector3;
import net.fabricmc.example.math.Vector4;
import org.terasology.jnlua.LuaState;

public class VectorsApi implements LuaApi {

    public void addTo(LuaState luaState) {
        //Push "vectors" table
        luaState.newTable();

        // vec2() function: creates new Vector2
        // vec2(num): Fills Vector2 with num for all values
        // vec2(x, y, z): Fills Vector2 with x, y
        luaState.pushJavaFunction(state -> {
            if (state.isNone(2)) {
                double val = state.checkNumber(1);
                state.pushJavaObject(new Vector2(val, val));
            } else {
                double x = state.checkNumber(1);
                double y = state.checkNumber(2);
                state.pushJavaObject(new Vector2(x, y));
            }
            return 1;
        });
        luaState.setField(-2, "vec2");

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

        // vec4() function: creates new Vector4
        // vec4(num): Fills Vector4 with num for all values
        // vec4(x, y, z, w): Fills Vector4 with x, y, z, w
        luaState.pushJavaFunction(state -> {
            if (state.isNone(2)) {
                double val = state.checkNumber(1);
                state.pushJavaObject(new Vector4(val, val, val, val));
            } else {
                double x = state.checkNumber(1);
                double y = state.checkNumber(2);
                double z = state.checkNumber(3);
                double w = state.checkNumber(4);
                state.pushJavaObject(new Vector4(x, y, z, w));
            }
            return 1;
        });
        luaState.setField(-2, "vec4");

        //Add our table to global values under the name "vectors"
        luaState.setGlobal("vectors");
    }

}
