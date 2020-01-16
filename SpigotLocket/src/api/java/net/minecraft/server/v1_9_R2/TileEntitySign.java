package net.minecraft.server.v1_9_R2;

/**
 * The type Tile entity.
 *
 * @author Himmelt
 */
public abstract class TileEntitySign extends TileEntity {
    public final IChatBaseComponent[] lines = new IChatBaseComponent[]{new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("")};
}
