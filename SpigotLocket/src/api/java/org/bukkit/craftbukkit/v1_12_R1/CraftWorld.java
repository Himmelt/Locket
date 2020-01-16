package org.bukkit.craftbukkit.v1_12_R1;

import net.minecraft.server.v1_12_R1.TileEntity;

/**
 * The type Craft world.
 *
 * @author Himmelt
 */
public abstract class CraftWorld {
    /**
     * Gets tile entity at.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @return the tile entity at
     */
    public abstract TileEntity getTileEntityAt(int x, int y, int z);
}
