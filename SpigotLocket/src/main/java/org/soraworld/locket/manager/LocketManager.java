package org.soraworld.locket.manager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.soraworld.locket.LocketPlugin;
import org.soraworld.locket.data.LockData;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.data.State;
import org.soraworld.locket.util.Helper;
import org.soraworld.violet.api.ICommandSender;
import org.soraworld.violet.api.IPlayer;
import org.soraworld.violet.api.IUser;
import org.soraworld.violet.command.Args;
import org.soraworld.violet.inject.Config;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.inventory.HandType;
import org.soraworld.violet.text.ChatColor;
import org.soraworld.violet.world.BlockPos;
import org.soraworld.violet.wrapper.Wrapper;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.soraworld.violet.Violet.MC_VERSION;
import static org.soraworld.violet.version.McVersion.v1_7_10;

/**
 * @author Himmelt
 */
@Config(id = Locket.PLUGIN_ID)
public class LocketManager extends IManager {
    @Setting
    private Set<Material> lockables = new HashSet<>();
    @Setting
    private Set<Material> doubleBlocks = new HashSet<>();
    @Setting
    private Set<Material> highDoors = new HashSet<>();

    private final HashSet<Material> itemSignTypes = new HashSet<>();
    private final HashSet<Material> wallSignTypes = new HashSet<>();
    private final HashSet<Material> postSignTypes = new HashSet<>();
    private final HashMap<Material, Material> signTypeMap = new HashMap<>();

