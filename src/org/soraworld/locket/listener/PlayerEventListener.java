package org.soraworld.locket.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Openable;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class PlayerEventListener implements Listener {

    // Quick protect for chests
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuickLockChest(PlayerInteractEvent event) {

        if (event.isCancelled()) return;
        Action action = event.getAction();
        Player player = event.getPlayer();
        // 右键手持木牌
        if (action == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.SIGN) {
            // 潜行方式,取消
            if (player.isSneaking()) return;
            // 如果玩家没有使用权限,取消
            if (!player.hasPermission("locket.lock")) return;
            BlockFace blockface = event.getBlockFace();
            // 木牌只能贴在四个侧面
            if (blockface == BlockFace.NORTH || blockface == BlockFace.WEST || blockface == BlockFace.EAST || blockface == BlockFace.SOUTH) {
                Block block = event.getClickedBlock();
                // 检查是否被其他插件保护,若不被该玩家保护则取消
                if (Depend.isProtectedFrom(block, player)) return;
                // 检查贴牌子的一面是否被占用
                if (block.getRelative(blockface).getType() != Material.AIR) return;
                // 在被保护方块列表内
                if (LocketAPI.isLockable(block)) {
                    boolean locked = LocketAPI.isLocked(block);
                    // 取消交互事件
                    event.setCancelled(true);
                    if (!locked && !LocketAPI.isUpDownLockedDoor(block)) {
                        // 拿掉玩家一个木牌
                        Utils.removeASign(player);
                        // 显示消息
                        Utils.sendMessages(player, Config.getLang("locked-quick"));
                        Utils.putSignOn(block, blockface, Config.getDefaultPrivateString(), player.getName());
                    } else if (!locked && LocketAPI.isOwnerUpDownLockedDoor(block, player)) {
                        Utils.removeASign(player);
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                        Utils.putSignOn(block, blockface, Config.getDefaultMoreString(), "");
                    } else if (LocketAPI.isOwner(block, player)) {
                        Utils.removeASign(player);
                        Utils.putSignOn(block, blockface, Config.getDefaultMoreString(), "");
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                    } else {
                        Utils.sendMessages(player, Config.getLang("cannot-lock-quick"));
                    }
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
