package org.soraworld.locket.util;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

public enum DoorType {
    ACACIA_DOOR(BlockTypes.ACACIA_DOOR),
    BIRCH_DOOR(BlockTypes.BIRCH_DOOR),
    DARK_OAK_DOOR(BlockTypes.DARK_OAK_DOOR),
    IRON_DOOR(BlockTypes.IRON_DOOR),
    JUNGLE_DOOR(BlockTypes.JUNGLE_DOOR),
    SPRUCE_DOOR(BlockTypes.SPRUCE_DOOR),
    WOODEN_DOOR(BlockTypes.WOODEN_DOOR),
    INVALID(BlockTypes.AIR);

    private final BlockType type;

    DoorType(BlockType type) {
        this.type = type;
    }

    public BlockType get() {
        return type;
    }

    public static DoorType resolve(BlockType type) {
        try {
            return DoorType.valueOf(type.getName());
        } catch (Exception e) {
            return INVALID;
        }
    }
}
