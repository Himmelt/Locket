package net.minecraft.server.v1_15_R1;

/**
 * The type Player chunk map.
 *
 * @author Himmelt
 */
public abstract class PlayerChunkMap {
    /**
     * Flag dirty.
     *
     * @param blockposition the blockposition
     */
    public abstract void flagDirty(BlockPosition blockposition);
}
