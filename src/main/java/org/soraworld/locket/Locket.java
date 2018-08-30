package org.soraworld.locket;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.LoggerFactory;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.command.CommandSpecs;
import org.soraworld.locket.config.LocketManager;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.listener.EventListener;
import org.soraworld.violet.manager.SpongeManager;
import org.soraworld.violet.plugin.SpongePlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

@Plugin(
        id = Constants.MOD_ID,
        name = Constants.NAME,
        version = Constants.VERSION)
public class Locket extends SpongePlugin {

    @Inject
    public Locket(PluginContainer plugin,
                  GuiceObjectMapperFactory factory,
                  @ConfigDir(sharedRoot = false) Path path,
                  @DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> loader) {
        LocketAPI.PLUGIN = plugin;
        LocketAPI.LOGGER = LoggerFactory.getLogger(Constants.NAME);
        LocketAPI.CONFIG = new LocketManager(path, loader, factory);
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        LocketAPI.CONFIG.load();
        LocketAPI.CONFIG.save();
        Sponge.getEventManager().registerListeners(this, new EventListener());
        Sponge.getCommandManager().register(this, CommandSpecs.CMD_LOCKET, "locket", "lock");
    }

    @Nonnull
    protected SpongeManager registerManager(Path path) {
        return null;
    }

    @Nullable
    protected List<Object> registerListeners() {
        return null;
    }

    @Listener
    public void onStop(GameStoppingServerEvent event) {
        LocketAPI.CONFIG.save();
    }

    @Nonnull
    public String assetsId() {
        return null;
    }
}
