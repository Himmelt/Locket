package org.soraworld.locket.nms;

import org.bukkit.block.Block;
import org.soraworld.locket.data.SignData;

import java.util.function.Predicate;

import static org.soraworld.violet.nms.Version.*;

/**
 * @author Himmelt
 */
public class TileSign {
    public static void touchSign(Block block, Predicate<SignData> change) {
        if (v1_7_R4) {
            try {
                net.minecraft.server.v1_7_R4.TileEntitySign sign = (net.minecraft.server.v1_7_R4.TileEntitySign) ((org.bukkit.craftbukkit.v1_7_R4.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_7_R4.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_7_R4.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_7_R4.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_7_R4.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_8_R1) {
            try {
                net.minecraft.server.v1_8_R1.TileEntitySign sign = (net.minecraft.server.v1_8_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_8_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_8_R3) {
            try {
                net.minecraft.server.v1_8_R3.TileEntitySign sign = (net.minecraft.server.v1_8_R3.TileEntitySign) ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_9_R1) {
            try {
                net.minecraft.server.v1_9_R1.TileEntitySign sign = (net.minecraft.server.v1_9_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_9_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_9_R2) {
            try {
                net.minecraft.server.v1_9_R2.TileEntitySign sign = (net.minecraft.server.v1_9_R2.TileEntitySign) ((org.bukkit.craftbukkit.v1_9_R2.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_10_R1) {
            try {
                net.minecraft.server.v1_10_R1.TileEntitySign sign = (net.minecraft.server.v1_10_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_10_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_11_R1) {
            try {
                net.minecraft.server.v1_11_R1.TileEntitySign sign = (net.minecraft.server.v1_11_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_11_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_12_R1) {
            try {
                net.minecraft.server.v1_12_R1.TileEntitySign sign = (net.minecraft.server.v1_12_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_12_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_13_R1) {
            try {
                net.minecraft.server.v1_13_R1.TileEntitySign sign = (net.minecraft.server.v1_13_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_13_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        } else if (v1_13_R2) {
            try {
                net.minecraft.server.v1_13_R2.TileEntitySign sign = (net.minecraft.server.v1_13_R2.TileEntitySign) ((org.bukkit.craftbukkit.v1_13_R2.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].toPlainText();
                data.line1 = sign.lines[1].toPlainText();
                data.line2 = sign.lines[2].toPlainText();
                data.line3 = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.line3);
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
