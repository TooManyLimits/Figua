package net.fabricmc.example.math;

import net.fabricmc.example.rendering.shader.UniformConvertible;
import net.minecraft.util.math.Matrix3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.JavaReflector;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.TypedJavaObject;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * A 3x3 matrix of doubles.
 */
public record Matrix3(double a11, double a21, double a31,
                      double a12, double a22, double a32,
                      double a13, double a23, double a33) implements UniformConvertible, TypedJavaObject, JavaReflector {

    public static final Matrix3 IDENTITY = new Matrix3(1, 0, 0, 0, 1, 0, 0, 0, 1);

    @Override
    public void uploadUniform(int loc) {
        GL32.glUniformMatrix3fv(loc, false, new float[]{
                (float) a11, (float) a21, (float) a31,
                (float) a12, (float) a22, (float) a32,
                (float) a13, (float) a23, (float) a33
        });
    }

    /**
     * Left-multiplies this matrix by another matrix.
     * For example:
     * Matrix4 a = (...) //A
     * Matrix4 b = (...) //B
     * a.mult(b) -> returns BA
     *
     * @param other The matrix to multiply by.
     * @return A new matrix, which is the multiplied version of the two provided.
     */
    public Matrix3 multiply(Matrix3 other) {
        return new Matrix3(
                other.a11 * a11 + other.a12 * a21 + other.a13 * a31,
                other.a21 * a11 + other.a22 * a21 + other.a23 * a31,
                other.a31 * a11 + other.a32 * a21 + other.a33 * a31,

                other.a11 * a12 + other.a12 * a22 + other.a13 * a32,
                other.a21 * a12 + other.a22 * a22 + other.a23 * a32,
                other.a31 * a12 + other.a32 * a22 + other.a33 * a32,

                other.a11 * a13 + other.a12 * a23 + other.a13 * a33,
                other.a21 * a13 + other.a22 * a23 + other.a23 * a33,
                other.a31 * a13 + other.a32 * a23 + other.a33 * a33
        );
    }

    public Matrix3 add(Matrix3 other) {
        return new Matrix3(
                a11 + other.a11, a21 + other.a21, a31 + other.a31,
                a12 + other.a12, a22 + other.a22, a32 + other.a32,
                a13 + other.a13, a23 + other.a23, a33 + other.a33
        );
    }

    public Matrix3 subtract(Matrix3 other) {
        return new Matrix3(
                a11 - other.a11, a21 - other.a21, a31 - other.a31,
                a12 - other.a12, a22 - other.a22, a32 - other.a32,
                a13 - other.a13, a23 - other.a23, a33 - other.a33
        );
    }

    public Matrix3 multiply(double n) {
        return new Matrix3(
                a11 * n, a21 * n, a31 * n,
                a12 * n, a22 * n, a32 * n,
                a13 * n, a23 * n, a33 * n
        );
    }

    public Matrix3 pow(int n) {
        if (n == 0) return IDENTITY;
        if (n == 1) return this;
        if (n % 2 == 0) {
            return (this.multiply(this)).pow(n/2);
        } else {
            return this.multiply((this.multiply(this)).pow(n/2));
        }
    }

    /**
     * Returns a scaling matrix, with factors x, y, and z.
     */
    public static Matrix3 scale(double x, double y, double z) {
        return new Matrix3(x, 0, 0, 0, y, 0, 0, 0, z);
    }

    /**
     * Returns a matrix for rotating about the x-axis by radians.
     *
     * @param radians The angle to rotate, in radians.
     */
    public static Matrix3 rotateX(double radians) {
        double s = Math.sin(radians);
        double c = Math.cos(radians);
        return new Matrix3(1, 0, 0, 0, c, s, 0, -s, c);
    }

    /**
     * Returns a matrix for rotating about the y-axis by radians.
     *
     * @param radians The angle to rotate, in radians.
     */
    public static Matrix3 rotateY(double radians) {
        double s = Math.sin(radians);
        double c = Math.cos(radians);
        return new Matrix3(c, 0, -s, 0, 1, 0, s, 0, c);
    }

    /**
     * Returns a matrix for rotating about the z-axis by radians.
     *
     * @param radians The angle to rotate, in radians.
     */
    public static Matrix3 rotateZ(double radians) {
        double s = Math.sin(radians);
        double c = Math.cos(radians);
        return new Matrix3(c, s, 0, -s, c, 0, 0, 0, 1);
    }

    //Floatbuffer for use with converting to and from minecraft matrices
    private static final FloatBuffer copyingBuffer = BufferUtils.createFloatBuffer(3*3);

    /**
     * Converts from a Minecraft Matrix3f to a figua Matrix3.
     * @param mat The Minecraft matrix to convert
     * @return The figua version of that matrix.
     */
    public static Matrix3 fromMatrix3f(Matrix3f mat) {
        copyingBuffer.clear();
        mat.writeColumnMajor(copyingBuffer);
        FloatBuffer b = copyingBuffer; //Shorthand
        return new Matrix3(
                b.get(), b.get(), b.get(),
                b.get(), b.get(), b.get(),
                b.get(), b.get(), b.get()
        );
    }

    /**
     * See fromMatrix3f, but reversed.
     */
    public static Matrix3f toMatrix3f(Matrix3 mat) {
        copyingBuffer.clear();
        copyingBuffer
                .put((float) mat.a11).put((float) mat.a21).put((float) mat.a31)
                .put((float) mat.a12).put((float) mat.a22).put((float) mat.a32)
                .put((float) mat.a13).put((float) mat.a23).put((float) mat.a33);
        Matrix3f result = new Matrix3f();
        result.readColumnMajor(copyingBuffer);
        return result;
    }

    /**
     * LUA BELOW
     */

    private static final Map<String, JavaFunction> luaFunctions = new HashMap<>() {{
        //TODO: Add an inverse function
        put("inverse", state -> {
            state.pushJavaObject(this);
            return 1;
        });
    }};

    @Override
    public JavaFunction getMetamethod(JavaReflector.Metamethod metamethod) {
        return switch (metamethod) {
            case ADD -> state -> {
                state.pushJavaObject(add(state.checkJavaObject(2, Matrix3.class)));
                return 1;
            };
            case SUB -> state -> {
                state.pushJavaObject(subtract(state.checkJavaObject(2, Matrix3.class)));
                return 1;
            };
            case MUL -> state -> {
                if (state.isNumber(1)) {
                    double val = state.toNumber(1);
                    state.pushJavaObject(multiply(val));
                } else if (state.isNumber(2)) {
                    double val = state.toNumber(2);
                    state.pushJavaObject(multiply(val));
                } else {
                    Matrix3 mat1 = state.checkJavaObject(1, Matrix3.class);
                    Matrix3 mat2 = state.checkJavaObject(2, Matrix3.class);
                    state.pushJavaObject(mat2.multiply(mat1));
                }
                return 1;
            };
            case DIV -> state -> {
                double val = state.checkNumber(2);
                state.pushJavaObject(multiply(1 / val));
                return 1;
            };
            case POW -> state -> {
                int n = (int) state.checkInteger(2);
                if (n < 0) throw new IllegalArgumentException("Cannot raise a matrix to a negative power");
                state.pushJavaObject(pow(n));
                return 1;
            };
            case INDEX -> state -> {
                String index = state.checkString(2);
                switch (index) {
                    case "v11", "1" -> state.pushNumber(a11);
                    case "v12", "4" -> state.pushNumber(a12);
                    case "v13", "7" -> state.pushNumber(a13);
                    case "v21", "2" -> state.pushNumber(a21);
                    case "v22", "5" -> state.pushNumber(a22);
                    case "v23", "8" -> state.pushNumber(a23);
                    case "v31", "3" -> state.pushNumber(a31);
                    case "v32", "6" -> state.pushNumber(a32);
                    case "v33", "9" -> state.pushNumber(a33);
                    default -> {
                        JavaFunction func = luaFunctions.get(index);
                        if (func == null) state.pushNil();
                        else state.pushJavaFunction(func);
                    }
                }
                return 1;
            };
            case NEWINDEX -> state -> {
                throw new LuaRuntimeException("Cannot set values in a matrix!");
            };
            case LEN -> state -> { //Was debating making this the length method, but decided against it
                state.pushInteger(9);
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
