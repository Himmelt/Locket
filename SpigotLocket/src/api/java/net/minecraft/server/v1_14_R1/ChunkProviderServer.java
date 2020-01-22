package net.minecraft.server.v1_14_R1;

/**
 * @author Himmelt
 */
public abstract class ChunkProviderServer {
    public abstract void flagDirty(BlockPosition blockposition);
}
