package org.soraworld.locket.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Setting;
import org.soraworld.locket.data.LockData;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.data.State;
import org.soraworld.locket.serializers.BlockTypeSerializer;
import org.soraworld.locket.serializers.ChatTypeSerializer;
import org.soraworld.locket.serializers.TextSerializer;
import org.soraworld.violet.data.DataAPI;
import org.soraworld.violet.inject.MainManager;
import org.soraworld.violet.manager.VManager;
import org.soraworld.violet.plugin.SpongePlugin;
import org.soraworld.violet.util.ChatColor;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@MainManager
public class LocketManager extends VManager {

    @Setting(comment = "comment.protectTile")
    private boolean protectTile = false;
    @Setting(comment = "comment.protectCarrier")
    private boolean protectCarrier = true;
    @Setting(comment = "comment.chatType")
    private ChatType chatType = ChatTypes.ACTION_BAR;
    @Setting(comment = "comment.defaultSign", trans = 0b1000)
    private Text privateSign = Text.of("" + ChatColor.DARK_RED + ChatColor.BOLD + "[Private]");
    @Setting(comment = "comment.ownerFormat", trans = 0b1000)
    private String ownerFormat = ChatColor.GREEN + "{$owner}";
    @Setting(comment = "comment.userFormat", trans = 0b1000)
    private String userFormat = "" + ChatColor.DARK_GRAY + ChatColor.ITALIC + "{$user}";
    @Setting(comment = "comment.acceptSigns", trans = 0b1000)
    private Set<String> acceptSigns = new HashSet<>();
    @Setting(comment = "comment.lockables")
    private Set<BlockType> lockables = new HashSet<>();
    @Setting(comment = "comment.doubleBlocks")
    private Set<BlockType> doubleBlocks = new HashSet<>();

    private static final String SELECTED_KEY = "lock:selected";

