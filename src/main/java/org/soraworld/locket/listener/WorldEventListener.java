package org.soraworld.locket.listener;

import org.soraworld.locket.api.LocketAPI;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class WorldEventListener {
    // Prevent tree break block
    @Listener(order = Order.EARLY)
    public void onStructureGrow(ChangeBlockEvent.Grow event) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (LocketAPI.isProtected(transaction.getOriginal().getLocation().get())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
