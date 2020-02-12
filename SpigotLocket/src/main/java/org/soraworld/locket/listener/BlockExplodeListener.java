package org.soraworld.locket.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.inject.InjectListener;
import org.soraworld.violet.inject.McVer;

/**
 * @author Himmelt
 */
@InjectListener
@McVer("[1.8.0,9.9.9]")
public final class BlockExplodeListener implements Listener {

    @Inject
    private static LocketManager manager;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosion(BlockExplodeEvent event) {
        if (manager.isPreventExplosion()) {
            event.blockList().removeIf(block -> block != null && manager.isLocked(block));
        }
    }
}
