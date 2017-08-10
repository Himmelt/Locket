package org.soraworld.locket.listener;

import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Iterator;

public class EntityEventListener {

    // Prevent explosion break block
    @Listener(order = Order.EARLY)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!Config.isExplosionProtection()) return;
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Location<World> block = it.next();
            if (LocketAPI.isProtected(block)) it.remove();
        }
    }

}
