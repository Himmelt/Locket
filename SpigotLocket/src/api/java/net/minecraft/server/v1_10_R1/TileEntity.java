package net.minecraft.server.v1_10_R1;

/**
 * The type Tile entity.
 *
 * @author Himmelt
 */
public abstract class TileEntity {
    protected World world;
    protected BlockPosition position;

    public World getWorld() {
        return this.world;
    }

    public BlockPosition getPosition() {
        return this.position;
    }
}
