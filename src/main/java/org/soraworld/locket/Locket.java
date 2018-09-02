package org.soraworld.locket;

import org.soraworld.locket.command.CommandLocket;
import org.soraworld.locket.listener.EventListener;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.Violet;
import org.soraworld.violet.command.SpongeBaseSubs;
import org.soraworld.violet.command.SpongeCommand;
import org.soraworld.violet.manager.SpongeManager;
import org.soraworld.violet.plugin.SpongePlugin;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Plugin(
        id = Locket.PLUGIN_ID,
        name = Locket.PLUGIN_NAME,
        version = Locket.PLUGIN_VERSION,
        description = "Locket Plugin",
        url = "https://github.com/Himmelt/Locket",
        authors = {"Himmelt"},
        dependencies = {@Dependency(
                id = Violet.PLUGIN_ID,
                version = Violet.PLUGIN_VERSION
        )}
)
public class Locket extends SpongePlugin {

    static final String PLUGIN_ID = "locket";
    static final String PLUGIN_NAME = "Locket";
    static final String PLUGIN_VERSION = "1.0.7";

    @Nonnull
    protected SpongeManager registerManager(Path path) {
        return new LocketManager(this, path);
    }

    @Nullable
    protected List<Object> registerListeners() {
        return Collections.singletonList(new EventListener((LocketManager) manager));
    }

    protected void registerCommands() {
        SpongeCommand command = new SpongeCommand(this.getId(), this.manager.defAdminPerm(), false, this.manager);
        command.extractSub(SpongeBaseSubs.class, "lang");
        command.extractSub(SpongeBaseSubs.class, "debug");
        command.extractSub(SpongeBaseSubs.class, "save");
        command.extractSub(SpongeBaseSubs.class, "reload");
        command.extractSub(SpongeBaseSubs.class, "help");
        command.extractSub(CommandLocket.class);
        register(this, command);
    }

    @Nonnull
    public String assetsId() {
        return PLUGIN_ID;
    }
}
