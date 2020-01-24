package org.soraworld.locket.nms;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.soraworld.locket.Locket;
import org.soraworld.violet.inject.Inject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

import static org.soraworld.violet.nms.Version.*;

/**
 * @author Himmelt
 */
@Inject
public class Helper {

    @Inject
    private static Locket plugin;
    public static final boolean v1_14_R1, v1_15_R1;

    static {
        boolean v1_14_r1 = false, v1_15_r1 = false;
        try {
            org.bukkit.craftbukkit.v1_14_R1.CraftWorld.class.getName();
            net.minecraft.server.v1_14_R1.WorldServer.class.getName();
            v1_14_r1 = true;
        } catch (Throwable ignored) {
        }
        try {
            org.bukkit.craftbukkit.v1_15_R1.CraftWorld.class.getName();
            net.minecraft.server.v1_15_R1.WorldServer.class.getName();
            v1_15_r1 = true;
        } catch (Throwable ignored) {
        }
        v1_14_R1 = v1_14_r1;
        v1_15_R1 = v1_15_r1;
    }

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

    public static void touchSign(Block block, @Nullable Predicate<SignData> sync, @Nullable Predicate<SignData> async) {
        Object sign = null;
        try {
            if (v1_7_R4) {
                sign = ((org.bukkit.craftbukkit.v1_7_R4.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_8_R1) {
                sign = ((org.bukkit.craftbukkit.v1_8_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_8_R3) {
                sign = ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_9_R1) {
                sign = ((org.bukkit.craftbukkit.v1_9_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_9_R2) {
                sign = ((org.bukkit.craftbukkit.v1_9_R2.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_10_R1) {
                sign = ((org.bukkit.craftbukkit.v1_10_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_11_R1) {
                sign = ((org.bukkit.craftbukkit.v1_11_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_12_R1) {
                sign = ((org.bukkit.craftbukkit.v1_12_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_13_R1) {
                sign = ((org.bukkit.craftbukkit.v1_13_R1.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_13_R2) {
                sign = ((org.bukkit.craftbukkit.v1_13_R2.CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
            } else if (v1_14_R1) {
                sign = ((org.bukkit.craftbukkit.v1_14_R1.CraftWorld) block.getWorld()).getHandle().getTileEntity(new net.minecraft.server.v1_14_R1.BlockPosition(block.getX(), block.getY(), block.getZ()));
            } else if (v1_15_R1) {
                sign = ((org.bukkit.craftbukkit.v1_15_R1.CraftWorld) block.getWorld()).getHandle().getTileEntity(new net.minecraft.server.v1_15_R1.BlockPosition(block.getX(), block.getY(), block.getZ()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        SignData data = getSignData(sign);
        if (sync != null && sync.test(data)) {
            updateSignData(sign, data);
        }
        if (async != null) {
            Object theSign = sign;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (async.test(data)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        updateSignData(theSign, data);
                    });
                }
            });
        }
    }

    private static SignData getSignData(Object tile) {
        SignData data = new SignData();
        if (v1_7_R4) {
            net.minecraft.server.v1_7_R4.TileEntitySign sign = (net.minecraft.server.v1_7_R4.TileEntitySign) tile;
            data.lines[0] = sign.lines[0];
            data.lines[1] = sign.lines[1];
            data.lines[2] = sign.lines[2];
            data.lines[3] = sign.lines[3];
        } else if (v1_8_R1) {
            net.minecraft.server.v1_8_R1.TileEntitySign sign = (net.minecraft.server.v1_8_R1.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].c();
            data.lines[1] = sign.lines[1].c();
            data.lines[2] = sign.lines[2].c();
            data.lines[3] = sign.lines[3].c();
        } else if (v1_8_R3) {
            net.minecraft.server.v1_8_R3.TileEntitySign sign = (net.minecraft.server.v1_8_R3.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].c();
            data.lines[1] = sign.lines[1].c();
            data.lines[2] = sign.lines[2].c();
            data.lines[3] = sign.lines[3].c();
        } else if (v1_9_R1) {
            net.minecraft.server.v1_9_R1.TileEntitySign sign = (net.minecraft.server.v1_9_R1.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].toPlainText();
            data.lines[1] = sign.lines[1].toPlainText();
            data.lines[2] = sign.lines[2].toPlainText();
            data.lines[3] = sign.lines[3].toPlainText();
        } else if (v1_9_R2) {
            net.minecraft.server.v1_9_R2.TileEntitySign sign = (net.minecraft.server.v1_9_R2.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].toPlainText();
            data.lines[1] = sign.lines[1].toPlainText();
            data.lines[2] = sign.lines[2].toPlainText();
            data.lines[3] = sign.lines[3].toPlainText();
        } else if (v1_10_R1) {
            net.minecraft.server.v1_10_R1.TileEntitySign sign = (net.minecraft.server.v1_10_R1.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].toPlainText();
            data.lines[1] = sign.lines[1].toPlainText();
            data.lines[2] = sign.lines[2].toPlainText();
            data.lines[3] = sign.lines[3].toPlainText();
        } else if (v1_11_R1) {
            net.minecraft.server.v1_11_R1.TileEntitySign sign = (net.minecraft.server.v1_11_R1.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].toPlainText();
            data.lines[1] = sign.lines[1].toPlainText();
            data.lines[2] = sign.lines[2].toPlainText();
            data.lines[3] = sign.lines[3].toPlainText();
        } else if (v1_12_R1) {
            net.minecraft.server.v1_12_R1.TileEntitySign sign = (net.minecraft.server.v1_12_R1.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].toPlainText();
            data.lines[1] = sign.lines[1].toPlainText();
            data.lines[2] = sign.lines[2].toPlainText();
            data.lines[3] = sign.lines[3].toPlainText();
        } else if (v1_13_R1) {
            net.minecraft.server.v1_13_R1.TileEntitySign sign = (net.minecraft.server.v1_13_R1.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].c();
            data.lines[1] = sign.lines[1].c();
            data.lines[2] = sign.lines[2].c();
            data.lines[3] = sign.lines[3].c();
        } else if (v1_13_R2) {
            net.minecraft.server.v1_13_R2.TileEntitySign sign = (net.minecraft.server.v1_13_R2.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].e();
            data.lines[1] = sign.lines[1].e();
            data.lines[2] = sign.lines[2].e();
            data.lines[3] = sign.lines[3].e();
        } else if (v1_14_R1) {
            net.minecraft.server.v1_14_R1.TileEntitySign sign = (net.minecraft.server.v1_14_R1.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].e();
            data.lines[1] = sign.lines[1].e();
            data.lines[2] = sign.lines[2].e();
            data.lines[3] = sign.lines[3].e();
        } else if (v1_15_R1) {
            net.minecraft.server.v1_15_R1.TileEntitySign sign = (net.minecraft.server.v1_15_R1.TileEntitySign) tile;
            data.lines[0] = sign.lines[0].getLegacyString();
            data.lines[1] = sign.lines[1].getLegacyString();
            data.lines[2] = sign.lines[2].getLegacyString();
            data.lines[3] = sign.lines[3].getLegacyString();
        }
        return data;
    }

    private static void updateSignData(Object tile, SignData data) {
        if (v1_7_R4) {
            net.minecraft.server.v1_7_R4.TileEntitySign sign = (net.minecraft.server.v1_7_R4.TileEntitySign) tile;
            sign.lines[0] = data.lines[0];
            sign.lines[1] = data.lines[1];
            sign.lines[2] = data.lines[2];
            sign.lines[3] = data.lines[3];
            ((net.minecraft.server.v1_7_R4.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.x, sign.y, sign.z);
        } else if (v1_8_R1) {
            net.minecraft.server.v1_8_R1.TileEntitySign sign = (net.minecraft.server.v1_8_R1.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_8_R1.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_8_R1.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_8_R3) {
            net.minecraft.server.v1_8_R3.TileEntitySign sign = (net.minecraft.server.v1_8_R3.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_8_R3.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_8_R3.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_9_R1) {
            net.minecraft.server.v1_9_R1.TileEntitySign sign = (net.minecraft.server.v1_9_R1.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_9_R1.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_9_R1.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_9_R2) {
            net.minecraft.server.v1_9_R2.TileEntitySign sign = (net.minecraft.server.v1_9_R2.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_9_R2.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_9_R2.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_10_R1) {
            net.minecraft.server.v1_10_R1.TileEntitySign sign = (net.minecraft.server.v1_10_R1.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_10_R1.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_10_R1.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_11_R1) {
            net.minecraft.server.v1_11_R1.TileEntitySign sign = (net.minecraft.server.v1_11_R1.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_11_R1.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_11_R1.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_12_R1) {
            net.minecraft.server.v1_12_R1.TileEntitySign sign = (net.minecraft.server.v1_12_R1.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_12_R1.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_12_R1.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_13_R1) {
            net.minecraft.server.v1_13_R1.TileEntitySign sign = (net.minecraft.server.v1_13_R1.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_13_R1.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_13_R1.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_13_R2) {
            net.minecraft.server.v1_13_R2.TileEntitySign sign = (net.minecraft.server.v1_13_R2.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_13_R2.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_13_R2.WorldServer) sign.getWorld()).getPlayerChunkMap().flagDirty(sign.getPosition());
        } else if (v1_14_R1) {
            net.minecraft.server.v1_14_R1.TileEntitySign sign = (net.minecraft.server.v1_14_R1.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_14_R1.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_14_R1.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_14_R1.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_14_R1.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_14_R1.WorldServer) sign.getWorld()).getChunkProvider().flagDirty(sign.getPosition());
        } else if (v1_15_R1) {
            net.minecraft.server.v1_15_R1.TileEntitySign sign = (net.minecraft.server.v1_15_R1.TileEntitySign) tile;
            sign.lines[0] = new net.minecraft.server.v1_15_R1.ChatComponentText(data.lines[0]);
            sign.lines[1] = new net.minecraft.server.v1_15_R1.ChatComponentText(data.lines[1]);
            sign.lines[2] = new net.minecraft.server.v1_15_R1.ChatComponentText(data.lines[2]);
            sign.lines[3] = new net.minecraft.server.v1_15_R1.ChatComponentText(data.lines[3]);
            ((net.minecraft.server.v1_15_R1.WorldServer) sign.getWorld()).getChunkProvider().flagDirty(sign.getPosition());
        }
    }

    public static void setSignRotation(Sign sign, BlockFace face) {
        if (v1_14_R1 || v1_15_R1) {
            org.bukkit.block.data.type.WallSign signData = (WallSign) sign.getBlockData();
            signData.setFacing(face);
            sign.setBlockData(signData);
        } else {
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();
            signData.setFacingDirection(face);
            sign.setData(signData);
        }
        sign.update();
    }

    public static Block getAttached(@NotNull Sign sign) {
        BlockFace face;
        if (v1_14_R1 || v1_15_R1) {
            face = ((WallSign) sign.getBlockData()).getFacing().getOppositeFace();
        } else {
            face = ((org.bukkit.material.Sign) sign.getData()).getAttachedFace();
        }
        return sign.getBlock().getRelative(face);
    }

    public static BlockFace getDoorFace(Block block) {
        if (v1_14_R1 || v1_15_R1) {
            return ((Door) block.getBlockData()).getFacing();
        }
        return ((org.bukkit.material.Door) block.getState().getData()).getFacing();
    }

    public static BlockFace getAttachedFace(@NotNull Sign sign) {
        if (v1_14_R1 || v1_15_R1) {
            return ((WallSign) sign.getBlockData()).getFacing().getOppositeFace();
        } else {
            return ((org.bukkit.material.Sign) sign.getData()).getAttachedFace();
        }
    }
}
