package org.bukkit.event.player;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

/**
 * @author Himmelt
 */
public abstract class PlayerInteractEvent extends PlayerEvent implements Cancellable {
    public PlayerInteractEvent(Player who) {
        super(who);
    }

    public abstract EquipmentSlot getHand();

    public abstract Block getClickedBlock();

    public abstract Action getAction();

    public abstract BlockFace getBlockFace();
}
