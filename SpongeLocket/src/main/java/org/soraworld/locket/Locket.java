package org.soraworld.locket;

import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.Violet;
import org.soraworld.violet.plugin.SpongePlugin;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

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
                        id = "griefprevention",
                        optional = true
                )
        }
)
public class Locket extends SpongePlugin<LocketManager> {
    public static final String PLUGIN_ID = "locket";
    public static final String PLUGIN_NAME = "Locket";
    public static final String PLUGIN_VERSION = "1.2.1";
}
