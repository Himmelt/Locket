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
import org.soraworld.locket.Locket;
import org.soraworld.locket.data.LockData;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.data.State;
import org.soraworld.locket.nms.HandType;
import org.soraworld.locket.nms.Helper;
import org.soraworld.locket.util.Util;
import org.soraworld.violet.inject.MainManager;
import org.soraworld.violet.manager.VManager;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.util.ChatColor;

import java.nio.file.Path;
import java.util.*;

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
    private final HashMap<Material, Material> signTypeMap = new HashMap<>();

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

        // Doors
        try {
            highDoors.add(Material.valueOf("WOODEN_DOOR"));
        } catch (Throwable e) {
            debug(e);
        }
        if (!v1_7_R4) {
            highDoors.add(Material.BIRCH_DOOR);
            highDoors.add(Material.ACACIA_DOOR);
            highDoors.add(Material.JUNGLE_DOOR);
            highDoors.add(Material.SPRUCE_DOOR);
            highDoors.add(Material.DARK_OAK_DOOR);
            try {
                highDoors.add(Material.valueOf("OAK_DOOR"));
            } catch (Throwable e) {
                debug(e);
            }
        }
        highDoors.add(Material.IRON_DOOR);

        // Sign Type Map
        try {
            signTypeMap.put(Material.valueOf("SIGN"), Material.valueOf("WALL_SIGN"));
        } catch (Throwable e) {
            debug(e);
        }
        try {
            signTypeMap.put(Material.valueOf("SIGN_POST"), Material.valueOf("WALL_SIGN"));
        } catch (Throwable e) {
            debug(e);
        }
        try {
            signTypeMap.put(Material.OAK_SIGN, Material.OAK_WALL_SIGN);
            signTypeMap.put(Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN);
            signTypeMap.put(Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN);
            signTypeMap.put(Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN);
            signTypeMap.put(Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN);
            signTypeMap.put(Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN);
        } catch (Throwable e) {
            debug(e);
        }
    }

    public boolean isLockable(@NotNull Block block) {
        Material type = block.getType();
        if (isSign(type)) {
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
        if (!v1_7_R4 && !v1_8_R1 && !v1_8_R3 && "action_bar".equalsIgnoreCase(chatType)) {
            sendActionKey(player, key, args);
        } else {
            sendKey(player, key, args);
        }
    }

    public boolean isPrivate(@NotNull String line) {
        return acceptSigns.contains(ChatColor.stripAllColor(line).replace(ChatColor.TRUE_COLOR_STRING, "").trim());
    }

    public String getPrivateText() {
        return privateSign;
    }

    public String getOwnerText(@NotNull OfflinePlayer owner) {
        return ownerFormat.replace("{$owner}", owner.getName() + Util.hideUuid(owner.getUniqueId()));
    }

    public String getUserText(@NotNull OfflinePlayer user) {
        return userFormat.replace("{$user}", user.getName() + Util.hideUuid(user.getUniqueId()));
    }

    public String getUserText(String name) {
        return name == null || name.isEmpty() ? "" : userFormat.replace("{$user}", name);
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
        if (isWallSign(type)) {
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
            } else if (isWallSign(relative.getType()) && getSignFace(relative).getOppositeFace() == face) {
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
                if (isWallSign(relative.getType()) && getSignFace(relative).getOppositeFace() == face) {
                    signs.add(relative);
                }
            }
        }

        // 检查相连的门
        for (Block door : getDoors(location)) {
            for (BlockFace face : FACES) {
                Block relative = door.getRelative(face);
                if (isWallSign(relative.getType()) && getSignFace(relative).getOppositeFace() == face) {
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
            Player owner = player;
            if (line == 1 && bypassPerm(player) && name != null && !name.equals(player.getName()) && !name.isEmpty()) {
                Player user = Bukkit.getPlayer(name);
                if (user != null) {
                    owner = user;
                } else {
                    sendKey(player, "invalidUsername", name);
                    return;
                }
            }

            String ownerText = getOwnerText(owner);
            Helper.touchSign(block, data -> {
                data.lines[0] = getPrivateText();
                data.lines[1] = ownerText;
                if ((line == 2 || line == 3) && name != null && !name.isEmpty()) {
                    data.lines[line] = getUserText(name);
                }
                return true;
            }, data -> {
                if ((line == 2 || line == 3)) {
                    Locket.parseUser(data.lines[line]).ifPresent(user -> data.lines[line] = getUserText(user));
                    return true;
                }
                return false;
            });
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

    public void placeLock(Player player, Block loc, BlockFace face, HandType hand, Material itemType) {
        Block side = loc.getRelative(face);
        side.setType(signTypeMap.getOrDefault(itemType, signTypeMap.values().iterator().next()));
        BlockState tile = side.getState();
        if (tile instanceof Sign) {
            Helper.setSignRotation((Sign) tile, face);
            Helper.touchSign(side, data -> {
                data.lines[0] = getPrivateText();
                data.lines[1] = getOwnerText(player);
                return true;
            }, null);
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
        if (isWallSign(type)) {
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
            } else if (isWallSign(relative.getType()) && getSignFace(relative).getOppositeFace() == face) {
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
                if (isWallSign(relative.getType()) && getSignFace(relative).getOppositeFace() == face) {
                    signs.add(relative);
                }
            }
        }

        // 检查相连的门
        for (Block door : getDoors(block)) {
            for (BlockFace face : FACES) {
                Block relative = door.getRelative(face);
                if (isWallSign(relative.getType()) && getSignFace(relative).getOppositeFace() == face) {
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
        ItemStack stack = Helper.getItemInHand(inv, hand);
        if (stack != null && stack.getAmount() >= 1) {
            stack.setAmount(stack.getAmount() - 1);
            Helper.setItemInHand(inv, hand, stack);
        } else {
            Helper.setItemInHand(inv, hand, null);
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

    public void asyncUpdateSign(@NotNull final Block block) {
        Helper.touchSign(block, null, data -> {
            if (isPrivate(data.lines[0])) {
                data.lines[0] = getPrivateText();
                Locket.parseUser(data.lines[1]).ifPresent(owner -> data.lines[1] = getOwnerText(owner));
                Locket.parseUser(data.lines[2]).ifPresent(user -> data.lines[2] = getUserText(user));
                Locket.parseUser(data.lines[3]).ifPresent(user -> data.lines[3] = getUserText(user));
                return true;
            }
            return false;
        });
    }

    public boolean isSign(Material type) {
        return signTypeMap.containsKey(type) || signTypeMap.containsValue(type);
    }

    public boolean isWallSign(Material type) {
        return signTypeMap.containsValue(type);
    }
}
