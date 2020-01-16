package org.bukkit.inventory;

public interface PlayerInventory {
    /* legacy */
    ItemStack getItemInHand();

    void setItemInHand(ItemStack stack);

    /* since 1.9+ */
    ItemStack getItemInMainHand();

    void setItemInMainHand(ItemStack stack);

    ItemStack getItemInOffHand();

    void setItemInOffHand(ItemStack stack);
}
