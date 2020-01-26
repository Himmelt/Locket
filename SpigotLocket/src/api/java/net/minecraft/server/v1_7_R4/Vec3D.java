package net.minecraft.server.v1_7_R4;

/**
 * @author Himmelt
 */
public class Vec3D {
    protected Vec3D(double x, double y, double z) {
    }

    public static Vec3D a(double x, double y, double z) {
        return new Vec3D(x, y, z);
    }
}
