package org.soraworld.locket;

import org.jetbrains.annotations.NotNull;
import org.soraworld.violet.Violet;
import org.soraworld.violet.plugin.SpongePlugin;
import org.soraworld.violet.text.ChatColor;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;
import java.nio.file.Path;

/**
 * @author Himmelt
 */
@Plugin(
        id = Locket.PLUGIN_ID,
        name = Locket.PLUGIN_NAME,
        version = Locket.PLUGIN_VERSION,
        description = "Locket Plugin",
        url = "https://github.com/Himmelt/Locket",
        authors = {"Himmelt"},
        dependencies = {
                @Dependency(
                        id = Violet.PLUGIN_ID,
                        version = Violet.PLUGIN_VERSION
                ),
                @Dependency(
                        id = "griefdefender",
                        optional = true
                ),
                @Dependency(
                        id = "griefprevention",
                        optional = true
                )
        }
)
public final class LocketPlugin extends SpongePlugin {

    @Inject
    public LocketPlugin(@ConfigDir(sharedRoot = false) Path path, PluginContainer container) {
        super(path, container);
    }

    @Override
    public String bStatsId() {
        return "6471";
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
