package org.soraworld.locket.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.inject.EventListener;
import org.soraworld.violet.inject.Inject;

/**
 * @author Himmelt
 */
@EventListener
public class HighVersionListener implements Listener {
    @Inject
    private LocketManager manager;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosion(BlockExplodeEvent event) {
        if (manager.isPreventExplosion()) {
            event.blockList().removeIf(block -> block != null && manager.isLocked(block));
        }
    }
}
