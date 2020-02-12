package org.soraworld.locket;

import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.util.Helper;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.text.ChatColor;

/**
 * @author Himmelt
 */
public final class LocketPlugin extends SpigotPlugin {

    static {
        Helper.injectTile();
    }

    @Override
    public String bStatsId() {
        return "6470";
    }

    @Override
    public String violetVersion() {
        return "2.5.0";
    }

    @Override
    public @NotNull ChatColor chatColor() {
        return ChatColor.YELLOW;
    }
}
