package net.fabricmc.example.math;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;

public class MathUtils {

    /**
     * Returns the matrix for an entity, used to transform from entity space to world space.
     * This is inelegant, will rework later. Currently makes a distinction for LivingEntities to use body yaw,
     * and non-living entities to just lerp their yaws.
     * @param e The entity to get the matrix for.
     * @return A matrix which represents the transformation from entity space to part space.
     */
    public static Matrix4 entityToWorldMatrix(Entity e) {
        float delta = MinecraftClient.getInstance().getTickDelta();
        double yaw;
        if (e instanceof LivingEntity)
            yaw = MathHelper.lerp(delta, ((LivingEntity) e).prevBodyYaw, ((LivingEntity) e).bodyYaw);
        else
            yaw = e.getYaw(MinecraftClient.getInstance().getTickDelta());
        return Matrix4.rotateY(Math.toRadians(180-yaw))
                .multiply(Matrix4.translate(e.getLerpedPos(delta)));
    }

    /**
     * Gets a matrix to transform from world space to part space, based on the player's camera position.
     * @return That matrix.
     */
    public static Matrix4 worldToViewMatrix() {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Matrix3f cameraMat = new Matrix3f(camera.getRotation());
        cameraMat.invert();
        return Matrix4.translate(camera.getPos().multiply(-1))
                .multiply(new Matrix4(Matrix3.fromMatrix3f(cameraMat)))
                .multiply(new Matrix4(
                        -1,0,0,0,
                        0,1,0,0,
                        0,0,-1,0,
                        0,0,0,1));

    }

    /**
     * Calculates the next power of 2 which is greater than or equal to x.
     */
    public static int nextPowerOfTwo(int x) {
        int r = 1;
        while (r < x)
            r <<= 1;
        return r;
    }

    /**
     * Converts from world space to part space. Unlike the prewrite, this is actually pretty easy since
     * it's just scaling by 16, no negatives or other weird stuff in sight.
     * @param vec The vector to convert
     * @return The converted vector
     */
    public static Vector3 worldToPart(Vector3 vec) {
        return vec.scale(16);
    }

    /**
     * See worldToPart(), but reversed.
     * @param vec The vector to convert
     * @return The converted vector
     */
    public static Vector3 partToWorld(Vector3 vec) {
        return vec.scale(1.0/16);
    }

}
