package org.soraworld.locket.manager;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.api.claim.TrustTypes;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.claim.TrustType;
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
import org.soraworld.violet.inject.MainManager;
import org.soraworld.violet.manager.VManager;
import org.soraworld.violet.plugin.SpongePlugin;
import org.soraworld.violet.util.ChatColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.spongepowered.api.block.BlockTypes.*;

@MainManager
public class LocketManager extends VManager {

    @Setting(comment = "comment.protectTile")
    private boolean protectTile = false;
    @Setting(comment = "comment.protectCarrier")
    private boolean protectCarrier = true;
    @Setting(comment = "comment.preventTransfer")
    private boolean preventTransfer = true;
    @Setting(comment = "comment.preventExplosion")
    private boolean preventExplosion = true;
    // TODO implementation
    @Setting(comment = "comment.preventWorldEdit")
    private boolean preventWorldEdit = false;
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
    @Setting(comment = "comment.highDoors")
    private Set<BlockType> highDoors = new HashSet<>();

    private boolean usingGriefDefender = false;
    private boolean usingGriefPrevention = false;
    private UserStorageService storageService = null;
    private final HashMap<UUID, Location<World>> selected = new HashMap<>();

    private static final Pattern HIDE_UUID = Pattern.compile("(\u00A7[0-9a-f]){32}");
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

    @Override
    @NotNull
    public ChatColor defChatColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public void afterLoad() {
        Sponge.getServiceManager().provide(UserStorageService.class).ifPresent(s -> storageService = s);
        if (storageService == null) {
            consoleKey("storageServiceInvalid");
        }
        if (privateSign == null) {
            privateSign = Text.of("[Private]");
        }
        acceptSigns.add(privateSign.toPlain());
        HashSet<String> temp = new HashSet<>();
        acceptSigns.forEach(sign -> temp.add(ChatColor.stripAllColor(sign)));
        acceptSigns.clear();
        acceptSigns.addAll(temp);
        lockables.add(BlockTypes.CHEST);
        lockables.add(BlockTypes.TRAPPED_CHEST);
        doubleBlocks.add(BlockTypes.CHEST);
        doubleBlocks.add(BlockTypes.TRAPPED_CHEST);
        highDoors.add(BlockTypes.WOODEN_DOOR);
        highDoors.add(BlockTypes.BIRCH_DOOR);
        highDoors.add(BlockTypes.ACACIA_DOOR);
        highDoors.add(BlockTypes.JUNGLE_DOOR);
        highDoors.add(BlockTypes.SPRUCE_DOOR);
        highDoors.add(BlockTypes.DARK_OAK_DOOR);
        highDoors.add(BlockTypes.IRON_DOOR);
        usingGriefDefender = Sponge.getPluginManager().isLoaded("griefdefender");
        usingGriefPrevention = Sponge.getPluginManager().isLoaded("griefprevention");
    }

    public boolean isLockable(@NotNull Location<World> location) {
        BlockType type = location.getBlockType();
        if (type == BlockTypes.WALL_SIGN || type == BlockTypes.STANDING_SIGN) {
            return false;
        }
        if (lockables.contains(type)) {
            return true;
        }
        if (doubleBlocks.contains(type)) {
            return true;
        }
        if (highDoors.contains(type)) {
            return true;
        }
        type = location.getRelative(Direction.UP).getBlockType();
        if (highDoors.contains(type)) {
            return true;
        }
        type = location.getRelative(Direction.DOWN).getBlockType();
        if (highDoors.contains(type)) {
            return true;
        }
        TileEntity tile = location.getTileEntity().orElse(null);
        return protectTile && tile != null || protectCarrier && tile instanceof TileEntityCarrier;
    }

    public boolean isPreventTransfer() {
        return preventTransfer;
    }

