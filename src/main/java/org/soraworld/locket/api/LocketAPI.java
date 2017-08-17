package org.soraworld.locket.api;

import org.soraworld.locket.Locket;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.constant.Permissions;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LocketAPI {

    private static final Locket locket = Locket.getLocket();
    private static final PluginContainer plugin = locket.getPlugin();

    private static final HashMap<Player, IPlayer> PLAYERS = new HashMap<>();

    public static IPlayer getPlayer(Player player) {
        if (PLAYERS.containsKey(player)) {
            return PLAYERS.get(player);
        } else {
            IPlayer iPlayer = new IPlayer(player);
            PLAYERS.put(player, iPlayer);
            return iPlayer;
        }
    }

    public static boolean canOpenDChest(Player player, Location<World> location) {
        // 1.是不是箱子
        BlockType type = location.getBlockType();
        if (Utils.isDChest(type)) {
            // 2.周围是否多于2个箱子
            List<Direction> chests = new ArrayList<>();
            for (Direction face : Constants.FACES) {
                if (type == location.getRelative(face).getBlockType()) chests.add(face);
            }
            if (chests.size() >= 2) {
                if (player.hasPermission(Permissions.ADMIN_OVERFLOW)) {
                    Utils.sendActionBar(player, "注意:你使用管理权限打开了一个多重箱子,这是一个BUG!");
                    return true;
                } else {
                    Utils.sendActionBar(player, "注意:这是一个多重箱子,是一个BUG,已禁止打开!");
                    return false;
                }
            }
            Direction link = chests.size() == 1 ? chests.get(0) : null;
            // 4.联合检查
            return canDTouch(player, location, link);
        }
        return false;
    }

    private static boolean canDTouch(Player player, Location<World> location, Direction direction) {
        if (player.hasPermission(Permissions.ADMIN_BYPASS)) return true;
        HashSet<String> owners = new HashSet<>();
        HashSet<String> users = new HashSet<>();
        for (Direction face : Constants.FACES) {
            touchSign(location.getRelative(face), owners, users);
        }
        if (direction != null) {
            Location<World> link = location.getRelative(direction);
            for (Direction face : Constants.FACES) {
                touchSign(link.getRelative(face), owners, users);
            }
        }
        return (owners.size() == 1 && owners.contains(player.getName())) || users.contains(player.getName());
    }

    private static boolean canTouch(Player player, Location<World> location) {
        if (player.hasPermission(Permissions.ADMIN_BYPASS)) return true;
        HashSet<String> owners = new HashSet<>();
        HashSet<String> users = new HashSet<>();
        for (Direction face : Constants.FACES) {
            Location<World> relative = location.getRelative(face);
            if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face.getOpposite()) {
                player.sendMessage(ChatTypes.ACTION_BAR, Text.of("方位:" + face.name() + "朝向:" + relative.get(Keys.DIRECTION).orElse(null)));
                touchSign(relative, owners, users);
            }
        }
        return (owners.size() == 1 && owners.contains(player.getName())) || users.contains(player.getName());
    }

    private static void touchSign(Location<World> location, HashSet<String> owners, HashSet<String> users) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            Sign sign = ((Sign) tile);
            String line = sign.lines().get(0).toPlain();
            String owner = sign.lines().get(1).toPlain();
            String user1 = sign.lines().get(2).toPlain();
            String user2 = sign.lines().get(3).toPlain();
            if (isPrivate(line)) {
                owners.add(owner);
                users.add(user1);
                users.add(user2);
            }
        }
    }

    private static boolean isOnSign(Player player, Location<World> location) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            Sign sign = ((Sign) tile);
            String line = sign.lines().get(0).toPlain();
            String owner = sign.lines().get(1).toPlain();
            String user1 = sign.lines().get(2).toPlain();
            String user2 = sign.lines().get(3).toPlain();
            return isPrivate(line) && (player.getName().equals(owner) || player.getName().equals(user1) || player.getName().equals(user2));
        }
        return false;
    }

    public static boolean isSigned(Location<World> location) {
        for (Direction face : Constants.FACES) {
            if (isPrivateSign(location.getRelative(face))) return true;
        }
        return false;
    }

    public static boolean isLocked(Location<World> location) {
        BlockType type = location.getBlockType();
        if (Utils.isDChest(type)) {
            for (Direction face : Constants.FACES) {
                Location<World> relative = location.getRelative(face);
                if (relative.getBlockType() == location.getBlockType()) {
                    //canDTouch(player,location,face)
                    if (isLockedSingleBlock(relative, face.getOpposite())) return true;
                }
            }
        }
        if (type == BlockTypes.CHEST || type == BlockTypes.TRAPPED_CHEST) {
            // Check second chest sign
            for (Direction chestFace : Constants.FACES) {
                Location<World> relativeChest = location.getRelative(chestFace);
                if (relativeChest.getBlockType() == location.getBlockType()) {
                    if (isLockedSingleBlock(relativeChest, chestFace)) return true;
                }
            }
            // Don't break here
            // Everything else (First block of container check goes here)
        } else {
            if (isLockedSingleBlock(location, null)) return true;
        }
        return false;
    }

    public static boolean isOwner(Location<World> location, Player player) {
        /*if (DoorType.isDoor(location)) {
            Location<World>[] doors = getDoor(location);
            if (doors == null) return false;
            for (BlockFace doorFace : newsFaces) {
                Location<World> relative0 = doors[0].getRelative(doorFace.get()), relative1 = doors[1].getRelative(doorFace.get());
                if (relative0.getBlockType() == doors[0].getBlockType() && relative1.getBlockType() == doors[1].getBlockType()) {
                    if (isOwnerSingleBlock(relative1.getRelative(Direction.UP), doorFace.getOppositeFace(), player))
                        return true;
                    if (isOwnerSingleBlock(relative1, doorFace.getOppositeFace(), player)) return true;
                    if (isOwnerSingleBlock(relative0, doorFace.getOppositeFace(), player)) return true;
                    if (isOwnerSingleBlock(relative0.getRelative(Direction.DOWN), doorFace.getOppositeFace(), player))
                        return true;
                }
            }
            if (isOwnerSingleBlock(doors[1].getRelative(Direction.UP), null, player)) return true;
            if (isOwnerSingleBlock(doors[1], null, player)) return true;
            if (isOwnerSingleBlock(doors[0], null, player)) return true;
            if (isOwnerSingleBlock(doors[0].getRelative(Direction.DOWN), null, player)) return true;
        } else if (location.getBlockType() == BlockTypes.CHEST || location.getBlockType() == BlockTypes.TRAPPED_CHEST) {
            for (BlockFace chestFace : newsFaces) {
                Location<World> relativeChest = location.getRelative(chestFace.get());
                if (relativeChest.getBlockType() == location.getBlockType()) {
                    if (isOwnerSingleBlock(relativeChest, chestFace.getOppositeFace(), player)) return true;
                }
            }
            // Don't break here
            // Everything else (First block of container check goes here)
        } else {
            if (isOwnerSingleBlock(location, null, player)) return true;
        }*/
        return false;
    }

    public static boolean isUser(Location<World> block, Player player) {
        // Double Doors
        if (block.getBlockType() == BlockTypes.CHEST || block.getBlockType() == BlockTypes.TRAPPED_CHEST) {
            // Check second chest sign
            for (Direction chestFace : Constants.FACES) {
                Location<World> relativeChest = block.getRelative(chestFace);
                if (relativeChest.getBlockType() == block.getBlockType()) {
                    if (isUserSingleBlock(relativeChest, chestFace.getOpposite(), player)) return true;
                }
            }
            // Don't break here
            // Everything else (First block of container check goes here)
        } else {
            if (isUserSingleBlock(block, null, player)) return true;
        }
        return false;
    }

    public static boolean isProtected(Location<World> block) {
        return isPrivateSign(block) || isLocked(block);
    }

    public static boolean isLockedSingleBlock(Location<World> location, Direction opposite) {
        for (Direction face : Constants.FACES) {
            if (face == opposite) continue;
            Location<World> relative = location.getRelative(face);
            if (isPrivateSign(relative) && location.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE) == face) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOwnerSingleBlock(Location<World> location, Direction exempt, Player player) {
        // Requires isLocked
        for (Direction blockface : Constants.FACES) {
            if (blockface == exempt) continue;
            Location<World> relativeBlock = location.getRelative(blockface);
            if (isPrivateSign(relativeBlock) && location.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE) == blockface) {
                if (isOwnerOnSign(relativeBlock, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isUserSingleBlock(Location<World> block, Direction opposite, Player player) {
        // Requires isLocked
        for (Direction face : Constants.FACES) {
            if (face == opposite) continue;
            Location<World> relativeBlock = block.getRelative(face);
            if (isLockOrMoreSign(relativeBlock) && block.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE) == face) {
                if (isUserOnSign(relativeBlock, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isOwnerOfSign(Location<World> sign, Player player) {
        // Requires isSign
        Location<World> protectedBlock = getAttachedBlock(sign);
        // Normal situation, that block is just locked by an adjacent sign
        if (isOwner(protectedBlock, player)) return true;
        // Situation where double door's block
        return false;
    }

    public static boolean isLockable(Location location) {
        BlockType type = location.getBlockType();
        return type != BlockTypes.WALL_SIGN && type != BlockTypes.STANDING_SIGN && Config.isLockable(type);
    }

    public static boolean isSign(Location<World> block) {
        return block.getBlockType() == BlockTypes.WALL_SIGN;
    }

    public static boolean isPrivateSign(Location<World> location) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            String line = ((Sign) tile).lines().get(0).toPlain();
            return isPrivate(line);
        }
        return false;
    }

    public static boolean isMoreSigned(Location<World> block) {
        TileEntity tile = block.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            String line = ((Sign) tile).lines().get(0).toPlain();
            return isMoreString(line);
        }
        return false;
    }

    public static boolean isLockOrMoreSign(Location<World> block) {
        TileEntity tile = block.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            String line = ((Sign) tile).lines().get(0).toPlain();
            return isLockOrMoreString(line);
        }
        return false;
    }

    public static boolean isOwnerOnSign(Location<World> block, Player player) {
        TileEntity tile = block.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            String line = ((Sign) tile).lines().get(1).toPlain();
            return Utils.isPlayerOnLine(player, line);
        }
        return false;
    }

    public static boolean isUserOnSign(Location<World> block, Player player) {
        TileEntity tile = block.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {

            for (int i = 1; i < 4; i++) {
                String line = ((Sign) tile).lines().get(i).toPlain();
                if (Utils.isPlayerOnLine(player, line)) {
                    return true;
                }
            }
            // For Towny & Factions
            for (int i = 1; i < 4; i++) {
                String line = ((Sign) tile).lines().get(i).toPlain();
                if (Depend.isTownyTownOrNationOf(line, player)) return true;
            }
        }
        return false;
    }

    public static boolean isPrivate(String line) {
        return Config.isPrivateSignString(line);
    }

    public static boolean isMoreString(String line) {
        return Config.isMoreSign(line);
    }

    public static boolean isLockOrMoreString(String line) {
        return isPrivate(line) || isMoreString(line);
    }

    public static Location<World> getAttachedBlock(Location<World> sign) {
        // Requires isSign
        return sign.getRelative(sign.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite());
    }

    public static Location getBottomDoorBlock(Location block) {
        // Requires isDoubleDoorBlock
        Location relative = block.getRelative(Direction.DOWN);
        if (relative.getBlockType() == block.getBlockType()) {
            return relative;
        } else {
            return block;
        }
    }

    public static void toggleDoor(Location<World> block, boolean open) {
        block.getBlock().cycleValue(Keys.HINGE_POSITION);
    }

    public static void toggleDoor(Location block) {
        block.getBlock().cycleValue(Keys.HINGE_POSITION);
    }

}
