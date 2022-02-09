package net.fabricmc.example.math;

import net.fabricmc.example.rendering.shader.UniformConvertible;
import org.lwjgl.opengl.GL32;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.JavaReflector;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.TypedJavaObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A Vector of 3 doubles.
 * All vectors are treated as columns.
 */
public record Vector3(double x, double y, double z) implements UniformConvertible, TypedJavaObject, JavaReflector {

    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    public static final Vector3 ONE = new Vector3(1, 1, 1);
    public static final Vector3 X = new Vector3(1, 0, 0);
    public static final Vector3 Y = new Vector3(0, 1, 0);
    public static final Vector3 Z = new Vector3(0, 0, 1);

    @Override
    public void uploadUniform(int loc) {
        GL32.glUniform3f(loc, (float) x, (float) y, (float) z);
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public Vector3 multiply(Vector3 other) {
        return new Vector3(x * other.x, y * other.y, z * other.z);
    }

    public Vector3 divide(Vector3 other) {
        return new Vector3(x / other.x, y / other.y, z / other.z);
    }

    public Vector3 mod(Vector3 other) {
        return new Vector3(x % other.x, y % other.y, z % other.z);
    }

    public Vector3 pow(Vector3 other) {
        return new Vector3(Math.pow(x, other.x), Math.pow(y, other.y), Math.pow(z, other.z));
    }

    public Vector3 scale(double factor) {
        return new Vector3(x * factor, y * factor, z * factor);
    }

    public Vector3 scale(double fX, double fY, double fZ) {
        return new Vector3(x * fX, y * fY, z * fZ);
    }

    public double dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vector3 normalized() {
        return scale(1 / length());
    }

    public Vector3 toRad() {
        return scale(Math.PI/180);
    }

    public Vector3 toDeg() {
        return scale(180/Math.PI);
    }

    public String toString() {
        return "{" + x + ", " + y + ", " + z + "}";
    }

    /**
     * LUA BELOW
     */

    private static final Map<String, JavaFunction> luaFunctions = new HashMap<>() {{
        put("length", state -> {
            Vector3 vec1 = state.checkJavaObject(1, Vector3.class);
            state.pushNumber(vec1.length());
            return 1;
        });
        put("dot", state -> {
            Vector3 vec1 = state.checkJavaObject(1, Vector3.class);
            Vector3 vec2 = state.checkJavaObject(2, Vector3.class);
            state.pushNumber(vec1.dot(vec2));
            return 1;
        });
        put("cross", state -> {
            Vector3 vec1 = state.checkJavaObject(1, Vector3.class);
            Vector3 vec2 = state.checkJavaObject(2, Vector3.class);
            state.pushJavaObject(vec1.cross(vec2));
            return 1;
        });
    }};

    //I don't really get how the reflection stuff works tbh
    //Got this style of code from Frangura, I'll copy it for other math items
    //But for APIs I'll just use the lua stack since I get that
    @Override
    public JavaFunction getMetamethod(Metamethod metamethod) {
        return switch (metamethod) {
            case ADD -> state -> {
                state.pushJavaObject(add(state.checkJavaObject(2, Vector3.class)));
                return 1;
            };
            case SUB -> state -> {
                state.pushJavaObject(subtract(state.checkJavaObject(2, Vector3.class)));
                return 1;
            };
            case MUL -> state -> {
                if (state.isNumber(1)) {
                    double val = state.toNumber(1);
                    state.pushJavaObject(scale(val));
                } else if (state.isNumber(2)) {
                    double val = state.toNumber(2);
                    state.pushJavaObject(scale(val));
                } else {
                    state.pushJavaObject(multiply(state.checkJavaObject(2, Vector3.class)));
                }
                return 1;
            };
            case DIV -> state -> {
                if (state.isNumber(2)) {
                    double val = state.toNumber(2);
                    state.pushJavaObject(scale(1 / val));
                } else {
                    state.pushJavaObject(divide(state.checkJavaObject(2, Vector3.class)));
                }
                return 1;
            };
            case MOD -> state -> {
                state.pushJavaObject(mod(state.checkJavaObject(2, Vector3.class)));
                return 1;
            };
            case POW -> state -> {
                state.pushJavaObject(pow(state.checkJavaObject(2, Vector3.class)));
                return 1;
            };
            case INDEX -> state -> {
                String index = state.checkString(2);
                switch (index) {
                    case "1", "x", "r" -> state.pushNumber(x);
                    case "2", "y", "g" -> state.pushNumber(y);
                    case "3", "z", "b" -> state.pushNumber(z);
                    default -> {
                        JavaFunction func = luaFunctions.get(index);
                        if (func == null) state.pushNil();
                        else state.pushJavaFunction(func);
                    }
                }
                return 1;
            };
            case NEWINDEX -> state -> {
                throw new LuaRuntimeException("Cannot set values in a vector!");
            };
            case LEN -> state -> { //Was debating making this the .length method, but decided against it
                state.pushInteger(3);
                return 1;
            };
            case TOSTRING -> state -> {
                state.pushString(toString());
                return 1;
            };
            default -> null;
        };
    }

    @Override
    public Object getObject() {
        return this;
    }

    @Override
    public Class<?> getType() {
        return getClass();
    }

    @Override
    public boolean isStrong() {
        return true;
    }

}
