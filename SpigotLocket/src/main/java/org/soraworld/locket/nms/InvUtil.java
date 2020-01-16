package org.soraworld.locket.nms;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.data.HandType;

import static org.soraworld.violet.nms.Version.*;

/**
 * @author Himmelt
 */
public class InvUtil {

    public static HandType getHandType(PlayerInteractEvent event) {
        if (v1_7_R4 || v1_8_R1 || v1_8_R3) {
            return HandType.MAIN_HAND;
        } else {
            return event.getHand().toString().toLowerCase().contains("off") ? HandType.OFF_HAND : HandType.MAIN_HAND;
        }
    }

    public static ItemStack getItemInHand(@NotNull PlayerInventory player, HandType type) {
        if (v1_7_R4 || v1_8_R1 || v1_8_R3) {
            return player.getItemInHand();
        } else if (type == HandType.OFF_HAND) {
            return player.getItemInOffHand();
        } else {
            return player.getItemInMainHand();
        }
    }

    public static void setItemInHand(@NotNull PlayerInventory player, HandType type, ItemStack stack) {
        if (v1_7_R4 || v1_8_R1 || v1_8_R3) {
            player.setItemInHand(stack);
        } else if (type == HandType.OFF_HAND) {
            player.setItemInOffHand(stack);
        } else {
            player.setItemInMainHand(stack);
        }
    }
}
