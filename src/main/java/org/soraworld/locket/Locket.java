package org.soraworld.locket;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.soraworld.locket.command.CommandSpecs;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.listener.EventListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

@Plugin(id = Constants.MODID, name = Constants.NAME, version = Constants.VERSION)
public class Locket {

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path cfgDir;
    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path cfgFile;
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    @Inject
    private Logger logger;
    @Inject
    private PluginContainer plugin;

    private static Locket locket;
    private Config config;

    public static Locket getLocket() {
        return locket;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        locket = this;
        config = new Config(this);
        Sponge.getEventManager().registerListeners(this, new EventListener());
        Sponge.getCommandManager().register(plugin, CommandSpecs.CMD_LOCKET, "locket", "lock");
    }

    public PluginContainer getPlugin() {
        return plugin;
    }
}
