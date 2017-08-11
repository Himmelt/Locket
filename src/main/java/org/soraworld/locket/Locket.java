package org.soraworld.locket;

import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.Constant;
import org.soraworld.locket.listener.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = Constant.MODID, name = Constant.NAME, version = Constant.VERSION)
public class Locket {

    private static Locket locket;
    private Config config;

    @Listener
    public void onInit(GameInitializationEvent event) {
        locket = this;
        config = new Config(this);
        Sponge.getEventManager().registerListeners(this, new BlockEventListener());
        Sponge.getEventManager().registerListeners(this, new EntityEventListener());
        Sponge.getEventManager().registerListeners(this, new InventoryEventListener());
        Sponge.getEventManager().registerListeners(this, new PlayerEventListener());
        Sponge.getEventManager().registerListeners(this, new WorldEventListener());
    }

}
