package net.minecraft.server.v1_15_R1;

/**
 * The type Tile entity.
 *
 * @author Himmelt
 */
public abstract class TileEntitySign extends TileEntity {
    public final IChatBaseComponent[] lines = new IChatBaseComponent[]{new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("")};
}
