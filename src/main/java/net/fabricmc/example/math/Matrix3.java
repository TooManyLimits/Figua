package net.fabricmc.example.math;

import net.fabricmc.example.rendering.shader.UniformConvertible;
import net.minecraft.util.math.Matrix3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32;

import java.nio.FloatBuffer;

/**
 * A 3x3 matrix of doubles.
 */
public record Matrix3(double a11, double a21, double a31,
                      double a12, double a22, double a32,
                      double a13, double a23, double a33) implements UniformConvertible {

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

}
