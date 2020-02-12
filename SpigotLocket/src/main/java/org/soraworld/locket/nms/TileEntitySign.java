package org.soraworld.locket.nms;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutUpdateSign;
import org.soraworld.violet.text.ChatColor;

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
        }
    }

    @Override
    public Packet getUpdatePacket() {
        return new PacketPlayOutUpdateSign(x, y, z, truncateSignLines());
    }

    private String[] truncateSignLines() {
        String[] texts = new String[4];
        for (int i = 0; i < 4; i++) {
            texts[i] = lines[i].length() <= 15 ? lines[i] : lines[i].substring(0, 15);
            int length = texts[i].length();
            if (length >= 1 && texts[i].charAt(length - 1) == ChatColor.TRUE_COLOR_CHAR) {
                texts[i] = texts[i].substring(0, length - 1);
            }
        }
        return texts;
    }
}
