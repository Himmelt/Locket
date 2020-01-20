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

/**
 * @author Himmelt
 */
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
                System.out.println("[Locket] Inject [Sign] -> [" + TileEntitySign.class.getName() + "]");
                // j => field_145853_j => classToNameMap
                Field classToName = getFiled(tileEntityClass, "j", "field_145853_j", "classToNameMap");
                classToName.setAccessible(true);
                HashMap<Class<?>, String> classToNameMap = (HashMap<Class<?>, String>) classToName.get(null);
                classToNameMap.put(TileEntitySign.class, "Sign");
                System.out.println("[Locket] Inject [" + TileEntitySign.class.getName() + "] -> [Sign]");

                Class<?> blocksClass = getClass("net.minecraft.server.v1_7_R4.Blocks", "net.minecraft.init.Blocks");
                Field postSign = getFiled(blocksClass, "SIGN_POST", "an", "field_150472_an", "standing_sign");
                postSign.setAccessible(true);
                Field wallSign = getFiled(blocksClass, "WALL_SIGN", "as", "field_150444_as", "wall_sign");
                wallSign.setAccessible(true);
                Class<?> blockSignClass = getClass("net.minecraft.server.v1_7_R4.BlockSign", "net.minecraft.block.BlockSign");
                Field tileClass = getFiled(blockSignClass, "a", "field_149968_a");
                tileClass.setAccessible(true);
                tileClass.set(postSign.get(null), TileEntitySign.class);
                System.out.println("[Locket] Inject class " + TileEntitySign.class.getName() + " into SIGN_POST.");
                tileClass.set(wallSign.get(null), TileEntitySign.class);
                System.out.println("[Locket] Inject class " + TileEntitySign.class.getName() + " into WALL_SIGN.");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static Class<?> getClass(String... names) throws ClassNotFoundException {
        if (names == null || names.length == 0) {
            throw new ClassNotFoundException("empty class name");
        }
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (Throwable ignored) {
            }
        }
        throw new ClassNotFoundException(Arrays.toString(names));
    }

    public static Field getFiled(Class<?> clazz, String... names) throws NoSuchFieldException {
        if (names == null || names.length == 0) {
            throw new NoSuchFieldException("empty field name");
        }
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
                net.minecraft.server.v1_7_R4.TileEntitySign sign = (net.minecraft.server.v1_7_R4.TileEntitySign) ((org.bukkit.craftbukkit.v1_7_R4.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0];
                data.lines[1] = sign.lines[1];
                data.lines[2] = sign.lines[2];
                data.lines[3] = sign.lines[3];
                if (change.test(data)) {
                    sign.lines[0] = data.lines[0];
                    sign.lines[1] = data.lines[1];
                    sign.lines[2] = data.lines[2];
                    sign.lines[3] = data.lines[3];
                    net.minecraft.server.v1_7_R4.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_7_R4.WorldServer) {
                        ((net.minecraft.server.v1_7_R4.WorldServer) world).getPlayerChunkMap().flagDirty(sign.x, sign.y, sign.z);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_8_R1) {
            try {
                net.minecraft.server.v1_8_R1.TileEntitySign sign = (net.minecraft.server.v1_8_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_8_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].c();
                data.lines[1] = sign.lines[1].c();
                data.lines[2] = sign.lines[2].c();
                data.lines[3] = sign.lines[3].c();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_8_R1.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_8_R1.WorldServer) {
                        ((net.minecraft.server.v1_8_R1.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_8_R3) {
            try {
                net.minecraft.server.v1_8_R3.TileEntitySign sign = (net.minecraft.server.v1_8_R3.TileEntitySign) ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].c();
                data.lines[1] = sign.lines[1].c();
                data.lines[2] = sign.lines[2].c();
                data.lines[3] = sign.lines[3].c();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_8_R3.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_8_R3.WorldServer) {
                        ((net.minecraft.server.v1_8_R3.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_9_R1) {
            try {
                net.minecraft.server.v1_9_R1.TileEntitySign sign = (net.minecraft.server.v1_9_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_9_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].toPlainText();
                data.lines[1] = sign.lines[1].toPlainText();
                data.lines[2] = sign.lines[2].toPlainText();
                data.lines[3] = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_9_R1.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_9_R1.WorldServer) {
                        ((net.minecraft.server.v1_9_R1.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_9_R2) {
            try {
                net.minecraft.server.v1_9_R2.TileEntitySign sign = (net.minecraft.server.v1_9_R2.TileEntitySign) ((org.bukkit.craftbukkit.v1_9_R2.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].toPlainText();
                data.lines[1] = sign.lines[1].toPlainText();
                data.lines[2] = sign.lines[2].toPlainText();
                data.lines[3] = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_9_R2.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_9_R2.WorldServer) {
                        ((net.minecraft.server.v1_9_R2.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_10_R1) {
            try {
                net.minecraft.server.v1_10_R1.TileEntitySign sign = (net.minecraft.server.v1_10_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_10_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].toPlainText();
                data.lines[1] = sign.lines[1].toPlainText();
                data.lines[2] = sign.lines[2].toPlainText();
                data.lines[3] = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_10_R1.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_10_R1.WorldServer) {
                        ((net.minecraft.server.v1_10_R1.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_11_R1) {
            try {
                net.minecraft.server.v1_11_R1.TileEntitySign sign = (net.minecraft.server.v1_11_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_11_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].toPlainText();
                data.lines[1] = sign.lines[1].toPlainText();
                data.lines[2] = sign.lines[2].toPlainText();
                data.lines[3] = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_11_R1.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_11_R1.WorldServer) {
                        ((net.minecraft.server.v1_11_R1.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_12_R1) {
            try {
                net.minecraft.server.v1_12_R1.TileEntitySign sign = (net.minecraft.server.v1_12_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_12_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].toPlainText();
                data.lines[1] = sign.lines[1].toPlainText();
                data.lines[2] = sign.lines[2].toPlainText();
                data.lines[3] = sign.lines[3].toPlainText();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_12_R1.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_12_R1.WorldServer) {
                        ((net.minecraft.server.v1_12_R1.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_13_R1) {
            try {
                net.minecraft.server.v1_13_R1.TileEntitySign sign = (net.minecraft.server.v1_13_R1.TileEntitySign) ((org.bukkit.craftbukkit.v1_13_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].c();
                data.lines[1] = sign.lines[1].c();
                data.lines[2] = sign.lines[2].c();
                data.lines[3] = sign.lines[3].c();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_13_R1.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_13_R1.WorldServer) {
                        ((net.minecraft.server.v1_13_R1.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (v1_13_R2) {
            try {
                net.minecraft.server.v1_13_R2.TileEntitySign sign = (net.minecraft.server.v1_13_R2.TileEntitySign) ((org.bukkit.craftbukkit.v1_13_R2.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
                SignData data = new SignData();
                data.lines[0] = sign.lines[0].e();
                data.lines[1] = sign.lines[1].e();
                data.lines[2] = sign.lines[2].e();
                data.lines[3] = sign.lines[3].e();
                if (change.test(data)) {
                    sign.lines[0] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.lines[0]);
                    sign.lines[1] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.lines[1]);
                    sign.lines[2] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.lines[2]);
                    sign.lines[3] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.lines[3]);
                    net.minecraft.server.v1_13_R2.World world = sign.getWorld();
                    if (world instanceof net.minecraft.server.v1_13_R2.WorldServer) {
                        ((net.minecraft.server.v1_13_R2.WorldServer) world).getPlayerChunkMap().flagDirty(sign.getPosition());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
