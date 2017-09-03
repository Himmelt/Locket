package org.soraworld.locket.api;

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

    private void removeSign(HandType hand) {
        if (GameModes.CREATIVE.equals(player.gameMode().get())) return;
        ItemStack stack = player.getItemInHand(hand).orElse(null);
        if (stack != null && stack.getQuantity() > 1) {
            stack.setQuantity(stack.getQuantity() - 1);
            player.setItemInHand(hand, stack);
        } else {
            player.setItemInHand(hand, null);
        }
    }

    public Result analyzeSign(@Nonnull Location<World> block) {
        TileEntity tile = block.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            return LocketAPI.parseSign((Sign) tile).getAccess(username);
        }
        return Result.SIGN_NOT_LOCK;
    }

    public Location<World> selection() {
        return selected;
    }

    public void select(@Nonnull Location<World> selected) {
        this.selected = selected;
    }

    public void placeLock(@Nonnull Location<World> location, Direction face, HandType hand) {
        Location<World> relative = location.getRelative(face);
        relative.setBlockType(BlockTypes.WALL_SIGN, Constants.PLUGIN_CAUSE);
        BlockState state = BlockTypes.WALL_SIGN.getDefaultState();
        relative.setBlock(state.with(Keys.DIRECTION, face).orElse(state), Constants.PLUGIN_CAUSE);
        TileEntity tile = relative.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, LocketAPI.CONFIG.getPrivateText());
            data.setElement(1, LocketAPI.CONFIG.getOwnerText(username));
            tile.offer(data);
        }
        removeSign(hand);
        player.playSound(SoundTypes.BLOCK_WOOD_PLACE, location.getPosition(), 1.0D);
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
            ChatType chatType = LocketAPI.CONFIG.getChatType();
            Text text = TextSerializers.FORMATTING_CODE.deserialize(message);
            if (chatType == ChatTypes.ACTION_BAR) {
                player.sendMessage(chatType, text);
            } else {
                player.sendMessage(chatType, LocketAPI.CONFIG.HEAD().concat(text));
            }
        }
    }

    public void adminNotify(Text text) {
        if (LocketAPI.CONFIG.isAdminNotify()) {
            ChatType chatType = LocketAPI.CONFIG.getChatType();
            if (chatType == ChatTypes.ACTION_BAR) {
                player.sendMessage(chatType, text);
            } else {
                player.sendMessage(chatType, LocketAPI.CONFIG.HEAD().concat(text));
            }
        }
    }

    public Result tryAccess(@Nonnull Location<World> block) {
        if (isOtherProtect(block)) return Result.SIGN_NO_ACCESS;
        BlockType type = block.getBlockType();
        boolean isDBlock = LocketAPI.isDBlock(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 自身也将参与检查
        signs.add(block);

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : Constants.FACES) {
            Location<World> relative = block.getRelative(face);
            if (isDBlock && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) return Result.M_BLOCKS;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(relative);
            }
        }
        // 检查相邻双联方块
        if (isDBlock && link != null) {
            if (isOtherProtect(link)) return Result.SIGN_NO_ACCESS;
            count = 0;
            for (Direction face : Constants.FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return Result.M_BLOCKS;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }
        return analyzeSign(signs);
    }

    public boolean isOtherProtect(Location<World> location) {
        return false;
    }

    public boolean hasPerm(String perm) {
        return player.hasPermission(perm);
    }

    public void lockSign(Location<World> location) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, LocketAPI.CONFIG.getPrivateText());
            data.setElement(1, LocketAPI.CONFIG.getOwnerText(username));
            tile.offer(data);
        }
    }

    // line: null/3/4   name:null/name
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
            data.setElement(line, Text.EMPTY);
            tile.offer(data);
        }
    }

    public BlockType getHeldBlockType() {
        ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        return stack == null ? BlockTypes.AIR : stack.getItem().getBlock().orElse(BlockTypes.AIR);
    }

}
