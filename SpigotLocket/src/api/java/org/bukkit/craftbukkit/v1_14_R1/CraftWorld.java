package org.bukkit.craftbukkit.v1_14_R1;

import net.minecraft.server.v1_14_R1.WorldServer;

/**
 * The type Craft world.
 *
 * @author Himmelt
 */
public abstract class CraftWorld {
    /**
     * Gets handle.
     *
     * @return the handle
     */
    public abstract WorldServer getHandle();
}
