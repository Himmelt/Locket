package org.soraworld.locket.manager;

import org.jetbrains.annotations.Nullable;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Setting;
import org.soraworld.locket.data.LockData;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.serializers.ChatTypeSerializer;
import org.soraworld.locket.serializers.TextSerializer;
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
import org.spongepowered.api.data.value.mutable.ListValue;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@MainManager
public class LocketManager extends VManager {

    @Setting(comment = "comment.protectTile")
    private boolean protectTile = false;
    @Setting(comment = "comment.protectCarrier")
    private boolean protectCarrier = false;
    @Setting(comment = "comment.chatType")
    private ChatType chatType = ChatTypes.CHAT;
    @Setting(comment = "comment.defaultSign", trans = 0b1000)
    private Text privateSign = Text.of("" + ChatColor.DARK_RED + ChatColor.BOLD + "[Private]");
    @Setting(comment = "comment.ownerFormat", trans = 0b1000)
    private String ownerFormat = ChatColor.GREEN + "{$owner}";
    @Setting(comment = "comment.userFormat", trans = 0b1000)
    private String userFormat = "" + ChatColor.DARK_GRAY + ChatColor.ITALIC + "{$user}";
    @Setting(comment = "comment.acceptSigns", trans = 0b1000)
    private Set<String> acceptSigns = new HashSet<>();
    @Setting(comment = "comment.lockables")
    private Set<String> lockables = new HashSet<>();
    @Setting(comment = "comment.doubleBlocks")
    private Set<String> doubleBlocks = new HashSet<>();

    private static final Direction[] FACES = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private final HashMap<UUID, Location<World>> selections = new HashMap<>();

    public LocketManager(SpongePlugin plugin, Path path) {
        super(plugin, path);
        try {
            options.registerType(new ChatTypeSerializer());
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
        lockables.add(BlockTypes.CHEST.getId());
        lockables.add(BlockTypes.TRAPPED_CHEST.getId());
        doubleBlocks.add(BlockTypes.CHEST.getId());
        doubleBlocks.add(BlockTypes.TRAPPED_CHEST.getId());
    }

    public boolean isLockable(Location<World> block) {
        BlockType type = block.getBlockType();
        if (type == BlockTypes.WALL_SIGN || type == BlockTypes.STANDING_SIGN) return false;
        if (lockables.contains(type.getId())) return true;
        if (doubleBlocks.contains(type.getId())) return true;
        TileEntity tile = block.getTileEntity().orElse(null);
        return protectTile && tile != null || protectCarrier && tile instanceof TileEntityCarrier;
    }

    public void addType(String id) {
        lockables.add(id);
    }

    public void addType(BlockType type) {
        lockables.add(type.getId());
    }

    public void addDType(String id) {
        doubleBlocks.add(id);
    }

    public void addDType(BlockType type) {
        doubleBlocks.add(type.getId());
    }

    public void removeType(String id) {
        lockables.remove(id);
    }

    public void removeType(BlockType type) {
        lockables.remove(type.getId());
    }

    public void removeDType(String id) {
        doubleBlocks.remove(id);
    }

    public void removeDType(BlockType type) {
        doubleBlocks.remove(type.getId());
    }

    public ChatType getChatType() {
        return chatType;
    }

    public boolean isPrivate(String line) {
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

    public boolean isDBlock(BlockType type) {
        return doubleBlocks.contains(type.getId());
    }

    public Location<World> getSelected(Player player) {
        return selections.get(player.getUniqueId());
    }

    public void setSelected(Player player, Location<World> location) {
        selections.put(player.getUniqueId(), location);
    }

    public Result tryAccess(@Nullable Player player, Location<World> location) {
        if (otherProtected(player, location)) return Result.OTHER_PROTECT;
        BlockType type = location.getBlockType();
        boolean isDBlock = doubleBlocks.contains(type.getId());
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
                if (++count >= 2) return Result.M_BLOCKS;
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
                if (relative.getBlockType() == type && ++count >= 2) return Result.M_BLOCKS;
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
        } else sendKey(player, "notSignTile");
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
    }

    public void cleanPlayer(Player player) {
        selections.remove(player.getUniqueId());
    }

    public boolean isLocked(Location<World> location) {
        return tryAccess(null, location) != Result.SIGN_NOT_LOCK;
    }

    public boolean notLocked(Location<World> location) {
        return tryAccess(null, location) == Result.SIGN_NOT_LOCK;
    }

    private Result analyzeSign(@Nullable Player player, HashSet<Location<World>> signs) {
        if (signs.isEmpty()) return Result.SIGN_NOT_LOCK;
        LockData data = new LockData();
        for (Location<World> block : signs) {
            TileEntity tile = block.getTileEntity().orElse(null);
            if (tile instanceof Sign) {
                ListValue<Text> lines = ((Sign) tile).lines();
                String line_0 = ChatColor.stripAllColor(lines.get(0).toPlain()).trim();
                String line_1 = ChatColor.stripAllColor(lines.get(1).toPlain()).trim();
                String line_2 = ChatColor.stripAllColor(lines.get(2).toPlain()).trim();
                String line_3 = ChatColor.stripAllColor(lines.get(3).toPlain()).trim();
                if (isPrivate(line_0)) data.puts(line_1, line_2, line_3);
            }
        }
        return data.accessBy(player);
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

    public static Location<World> getAttached(Location<World> location) {
        return location.getRelative(location.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite());
    }
}
