package org.soraworld.locket.listener;

/* Created by Himmelt on 2016/7/16.*/

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.Utils;

public class BlockEventListener implements Listener {

    // Protect block from interfere block
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptPlaceInterfereBlocks(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (player.hasPermission("locket.admin.interfere")) return;
        if (LocketAPI.mayInterfere(block, player)) {
            Utils.sendMessages(player, Config.getLang("cannot-interfere-with-others"));
            event.setCancelled(true);
        }
    }

    // Tell player about locket
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlaceFirstBlockNotify(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!player.hasPermission("locket.lock")) return;
        if (Utils.shouldNotify(player) && Config.isLockable(block.getType())) {
            Utils.sendMessages(player, Config.getLang("you-can-quick-lock-it"));
        }
    }

    // Player break sign
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptBreakSign(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (player.hasPermission("locket.admin.break")) return;
        if (LocketAPI.isLockSign(block)) {
            if (LocketAPI.isOwnerOfSign(block, player)) {
                Utils.sendMessages(player, Config.getLang("break-own-lock-sign"));
                // Remove additional signs?
            } else {
                Utils.sendMessages(player, Config.getLang("cannot-break-this-lock-sign"));
                event.setCancelled(true);
            }
        } else if (LocketAPI.isAdditionalSign(block)) {
            if (LocketAPI.isOwnerOfSign(block, player)) {
                Utils.sendMessages(player, Config.getLang("break-own-additional-sign"));
            } else if (!LocketAPI.isProtected(LocketAPI.getAttachedBlock(block))) {
                Utils.sendMessages(player, Config.getLang("break-redundant-additional-sign"));
            } else {
                Utils.sendMessages(player, Config.getLang("cannot-break-this-additional-sign"));
                event.setCancelled(true);
            }
        }
    }

    // Protect block from being destroyed
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptBreakLockedBlocks(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (LocketAPI.isLocked(block) || LocketAPI.isUpDownLockedDoor(block)) {
            Utils.sendMessages(player, Config.getLang("block-is-locked"));
            event.setCancelled(true);
        }
    }

    // 活塞推出事件
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (LocketAPI.isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 活塞收回事件
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Block block = event.getRetractLocation().getBlock();
        if (LocketAPI.isProtected(block)) {
            event.setCancelled(true);
        }
    }

    // Prevent redstone current open doors
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (LocketAPI.isProtected(event.getBlock())) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    // Manual protection
    @EventHandler(priority = EventPriority.NORMAL)
    public void onManualLock(SignChangeEvent event) {
        if (event.getBlock().getType() != Material.WALL_SIGN) return;
        String topline = event.getLine(0);
        Player player = event.getPlayer();
        if (!player.hasPermission("locket.lock")) {
            if (LocketAPI.isLockString(topline) || LocketAPI.isAdditionalString(topline)) {
                event.setLine(0, Config.getLang("sign-error"));
                Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
            }
            return;
        }
        if (LocketAPI.isLockString(topline) || LocketAPI.isAdditionalString(topline)) {
            Block block = LocketAPI.getAttachedBlock(event.getBlock());
            if (LocketAPI.isLockable(block)) {
                // 检查其他插件保护
                if (Depend.isProtectedFrom(block, player)) {
                    event.setLine(0, Config.getLang("sign-error"));
                    Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
                    return;
                }
                boolean locked = LocketAPI.isLocked(block);
                if (!locked && !LocketAPI.isUpDownLockedDoor(block)) {
                    if (LocketAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("locked-manual"));
                        if (!player.hasPermission("locket.lockothers")) {
                            // Player with permission can lock with another name
                            event.setLine(1, player.getName());
                        }
                    } else {
                        Utils.sendMessages(player, Config.getLang("not-locked-yet-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    }
                } else if (!locked && LocketAPI.isOwnerUpDownLockedDoor(block, player)) {
                    if (LocketAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("cannot-lock-door-nearby-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    } else {
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-manual"));
                    }
                } else if (LocketAPI.isOwner(block, player)) {
                    if (LocketAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("block-already-locked-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    } else {
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-manual"));
                    }
                } else {
                    // Not possible to fall here except override
                    Utils.sendMessages(player, Config.getLang("block-already-locked-manual"));
                    event.getBlock().breakNaturally();
                }
            } else {
                Utils.sendMessages(player, Config.getLang("block-is-not-lockable"));
                event.setLine(0, Config.getLang("sign-error"));
            }
        }
    }

}
