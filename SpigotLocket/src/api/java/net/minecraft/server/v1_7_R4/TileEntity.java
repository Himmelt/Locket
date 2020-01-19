package net.minecraft.server.v1_7_R4;

/**
 * The type Tile entity.
 *
 * @author Himmelt
 */
public abstract class TileEntity {
    public int x;
    public int y;
    public int z;
    protected World world;

    public World getWorld() {
        return this.world;
    }
}
