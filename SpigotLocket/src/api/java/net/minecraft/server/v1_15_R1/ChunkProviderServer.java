package net.minecraft.server.v1_15_R1;

/**
 * @author Himmelt
 */
public abstract class ChunkProviderServer {
    public abstract void flagDirty(BlockPosition blockposition);
}
