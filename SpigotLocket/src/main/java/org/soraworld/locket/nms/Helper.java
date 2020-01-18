package org.soraworld.locket.nms;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

import static org.soraworld.violet.nms.Version.*;

public class Helper {

    public static void injectTile() {
        if (v1_7_R4) {
            try {
                Class<?> tileEntityClass = getClass("net.minecraft.server.v1_7_R4.TileEntity", "net.minecraft.tileentity.TileEntity");
                // i => field_145855_i => nameToClassMap
                Field nameToClass = getFiled(tileEntityClass, "i", "field_145855_i", "nameToClassMap");
                nameToClass.setAccessible(true);
                HashMap<String, Class<?>> nameToClassMap = (HashMap<String, Class<?>>) nameToClass.get(null);
                nameToClassMap.put("Sign", TileEntitySign.class);
                System.out.println("[Locket] Inject id Sign to class " + TileEntitySign.class.getName());
                // j => field_145853_j => classToNameMap
                Field classToName = getFiled(tileEntityClass, "j", "field_145853_j", "classToNameMap");
                classToName.setAccessible(true);
                HashMap<Class<?>, String> classToNameMap = (HashMap<Class<?>, String>) classToName.get(null);
                classToNameMap.put(TileEntitySign.class, "Sign");
                System.out.println("[Locket] Inject class " + TileEntitySign.class.getName() + " to id Sign");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static Class<?> getClass(String... names) throws ClassNotFoundException {
        if (names == null || names.length == 0) throw new ClassNotFoundException("empty class name");
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (Throwable ignored) {
            }
        }
        throw new ClassNotFoundException(Arrays.toString(names));
    }

    public static Field getFiled(Class<?> clazz, String... names) throws NoSuchFieldException {
        if (names == null || names.length == 0) throw new NoSuchFieldException("empty field name");
        for (String name : names) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (Throwable ignored) {
            }
        }
        throw new NoSuchFieldException(Arrays.toString(names));
    }

    public static HandType getHandType(PlayerInteractEvent event) {
        if (v1_7_R4 || v1_8_R1 || v1_8_R3) {
            return HandType.MAIN_HAND;
        } else {
            return event.getHand().toString().toLowerCase().contains("off") ? HandType.OFF_HAND : HandType.MAIN_HAND;
        }
    }

    public static ItemStack getItemInHand(@NotNull PlayerInventory player, HandType type) {
        if (v1_7_R4 || v1_8_R1 || v1_8_R3) {
            return player.getItemInHand();
        } else if (type == HandType.OFF_HAND) {
            return player.getItemInOffHand();
        } else {
            return player.getItemInMainHand();
        }
    }

    public static void setItemInHand(@NotNull PlayerInventory player, HandType type, ItemStack stack) {
        if (v1_7_R4 || v1_8_R1 || v1_8_R3) {
            player.setItemInHand(stack);
        } else if (type == HandType.OFF_HAND) {
            player.setItemInOffHand(stack);
        } else {
            player.setItemInMainHand(stack);
        }
    }

    public static void touchSign(Block block, Predicate<SignData> change) {
        if (v1_7_R4) {
            try {
                TileEntitySign sign = (TileEntitySign) ((org.bukkit.craftbukkit.v1_7_R4.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0];
                data.line1 = sign.lines[1];
                data.line2 = sign.lines[2];
                data.line3 = sign.lines[3];
                if (change.test(data)) {
                    sign.lines[0] = data.line0;
                    sign.lines[1] = data.line1;
                    sign.lines[2] = data.line2;
                    sign.lines[3] = data.line3;
                }
            } catch (Throwable e) {
                e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_8_R3) {
            try {
                net.minecraft.server.v1_8_R3.TileEntitySign sign = (net.minecraft.server.v1_8_R3.TileEntitySign) ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.line0 = sign.lines[0].c();
                data.line1 = sign.lines[1].c();
                data.line2 = sign.lines[2].c();
                data.line3 = sign.lines[3].c();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.line0);
                    sign.lines[1] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.line1);
                    sign.lines[2] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.line2);
                    sign.lines[3] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.line3);
                    System.out.println(data.line0);
                    System.out.println(data.line1);
                    System.out.println(data.line2);
                    System.out.println(data.line3);
                }
            } catch (Throwable e) {
                e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
