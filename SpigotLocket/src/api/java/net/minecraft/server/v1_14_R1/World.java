package net.minecraft.server.v1_14_R1;

/**
 * The type World.
 *
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
