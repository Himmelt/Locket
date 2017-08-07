package org.soraworld.locket.listener;

import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.ArrayList;
import java.util.List;

public class PlayerEventListener {

    // Quick protect for chests
    @Listener(order = Order.DEFAULT)
    public void onPlayerQuickLockChest(InteractBlockEvent.Secondary event, @First Player player) {
        // 右键手持木牌
        if (ItemTypes.SIGN.equals(player.getItemInHand(event.getHandType()).get())) {
            // 潜行方式,取消
            if (player.get(Keys.IS_SNEAKING).orElse(false)) return;
            // 如果玩家没有使用权限,取消
            if (!player.hasPermission("locket.lock")) return;
            Direction face = event.getTargetSide();
            // 木牌只能贴在四个侧面
            if (face == Direction.NORTH || face == Direction.WEST || face == Direction.EAST || face == Direction.SOUTH) {
                Location location = event.getTargetBlock().getLocation().orElse(null);
                if (location == null || Depend.isProtectedFrom(location, player) ||
                        location.getRelative(face).getBlockType() != BlockTypes.AIR||
                        !LocketAPI.isLockable(location)) return;
                // 在被保护方块列表内
                boolean locked = LocketAPI.isLocked(location);
                // 取消交互事件
                event.setCancelled(true);
                if (!locked && !LocketAPI.isUpDownLockedDoor(block)) {
                    // 拿掉玩家一个木牌
                    Utils.removeASign(player);
                    // 显示消息
                    Utils.sendMessages(player, Config.getLang("locked-quick"));
                    Utils.putSignOn(block, face, Config.getDefaultPrivateString(), player.getName());
                } else if (!locked && LocketAPI.isOwnerUpDownLockedDoor(block, player)) {
                    Utils.removeASign(player);
                    Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                    Utils.putSignOn(block, face, Config.getDefaultMoreString(), "");
                } else if (LocketAPI.isOwner(block, player)) {
                    Utils.removeASign(player);
                    Utils.putSignOn(block, face, Config.getDefaultMoreString(), "");
                    Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                } else {
                    Utils.sendMessages(player, Config.getLang("cannot-lock-quick"));
                }
            }
        }
    }

    // Player select sign
    @EventHandler(priority = EventPriority.LOW)
    public void playerSelectSign(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.WALL_SIGN) {
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();
            if (!player.hasPermission("locket.edit")) return;
            if (LocketAPI.isOwnerOfSign(block, player) || (LocketAPI.isLockOrMoreSign(block) && player.hasPermission("locket.edit.admin"))) {
                Utils.selectSign(player, block);
                Utils.sendMessages(player, Config.getLang("sign-selected"));
            }
        }
    }

    // Protect block from being used & handle double doors
    // Bukkit-1.7.10 dose not support event.getHand()
    // 保护方块被使用
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptInteractLockedBlocks(PlayerInteractEvent event) {
        Action action = event.getAction();
        switch (action) {
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
                Block block = event.getClickedBlock();
                Player player = event.getPlayer();
                if (((LocketAPI.isLocked(block) && !LocketAPI.isUser(block, player)) ||
                        (LocketAPI.isUpDownLockedDoor(block) && !LocketAPI.isUserUpDownLockedDoor(block, player)))
                        && !player.hasPermission("locket.admin.use")) {
                    Utils.sendMessages(player, Config.getLang("block-is-locked"));
                    event.setCancelled(true);
                } else {
                    // Handle double doors
                    if (action == Action.RIGHT_CLICK_BLOCK) {
                        if (LocketAPI.isDoubleDoorBlock(block) && LocketAPI.isLocked(block)) {
                            Block doorBlock = LocketAPI.getBottomDoorBlock(block);
                            BlockState doorState = doorBlock.getState();
                            Openable openableState = (Openable) doorState.getData();
                            boolean shouldOpen = !openableState.isOpen(); // Move to here
                            List<Block> doors = new ArrayList<>();
                            doors.add(doorBlock);
                            if (doorBlock.getType() == Material.IRON_DOOR_BLOCK) {
                                LocketAPI.toggleDoor(doorBlock, shouldOpen);
                            }
                            for (BlockFace blockface : LocketAPI.newsFaces) {
                                Block relative = doorBlock.getRelative(blockface);
                                if (relative.getType() == doorBlock.getType()) {
                                    doors.add(relative);
                                    LocketAPI.toggleDoor(relative, shouldOpen);
                                }
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

}
