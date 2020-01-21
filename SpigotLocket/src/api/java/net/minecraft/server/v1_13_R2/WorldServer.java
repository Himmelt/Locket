package net.minecraft.server.v1_13_R2;

/**
 * The type World server.
 *
 * @author Himmelt
 */
public abstract class WorldServer extends World {
    /**
     * Gets player chunk map.
     *
     * @return the player chunk map
     */
    public abstract PlayerChunkMap getPlayerChunkMap();
}
