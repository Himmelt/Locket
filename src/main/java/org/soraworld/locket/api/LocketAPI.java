package org.soraworld.locket.api;

import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.BlockFace;
import org.soraworld.locket.util.DoorType;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

public class LocketAPI {

    public static BlockFace[] newsFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    public static BlockFace[] allFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    public static boolean isLocked(Location location) {
        BlockType type = location.getBlockType();
        if (DoorType.resolve(type) != DoorType.INVALID) {

        }
        switch () {
            // Double Doors
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                Block[] doors = getDoors(location);
                if (doors == null) return false;
                for (BlockFace doorFace : newsFaces) {
                    Block relative0 = doors[0].getRelative(doorFace), relative1 = doors[1].getRelative(doorFace);
                    if (relative0.getType() == doors[0].getType() && relative1.getType() == doors[1].getType()) {
                        if (isLockedSingleBlock(relative1.getRelative(BlockFace.UP), doorFace.getOppositeFace()))
                            return true;
                        if (isLockedSingleBlock(relative1, doorFace.getOppositeFace())) return true;
                        if (isLockedSingleBlock(relative0, doorFace.getOppositeFace())) return true;
                        if (isLockedSingleBlock(relative0.getRelative(BlockFace.DOWN), doorFace.getOppositeFace()))
                            return true;
                    }
                }
                if (isLockedSingleBlock(doors[1].getRelative(BlockFace.UP), null)) return true;
                if (isLockedSingleBlock(doors[1], null)) return true;
                if (isLockedSingleBlock(doors[0], null)) return true;
                if (isLockedSingleBlock(doors[0].getRelative(BlockFace.DOWN), null)) return true;
                break;
            // Chests (Second block only)
            case CHEST:
            case TRAPPED_CHEST:
                // Check second chest sign
                for (BlockFace chestFace : newsFaces) {
                    Block relativeChest = location.getRelative(chestFace);
                    if (relativeChest.getType() == location.getType()) {
                        if (isLockedSingleBlock(relativeChest, chestFace.getOppositeFace())) return true;
                    }
                }
                // Don't break here
                // Everything else (First block of container check goes here)
            default:
                if (isLockedSingleBlock(location, null)) return true;
                break;
        }
        return false;
    }

    public static boolean isOwner(Block block, Player player) {
        switch (block.getType()) {
            // Double Doors
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                Block[] doors = getDoors(block);
                if (doors == null) return false;
                for (BlockFace doorFace : newsFaces) {
                    Block relative0 = doors[0].getRelative(doorFace), relative1 = doors[1].getRelative(doorFace);
                    if (relative0.getType() == doors[0].getType() && relative1.getType() == doors[1].getType()) {
                        if (isOwnerSingleBlock(relative1.getRelative(BlockFace.UP), doorFace.getOppositeFace(), player))
                            return true;
                        if (isOwnerSingleBlock(relative1, doorFace.getOppositeFace(), player)) return true;
                        if (isOwnerSingleBlock(relative0, doorFace.getOppositeFace(), player)) return true;
                        if (isOwnerSingleBlock(relative0.getRelative(BlockFace.DOWN), doorFace.getOppositeFace(), player))
                            return true;
                    }
                }
                if (isOwnerSingleBlock(doors[1].getRelative(BlockFace.UP), null, player)) return true;
                if (isOwnerSingleBlock(doors[1], null, player)) return true;
                if (isOwnerSingleBlock(doors[0], null, player)) return true;
                if (isOwnerSingleBlock(doors[0].getRelative(BlockFace.DOWN), null, player)) return true;
                break;
            // Chests (Second block only)
            case CHEST:
            case TRAPPED_CHEST:
                // Check second chest sign
                for (BlockFace chestFace : newsFaces) {
                    Block relativeChest = block.getRelative(chestFace);
                    if (relativeChest.getType() == block.getType()) {
                        if (isOwnerSingleBlock(relativeChest, chestFace.getOppositeFace(), player)) return true;
                    }
                }
                // Don't break here
                // Everything else (First block of container check goes here)
            default:
                if (isOwnerSingleBlock(block, null, player)) return true;
                break;
        }
        return false;
    }

