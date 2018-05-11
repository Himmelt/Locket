package org.soraworld.locket.core;

import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.constant.Result;
import org.soraworld.locket.data.LockSignData;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;

public class WrappedPlayer implements IPlayer {

    private final String username;
    private final Player player;
    private Location<World> selected;
    private Result latest;
    private long last;
    private Location<World> target;
    private static final int delay = 1000;
    private static final Direction[] FACES = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public WrappedPlayer(@Nonnull Player player) {
        this.player = player;
        this.username = player.getName();
        this.latest = Result.SIGN_NO_ACCESS;
        this.last = System.currentTimeMillis();
    }

    public boolean canAccess(Location<World> loc) {
        if (otherProtected(loc)) return false;
        List<Location<World>> signs = LocketAPI.getSideSigns(loc);
        if (signs == null) return false;
        for (Location<World> sign : signs) {
            TileEntity tile = sign.getTileEntity().orElse(null);
            if (tile instanceof Sign && LocketAPI.isPrivate((Sign) tile)) return true;
        }
        return true;
    }

    public Role access(@Nonnull List<Location<World>> signs) {
        HashSet<String> users = new HashSet<>();
        HashSet<String> owners = new HashSet<>();
        for (Location<World> sign : signs) {
            TileEntity tile = sign.getTileEntity().orElse(null);
            if (tile instanceof Sign && LocketAPI.isPrivate(((Sign) tile).lines().get(0).toPlain())) {
                owners.add(((Sign) tile).lines().get(1).toPlain());
                users.add(((Sign) tile).lines().get(2).toPlain());
                users.add(((Sign) tile).lines().get(3).toPlain());
            }
        }
        if (owners.size() <= 0) return Role.NONE;
        if (owners.size() >= 2) return Role.SIGN_M_OWNERS;
        if (owners.contains(username)) return Role.SIGN_OWNER;
        if (users.contains(username)) return Role.SIGN_USER;
        return Role.NONE;
    }

    private Result analyzeSign(@Nonnull HashSet<Location<World>> signs) {
        if (signs.isEmpty()) return Result.SIGN_NOT_LOCK;
        LockSignData data = new LockSignData();
        for (Location<World> block : signs) {
            TileEntity tile = block.getTileEntity().orElse(null);
            if (tile != null && tile instanceof Sign) {
                data.append(LocketAPI.parseSign((Sign) tile));
            }
        }
        return data.getAccess(username);
    }

    private void removeOneSign(HandType hand) {
        if (GameModes.CREATIVE.equals(player.gameMode().get())) return;
        ItemStack stack = player.getItemInHand(hand).orElse(null);
        if (stack != null && stack.getQuantity() > 1) {
            stack.setQuantity(stack.getQuantity() - 1);
            player.setItemInHand(hand, stack);
        } else {
            player.setItemInHand(hand, null);
        }
    }

    public Location<World> selection() {
        return selected;
    }

    public void select(Location<World> selected) {
        this.selected = selected;
    }

    public void placeLock(@Nonnull Location<World> loc, Direction face, HandType hand) {
        Location<World> side = loc.getRelative(face);
        side.setBlockType(BlockTypes.WALL_SIGN, BlockChangeFlags.NONE);
        BlockState state = BlockTypes.WALL_SIGN.getDefaultState();
        side.setBlock(state.with(Keys.DIRECTION, face).orElse(state), BlockChangeFlags.NONE);
        TileEntity tile = side.getTileEntity().orElse(null);
        if (tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, LocketAPI.CONFIG.getPrivateText());
            data.setElement(1, LocketAPI.CONFIG.getOwnerText(username));
            tile.offer(data);
        }
        removeOneSign(hand);
        player.playSound(SoundTypes.BLOCK_WOOD_PLACE, loc.getPosition(), 1.0D);
    }

    public void sendChat(String message) {
        ChatType chatType = LocketAPI.CONFIG.getChatType();
        Text text = TextSerializers.FORMATTING_CODE.deserialize(message);
        if (chatType == ChatTypes.ACTION_BAR) {
            player.sendMessage(chatType, text);
        } else {
            player.sendMessage(chatType, LocketAPI.CONFIG.HEAD().concat(text));
        }
    }

    public void sendChat(Text text) {
        ChatType chatType = LocketAPI.CONFIG.getChatType();
        if (chatType == ChatTypes.ACTION_BAR) {
            player.sendMessage(chatType, text);
        } else {
            player.sendMessage(chatType, LocketAPI.CONFIG.HEAD().concat(text));
        }
    }

    public void sendChat(ChatType type, String message) {
        Text text = TextSerializers.FORMATTING_CODE.deserialize(message);
        if (type == ChatTypes.ACTION_BAR) {
            player.sendMessage(type, text);
        } else {
            player.sendMessage(type, LocketAPI.CONFIG.HEAD().concat(text));
        }
    }

    public void adminNotify(String message) {
        if (LocketAPI.CONFIG.isAdminNotify()) {
            Text text = TextSerializers.FORMATTING_CODE.deserialize(message);
            player.sendMessage(LocketAPI.CONFIG.HEAD().concat(text));
        }
    }

    public void adminNotify(Text text) {
        if (LocketAPI.CONFIG.isAdminNotify()) {
            player.sendMessage(LocketAPI.CONFIG.HEAD().concat(text));
        }
    }

    public Result tryAccess(@Nonnull Location<World> block) {
        if (block.equals(target) && last + delay > System.currentTimeMillis()) {
            return latest;
        }
        target = block;
        last = System.currentTimeMillis();

        if (otherProtected(block)) {
            latest = Result.SIGN_NO_ACCESS;
            return latest;
        }
        BlockType type = block.getBlockType();
        boolean isDBlock = LocketAPI.isDuplex(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 自身也将参与检查
        signs.add(block);

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : FACES) {
            Location<World> relative = block.getRelative(face);
            if (isDBlock && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) {
                    latest = Result.M_BLOCKS;
                    return latest;
                }
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(relative);
            }
        }
        // 检查相邻双联方块
        if (isDBlock && link != null) {
            if (otherProtected(link)) {
                latest = Result.SIGN_NO_ACCESS;
                return latest;
            }
            count = 0;
            for (Direction face : Constants.FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) {
                    latest = Result.M_BLOCKS;
                    return latest;
                }
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }
        latest = analyzeSign(signs);
        return latest;
    }

    public boolean otherProtected(Location<World> location) {
        return false;
    }

    public boolean hasPerm(String perm) {
        return player.hasPermission(perm);
    }

    public void lockSign(Location<World> location, Integer line, String name) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, LocketAPI.CONFIG.getPrivateText());
            data.setElement(1, LocketAPI.CONFIG.getOwnerText(username));
            if (line != null && (line == 3 || line == 4) && name != null && !name.isEmpty()) {
                data.setElement(line - 1, LocketAPI.CONFIG.getUserText(name));
            }
            tile.offer(data);
        }
    }

    public void unLockSign(Location<World> location, int line) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(line - 1, Text.EMPTY);
            tile.offer(data);
        }
    }

    public BlockType getHeldBlockType() {
        ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        return stack == null ? BlockTypes.AIR : stack.getType().getBlock().orElse(BlockTypes.AIR);
    }

}
