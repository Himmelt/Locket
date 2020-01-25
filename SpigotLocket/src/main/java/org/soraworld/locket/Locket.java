package org.soraworld.locket;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.nms.Helper;
import org.soraworld.locket.util.Util;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.util.ChatColor;

import java.util.Optional;

/**
 * @author Himmelt
 */
public final class Locket extends SpigotPlugin<LocketManager> {
    public static final String PLUGIN_ID = "locket";
    public static final String PLUGIN_NAME = "Locket";
    public static final String PLUGIN_VERSION = "1.2.4";

    static {
        Helper.injectTile();
    }

    public static Optional<OfflinePlayer> parseUser(String text) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        return Util.parseUuid(text).map(value -> Optional.of(Bukkit.getOfflinePlayer(value))).orElseGet(() -> Optional.of(Bukkit.getOfflinePlayer(ChatColor.stripAllColor(text).trim())));
    }
}
