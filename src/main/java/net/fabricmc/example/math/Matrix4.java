package net.fabricmc.example.math;

import net.fabricmc.example.rendering.shader.UniformConvertible;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32;

import java.nio.FloatBuffer;

/**
 * A 4x4 matrix of doubles.
 */
public record Matrix4(double a11, double a21, double a31, double a41,
                      double a12, double a22, double a32, double a42,
                      double a13, double a23, double a33, double a43,
                      double a14, double a24, double a34, double a44) implements UniformConvertible {

    public static final Matrix4 IDENTITY = new Matrix4(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);

    @Override
    public void uploadUniform(int loc) {
        GL32.glUniformMatrix4fv(loc, false, new float[]{
                (float) a11, (float) a21, (float) a31, (float) a41,
                (float) a12, (float) a22, (float) a32, (float) a42,
                (float) a13, (float) a23, (float) a33, (float) a43,
                (float) a14, (float) a24, (float) a34, (float) a44
        });
    }

    public Matrix4(Matrix3 mat) {
        this(
                mat.a11(), mat.a21(), mat.a31(), 0,
                mat.a12(), mat.a22(), mat.a32(), 0,
                mat.a13(), mat.a23(), mat.a33(), 0,
                0, 0, 0, 1
                );
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
    public Matrix4 multiply(Matrix4 other) {
        return new Matrix4(
                other.a11 * a11 + other.a12 * a21 + other.a13 * a31 + other.a14 * a41,
                other.a21 * a11 + other.a22 * a21 + other.a23 * a31 + other.a24 * a41,
                other.a31 * a11 + other.a32 * a21 + other.a33 * a31 + other.a34 * a41,
                other.a41 * a11 + other.a42 * a21 + other.a43 * a31 + other.a44 * a41,

                other.a11 * a12 + other.a12 * a22 + other.a13 * a32 + other.a14 * a42,
                other.a21 * a12 + other.a22 * a22 + other.a23 * a32 + other.a24 * a42,
                other.a31 * a12 + other.a32 * a22 + other.a33 * a32 + other.a34 * a42,
                other.a41 * a12 + other.a42 * a22 + other.a43 * a32 + other.a44 * a42,

                other.a11 * a13 + other.a12 * a23 + other.a13 * a33 + other.a14 * a43,
                other.a21 * a13 + other.a22 * a23 + other.a23 * a33 + other.a24 * a43,
                other.a31 * a13 + other.a32 * a23 + other.a33 * a33 + other.a34 * a43,
                other.a41 * a13 + other.a42 * a23 + other.a43 * a33 + other.a44 * a43,

                other.a11 * a14 + other.a12 * a24 + other.a13 * a34 + other.a14 * a44,
                other.a21 * a14 + other.a22 * a24 + other.a23 * a34 + other.a24 * a44,
                other.a31 * a14 + other.a32 * a24 + other.a33 * a34 + other.a34 * a44,
                other.a41 * a14 + other.a42 * a24 + other.a43 * a34 + other.a44 * a44
        );
    }

    /**
     * Uploads this matrix to the given FloatBuffer.
     * This is used to update a TransformTexture.
     * @param buf The buffer to upload the matrix to.
     */
    public void uploadToBuffer(FloatBuffer buf) {
        buf
                .put((float) a11).put((float) a21).put((float) a31).put((float) a41)
                .put((float) a12).put((float) a22).put((float) a32).put((float) a42)
                .put((float) a13).put((float) a23).put((float) a33).put((float) a43)
                .put((float) a14).put((float) a24).put((float) a34).put((float) a44);
    }

    /**
     * Returns a matrix for scaling by x, y, and z.
     */
    public static Matrix4 scale(double x, double y, double z) {
        return new Matrix4(x, 0, 0, 0, 0, y, 0, 0, 0, 0, z, 0, 0, 0, 0, 1);
    }

    public static Matrix4 scale(Vector3 vec) {
        return scale(vec.x(), vec.y(), vec.z());
    }

    /**
     * Returns a matrix for translating by x, y, and z.
     */
    public static Matrix4 translate(double x, double y, double z) {
        return new Matrix4(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, x, y, z, 1);
    }

    public static Matrix4 translate(Vector3 vec) {
        return translate(vec.x(), vec.y(), vec.z());
    }

    public static Matrix4 translate(Vec3d vec) {
        return translate(vec.x, vec.y, vec.z);
    }

    public static Matrix4 translate(Vec3f vec) {
        return translate(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Returns a matrix for rotating about the x-axis by radians.
     *
     * @param radians The angle to rotate, in radians.
     */
    public static Matrix4 rotateX(double radians) {
        double s = Math.sin(radians);
        double c = Math.cos(radians);
        return new Matrix4(1, 0, 0, 0, 0, c, s, 0, 0, -s, c, 0, 0, 0, 0, 1);
    }

    /**
     * Returns a matrix for rotating about the y-axis by radians.
     *
     * @param radians The angle to rotate, in radians.
     */
    public static Matrix4 rotateY(double radians) {
        double s = Math.sin(radians);
        double c = Math.cos(radians);
        return new Matrix4(c, 0, -s, 0, 0, 1, 0, 0, s, 0, c, 0, 0, 0, 0, 1);
    }

    /**
     * Returns a matrix for rotating about the z-axis by radians.
     *
     * @param radians The angle to rotate, in radians.
     */
    public static Matrix4 rotateZ(double radians) {
        double s = Math.sin(radians);
        double c = Math.cos(radians);
        return new Matrix4(c, s, 0, 0, -s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
    }

    //Floatbuffer for use with converting to and from minecraft matrices
    private static final FloatBuffer copyingBuffer = BufferUtils.createFloatBuffer(4*4);

    public static Matrix4 fromMatrix4f(Matrix4f mat) {
        copyingBuffer.clear();
        mat.writeColumnMajor(copyingBuffer);
        FloatBuffer b = copyingBuffer; //Shorthand
        return new Matrix4(
                b.get(), b.get(), b.get(), b.get(),
                b.get(), b.get(), b.get(), b.get(),
                b.get(), b.get(), b.get(), b.get(),
                b.get(), b.get(), b.get(), b.get()
        );
    }

    public static Matrix4f toMatrix4f(Matrix4 mat) {
        copyingBuffer.clear();
        copyingBuffer
                .put((float) mat.a11).put((float) mat.a21).put((float) mat.a31).put((float) mat.a41)
                .put((float) mat.a12).put((float) mat.a22).put((float) mat.a32).put((float) mat.a42)
                .put((float) mat.a13).put((float) mat.a23).put((float) mat.a33).put((float) mat.a43)
                .put((float) mat.a14).put((float) mat.a24).put((float) mat.a34).put((float) mat.a44);
        Matrix4f result = new Matrix4f();
        result.readColumnMajor(copyingBuffer);
        return result;
    }

}