    public boolean isPreventExplosion() {
        return preventExplosion;
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

    private Optional<User> getUser(String name) {
        if (storageService != null) {
            try {
                return storageService.get(name);
            } catch (Throwable ignored) {
            }
        }
        return Optional.empty();
    }

    private Optional<User> getUser(UUID uuid) {
        if (storageService != null) {
            return storageService.get(uuid);
        }
        return Optional.empty();
    }

    public Text getOwnerText(@NotNull User owner) {
        return Text.of(ownerFormat.replace("{$owner}", owner.getName()) + hideUUID(owner.getUniqueId()));
    }

    public Text getUserText(@NotNull User user) {
        return Text.of(userFormat.replace("{$user}", user.getName()) + hideUUID(user.getUniqueId()));
    }

    public Text getUserText(@NotNull String name) {
        return Text.of(userFormat.replace("{$user}", name));
    }

    public boolean isDBlock(@NotNull BlockType type) {
        return doubleBlocks.contains(type);
    }

    @Nullable
    public Location<World> getSelected(@NotNull Player player) {
        return selected.get(player.getUniqueId());
    }

    public void setSelected(@NotNull Player player, Location<World> location) {
        selected.put(player.getUniqueId(), location);
    }

    public void clearSelected(@NotNull UUID uuid) {
        selected.remove(uuid);
    }

    public Result tryAccess(@NotNull Player player, @NotNull Location<World> location, boolean needEdit) {

        if (needEdit && !canEditOther(player, location)) {
            return Result.OTHER_PROTECT;
        }

        BlockType type = location.getBlockType();
        boolean isDBlock = doubleBlocks.contains(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 自身也将参与检查
        if (type == BlockTypes.WALL_SIGN) {
            signs.add(location);
        }

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : FACES) {
            Location<World> relative = location.getRelative(face);
            if (isDBlock && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) {
                    return Result.MULTI_BLOCKS;
                }
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(relative);
            }
        }

        // 检查相邻双联方块
        if (isDBlock && link != null) {
            count = 0;
            for (Direction face : FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) {
                    return Result.MULTI_BLOCKS;
                }
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }

        // 检查相连的门
        for (Location<World> door : getDoors(location)) {
            for (Direction face : FACES) {
                Location<World> relative = door.getRelative(face);
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }

        return analyzeSign(player, signs);
    }

    private List<Location<World>> getDoors(@NotNull Location<World> location) {
        BlockType type = location.getBlockType();
        Location<World> up = location.getRelative(Direction.UP);
        Location<World> down = location.getRelative(Direction.DOWN);
        ArrayList<Location<World>> list = new ArrayList<>();
        if (highDoors.contains(type)) {
            list.add(up);
            list.add(down);
            if (highDoors.contains(up.getBlockType())) {
                list.add(up.getRelative(Direction.UP));
            }
            if (highDoors.contains(down.getBlockType())) {
                list.add(down.getRelative(Direction.DOWN));
            }
        } else {
            if (highDoors.contains(up.getBlockType())) {
                list.add(up);
                list.add(up.getRelative(Direction.UP));
                list.add(up.getRelative(Direction.UP).getRelative(Direction.UP));
            }
            if (highDoors.contains(down.getBlockType())) {
                list.add(down);
                list.add(down.getRelative(Direction.DOWN));
                list.add(down.getRelative(Direction.DOWN).getRelative(Direction.DOWN));
            }
        }
        return list;
    }

    public boolean otherProtected(Player player, Location<World> block) {
        return false;
    }

    public void lockSign(Player player, Location<World> selected, int line, String name) {
        TileEntity tile = selected.getTileEntity().orElse(null);
        if (tile instanceof Sign) {
            if (line == 1 && bypassPerm(player) && name != null && !name.equals(player.getName()) && !name.isEmpty()) {
                Player user = Sponge.getServer().getPlayer(name).orElse(null);
                if (user != null) {
                    player = user;
                } else {
                    sendKey(player, "invalidUsername", name);
                    return;
                }
            }
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, getPrivateText());
            data.setElement(1, getOwnerText(player));
            if ((line == 2 || line == 3) && name != null && !name.isEmpty()) {
                data.setElement(line, getUserText(name));
            }
            tile.offer(data);
            asyncUpdateSign((Sign) tile, 50);
            sendHint(player, "manuLock");
        } else {
            sendHint(player, "notSignTile");
        }
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
            data.setElement(1, getOwnerText(player));
            tile.offer(data);
            asyncUpdateSign((Sign) tile, 50);
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

    private boolean canEditOther(Player player, Location<World> location) {
        if (usingGriefDefender) {
            World world = location.getExtent();
            try {
                ClaimManager manager = GriefDefender.getCore().getClaimManager(world.getUniqueId());
                Claim claim = manager.getClaimAt(location.getBlockPosition());
                if (claim == null || claim == manager.getWildernessClaim()) {
                    return true;
                }
                return claim.isUserTrusted(player.getUniqueId(), TrustTypes.BUILDER);
            } catch (Throwable e) {
                debug(e);
            }
        }
        if (usingGriefPrevention) {
            World world = location.getExtent();
            try {
                me.ryanhamshire.griefprevention.api.claim.ClaimManager manager = GriefPrevention.getApi().getClaimManager(world);
                me.ryanhamshire.griefprevention.api.claim.Claim claim = manager.getClaimAt(location);
                if (claim == null || claim == manager.getWildernessClaim()) {
                    return true;
                }
                return claim.isUserTrusted(player.getUniqueId(), TrustType.BUILDER);
            } catch (Throwable e) {
                debug(e);
            }
        }
        return true;
    }

    public State checkState(@NotNull Location<World> location) {
        BlockType type = location.getBlockType();
        boolean isDBlock = doubleBlocks.contains(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 自身也将参与检查
        if (type == BlockTypes.WALL_SIGN) {
            signs.add(location);
        }

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : FACES) {
            Location<World> relative = location.getRelative(face);
            if (isDBlock && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) {
                    return State.MULTI_BLOCKS;
                }
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(relative);
            }
        }

        // 检查相邻双联方块
        if (isDBlock && link != null) {
            count = 0;
            for (Direction face : FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) {
                    return State.MULTI_BLOCKS;
                }
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }

        // 检查相连的门
        for (Location<World> door : getDoors(location)) {
            for (Direction face : FACES) {
                Location<World> relative = door.getRelative(face);
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }

        return new LockData(signs).getState();
    }

    private Result analyzeSign(@NotNull Player player, HashSet<Location<World>> signs) {
        if (signs.isEmpty()) {
            return Result.NOT_LOCKED;
        }
        LockData data = new LockData(signs);
        return data.tryAccess(player.getUniqueId());
    }

    private static void removeOneItem(Player player, HandType hand) {
        if (GameModes.CREATIVE.equals(player.gameMode().get())) {
            return;
        }
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

    public boolean bypassPerm(CommandSource sender) {
        return hasPermission(sender, plugin.getId() + ".bypass");
    }

    public boolean canPlaceLock(@NotNull BlockType type) {
        return type == AIR || type == GRASS || type == SNOW_LAYER || type == FLOWING_WATER || type == WATER;
    }

    public static String hideUUID(UUID uuid) {
        String text = uuid.toString().replace("-", "");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            builder.append(ChatColor.TRUE_COLOR_CHAR).append(text.charAt(i));
        }
        return builder.toString();
    }

    public Optional<UUID> parseUuid(String text) {
        Matcher matcher = HIDE_UUID.matcher(text);
        if (matcher.find()) {
            String hex = matcher.group().replace(ChatColor.TRUE_COLOR_STRING, "");
            if (hex.length() == 32) {
                long most = Long.parseUnsignedLong(hex.substring(0, 16), 16);
                long least = Long.parseUnsignedLong(hex.substring(16), 16);
                return Optional.of(new UUID(most, least));
            }
        }
        return Optional.empty();
    }

    private Optional<User> parseUser(String text) {
        Matcher matcher = HIDE_UUID.matcher(text);
        if (matcher.find()) {
            String hex = matcher.group().replace(ChatColor.TRUE_COLOR_STRING, "");
            if (hex.length() == 32) {
                long most = Long.parseUnsignedLong(hex.substring(0, 16), 16);
                long least = Long.parseUnsignedLong(hex.substring(16), 16);
                return getUser(new UUID(most, least));
            }
        } else {
            return getUser(ChatColor.stripColor(text));
        }
        return Optional.empty();
    }

    public void asyncUpdateSign(@NotNull final Sign sign, long delay) {
        Sponge.getScheduler().createAsyncExecutor(plugin).schedule(() -> {
            final SignData data = sign.getSignData();
            ListValue<Text> lines = data.lines();
            String line0 = ChatColor.stripColor(lines.get(0).toPlain()).trim();
            if (isPrivate(line0)) {
                String line1 = lines.get(1).toPlain().trim();
                String line2 = lines.get(2).toPlain().trim();
                String line3 = lines.get(3).toPlain().trim();

                parseUser(line1).ifPresent(owner -> data.setElement(1, getOwnerText(owner)));
                parseUser(line2).ifPresent(user -> data.setElement(2, getUserText(user)));
                parseUser(line3).ifPresent(user -> data.setElement(3, getUserText(user)));

                Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> sign.offer(data));
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
