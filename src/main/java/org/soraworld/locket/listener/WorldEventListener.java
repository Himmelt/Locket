package org.soraworld.locket.listener;

/* Created by Himmelt on 2016/7/16.*/

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.soraworld.locket.api.LocketAPI;

public class WorldEventListener implements Listener {
    // Prevent tree break block
    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureGrow(StructureGrowEvent event) {
        for (BlockState blockstate : event.getBlocks()) {
            if (LocketAPI.isProtected(blockstate.getBlock())) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
