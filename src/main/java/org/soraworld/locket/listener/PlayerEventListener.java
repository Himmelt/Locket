package org.soraworld.locket.listener;

import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.Permissions;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.BlockFace;
import org.soraworld.locket.util.SignUtil;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Hinges;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class PlayerEventListener {

    @Listener(order = Order.LAST)
    public void onPlayerLockChest(InteractBlockEvent.Secondary event, @First Player player) {
        Direction face = event.getTargetSide();
        ItemStack stack = player.getItemInHand(event.getHandType()).orElse(null);
        if (!Utils.isFace(face) || player.get(Keys.IS_SNEAKING).orElse(false) || stack == null || ItemTypes.SIGN != stack.getItem())
            return;
        event.setCancelled(true);
        if (!player.hasPermission(Permissions.LOCK)) {
            Utils.sendActionBar(player, "&a&lYou need permission &b[" + Permissions.LOCK + "]&a to do this!");
            return;
        }
        Utils.sendActionBar(player, "&a&lYou need permission &b[" + Permissions.LOCK + "]&a to do this!");

        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if (location == null || Depend.isProtectedFrom(location, player) || location.getRelative(face).getBlockType() != BlockTypes.AIR || !LocketAPI.isLockable(location)) {
            Utils.sendActionBar(player, "&c&lYou can't lock this block!");
            return;
        }
        boolean locked = LocketAPI.isLocked(location);
        //locked = false;
        if (!locked && !LocketAPI.isUpDownLockedDoor(location)) {
            Utils.removeOne(player, event.getHandType());
            SignUtil.placeWallSign(location.getBlockRelative(face), face, Text.of("[Private]"), Text.of(player.getName()));
            Utils.sendActionBar(player, "&c保护锁放置成功!");
        } else if (!locked && LocketAPI.isOwnerUpDownLockedDoor(location, player)) {
            Utils.removeOne(player, event.getHandType());
            SignUtil.placeWallSign(location.getBlockRelative(face), face, Text.of("[More]"));
            Utils.sendActionBar(player, "additional-sign-added-quick");
        } else if (LocketAPI.isOwner(location, player)) {
            Utils.removeOne(player, event.getHandType());
            Utils.putSignMore(player, location, face);
            Utils.sendActionBar(player, "additional-sign-added-quick");
        } else {
            Utils.sendMessages(player, Config.getLang("cannot-lock-quick"));
        }
    }

    // Player select sign
    @Listener(order = Order.LATE)
    public void playerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if (!player.hasPermission("locket.edit")) return;
        if (LocketAPI.isOwnerOfSign(location, player) || (LocketAPI.isLockOrMoreSign(location) && player.hasPermission("locket.edit.admin"))) {
            Utils.selectSign(player, location);
            Utils.sendMessages(player, Config.getLang("sign-selected"));
        }
    }

    // Protect block from being used & handle double doors
    // Bukkit-1.7.10 dose not support event.getHand()
    // 保护方块被使用
    @Listener(order = Order.FIRST)
    public void onAttemptInteractLockedBlocks(InteractBlockEvent event, @First Player player) {
        Location location = event.getTargetBlock().getLocation().orElse(null);
        if (((LocketAPI.isLocked(location) && !LocketAPI.isUser(location, player)) ||
                (LocketAPI.isUpDownLockedDoor(location) && !LocketAPI.isUserUpDownLockedDoor(location, player)))
                && !player.hasPermission("locket.admin.use")) {
            Utils.sendMessages(player, Config.getLang("block-is-locked"));
            event.setCancelled(true);
            return;
        }
        if (event instanceof InteractBlockEvent.Secondary && LocketAPI.isDoubleDoorBlock(location) && LocketAPI.isLocked(location)) {
            Location doorBlock = LocketAPI.getBottomDoorBlock(location);
            boolean shouldOpen = !(doorBlock.getBlock().get(Keys.HINGE_POSITION).get() == Hinges.LEFT); // Move to here
            List<Location> doors = new ArrayList<>();
            doors.add(doorBlock);
            if (doorBlock.getBlockType() == BlockTypes.IRON_DOOR) {
                LocketAPI.toggleDoor(doorBlock, shouldOpen);
            }
            for (BlockFace blockface : LocketAPI.newsFaces) {
                Location relative = doorBlock.getRelative(blockface.get());
                if (relative.getBlockType() == doorBlock.getBlockType()) {
                    doors.add(relative);
                    LocketAPI.toggleDoor(relative, shouldOpen);
                }
            }
        }
    }

}
