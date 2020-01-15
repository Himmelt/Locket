package org.soraworld.locket.nms;

import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.TileEntitySign;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.soraworld.locket.data.SignData;

import java.util.function.Predicate;

/**
 * @author Himmelt
 */
public class TileSign {
    public static void touchSign(Block block, Predicate<SignData> change) {
        try {
            TileEntitySign sign = (TileEntitySign) ((CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            SignData data = new SignData();
            data.line0 = sign.lines[0].toPlainText();
            data.line1 = sign.lines[1].toPlainText();
            data.line2 = sign.lines[2].toPlainText();
            data.line3 = sign.lines[3].toPlainText();
            if (change.test(data)) {
                sign.lines[0] = new ChatComponentText(data.line0);
                sign.lines[1] = new ChatComponentText(data.line1);
                sign.lines[2] = new ChatComponentText(data.line2);
                sign.lines[3] = new ChatComponentText(data.line3);
            }
        } catch (Throwable ignored) {
        }
    }
}
