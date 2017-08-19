package org.soraworld.locket.api;

import org.soraworld.locket.Locket;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.AccessResult;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.constant.Permissions;
import org.soraworld.locket.data.LockSignData;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class IPlayer {

    private final String username;
    private final Player player;
    private Location<World> selected;

    IPlayer(@Nonnull Player player) {
        this.player = player;
        this.username = player.getName();
    }

    public AccessResult canLock(@Nonnull Location<World> location) {
        if (!player.hasPermission(Permissions.LOCK)) return AccessResult.NO_PERM_LOCK;
        BlockType type = location.getBlockType();
        if (!Config.isLockable(type)) return AccessResult.UNLOCKABLE;
        boolean isDChest = LocketAPI.isDChest(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : Constants.FACES) {
            Location<World> relative = location.getRelative(face);
            if (isDChest && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) return AccessResult.M_CHESTS;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                signs.add(relative);
            }
        }
        // 检查相邻箱子
        if (isDChest && link != null) {
            count = 0;
            for (Direction face : Constants.FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return AccessResult.M_CHESTS;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                    signs.add(relative);
                }
            }
        }
        return analyzeSign(signs);
    }

    public AccessResult canInteract(@Nonnull Location<World> location) {
        BlockType type = location.getBlockType();
        if (player.hasPermission(Permissions.ADMIN_INTERACT)) return AccessResult.ADMIN_INTERACT;

        int count = 0;
        boolean isDChest = LocketAPI.isDChest(type);
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : Constants.FACES) {
            Location<World> relative = location.getRelative(face);
            if (isDChest && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) return AccessResult.M_CHESTS;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                signs.add(relative);
            }
        }
        // 检查相邻箱子
        if (isDChest && link != null) {
            count = 0;
            for (Direction face : Constants.FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return AccessResult.M_CHESTS;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                    signs.add(relative);
                }
            }
        }
        return analyzeSign(signs);
    }

    public AccessResult canBreak(@Nonnull Location<World> location) {
        BlockType type = location.getBlockType();
        if (type == BlockTypes.WALL_SIGN) {
            if (player.hasPermission(Permissions.ADMIN_UNLOCK)) return AccessResult.ADMIN_UNLOK;
            return analyzeSign(location);
        }
        if (player.hasPermission(Permissions.ADMIN_BREAK)) return AccessResult.ADMIN_BREAK;

        int count = 0;
        boolean isDChest = LocketAPI.isDChest(type);
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : Constants.FACES) {
            Location<World> relative = location.getRelative(face);
            if (isDChest && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) return AccessResult.M_CHESTS;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                signs.add(relative);
            }
        }
        // 检查相邻箱子
        if (isDChest && link != null) {
            count = 0;
            for (Direction face : Constants.FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return AccessResult.M_CHESTS;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                    signs.add(relative);
                }
            }
        }
        return analyzeSign(signs);
    }

    private AccessResult analyzeSign(@Nonnull Location<World> location) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            return LocketAPI.parseSign((Sign) tile).getAccess(username);
        }
        return AccessResult.NOT_LOCK;
    }

    private AccessResult analyzeSign(@Nonnull HashSet<Location<World>> locations) {
        if (locations.isEmpty()) return AccessResult.NOT_LOCK;
        LockSignData data = new LockSignData();
        for (Location<World> location : locations) {
            TileEntity tile = location.getTileEntity().orElse(null);
            if (tile != null && tile instanceof Sign) {
                data.append(LocketAPI.parseSign((Sign) tile));
            }
        }
        return data.getAccess(username);
    }

    public Location<World> selection() {
        return selected;
    }

    public void select(@Nonnull Location<World> selected) {
        this.selected = selected;
    }

    public void placeLock(@Nonnull Location<World> location, Direction face) {
        Location<World> relative = location.getRelative(face);
        relative.setBlockType(BlockTypes.WALL_SIGN, Cause.source(Locket.getLocket().getPlugin()).build());
        BlockState state = BlockTypes.WALL_SIGN.getDefaultState();
        relative.setBlock(state.with(Keys.DIRECTION, face).orElse(state), Cause.source(Locket.getLocket().getPlugin()).build());

        TileEntity tile = relative.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, Text.of("[Private]" + player.getName()));
            tile.offer(data);
        }
        player.playSound(SoundTypes.BLOCK_WOOD_PLACE, location.getPosition(), 1.0D);
    }

    public void removeSign(HandType hand) {
        if (GameModes.CREATIVE.equals(player.gameMode().get())) return;
        ItemStack stack = player.getItemInHand(hand).orElse(null);
        if (stack != null && stack.getQuantity() > 1) {
            stack.setQuantity(stack.getQuantity() - 1);
            player.setItemInHand(hand, stack);
        } else {
            player.setItemInHand(hand, null);
        }
    }

    public void sendChat(String message) {
        player.sendMessage(ChatTypes.CHAT, TextSerializers.FORMATTING_CODE.deserialize(message));
    }

    public boolean isOtherProtect(Location<World> location) {
        return false;
    }

    public boolean canInterfere(Location<World> location) {
        return true;
    }
}
