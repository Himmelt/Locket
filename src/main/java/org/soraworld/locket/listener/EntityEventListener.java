package org.soraworld.locket.listener;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;

import java.util.Iterator;

public class EntityEventListener implements Listener {

    // Prevent explosion break block
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!Config.isExplosionProtection()) return;
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (LocketAPI.isProtected(block)) it.remove();
        }
    }

}
