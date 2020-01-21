package net.minecraft.server.v1_8_R1;

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
