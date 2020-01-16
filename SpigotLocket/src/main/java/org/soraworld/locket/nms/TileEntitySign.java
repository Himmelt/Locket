package org.soraworld.locket.nms;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutUpdateSign;

/**
 * @author Himmelt
 */
public class TileEntitySign extends net.minecraft.server.v1_7_R4.TileEntitySign {
    @Override
    public void a(NBTTagCompound compound) {
        this.isEditable = false;
        this.x = compound.getInt("x");
        this.y = compound.getInt("y");
        this.z = compound.getInt("z");
        for (int i = 0; i < 4; ++i) {
            this.lines[i] = compound.getString("Text" + (i + 1));
            if (this.lines[i].length() > 15) {
                this.lines[i] = this.lines[i].substring(0, 15);
            }
        }
    }

    @Override
    public Packet getUpdatePacket() {
        return new PacketPlayOutUpdateSign(this.x, this.y, this.z, this.lines);
    }
}
