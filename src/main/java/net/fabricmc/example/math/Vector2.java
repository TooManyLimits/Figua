package net.fabricmc.example.math;


import net.fabricmc.example.rendering.shader.UniformConvertible;
import org.lwjgl.opengl.GL32;

/**
 * A Vector of 2 doubles.
 * All vectors are treated as columns.
 */
public record Vector2(double x, double y) implements UniformConvertible {

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

}
