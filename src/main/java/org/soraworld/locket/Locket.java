package org.soraworld.locket;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.LoggerFactory;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.command.CommandSpecs;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.listener.SpongeEventListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

@Plugin(id = Constants.MODID, name = Constants.NAME, version = Constants.VERSION)
public class Locket {

    @Inject
    public Locket(PluginContainer plugin,
                  GuiceObjectMapperFactory factory,
                  @ConfigDir(sharedRoot = false) Path cfgDir,
                  @DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> cfgLoader) {
        LocketAPI.PLUGIN = plugin;
        LocketAPI.LOGGER = LoggerFactory.getLogger(Constants.NAME);
        LocketAPI.CONFIG = new Config(cfgDir, cfgLoader, factory);
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        LocketAPI.CONFIG.load();
        LocketAPI.CONFIG.save();
        Sponge.getEventManager().registerListeners(this, new SpongeEventListener());
        Sponge.getCommandManager().register(this, CommandSpecs.CMD_LOCKET, "locket", "lock");
    }

    @Listener
    public void onStop(GameStoppingServerEvent event) {
        LocketAPI.CONFIG.save();
    }
}
