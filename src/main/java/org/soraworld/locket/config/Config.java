package org.soraworld.locket.config;

import org.soraworld.locket.Locket;
import org.spongepowered.api.block.BlockType;

public class Config {

    private Locket locket;

    public Config(Locket locket) {
        this.locket = locket;
        reload();
    }

    public void reload() {
    }

    public static boolean isLockable(BlockType type) {
        return true;
    }
}
