package net.minecraft.server.v1_13_R2;

/**
 * @author Himmelt
 */
public abstract class World {
    /**
     * Gets tile entity.
     *
     * @param blockposition the blockposition
     * @return the tile entity
     */
    public abstract TileEntity getTileEntity(BlockPosition blockposition);
}
