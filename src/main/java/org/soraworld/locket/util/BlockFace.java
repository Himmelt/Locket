package org.soraworld.locket.util;

import org.spongepowered.api.util.Direction;

public enum BlockFace {
    NORTH(Direction.NORTH),
    EAST(Direction.EAST),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    UP(Direction.UP),
    DOWN(Direction.DOWN);

    private final Direction direction;

    BlockFace(Direction direction) {
        this.direction = direction;
    }

    public Direction get() {
        return this.direction;
    }
}
