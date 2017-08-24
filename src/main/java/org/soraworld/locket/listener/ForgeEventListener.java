package org.soraworld.locket.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.BlockEvent;

public class ForgeEventListener {
    //@SubscribeEvent
    public void onInventoryTransfer(BlockEvent.BreakEvent event) {
        System.out.println(event.getClass());
        System.out.println(event.getPos());
        System.out.println(event.getState());
    }

    //@SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onForgeStructureGrow(BlockEvent.CropGrowEvent event) {
        System.out.println("===== onForgeStructureGrow =====");
        System.out.println(event.getPos());
        System.out.println(event.getResult());
        System.out.println(event.getState());
    }

    //@SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
    }
}
