package net.fabricmc.example.rendering.shader;

/**
 * An object which can be uploaded as a uniform to GLSL.
 */
public interface UniformConvertible {

    /**
     * Uploads this object as a uniform to the specified uniform location.
     * @param loc The uniform location to set our value at.
     */
    void uploadUniform(int loc);

}
