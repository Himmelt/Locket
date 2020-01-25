package net.minecraft.server.v1_8_R3;

/**
 * @author Himmelt
 */
public abstract class World {
    /**
     * Ray trace moving object position.
     *
     * @param start                         the start
     * @param end                           the end
     * @param stopOnLiquid                  the stop on liquid
     * @param ignoreBlockWithoutBoundingBox the ignore block without bounding box
     * @param returnLastUncollidableBlock   the return last uncollidable block
     * @return the moving object position
     */
    public abstract MovingObjectPosition rayTrace(Vec3D start, Vec3D end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock);
}
