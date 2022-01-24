package net.fabricmc.example.math;

import net.fabricmc.example.rendering.shader.UniformConvertible;
import org.lwjgl.opengl.GL32;

/**
 * A Vector of 4 doubles.
 * All vectors are treated as columns.
 */
public record Vector4(double x, double y, double z, double w) implements UniformConvertible {

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

}