    private static final Direction[] FACES = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public LocketManager(SpongePlugin plugin, Path path) {
        super(plugin, path);
        try {
            options.registerType(new ChatTypeSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            options.registerType(new BlockTypeSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            options.registerType(new TextSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    public ChatColor defChatColor() {
        return ChatColor.YELLOW;
    }

    public void afterLoad() {
        if (privateSign == null) privateSign = Text.of("[Private]");
        acceptSigns.add(privateSign.toPlain());
        HashSet<String> temp = new HashSet<>();
        acceptSigns.forEach(sign -> temp.add(ChatColor.stripAllColor(sign)));
        acceptSigns.clear();
        acceptSigns.addAll(temp);
        lockables.add(BlockTypes.CHEST);
        lockables.add(BlockTypes.TRAPPED_CHEST);
        doubleBlocks.add(BlockTypes.CHEST);
        doubleBlocks.add(BlockTypes.TRAPPED_CHEST);
    }

    public boolean isLockable(@NotNull Location<World> location) {
        BlockType type = location.getBlockType();
        if (type == BlockTypes.WALL_SIGN || type == BlockTypes.STANDING_SIGN) return false;
        if (lockables.contains(type)) return true;
        if (doubleBlocks.contains(type)) return true;
        TileEntity tile = location.getTileEntity().orElse(null);
        return protectTile && tile != null || protectCarrier && tile instanceof TileEntityCarrier;
    }

    public void addType(@NotNull BlockType type) {
        lockables.add(type);
    }

    public void addDType(@NotNull BlockType type) {
        doubleBlocks.add(type);
    }

    public void removeType(BlockType type) {
        lockables.remove(type);
    }

    public void removeDType(@NotNull BlockType type) {
        doubleBlocks.remove(type);
    }

    public void sendHint(Player player, String key, Object... args) {
        if (chatType == ChatTypes.CHAT || chatType == ChatTypes.SYSTEM) {
            sendKey(player, key, args);
        } else {
            sendActionKey(player, key, args);
        }
    }

    public boolean isPrivate(@NotNull String line) {
        return acceptSigns.contains(line);
    }

    public Text getPrivateText() {
        return privateSign;
    }

    public Text getOwnerText(String owner) {
        return Text.of(ownerFormat.replace("{$owner}", owner));
    }

    public Text getUserText(String user) {
        return Text.of(userFormat.replace("{$user}", user));
    }

    public boolean isDBlock(@NotNull BlockType type) {
        return doubleBlocks.contains(type);
    }

    @Nullable
    public Location<World> getSelected(@NotNull Player player) {
        return DataAPI.getTemp(player.getUniqueId(), SELECTED_KEY, Location.class);
    }

    public void setSelected(@NotNull Player player, Location<World> location) {
        DataAPI.setTemp(player.getUniqueId(), SELECTED_KEY, location);
    }

    /* TODO Only player access consider third-party plugin */
    public Result tryAccess(@NotNull Player player, @NotNull Location<World> location) {
        if (otherProtected(player, location)) return Result.OTHER_PROTECT;
        BlockType type = location.getBlockType();
        boolean isDBlock = doubleBlocks.contains(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 自身也将参与检查
        signs.add(location);

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : FACES) {
            Location<World> relative = location.getRelative(face);
            if (isDBlock && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) return Result.MULTI_BLOCKS;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(relative);
            }
        }
        // 检查相邻双联方块
        if (isDBlock && link != null) {
            if (otherProtected(player, link)) return Result.OTHER_PROTECT;
            count = 0;
            for (Direction face : FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return Result.MULTI_BLOCKS;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }
        return analyzeSign(player, signs);
    }

    public boolean otherProtected(Player player, Location<World> block) {
        return false;
    }

    public void lockSign(Player player, Location<World> selected, int line, String name) {
        TileEntity tile = selected.getTileEntity().orElse(null);
        if (tile instanceof Sign) {
            // ??? ((Sign) tile).lines().set()
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, privateSign);
            data.setElement(1, getOwnerText(player.getName()));
            if ((line == 2 || line == 3) && name != null && !name.isEmpty()) {
                data.setElement(line, getUserText(name));
            }
            tile.offer(data);
            sendHint(player, "manuLock");
        } else sendHint(player, "notSignTile");
    }

    public void unLockSign(Location<World> location, int line) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(line, Text.EMPTY);
            tile.offer(data);
        }
    }

    public void placeLock(Player player, Location<World> loc, Direction face, HandType hand) {
        Location<World> side = loc.getRelative(face);
        side.setBlockType(BlockTypes.WALL_SIGN, BlockChangeFlags.NONE);
        BlockState state = BlockTypes.WALL_SIGN.getDefaultState();
        side.setBlock(state.with(Keys.DIRECTION, face).orElse(state), BlockChangeFlags.NONE);
        TileEntity tile = side.getTileEntity().orElse(null);
        if (tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, getPrivateText());
            data.setElement(1, getOwnerText(player.getName()));
            tile.offer(data);
        }
        removeOneItem(player, hand);
        player.playSound(SoundTypes.BLOCK_WOOD_PLACE, loc.getPosition(), 1.0D);
        sendHint(player, "quickLock");
    }

    public boolean isLocked(@NotNull Location<World> location) {
        return checkState(location) != State.NOT_LOCKED;
    }

    public boolean notLocked(@NotNull Location<World> location) {
        return checkState(location) == State.NOT_LOCKED;
    }

    private State checkState(@NotNull Location<World> location) {
        BlockType type = location.getBlockType();
        boolean isDBlock = doubleBlocks.contains(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();
        signs.add(location);

        for (Direction face : FACES) {
            Location<World> relative = location.getRelative(face);
            if (isDBlock && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) return State.MULTI_BLOCKS;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(relative);
            }
        }

        if (isDBlock && link != null) {
            count = 0;
            for (Direction face : FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return State.MULTI_BLOCKS;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }

        return new LockData(signs).getState();
    }

    private Result analyzeSign(@NotNull Player player, HashSet<Location<World>> signs) {
        if (signs.isEmpty()) return Result.NOT_LOCKED;
        LockData data = new LockData(signs);
        return data.tryAccess(player);
    }

    private static void removeOneItem(Player player, HandType hand) {
        if (GameModes.CREATIVE.equals(player.gameMode().get())) return;
        ItemStack stack = player.getItemInHand(hand).orElse(null);
        if (stack != null && stack.getQuantity() >= 1) {
            stack.setQuantity(stack.getQuantity() - 1);
            player.setItemInHand(hand, stack);
        } else {
            player.setItemInHand(hand, null);
        }
    }

    public static Location<World> getAttached(@NotNull Location<World> location) {
        return location.getRelative(location.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite());
    }
}
