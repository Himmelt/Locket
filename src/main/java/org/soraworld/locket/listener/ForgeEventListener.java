package org.soraworld.locket.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
import org.spongepowered.api.block.tileentity.Sign;

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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player instanceof EntityPlayerMP) {
            IPlayer iPlayer = LocketAPI.getPlayer(player.getName());
            TileEntity tile = event.getWorld().getTileEntity(event.getPos());
            if (tile instanceof Sign) {
                switch (iPlayer.canBreak((Sign) tile)) {
                    case NOT_LOCK:
                    case ADMIN_UNLOCK:
                    case ADMIN_BREAK:
                    case OWNER:
                        iPlayer.sendChat(iPlayer.canBreak((Sign) tile).name());
                        return;
                    default:
                        event.setCanceled(true);
                        iPlayer.sendChat("你不能破坏此方块!");
                }
            }
        }
    }
}
