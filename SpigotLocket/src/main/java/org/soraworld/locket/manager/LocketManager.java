package org.soraworld.locket.manager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.soraworld.hocon.node.Setting;
import org.soraworld.locket.data.HandType;
import org.soraworld.locket.data.LockData;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.data.State;
import org.soraworld.locket.nms.InvUtil;
import org.soraworld.locket.nms.TileSign;
import org.soraworld.violet.inject.MainManager;
import org.soraworld.violet.manager.VManager;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.util.ChatColor;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.soraworld.violet.nms.Version.*;

/**
 * @author Himmelt
 */
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
    private String chatType = "action_bar";
    @Setting(comment = "comment.privateSign", trans = 0b1000)
    private String privateSign = ChatColor.DARK_RED.toString() + ChatColor.BOLD + "[Private]";
    @Setting(comment = "comment.ownerFormat", trans = 0b1000)
    private String ownerFormat = ChatColor.GREEN + "{$owner}";
    @Setting(comment = "comment.userFormat", trans = 0b1000)
    private String userFormat = "" + ChatColor.DARK_GRAY + ChatColor.ITALIC + "{$user}";
    @Setting(comment = "comment.acceptSigns", trans = 0b1000)
    private Set<String> acceptSigns = new HashSet<>();
    @Setting(comment = "comment.lockables")
    private Set<Material> lockables = new HashSet<>();
    @Setting(comment = "comment.doubleBlocks")
    private Set<Material> doubleBlocks = new HashSet<>();
    @Setting(comment = "comment.highDoors")
    private Set<Material> highDoors = new HashSet<>();

    private final HashMap<UUID, Location> selected = new HashMap<>();

    private static final Pattern HIDE_UUID = Pattern.compile("(\u00A7[0-9a-f]){32}");
    private static final BlockFace[] FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public LocketManager(SpigotPlugin plugin, Path path) {
        super(plugin, path);
    }

    @Override
    @NotNull
    public ChatColor defChatColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public void afterLoad() {
        if (privateSign == null) {
            privateSign = "[Private]";
        }
        acceptSigns.add(privateSign);
        HashSet<String> temp = new HashSet<>();
        acceptSigns.forEach(sign -> temp.add(ChatColor.stripAllColor(sign)));
        acceptSigns.clear();
        acceptSigns.addAll(temp);
        lockables.add(Material.CHEST);
        lockables.add(Material.TRAPPED_CHEST);
        doubleBlocks.add(Material.CHEST);
        doubleBlocks.add(Material.TRAPPED_CHEST);
        if (!v1_7_R4) {
            highDoors.add(Material.BIRCH_DOOR);
            highDoors.add(Material.ACACIA_DOOR);
            highDoors.add(Material.JUNGLE_DOOR);
            highDoors.add(Material.SPRUCE_DOOR);
            highDoors.add(Material.DARK_OAK_DOOR);
            if (v1_13_R1 || v1_13_R2) {
                highDoors.add(Material.OAK_DOOR);
            }
        } else {
            highDoors.add(Material.valueOf("WOODEN_DOOR"));
        }
        highDoors.add(Material.IRON_DOOR);
    }

    public boolean isLockable(@NotNull Block block) {
        Material type = block.getType();
        if (type == Material.WALL_SIGN || type == Material.SIGN) {
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
        type = block.getRelative(BlockFace.UP).getType();
        if (highDoors.contains(type)) {
            return true;
        }
        type = block.getRelative(BlockFace.DOWN).getType();
        if (highDoors.contains(type)) {
            return true;
        }
        BlockState tile = block.getState();
        return protectTile && tile != null || protectCarrier && tile instanceof InventoryHolder;
    }

    public boolean isPreventTransfer() {
        return preventTransfer;
    }

    public boolean isPreventExplosion() {
        return preventExplosion;
    }

    public void addType(@NotNull Material type) {
        lockables.add(type);
    }

    public void addDType(@NotNull Material type) {
        doubleBlocks.add(type);
    }

    public void removeType(Material type) {
        lockables.remove(type);
    }

    public void removeDType(@NotNull Material type) {
        doubleBlocks.remove(type);
    }

    public void sendHint(Player player, String key, Object... args) {
        if ("action_bar".equalsIgnoreCase(chatType)) {
            sendActionKey(player, key, args);
        } else {
            sendKey(player, key, args);
        }
    }

    public boolean isPrivate(@NotNull String line) {
        return acceptSigns.contains(ChatColor.stripAllColor(line).trim());
    }

    public String getPrivateText() {
        return privateSign;
    }

    private Optional<OfflinePlayer> getUser(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Bukkit.getOfflinePlayer(name));
        } catch (Throwable ignored) {
        }
        return Optional.empty();
    }

    private Optional<OfflinePlayer> getUser(UUID uuid) {
        return Optional.of(Bukkit.getOfflinePlayer(uuid));
    }

    public String getOwnerText(@NotNull OfflinePlayer owner) {
        return ownerFormat.replace("{$owner}", owner.getName() + hideUuid(owner.getUniqueId()));
    }

    public String getUserText(@NotNull OfflinePlayer user) {
        return userFormat.replace("{$user}", user.getName() + hideUuid(user.getUniqueId()));
    }

    public String getUserText(@NotNull String name) {
        return userFormat.replace("{$user}", name);
    }

    public boolean isDBlock(@NotNull Material type) {
        return doubleBlocks.contains(type);
    }

    @Nullable
    public Location getSelected(@NotNull Player player) {
        return selected.get(player.getUniqueId());
    }

    public void setSelected(@NotNull Player player, Location location) {
        selected.put(player.getUniqueId(), location);
    }

    public void clearSelected(@NotNull UUID uuid) {
        selected.remove(uuid);
    }

    public Result tryAccess(@NotNull Player player, @NotNull Block location, boolean needEdit) {

        if (needEdit && !canEditOther(player, location)) {
            return Result.OTHER_PROTECT;
        }

        Material type = location.getType();
        boolean isDBlock = doubleBlocks.contains(type);
        int count = 0;
        Block link = null;
        HashSet<Block> signs = new HashSet<>();

        // 自身也将参与检查
        if (type == Material.WALL_SIGN) {
            signs.add(location);
        }

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (BlockFace face : FACES) {
            Block relative = location.getRelative(face);
            if (isDBlock && relative.getType() == type) {
                link = relative;
                if (++count >= 2) {
                    return Result.MULTI_BLOCKS;
                }
            } else if (relative.getType() == Material.WALL_SIGN && getSignFace(relative).getOppositeFace() == face) {
                signs.add(relative);
            }
        }

        // 检查相邻双联方块
        if (isDBlock && link != null) {
            count = 0;
            for (BlockFace face : FACES) {
                Block relative = link.getRelative(face);
                if (relative.getType() == type && ++count >= 2) {
                    return Result.MULTI_BLOCKS;
                }
                if (relative.getType() == Material.WALL_SIGN && getSignFace(relative).getOppositeFace() == face) {
                    signs.add(relative);
                }
            }
        }

        // 检查相连的门
        for (Block door : getDoors(location)) {
            for (BlockFace face : FACES) {
                Block relative = door.getRelative(face);
                if (relative.getType() == Material.WALL_SIGN && getSignFace(relative).getOppositeFace() == face) {
                    signs.add(relative);
                }
            }
        }

        return analyzeSign(player, signs);
    }

    private List<Block> getDoors(@NotNull Block location) {
        Material type = location.getType();
        Block up = location.getRelative(BlockFace.UP);
        Block down = location.getRelative(BlockFace.DOWN);
        ArrayList<Block> list = new ArrayList<>();
        if (highDoors.contains(type)) {
            list.add(up);
            list.add(down);
            if (highDoors.contains(up.getType())) {
                list.add(up.getRelative(BlockFace.UP));
            }
            if (highDoors.contains(down.getType())) {
                list.add(down.getRelative(BlockFace.DOWN));
            }
        } else {
            if (highDoors.contains(up.getType())) {
                list.add(up);
                list.add(up.getRelative(BlockFace.UP));
                list.add(up.getRelative(BlockFace.UP).getRelative(BlockFace.UP));
            }
            if (highDoors.contains(down.getType())) {
                list.add(down);
                list.add(down.getRelative(BlockFace.DOWN));
                list.add(down.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN));
            }
        }
        return list;
    }

    public boolean otherProtected(Player player, Block block) {
        return false;
    }

    public void lockSign(Player player, Block block, int line, String name) {
        if (block.getState() instanceof Sign) {
            if (line == 1 && bypassPerm(player) && name != null && !name.equals(player.getName()) && !name.isEmpty()) {
                Player user = Bukkit.getPlayer(name);
                if (user != null) {
                    player = user;
                } else {
                    sendKey(player, "invalidUsername", name);
                    return;
                }
            }
            Player finalPlayer = player;
            TileSign.touchSign(block, data -> {
                data.line0 = getPrivateText();
                data.line1 = getOwnerText(finalPlayer);
                if (name != null && !name.isEmpty()) {
                    if (line == 2) {
                        data.line2 = getUserText(name);
                    } else if (line == 3) {
                        data.line3 = getUserText(name);
                    }
                }
                return true;
            });
            asyncUpdateSign(block);
            sendHint(player, "manuLock");
        } else {
            sendHint(player, "notSignTile");
        }
    }

    public void unLockSign(Location location, int line) {
        BlockState tile = location.getBlock().getState();
        if (tile instanceof Sign) {
            ((Sign) tile).setLine(line, "");
            tile.update();
        }
    }

    public void placeLock(Player player, Block loc, BlockFace face, HandType hand) {
        Block side = loc.getRelative(face);
        side.setType(Material.WALL_SIGN);
        BlockState tile = side.getState();
        if (tile instanceof Sign) {
            Sign sign = (Sign) tile;
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();
            signData.setFacingDirection(face);
            sign.setData(signData);
            sign.update();
            TileSign.touchSign(side, data -> {
                data.line0 = getPrivateText();
                data.line1 = getOwnerText(player);
                return true;
            });
            asyncUpdateSign(side);
        }
        removeOneItem(player, hand);
        if (v1_7_R4) {
            player.playSound(loc.getLocation(), "random.wood_click", 1.0F, 0F);
        } else {
            player.playSound(loc.getLocation(), "block.wood.break", 1.0F, 0F);
        }
        sendHint(player, "quickLock");
    }

    public boolean isLocked(@NotNull Block block) {
        return checkState(block) != State.NOT_LOCKED;
    }

    public boolean notLocked(@NotNull Block location) {
        return checkState(location) == State.NOT_LOCKED;
    }

    private boolean canEditOther(Player player, Block location) {
        return true;
    }

    public State checkState(@NotNull Block block) {
        Material type = block.getType();
        boolean isDBlock = doubleBlocks.contains(type);
        int count = 0;
        Block link = null;
        HashSet<Block> signs = new HashSet<>();

        // 自身也将参与检查
        if (type == Material.WALL_SIGN) {
            signs.add(block);
        }

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (BlockFace face : FACES) {
            Block relative = block.getRelative(face);
            if (isDBlock && relative.getType() == type) {
                link = relative;
                if (++count >= 2) {
                    return State.MULTI_BLOCKS;
                }
            } else if (relative.getType() == Material.WALL_SIGN && getSignFace(relative).getOppositeFace() == face) {
                signs.add(relative);
            }
        }

        // 检查相邻双联方块
        if (isDBlock && link != null) {
            count = 0;
            for (BlockFace face : FACES) {
                Block relative = link.getRelative(face);
                if (relative.getType() == type && ++count >= 2) {
                    return State.MULTI_BLOCKS;
                }
                if (relative.getType() == Material.WALL_SIGN && getSignFace(relative).getOppositeFace() == face) {
                    signs.add(relative);
                }
            }
        }

        // 检查相连的门
        for (Block door : getDoors(block)) {
            for (BlockFace face : FACES) {
                Block relative = door.getRelative(face);
                if (relative.getType() == Material.WALL_SIGN && getSignFace(relative).getOppositeFace() == face) {
                    signs.add(relative);
                }
            }
        }

        return new LockData(signs).getState();
    }

    private Result analyzeSign(@NotNull Player player, HashSet<Block> signs) {
        if (signs.isEmpty()) {
            return Result.NOT_LOCKED;
        }
        LockData data = new LockData(signs);
        return data.tryAccess(player.getUniqueId());
    }

    private static void removeOneItem(Player player, HandType hand) {
        if (GameMode.CREATIVE == player.getGameMode()) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        ItemStack stack = InvUtil.getItemInHand(inv, hand);
        if (stack != null && stack.getAmount() >= 1) {
            stack.setAmount(stack.getAmount() - 1);
            InvUtil.setItemInHand(inv, hand, stack);
        } else {
            InvUtil.setItemInHand(inv, hand, null);
        }
    }

    public static Block getAttached(@NotNull Block block) {
        return block.getRelative(((org.bukkit.material.Sign) block.getState().getData()).getAttachedFace());
    }

    private static BlockFace getDoorFace(Block block) {
        return ((org.bukkit.material.Door) block.getState().getData()).getFacing();
    }

    private static BlockFace getSignFace(Block block) {
        return ((org.bukkit.material.Sign) block.getState().getData()).getAttachedFace();
    }

    public boolean bypassPerm(CommandSender sender) {
        return hasPermission(sender, plugin.getId() + ".bypass");
    }

    public boolean canPlaceLock(@NotNull Material type) {
        String typeName = type.name();
        return type == Material.AIR || type == Material.GRASS || type == Material.SNOW
                || type == Material.WATER || type == Material.LAVA
                || "STATIONARY_WATER".equalsIgnoreCase(typeName) || "STATIONARY_LAVA".equalsIgnoreCase(typeName);
    }

    private static String hideUuid(UUID uuid) {
        String text = uuid.toString().replace("-", "");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            builder.append(ChatColor.TRUE_COLOR_CHAR).append(text.charAt(i));
        }
        return builder.toString();
    }

    private Optional<OfflinePlayer> parseUser(String text) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
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

    public void asyncUpdateSign(@NotNull final Block block) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> TileSign.touchSign(block, data -> {
            if (isPrivate(data.line0)) {
                data.line0 = getPrivateText();
                parseUser(data.line1).ifPresent(owner -> data.line1 = getOwnerText(owner));
                parseUser(data.line2).ifPresent(user -> data.line2 = getUserText(user));
                parseUser(data.line3).ifPresent(user -> data.line3 = getUserText(user));
                return true;
            }
            return false;
        }), 1);
    }
}
