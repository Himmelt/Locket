package org.soraworld.locket.config;

import org.soraworld.hocon.node.Setting;
import org.soraworld.locket.Locket;
import org.soraworld.locket.data.LockData;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.serializers.ChatTypeSerializer;
import org.soraworld.violet.manager.SpongeManager;
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
import java.util.*;

public class LocketManager extends SpongeManager {

    @Setting(comment = "Whether protect all tileentities")
    private boolean protectTile = false;
    @Setting(comment = "Whether protect all containers")
    private boolean protectCarrier = false;
    @Setting(comment = "ChatType: chat,action-bar")
    private ChatType chatType = ChatTypes.CHAT;
    @Setting(comment = "Default Private text")
    private Text defaultSign = Locket.DEFAULT_PRIVATE;
    @Setting(comment = "Acceptable Private texts")
    private Set<String> acceptSigns = new HashSet<>();
    @Setting(comment = "Lockable Block ID(s)")
    private Set<String> lockables = new HashSet<>();
    @Setting(comment = "The double-chest like blocks, which can be accessed from neighbors")
    private Set<String> doubleBlocks = new HashSet<>();

    private Text privateSign;
    private static final Direction[] FACES = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private final HashMap<UUID, Location<World>> selections = new HashMap<>();

    public LocketManager(SpongePlugin plugin, Path path) {
        super(plugin, path);
        options.registerType(new ChatTypeSerializer());
    }

    @Nonnull
    public ChatColor defChatColor() {
        return ChatColor.YELLOW;
    }

    public void afterLoad() {
        // TODO fix
        //privateSign = I18n.formatText(LangKeys.PRIVATE_SIGN);
        acceptSigns.add(defaultSign.toPlain());
        acceptSigns.add(privateSign.toPlain());

        // TODO Init ???
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
        return Text.of(trans("ownerFormat", owner));
    }

    public Text getUserText(String user) {
        return Text.of(trans("userFormat", user));
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

    public void removeLine(Player player, int line) {

    }

    public Result tryAccess(Player player, Location<World> block) {
        if (otherProtected(player, block)) return Result.OTHER_PROTECT;
        BlockType type = block.getBlockType();
        boolean isDBlock = isDBlock(type);
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

    public void lockSign(Player player, int line, String name) {
        Location<World> selected = selections.get(player.getUniqueId());
        if (selected != null) {
            TileEntity tile = selected.getTileEntity().orElse(null);
            if (tile instanceof Sign) {
                SignData data = ((Sign) tile).getSignData();
                data.setElement(0, getPrivateText());
                data.setElement(1, getOwnerText(plainHead));
                if ((line == 3 || line == 4) && name != null && !name.isEmpty()) {
                    data.setElement(line - 1, getUserText(name));
                }
                tile.offer(data);
            }
        }
        // TODO check ???
    }

    public void unLockSign(Location<World> location, int line) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(line - 1, Text.EMPTY);
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

    public List<Location<World>> getSideSigns(Location<World> loc) {
        byte count = 0;
        Location<World> link = null, side;
        BlockType type = loc.getBlockType();
        boolean duplex = isDBlock(type);
        ArrayList<Location<World>> signs = new ArrayList<>();

        if (type == BlockTypes.WALL_SIGN || type == BlockTypes.STANDING_SIGN) signs.add(loc);
        for (Direction face : FACES) {
            side = loc.getRelative(face);
            if (duplex && side.getBlockType() == type) {
                link = side;
                if (++count >= 2) {
                    consoleKey("MultiBlockNotify", loc.toString());
                    return null;
                }
            } else if (side.getBlockType() == BlockTypes.WALL_SIGN && side.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(side);
            }
        }
        if (duplex && link != null) {
            count = 0;
            for (Direction face : FACES) {
                side = link.getRelative(face);
                if (side.getBlockType() == type && ++count >= 2) {
                    consoleKey("MultiBlockNotify", loc.toString());
                    return null;
                }
                if (side.getBlockType() == BlockTypes.WALL_SIGN && side.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(side);
                }
            }
        }
        return signs;
    }

    public boolean isLocked(Location<World> loc) {
        List<Location<World>> signs = getSideSigns(loc);
        if (signs == null) return true;
        for (Location<World> sign : signs) {
            TileEntity tile = sign.getTileEntity().orElse(null);
            if (tile instanceof Sign && isPrivate((Sign) tile)) return true;
        }
        return false;
    }

    public boolean isPrivate(Sign sign) {
        return sign.lines().size() >= 1 && isPrivate(sign.lines().get(0).toPlain());
    }

    private Result analyzeSign(Player player, HashSet<Location<World>> signs) {
        if (signs.isEmpty()) return Result.SIGN_NOT_LOCK;
        LockData data = new LockData();
        for (Location<World> block : signs) {
            TileEntity tile = block.getTileEntity().orElse(null);
            if (tile instanceof Sign) {
                data.append(parseSign((Sign) tile));
            }
        }
        return data.getAccess(player.getName());
    }

    private LockData parseSign(Sign tile) {
        return null;
    }

    private void removeOneItem(Player player, HandType hand) {
        if (GameModes.CREATIVE.equals(player.gameMode().get())) return;
        ItemStack stack = player.getItemInHand(hand).orElse(null);
        if (stack != null && stack.getQuantity() > 1) {
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
