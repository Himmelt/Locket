package org.soraworld.locket.util;

import org.spongepowered.api.util.Direction;

public enum BlockFace {
    NORTH(Direction.NORTH),
    EAST(Direction.EAST),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    UP(Direction.UP),
    DOWN(Direction.DOWN),
    NONE(Direction.NONE);

    private final Direction direction;

    BlockFace(Direction direction) {
        this.direction = direction;
    }

    public Direction get() {
        return this.direction;
    }

    public BlockFace getOppositeFace() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            default:
                return NONE;
        }
    }
}
