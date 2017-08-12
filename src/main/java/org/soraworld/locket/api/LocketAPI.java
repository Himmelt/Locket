package org.soraworld.locket.api;

import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.BlockFace;
import org.soraworld.locket.util.DoorType;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LocketAPI {

    public static BlockFace[] newsFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    public static BlockFace[] allFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    public static boolean isLocked(Location<World> location) {
        BlockType type = location.getBlockType();
        if (DoorType.resolve(type) != DoorType.INVALID) {
            System.out.println("IS DOOR");
            Location<World>[] doors = getDoors(location);
            if (doors == null) return false;
            for (BlockFace doorFace : newsFaces) {
                Location<World> relative0 = doors[0].getRelative(doorFace.get()), relative1 = doors[1].getRelative(doorFace.get());
                if (relative0.getBlockType() == doors[0].getBlockType() && relative1.getBlockType() == doors[1].getBlockType()) {
                    if (isLockedSingleBlock(relative1.getRelative(Direction.UP), doorFace.getOppositeFace()))
                        return true;
                    if (isLockedSingleBlock(relative1, doorFace.getOppositeFace())) return true;
                    if (isLockedSingleBlock(relative0, doorFace.getOppositeFace())) return true;
                    if (isLockedSingleBlock(relative0.getRelative(Direction.DOWN), doorFace.getOppositeFace()))
                        return true;
                }
            }
            if (isLockedSingleBlock(doors[1].getRelative(Direction.UP), null)) return true;
            if (isLockedSingleBlock(doors[1], null)) return true;
            if (isLockedSingleBlock(doors[0], null)) return true;
            if (isLockedSingleBlock(doors[0].getRelative(Direction.DOWN), null)) return true;
        } else if (type == BlockTypes.CHEST || type == BlockTypes.TRAPPED_CHEST) {
            // Check second chest sign
            System.out.println("IS CHEST");
            for (BlockFace chestFace : newsFaces) {
                Location<World> relativeChest = location.getRelative(chestFace.get());
                if (relativeChest.getBlockType() == location.getBlockType()) {
                    if (isLockedSingleBlock(relativeChest, chestFace.getOppositeFace())) return true;
                }
            }
            // Don't break here
            // Everything else (First block of container check goes here)
        } else {
            if (isLockedSingleBlock(location, null)) return true;
        }
        return false;
    }

    public static boolean isOwner(Location<World> block, Player player) {
        if (DoorType.isDoor(block)) {
            Location<World>[] doors = getDoors(block);
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
        } else if (block.getBlockType() == BlockTypes.CHEST || block.getBlockType() == BlockTypes.TRAPPED_CHEST) {
            // Check second chest sign
            for (BlockFace chestFace : newsFaces) {
                Location<World> relativeChest = block.getRelative(chestFace.get());
                if (relativeChest.getBlockType() == block.getBlockType()) {
                    if (isOwnerSingleBlock(relativeChest, chestFace.getOppositeFace(), player)) return true;
                }
            }
            // Don't break here
            // Everything else (First block of container check goes here)
        } else {
            if (isOwnerSingleBlock(block, null, player)) return true;
        }
        return false;
    }

    public static boolean isUser(Location<World> block, Player player) {
        // Double Doors
        if (DoorType.isDoor(block)) {
            Location<World>[] doors = getDoors(block);
            if (doors == null) return false;
            for (BlockFace doorFace : newsFaces) {
                Location<World> relative0 = doors[0].getRelative(doorFace.get()), relative1 = doors[1].getRelative(doorFace.get());
                if (relative0.getBlockType() == doors[0].getBlockType() && relative1.getBlockType() == doors[1].getBlockType()) {
                    if (isUserSingleBlock(relative1.getRelative(Direction.UP), doorFace.getOppositeFace(), player))
                        return true;
                    if (isUserSingleBlock(relative1, doorFace.getOppositeFace(), player)) return true;
                    if (isUserSingleBlock(relative0, doorFace.getOppositeFace(), player)) return true;
                    if (isUserSingleBlock(relative0.getRelative(Direction.DOWN), doorFace.getOppositeFace(), player))
                        return true;
                }
            }
            if (isUserSingleBlock(doors[1].getRelative(Direction.UP), null, player)) return true;
            if (isUserSingleBlock(doors[1], null, player)) return true;
            if (isUserSingleBlock(doors[0], null, player)) return true;
            if (isUserSingleBlock(doors[0].getRelative(Direction.DOWN), null, player)) return true;
        } else if (block.getBlockType() == BlockTypes.CHEST || block.getBlockType() == BlockTypes.TRAPPED_CHEST) {
            // Check second chest sign
            for (BlockFace chestFace : newsFaces) {
                Location<World> relativeChest = block.getRelative(chestFace.get());
                if (relativeChest.getBlockType() == block.getBlockType()) {
                    if (isUserSingleBlock(relativeChest, chestFace.getOppositeFace(), player)) return true;
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
        return (isLockSigned(block) || isLocked(block) || isUpDownLockedDoor(block));
    }

    public static boolean isLockedSingleBlock(Location<World> block, BlockFace exempt) {
        for (BlockFace blockface : newsFaces) {
            if (blockface == exempt) continue;
            Location<World> relativeBlock = block.getRelative(blockface.get());
            if (isLockSigned(relativeBlock) && block.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE) == blockface.get()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOwnerSingleBlock(Location<World> block, BlockFace exempt, Player player) {
        // Requires isLocked
        for (BlockFace blockface : newsFaces) {
            if (blockface == exempt) continue;
            Location<World> relativeBlock = block.getRelative(blockface.get());
            if (isLockSigned(relativeBlock) && block.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE) == blockface.get()) {
                if (isOwnerOnSign(relativeBlock, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isUserSingleBlock(Location<World> block, BlockFace exempt, Player player) {
        // Requires isLocked
        for (BlockFace face : newsFaces) {
            if (face == exempt) continue;
            Location<World> relativeBlock = block.getRelative(face.get());
            if (isLockOrMoreSign(relativeBlock) && block.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE) == face.get()) {
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
        return false;
    }

    public static boolean isUpDownAlsoLockableBlock(BlockType type) {
        return DoorType.resolve(type) != DoorType.INVALID;
    }

    public static boolean mayInterfere(BlockSnapshot block, Player player) {
        BlockType type = block.getState().getType();
        // DOOR
        if (type == BlockTypes.WOODEN_DOOR || type == BlockTypes.ACACIA_DOOR || type == BlockTypes.IRON_DOOR || type == BlockTypes.BIRCH_DOOR) {
            for (BlockFace face : newsFaces) {
                Location<World> newBlock = block.getLocation().get().getBlockRelative(face.get());
                BlockType newType = newBlock.getBlockType();
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
            Location<World> newBlock2 = block.getLocation().get().getBlockRelative(Direction.UP);
            Location<World> newBlock3 = block.getLocation().get().getBlockRelative(Direction.DOWN);
            if (isLocked(newBlock3) && !isOwner(newBlock3, player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSign(Location<World> block) {
        return block.getBlockType() == BlockTypes.WALL_SIGN;
    }

    public static boolean isLockSigned(Location<World> block) {
        TileEntity tile = block.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            String line = ((Sign) tile).lines().get(0).toPlain();
            return isLockString(line);
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

    public static boolean isUpDownLockedDoor(Location<World> block) {
        return false;
        /*Location blockUp = block.getRelative(Direction.UP);
        if (isUpDownAlsoLockableBlock(blockUp.getBlockType()) && isLocked(blockUp)) return true;
        Location blockDown = block.getRelative(Direction.DOWN);
        return isUpDownAlsoLockableBlock(blockDown.getBlockType()) && isLocked(blockDown);*/
    }

    public static boolean isOwnerUpDownLockedDoor(Location<World> block, Player player) {
        Location blockUp = block.getRelative(Direction.UP);
        if (isUpDownAlsoLockableBlock(blockUp.getBlockType()) && isOwner(blockUp, player)) return true;
        Location blockDown = block.getRelative(Direction.DOWN);
        return isUpDownAlsoLockableBlock(blockDown.getBlockType()) && isOwner(blockDown, player);
    }

    public static boolean isUserUpDownLockedDoor(Location<World> block, Player player) {
        Location blockUp = block.getRelative(Direction.UP);
        if (isUpDownAlsoLockableBlock(blockUp.getBlockType()) && isUser(blockUp, player)) return true;
        Location blockDown = block.getRelative(Direction.DOWN);
        return isUpDownAlsoLockableBlock(blockDown.getBlockType()) && isUser(blockDown, player);
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

    public static Location<World> getAttachedBlock(Location<World> sign) {
        // Requires isSign
        return sign.getRelative(sign.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite());
    }


    public static Location<World>[] getDoors(Location<World> block) {
        Location[] doors = new Location[2];
        boolean found = false;
        Location<World> up = block.getRelative(Direction.UP), down = block.getRelative(Direction.DOWN);
        if (up.getBlockType() == block.getBlockType()) {
            found = true;
            doors[0] = block;
            doors[1] = up;
        }
        if (down.getBlockType() == block.getBlockType()) {
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

    public static boolean isDoubleDoorBlock(Location block) {
        return DoorType.resolve(block.getBlockType()) != DoorType.INVALID;
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