    public static boolean isUser(Block block, Player player) {
        switch (block.getType()) {
            // Double Doors
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                Block[] doors = getDoors(block);
                if (doors == null) return false;
                for (BlockFace doorFace : newsFaces) {
                    Block relative0 = doors[0].getRelative(doorFace), relative1 = doors[1].getRelative(doorFace);
                    if (relative0.getType() == doors[0].getType() && relative1.getType() == doors[1].getType()) {
                        if (isUserSingleBlock(relative1.getRelative(BlockFace.UP), doorFace.getOppositeFace(), player))
                            return true;
                        if (isUserSingleBlock(relative1, doorFace.getOppositeFace(), player)) return true;
                        if (isUserSingleBlock(relative0, doorFace.getOppositeFace(), player)) return true;
                        if (isUserSingleBlock(relative0.getRelative(BlockFace.DOWN), doorFace.getOppositeFace(), player))
                            return true;
                    }
                }
                if (isUserSingleBlock(doors[1].getRelative(BlockFace.UP), null, player)) return true;
                if (isUserSingleBlock(doors[1], null, player)) return true;
                if (isUserSingleBlock(doors[0], null, player)) return true;
                if (isUserSingleBlock(doors[0].getRelative(BlockFace.DOWN), null, player)) return true;
                break;
            // Chests (Second block only)
            case CHEST:
            case TRAPPED_CHEST:
                // Check second chest sign
                for (BlockFace chestFace : newsFaces) {
                    Block relativeChest = block.getRelative(chestFace);
                    if (relativeChest.getType() == block.getType()) {
                        if (isUserSingleBlock(relativeChest, chestFace.getOppositeFace(), player)) return true;
                    }
                }
                // Don't break here
                // Everything else (First block of container check goes here)
            default:
                if (isUserSingleBlock(block, null, player)) return true;
                break;
        }
        return false;
    }

    public static boolean isProtected(Block block) {
        return (isLockSigned(block) || isLocked(block) || isUpDownLockedDoor(block));
    }

