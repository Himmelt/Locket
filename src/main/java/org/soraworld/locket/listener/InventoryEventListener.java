package org.soraworld.locket.listener;

import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.entity.vehicle.minecart.ChestMinecart;
import org.spongepowered.api.entity.vehicle.minecart.HopperMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;

public class InventoryEventListener {

    // 漏斗输运事件
    @Listener(order = Order.FIRST)
    @IsCancelled(value = Tristate.UNDEFINED)
    public void onInventoryMove(ChangeInventoryEvent.Transfer event) {
        if (Config.isItemTransferOutBlocked() || Config.getHopperMinecartAction()) {
            if (isInventoryLocked(event.getCause().first(HopperMinecart.class).get())) {
                if (Config.isItemTransferOutBlocked()) {
                    event.setCancelled(true);
                }
                // Additional Hopper Minecart Check
                if (event.getTargetInventory() instanceof HopperMinecart) {
                    boolean hopperMinecartAction = Config.getHopperMinecartAction();
                    if (hopperMinecartAction) {
                        // case 0 - Impossible
                        // Cancel only, it is not called if !Config.isItemTransferOutBlocked()
                        event.setCancelled(true);
                    }
                }
                return;
            }
        }
        if (Config.isItemTransferInBlocked()) {
            if (isInventoryLocked(event.getTargetInventory())) {
                event.setCancelled(true);
            }
        }
    }

    public boolean isInventoryLocked(Inventory inventory) {
        InventoryArchetype archetype = inventory.getArchetype();
        if (archetype == InventoryArchetypes.DOUBLE_CHEST) {
            inventory = ((ChestMinecart) inventory);
        }
        if (inventory instanceof CarriedInventory) {
            CarriedInventory carriedInventory = (CarriedInventory) inventory;
            if (carriedInventory.getCarrier().get() instanceof TileEntityCarrier) {
                TileEntityCarrier carrier = (TileEntityCarrier) carriedInventory.getCarrier().get();
                Location block = carrier.getLocation();
                return LocketAPI.isLocked(block);
            }
        }
        return false;
    }
}
