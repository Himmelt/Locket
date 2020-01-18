package net.minecraft.server.v1_7_R4;

/**
 * @author Himmelt
 */
public class TileEntitySign extends TileEntity {
    public String[] lines = new String[]{"", "", "", ""};
    public boolean isEditable = true;

    public TileEntitySign() {
    }

    public void a(NBTTagCompound compound) {
    }

    public Packet getUpdatePacket() {
        return null;
    }
}
