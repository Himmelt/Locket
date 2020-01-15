package net.minecraft.server.v1_8_R1;

/**
 * The type Tile entity.
 *
 * @author Himmelt
 */
public class TileEntitySign extends TileEntity {
    public final IChatBaseComponent[] lines = new IChatBaseComponent[]{new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("")};
}
