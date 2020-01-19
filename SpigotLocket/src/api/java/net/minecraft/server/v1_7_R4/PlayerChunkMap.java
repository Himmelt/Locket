package net.minecraft.server.v1_7_R4;

/**
 * The type Player chunk map.
 *
 * @author Himmelt
 */
public abstract class PlayerChunkMap {
    /**
     * Flag dirty.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public abstract void flagDirty(int x, int y, int z);
}