    private static final BlockFace[] FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    @Inject
    private static LocketPlugin plugin;

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
            plugin.debug(e);
        }
        if (MC_VERSION.higher(v1_7_10)) {
            highDoors.add(Material.BIRCH_DOOR);
            highDoors.add(Material.ACACIA_DOOR);
            highDoors.add(Material.JUNGLE_DOOR);
            highDoors.add(Material.SPRUCE_DOOR);
            highDoors.add(Material.DARK_OAK_DOOR);
            try {
                highDoors.add(Material.valueOf("OAK_DOOR"));
            } catch (Throwable e) {
                plugin.debug(e);
            }
        }
        highDoors.add(Material.IRON_DOOR);

        // Sign Type Map
        try {
            itemSignTypes.add(Material.valueOf("SIGN"));
            wallSignTypes.add(Material.valueOf("WALL_SIGN"));
            signTypeMap.put(Material.valueOf("SIGN"), Material.valueOf("WALL_SIGN"));
        } catch (Throwable e) {
            plugin.debug(e);
        }
        try {
            postSignTypes.add(Material.valueOf("SIGN_POST"));
        } catch (Throwable e) {
            plugin.debug(e);
        }
        try {
            itemSignTypes.add(Material.OAK_SIGN);
            postSignTypes.add(Material.OAK_SIGN);
            wallSignTypes.add(Material.OAK_WALL_SIGN);
            signTypeMap.put(Material.OAK_SIGN, Material.OAK_WALL_SIGN);
            itemSignTypes.add(Material.ACACIA_SIGN);
            postSignTypes.add(Material.ACACIA_SIGN);
            wallSignTypes.add(Material.ACACIA_WALL_SIGN);
            signTypeMap.put(Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN);
            itemSignTypes.add(Material.BIRCH_SIGN);
            postSignTypes.add(Material.BIRCH_SIGN);
            wallSignTypes.add(Material.BIRCH_WALL_SIGN);
            signTypeMap.put(Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN);
            itemSignTypes.add(Material.DARK_OAK_SIGN);
            postSignTypes.add(Material.DARK_OAK_SIGN);
            wallSignTypes.add(Material.DARK_OAK_WALL_SIGN);
            signTypeMap.put(Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN);
            itemSignTypes.add(Material.JUNGLE_SIGN);
            postSignTypes.add(Material.JUNGLE_SIGN);
            wallSignTypes.add(Material.JUNGLE_WALL_SIGN);
            signTypeMap.put(Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN);
            itemSignTypes.add(Material.SPRUCE_SIGN);
            postSignTypes.add(Material.SPRUCE_SIGN);
            wallSignTypes.add(Material.SPRUCE_WALL_SIGN);
            signTypeMap.put(Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN);
        } catch (Throwable e) {
            plugin.debug(e);
        }
    }

    public final boolean bypassPerm(@NotNull Player player) {
        return player.hasPermission(plugin.id() + ".bypass");
    }

    public void sendHint(@NotNull Player player, @NotNull String key, Object... args) {
        plugin.sendMessageKey(player, chatType, key, args);
    }

    @Override
    public List<String> getMatchedPlayers(@NotNull String prefix) {
        prefix = prefix.toLowerCase();
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(prefix)) {
                names.add(player.getName());
            }
        }
        return names;
    }

    @Override
    public void touchSign(Object block, @Nullable Predicate<String[]> sync, @Nullable Predicate<String[]> async) {
        if (block instanceof Block) {
            Helper.touchSign((Block) block, sync, async);
        }
    }

    @Override
    public void processType(@NotNull ICommandSender sender, @NotNull Args args, @NotNull String key) {
        switch (key) {
            case "typeAdd":
                processType(sender.getHandle(CommandSender.class), args, this::addType, key);
                break;
            case "typeRemove":
                processType(sender.getHandle(CommandSender.class), args, this::removeType, key);
                break;
            case "dTypeAdd":
                processType(sender.getHandle(CommandSender.class), args, this::addDType, key);
                break;
            case "dTypeRemove":
                processType(sender.getHandle(CommandSender.class), args, this::removeDType, key);
                break;
            default:
        }
    }

    @Override
    public void beforeSave() {
    }

    public boolean isLockable(@NotNull Block block) {
        Material type = block.getType();
        if (type == Material.AIR || isSign(type)) {
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

    public boolean isDBlock(@NotNull Material type) {
        return doubleBlocks.contains(type);
    }

    public Result tryAccess(@NotNull Player player, @NotNull Block block, boolean needEdit) {

        if (needEdit && !canEditOther(player, block)) {
            return Result.OTHER_PROTECT;
        }

        Material type = block.getType();
        boolean isDBlock = doubleBlocks.contains(type);
        int count = 0;
        Block link = null;
        HashSet<BlockPos> signs = new HashSet<>();

        // 自身也将参与检查
        if (isWallSign(type)) {
            signs.add(Wrapper.wrapper(block));
        }

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (BlockFace face : FACES) {
            Block relative = block.getRelative(face);
            if (isDBlock && relative.getType() == type) {
                link = relative;
                if (++count >= 2) {
                    return Result.MULTI_BLOCKS;
                }
            } else if (isWallSign(relative.getType()) && Helper.getAttachedFace((Sign) relative.getState()) == face) {
                signs.add(Wrapper.wrapper(relative));
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
                if (isWallSign(relative.getType()) && Helper.getAttachedFace((Sign) relative.getState()) == face) {
                    signs.add(Wrapper.wrapper(relative));
                }
            }
        }

        // 检查相连的门
        for (Block door : getDoors(block)) {
            for (BlockFace face : FACES) {
                Block relative = door.getRelative(face);
                if (isWallSign(relative.getType()) && Helper.getAttachedFace((Sign) relative.getState()) == face) {
                    signs.add(Wrapper.wrapper(relative));
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

    public void lockSign(@NotNull IPlayer player, Block block, int line, String name) {
        if (block.getState() instanceof Sign) {
            IPlayer owner = player;
            if (line == 1 && bypassPerm(player) && name != null && !name.equals(player.getName()) && !name.isEmpty()) {
                IPlayer user = Wrapper.wrapper(name);
                if (user != null) {
                    owner = user;
                } else {
                    plugin.sendMessageKey(player, "invalidUsername", name);
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
                    LocketPlugin.parseUser(data.lines[line]).ifPresent(user -> data.lines[line] = getUserText(user));
                    return true;
                }
                return false;
            });
            sendHint(player, "manuLock");
        } else {
            sendHint(player, "notSignTile");
        }
    }

    public void unLockSign(Block block, int line) {
        Helper.touchSign(block, data -> {
            if (line >= 0 && line <= 3) {
                data.lines[line] = "";
                return true;
            } else {
                return false;
            }
        }, null);
    }

    public String getOwnerText(@NotNull OfflinePlayer owner) {
        return ownerFormat.replace("{$owner}", owner.getName() + Locket.hideUuid(owner.getUniqueId()));
    }

    public void placeLock(@NotNull Player player, Block loc, BlockFace face, HandType hand, Material itemType) {
        Block side = loc.getRelative(face);
        side.setType(getSignBlockType(itemType));
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
        if (MC_VERSION.matchCraft(1, 7, 4)) {
            player.playSound(loc.getLocation(), "random.wood_click", 1.0F, 0F);
        } else {
            player.playSound(loc.getLocation(), "block.wood.break", 1.0F, 0F);
        }
        sendHint(Wrapper.wrapper(player), "quickLock");
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
        HashSet<BlockPos> signs = new HashSet<>();

        // 自身也将参与检查
        if (isWallSign(type)) {
            signs.add(Wrapper.wrapper(block));
        }

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (BlockFace face : FACES) {
            Block relative = block.getRelative(face);
            if (isDBlock && relative.getType() == type) {
                link = relative;
                if (++count >= 2) {
                    return State.MULTI_BLOCKS;
                }
            } else if (isWallSign(relative.getType()) && Helper.getAttachedFace((Sign) relative.getState()) == face) {
                signs.add(Wrapper.wrapper(relative));
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
                if (isWallSign(relative.getType()) && Helper.getAttachedFace((Sign) relative.getState()) == face) {
                    signs.add(Wrapper.wrapper(relative));
                }
            }
        }

        // 检查相连的门
        for (Block door : getDoors(block)) {
            for (BlockFace face : FACES) {
                Block relative = door.getRelative(face);
                if (isWallSign(relative.getType()) && Helper.getAttachedFace((Sign) relative.getState()) == face) {
                    signs.add(Wrapper.wrapper(relative));
                }
            }
        }

        return new LockData(signs).getState();
    }

    private Result analyzeSign(@NotNull Player player, HashSet<BlockPos> signs) {
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
                LocketPlugin.parseUser(data.lines[1]).ifPresent(owner -> data.lines[1] = getOwnerText(owner));
                LocketPlugin.parseUser(data.lines[2]).ifPresent(user -> data.lines[2] = getUserText(user));
                LocketPlugin.parseUser(data.lines[3]).ifPresent(user -> data.lines[3] = getUserText(user));
                return true;
            }
            return false;
        });
    }

    public Material getSignBlockType(Material itemType) {
        return signTypeMap.get(itemType);
    }

    public boolean isSign(Material type) {
        return itemSignTypes.contains(type) || postSignTypes.contains(type) || wallSignTypes.contains(type);
    }

    public boolean isWallSign(Material type) {
        return wallSignTypes.contains(type);
    }

    private void processType(@NotNull CommandSender sender, @NotNull Args args, @NotNull Consumer<Material> consumer, @NotNull String key) {
        Material type;
        if (args.notEmpty()) {
            if ("look".equals(args.first()) && sender instanceof Player) {
                Player player = (Player) sender;
                Block block = org.soraworld.violet.util.Helper.getLookAt(player, 25);
                if (block != null) {
                    type = block.getType();
                } else {
                    plugin.sendMessageKey(player, "notLookBlock");
                    return;
                }
            } else {
                type = Material.getMaterial(args.first());
            }
        } else if (sender instanceof Player) {
            ItemStack stack = Helper.getItemInHand(((Player) sender).getInventory(), HandType.MAIN_HAND);
            type = stack == null ? null : stack.getType();
        } else {
            plugin.sendMessageKey(sender, "emptyArgs");
            return;
        }
        if (type == null) {
            plugin.sendMessageKey(sender, "nullBlockType");
            return;
        }
        if (type == Material.AIR || isSign(type)) {
            plugin.sendMessageKey(sender, "illegalType");
            return;
        }
        consumer.accept(type);
        plugin.sendMessageKey(sender, key, type.name());
        // TODO manager.asyncSave(null);
    }

    public static Optional<IUser> parseUser(String text) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        return Locket.parseUuid(text).map(value -> Optional.of(Wrapper.wrapper(Bukkit.getOfflinePlayer(value)))).orElseGet(() -> Optional.of(Wrapper.wrapper(Bukkit.getOfflinePlayer(ChatColor.stripAllColor(text).trim()))));
    }

}
