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
 * A Vector of 2 doubles.
 * All vectors are treated as columns.
 */
public record Vector2(double x, double y) implements UniformConvertible, TypedJavaObject, JavaReflector {

    public static final Vector2 ZERO = new Vector2(0, 0);
    public static final Vector2 ONE = new Vector2(1, 1);
    public static final Vector2 X = new Vector2(1, 0);
    public static final Vector2 Y = new Vector2(0, 1);

    @Override
    public void uploadUniform(int loc) {
        GL32.glUniform2f(loc, (float) x, (float) y);
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    public Vector2 subtract(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    public Vector2 multiply(Vector2 other) {
        return new Vector2(x * other.x, y * other.y);
    }

    public Vector2 divide(Vector2 other) {
        return new Vector2(x / other.x, y / other.y);
    }

    public Vector2 mod(Vector2 other) {
        return new Vector2(x % other.x, y % other.y);
    }

    public Vector2 pow(Vector2 other) {
        return new Vector2(Math.pow(x, other.x), Math.pow(y, other.y));
    }

    public Vector2 scale(double factor) {
        return new Vector2(x * factor, y * factor);
    }

    public Vector2 scale(double fX, double fY) {
        return new Vector2(x * fX, y * fY);
    }

    public double dot(Vector2 other) {
        return x * other.x + y * other.y;
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vector2 normalized() {
        return scale(1 / length());
    }

    public Vector2 toRad() {
        return scale(Math.PI/180);
    }

    public Vector2 toDeg() {
        return scale(180/Math.PI);
    }

    /**
     * LUA BELOW
     */

    private static final Map<String, JavaFunction> luaFunctions = new HashMap<>() {{
        put("length", state -> {
            Vector2 vec1 = state.checkJavaObject(1, Vector2.class);
            state.pushNumber(vec1.length());
            return 1;
        });
        put("dot", state -> {
            Vector2 vec1 = state.checkJavaObject(1, Vector2.class);
            Vector2 vec2 = state.checkJavaObject(2, Vector2.class);
            state.pushNumber(vec1.dot(vec2));
            return 1;
        });
    }};

    @Override
    public JavaFunction getMetamethod(JavaReflector.Metamethod metamethod) {
        return switch (metamethod) {
            case ADD -> state -> {
                state.pushJavaObject(add(state.checkJavaObject(2, Vector2.class)));
                return 1;
            };
            case SUB -> state -> {
                state.pushJavaObject(subtract(state.checkJavaObject(2, Vector2.class)));
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
                    state.pushJavaObject(multiply(state.checkJavaObject(2, Vector2.class)));
                }
                return 1;
            };
            case DIV -> state -> {
                if (state.isNumber(2)) {
                    double val = state.toNumber(2);
                    state.pushJavaObject(scale(1 / val));
                } else {
                    state.pushJavaObject(divide(state.checkJavaObject(2, Vector2.class)));
                }
                return 1;
            };
            case MOD -> state -> {
                state.pushJavaObject(mod(state.checkJavaObject(2, Vector2.class)));
                return 1;
            };
            case POW -> state -> {
                state.pushJavaObject(pow(state.checkJavaObject(2, Vector2.class)));
                return 1;
            };
            case INDEX -> state -> {
                String index = state.checkString(2);
                switch (index) {
                    case "1", "x", "r" -> state.pushNumber(x);
                    case "2", "y", "g" -> state.pushNumber(y);
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
            case LEN -> state -> { //Was debating making this the length method, but decided against it
                state.pushInteger(2);
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
