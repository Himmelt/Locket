package org.soraworld.locket.api;

/* Created by Himmelt on 2016/7/15.*/

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.Utils;

public class LocketAPI {

    public static BlockFace[] newsFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    public static BlockFace[] allFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    public static boolean isLocked(Block block) {
        switch (block.getType()) {
            // Double Doors
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                Block[] doors = getDoors(block);
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
                    Block relativeChest = block.getRelative(chestFace);
                    if (relativeChest.getType() == block.getType()) {
                        if (isLockedSingleBlock(relativeChest, chestFace.getOppositeFace())) return true;
                    }
                }
                // Don't break here
                // Everything else (First block of container check goes here)
            default:
                if (isLockedSingleBlock(block, null)) return true;
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
        return (isLockSign(block) || isLocked(block) || isUpDownLockedDoor(block));
    }

    public static boolean isLockedSingleBlock(Block block, BlockFace exempt) {
        for (BlockFace blockface : newsFaces) {
            if (blockface == exempt) continue;
            Block relativeBlock = block.getRelative(blockface);
            if (isLockSign(relativeBlock) && (((org.bukkit.material.Sign) relativeBlock.getState().getData()).getFacing() == blockface)) {
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
            if (isLockSign(relativeBlock) && (((org.bukkit.material.Sign) relativeBlock.getState().getData()).getFacing() == blockface)) {
                if (isOwnerOnSign(relativeBlock, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isUserSingleBlock(Block block, BlockFace exempt, Player player) {
        // Requires isLocked
        for (BlockFace blockface : newsFaces) {
            if (blockface == exempt) continue;
            Block relativeBlock = block.getRelative(blockface);
            if (isLockSignOrAdditionalSign(relativeBlock) && (((org.bukkit.material.Sign) relativeBlock.getState().getData()).getFacing() == blockface)) {
                if (isUserOnSign(relativeBlock, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isOwnerOfSign(Block block, Player player) {
        // Requires isSign
        Block protectedBlock = getAttachedBlock(block);
        // Normal situation, that block is just locked by an adjacent sign
        if (isOwner(protectedBlock, player)) return true;
        // Situation where double door's block
        if (isUpDownLockedDoor(protectedBlock) && isOwnerUpDownLockedDoor(protectedBlock, player)) return true;
        // Otherwise...
        return false;
    }

    public static boolean isLockable(Block block) {
        Material material = block.getType();
        //Bad blocks
        switch (material) {
            case SIGN:
            case WALL_SIGN:
            case SIGN_POST:
                return false;
            default:
                break;
        }
        if (Config.isLockable(material)) {
            // Directly lockable
            return true;
        } else {
            // Indirectly lockable
            Block blockUp = block.getRelative(BlockFace.UP);
            if (blockUp != null && isUpDownAlsoLockableBlock(blockUp)) return true;
            Block blockDown = block.getRelative(BlockFace.DOWN);
            return blockDown != null && isUpDownAlsoLockableBlock(blockDown);
        }
    }

    public static boolean isUpDownAlsoLockableBlock(Block block) {
        if (Config.isLockable(block.getType())) {
            switch (block.getType()) {
                case WOODEN_DOOR:
                case IRON_DOOR_BLOCK:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public static boolean mayInterfere(Block block, Player player) {
        // if LEFT may interfere RIGHT
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

    public static boolean isLockSign(Block block) {
        return isSign(block) && isLockString(((Sign) block.getState()).getLine(0));
    }

    public static boolean isAdditionalSign(Block block) {
        return isSign(block) && isAdditionalString(((Sign) block.getState()).getLine(0));
    }

    public static boolean isLockSignOrAdditionalSign(Block block) {
        if (isSign(block)) {
            String line = ((Sign) block.getState()).getLine(0);
            return isLockStringOrAdditionalString(line);
        } else {
            return false;
        }
    }

    public static boolean isOwnerOnSign(Block block, Player player) {
        // Requires isLockSign
        String[] lines = ((Sign) block.getState()).getLines();
        return Utils.isPlayerOnLine(player, lines[1]);
    }

    public static boolean isUserOnSign(Block block, Player player) {
        // Requires (isLockSign or isAdditionalSign)
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

    public static boolean isAdditionalString(String line) {
        return Config.isMoreSign(line);
    }

    public static boolean isLockStringOrAdditionalString(String line) {
        return isLockString(line) || isAdditionalString(line);
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
