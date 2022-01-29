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
 * A Vector of 4 doubles.
 * All vectors are treated as columns.
 */
public record Vector4(double x, double y, double z, double w) implements UniformConvertible, TypedJavaObject, JavaReflector {

    public static final Vector4 ZERO = new Vector4(0, 0, 0, 0);
    public static final Vector4 ONE = new Vector4(1, 1, 1, 1);

    @Override
    public void uploadUniform(int loc) {
        GL32.glUniform4f(loc, (float) x, (float) y, (float) z, (float) w);
    }

    public Vector4 multiply(Matrix4 matrix) {
        return new Vector4(
                matrix.a11() * x + matrix.a12() * y + matrix.a13() * z + matrix.a14() * w,
                matrix.a21() * x + matrix.a22() * y + matrix.a23() * z + matrix.a24() * w,
                matrix.a31() * x + matrix.a32() * y + matrix.a33() * z + matrix.a34() * w,
                matrix.a41() * x + matrix.a42() * y + matrix.a43() * z + matrix.a44() * w
        );
    }

    public Vector4 add(Vector4 other) {
        return new Vector4(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    public Vector4 subtract(Vector4 other) {
        return new Vector4(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    public Vector4 multiply(Vector4 other) {
        return new Vector4(x * other.x, y * other.y, z * other.z, w * other.w);
    }

    public Vector4 divide(Vector4 other) {
        return new Vector4(x / other.x, y / other.y, z / other.z, w / other.w);
    }

    public Vector4 mod(Vector4 other) {
        return new Vector4(x % other.x, y % other.y, z % other.z, w % other.w);
    }

    public Vector4 pow(Vector4 other) {
        return new Vector4(Math.pow(x, other.x), Math.pow(y, other.y), Math.pow(z, other.z), Math.pow(w, other.w));
    }

    public Vector4 scale(double factor) {
        return new Vector4(x * factor, y * factor, z * factor, w * factor);
    }

    public Vector4 scale(double fX, double fY, double fZ, double fW) {
        return new Vector4(x * fX, y * fY, z * fZ, w * fW);
    }

    public double dot(Vector4 other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    public double lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vector4 normalized() {
        return scale(1 / length());
    }

    public Vector4 toRad() {
        return scale(Math.PI/180);
    }

    public Vector4 toDeg() {
        return scale(180/Math.PI);
    }

    /**
     * LUA BELOW
     */

    private static final Map<String, JavaFunction> luaFunctions = new HashMap<>() {{
        put("length", state -> {
            Vector4 vec1 = state.checkJavaObject(1, Vector4.class);
            state.pushNumber(vec1.length());
            return 1;
        });
        put("dot", state -> {
            Vector4 vec1 = state.checkJavaObject(1, Vector4.class);
            Vector4 vec2 = state.checkJavaObject(2, Vector4.class);
            state.pushNumber(vec1.dot(vec2));
            return 1;
        });
    }};

    @Override
    public JavaFunction getMetamethod(JavaReflector.Metamethod metamethod) {
        return switch (metamethod) {
            case ADD -> state -> {
                state.pushJavaObject(add(state.checkJavaObject(2, Vector4.class)));
                return 1;
            };
            case SUB -> state -> {
                state.pushJavaObject(subtract(state.checkJavaObject(2, Vector4.class)));
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
                    state.pushJavaObject(multiply(state.checkJavaObject(2, Vector4.class)));
                }
                return 1;
            };
            case DIV -> state -> {
                if (state.isNumber(2)) {
                    double val = state.toNumber(2);
                    state.pushJavaObject(scale(1 / val));
                } else {
                    state.pushJavaObject(divide(state.checkJavaObject(2, Vector4.class)));
                }
                return 1;
            };
            case MOD -> state -> {
                state.pushJavaObject(mod(state.checkJavaObject(2, Vector4.class)));
                return 1;
            };
            case POW -> state -> {
                state.pushJavaObject(pow(state.checkJavaObject(2, Vector4.class)));
                return 1;
            };
            case INDEX -> state -> {
                String index = state.checkString(2);
                switch (index) {
                    case "1", "x", "r" -> state.pushNumber(x);
                    case "2", "y", "g" -> state.pushNumber(y);
                    case "3", "z", "b" -> state.pushNumber(z);
                    case "4", "w", "a" -> state.pushNumber(w);
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
                state.pushInteger(4);
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