    public static boolean isLockedSingleBlock(Block block, BlockFace exempt) {
        for (BlockFace blockface : newsFaces) {
            if (blockface == exempt) continue;
            Block relativeBlock = block.getRelative(blockface);
            if (isLockSigned(relativeBlock) && (((org.bukkit.material.Sign) relativeBlock.getState().getData()).getFacing() == blockface)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOwnerSingleBlock(Block block, BlockFace exempt, Player player) {
        // Requires isLocked
        for (BlockFace blockface : newsFaces) {
            if (blockface == exempt) continue;
            Block relativeBlock = block.getRelative(blockface);
            if (isLockSigned(relativeBlock) && (((org.bukkit.material.Sign) relativeBlock.getState().getData()).getFacing() == blockface)) {
                if (isOwnerOnSign(relativeBlock, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isUserSingleBlock(Block block, BlockFace exempt, Player player) {
        // Requires isLocked
        for (BlockFace face : newsFaces) {
            if (face == exempt) continue;
            Block relativeBlock = block.getRelative(face);
            if (isLockOrMoreSign(relativeBlock) && (((org.bukkit.material.Sign) relativeBlock.getState().getData()).getFacing() == face)) {
                if (isUserOnSign(relativeBlock, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isOwnerOfSign(Sign sign, Player player) {
        // Requires isSign
        Block protectedBlock = getAttachedBlock(sign);
        // Normal situation, that block is just locked by an adjacent sign
        if (isOwner(protectedBlock, player)) return true;
        // Situation where double door's block
        if (isUpDownLockedDoor(protectedBlock) && isOwnerUpDownLockedDoor(protectedBlock, player)) return true;
        // Otherwise...
        return false;
    }

    public static boolean isLockable(Location location) {
        BlockType type = location.getBlockType();
        if (type == BlockTypes.WALL_SIGN || type == BlockTypes.STANDING_SIGN) return false;
        if (Config.isLockable(type)) {
            return true;
        } else {
            BlockType typeUp = location.getRelative(Direction.UP).getBlockType();
            if (isUpDownAlsoLockableBlock(typeUp)) return true;
            BlockType typeDown = location.getRelative(Direction.DOWN).getBlockType();
            if (isUpDownAlsoLockableBlock(typeDown)) return true;
        }
    }

    public static boolean isUpDownAlsoLockableBlock(BlockType type) {
        return DoorType.resolve(type) != DoorType.INVALID;
    }

    public static boolean mayInterfere(BlockSnapshot block, Player player) {
        BlockType type = block.getState().getType();
        // DOOR
        if (type == BlockTypes.WOODEN_DOOR || type == BlockTypes.ACACIA_DOOR || type == BlockTypes.IRON_DOOR || type == BlockTypes.BIRCH_DOOR) {
            for (BlockFace face : newsFaces) {
                BlockType newType = block.getLocation().get().getBlockRelative(face.get()).getBlockType();
                switch (DoorType.resolve(newType)) {
                    case WOODEN_DOOR:
                    case BIRCH_DOOR:
                    case ACACIA_DOOR:
                    case JUNGLE_DOOR:
                    case SPRUCE_DOOR:
                    case DARK_OAK_DOOR:
                    case IRON_DOOR:
                        if (isLocked(newBlock) && !isOwner(newBlock, player)) {
                            return true;
                        }
                    default:
                        break;
                }
            }
            Block newBlock2 = block.getRelative(BlockFace.UP, 2);
            switch (newBlock2.getType()) {
                default:
                    if (isLocked(newBlock2) && !isOwner(newBlock2, player)) {
                        return true;
                    }
                    break;
            }
            Block newBlock3 = block.getRelative(BlockFace.DOWN, 1);
            switch (newBlock3.getType()) {
                default:
                    if (isLocked(newBlock3) && !isOwner(newBlock3, player)) {
                        return true;
                    }
                    break;
            }
            break;
        }

        switch (block.getType()) {
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                for (BlockFace blockface : newsFaces) {
                    Block newBlock = block.getRelative(blockface);
                    switch (newBlock.getType()) {
                        case WOODEN_DOOR:
                        case IRON_DOOR_BLOCK:
                            if (isLocked(newBlock) && !isOwner(newBlock, player)) {
                                return true;
                            }
                        default:
                            break;
                    }
                }
                // Temp workaround bad code for checking up and down signs

                // End temp workaround bad code for checking up and down signs
            case CHEST:
            case TRAPPED_CHEST:
            case WALL_SIGN:
            case SIGN_POST:
                for (BlockFace blockface : allFaces) {
                    Block newBlock = block.getRelative(blockface);
                    switch (newBlock.getType()) {
                        case CHEST:
                        case TRAPPED_CHEST:
                            if (isLockedSingleBlock(newBlock, null) && !isOwnerSingleBlock(newBlock, null, player)) {
                                return true;
                            }
                        default:
                            break;
                    }
                }
                break;
            // This is extra interfere block
            case HOPPER:
            case DISPENSER:
            case DROPPER:
                if (!Config.isInterferePlacementBlocked()) return false;
                for (BlockFace blockface : allFaces) {
                    Block newBlock = block.getRelative(blockface);
                    switch (newBlock.getType()) {
                        case CHEST:
                        case TRAPPED_CHEST:
                        case HOPPER:
                        case DISPENSER:
                        case DROPPER:
                            if (isLocked(newBlock) && !isOwner(newBlock, player)) {
                                return true;
                            }
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    public static boolean isSign(Block block) {
        return block.getType() == Material.WALL_SIGN;
    }

    public static boolean isLockSigned(Block block) {
        return isSign(block) && isLockString(((Sign) block.getState()).getLine(0));
    }

    public static boolean isMoreSigned(Block block) {
        return isSign(block) && isMoreString(((Sign) block.getState()).getLine(0));
    }

    public static boolean isLockOrMoreSign(Block block) {
        if (isSign(block)) {
            String line = ((Sign) block.getState()).getLine(0);
            return isLockOrMoreString(line);
        } else {
            return false;
        }
    }

    public static boolean isOwnerOnSign(Block block, Player player) {
        // Requires isLockSigned
        String[] lines = ((Sign) block.getState()).getLines();
        return Utils.isPlayerOnLine(player, lines[1]);
    }

    public static boolean isUserOnSign(Block block, Player player) {
        // Requires (isLockSigned or isMoreSigned)
        String[] lines = ((Sign) block.getState()).getLines();
        // Normal
        for (int i = 1; i < 4; i++) {
            if (Utils.isPlayerOnLine(player, lines[i])) {
                return true;
            }
        }
        // For Towny & Factions
        for (int i = 1; i < 4; i++) {
            if (Depend.isTownyTownOrNationOf(lines[i], player)) return true;
        }
        return false;
    }

    public static boolean isUpDownLockedDoor(Block block) {
        Block blockUp = block.getRelative(BlockFace.UP);
        if (blockUp != null && isUpDownAlsoLockableBlock(blockUp) && isLocked(blockUp)) return true;
        Block blockDown = block.getRelative(BlockFace.DOWN);
        return blockDown != null && isUpDownAlsoLockableBlock(blockDown) && isLocked(blockDown);
    }

    public static boolean isOwnerUpDownLockedDoor(Block block, Player player) {
        Block blockUp = block.getRelative(BlockFace.UP);
        if (blockUp != null && isUpDownAlsoLockableBlock(blockUp) && isOwner(blockUp, player)) return true;
        Block blockDown = block.getRelative(BlockFace.DOWN);
        return blockDown != null && isUpDownAlsoLockableBlock(blockDown) && isOwner(blockDown, player);
    }

    public static boolean isUserUpDownLockedDoor(Block block, Player player) {
        Block blockUp = block.getRelative(BlockFace.UP);
        if (blockUp != null && isUpDownAlsoLockableBlock(blockUp) && isUser(blockUp, player)) return true;
        Block blockDown = block.getRelative(BlockFace.DOWN);
        return blockDown != null && isUpDownAlsoLockableBlock(blockDown) && isUser(blockDown, player);
    }

    public static boolean isLockString(String line) {
        return Config.isPrivateSignString(line);
    }

    public static boolean isMoreString(String line) {
        return Config.isMoreSign(line);
    }

    public static boolean isLockOrMoreString(String line) {
        return isLockString(line) || isMoreString(line);
    }

    public static Block getAttachedBlock(Block sign) {
        // Requires isSign
        return sign.getRelative(((org.bukkit.material.Sign) sign.getState().getData()).getFacing().getOppositeFace());
    }


    public static Block[] getDoors(Block block) {
        Block[] doors = new Block[2];
        boolean found = false;
        Block up = block.getRelative(BlockFace.UP), down = block.getRelative(BlockFace.DOWN);
        if (up.getType() == block.getType()) {
            found = true;
            doors[0] = block;
            doors[1] = up;
        }
        if (down.getType() == block.getType()) {
            if (found) {
                // error 3 doors
                return null;
            }
            doors[1] = block;
            doors[0] = down;
            found = true;
        }
        if (!found) {
            // error 1 door
            return null;
        }
        return doors;
    }

    public static boolean isDoubleDoorBlock(Block block) {
        switch (block.getType()) {
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                return true;
            default:
                return false;
        }
    }

    public static Block getBottomDoorBlock(Block block) {
        // Requires isDoubleDoorBlock
        Block relative = block.getRelative(BlockFace.DOWN);
        if (relative.getType() == block.getType()) {
            return relative;
        } else {
            return block;
        }
    }

    public static void toggleDoor(Block block, boolean open) {
        BlockState doorState = block.getState();
        Openable openableState = (Openable) doorState.getData();
        openableState.setOpen(open);
        doorState.setData((MaterialData) openableState);
        doorState.update();
        block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
    }

    public static void toggleDoor(Block block) {
        BlockState doorState = block.getState();
        Openable openableState = (Openable) doorState.getData();
        openableState.setOpen(!openableState.isOpen());
        doorState.setData((MaterialData) openableState);
        doorState.update();
        block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
    }

}
