package org.soraworld.locket.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.soraworld.locket.data.State;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.nms.HandType;
import org.soraworld.locket.nms.Helper;
import org.soraworld.violet.inject.EventListener;
import org.soraworld.violet.inject.Inject;

import java.util.Arrays;

import static org.soraworld.violet.nms.Version.v1_7_R4;

/**
 * @author Himmelt
 */
@EventListener
public class LocketListener implements Listener {

    @Inject
    private LocketManager manager;

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (manager.isPreventExplosion()) {
            event.blockList().removeIf(block -> block != null && manager.isLocked(block));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeBlock(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (manager.isLocked(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeBlock(BlockPistonRetractEvent event) {
        if (v1_7_R4) {
            if (manager.isLocked(event.getRetractLocation().getBlock())) {
                event.setCancelled(true);
            }
        } else {
            for (Block block : event.getBlocks()) {
                if (manager.isLocked(block)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeBlock(BlockFromToEvent event) {
        onChangeBlock(null, event.getBlock(), event);
        onChangeBlock(null, event.getToBlock(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeBlock(BlockBurnEvent event) {
        onChangeBlock(null, event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeBlock(BlockFadeEvent event) {
        onChangeBlock(null, event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeBlock(LeavesDecayEvent event) {
        onChangeBlock(null, event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeBlock(BlockPlaceEvent event) {
        onChangeBlock(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeBlock(BlockBreakEvent event) {
        onChangeBlock(event.getPlayer(), event.getBlock(), event);
    }

    private void onChangeBlock(Player player, Block block, Cancellable event) {
        if (player == null) {
            if (manager.isLocked(block)) {
                event.setCancelled(true);
            }
        } else {
            if (manager.bypassPerm(player)) {
                return;
            }
            if (!manager.tryAccess(player, block, true).canEdit()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Action action = event.getAction();
        if (block != null) {
            if (manager.bypassPerm(player)) {
                return;
            }
            Material type = block.getType();
            switch (manager.tryAccess(player, manager.isWallSign(type) ? LocketManager.getAttached(block) : block, false)) {
                case SIGN_USER:
                    if (action == Action.LEFT_CLICK_BLOCK || manager.isWallSign(type)) {
                        event.setCancelled(true);
                    }
                    break;
                case SIGN_OWNER:
                    if (action == Action.LEFT_CLICK_BLOCK && !manager.isWallSign(type) && player.getGameMode() == GameMode.CREATIVE) {
                        event.setCancelled(true);
                    }
                    break;
                case NOT_LOCKED:
                    break;
                case LOCKED:
                    manager.sendHint(player, "noAccess");
                    event.setCancelled(true);
                    break;
                case MULTI_OWNERS:
                    manager.sendHint(player, "multiOwners");
                    event.setCancelled(true);
                    break;
                case MULTI_BLOCKS:
                    manager.sendHint(player, "multiBlocks");
                    event.setCancelled(true);
                    break;
                case OTHER_PROTECT:
                    manager.sendHint(player, "otherProtect");
                    event.setCancelled(true);
                    break;
                default:
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryTransfer(InventoryMoveItemEvent event) {
        if (manager.isPreventTransfer()) {
            InventoryHolder srcHolder = event.getSource().getHolder();
            InventoryHolder desHolder = event.getDestination().getHolder();
            State source = State.NOT_LOCKED, target = State.NOT_LOCKED;

            if (srcHolder instanceof BlockState) {
                source = manager.checkState(((BlockState) srcHolder).getBlock());
            }
            if (desHolder instanceof BlockState) {
                target = manager.checkState(((BlockState) desHolder).getBlock());
            }

            // 允许情况: 相同所有者 或 都没上锁
            if ((source != State.NOT_LOCKED || target != State.NOT_LOCKED) && !source.sameOwnerTo(target)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLockBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        HandType handType = Helper.getHandType(event);
        ItemStack stack = Helper.getItemInHand(player.getInventory(), handType);
        if (stack == null || !manager.isSign(stack.getType())) {
            return;
        }
        if (player.isSneaking()) {
            return;
        }
        BlockFace face = event.getBlockFace();
        if (face != BlockFace.NORTH && face != BlockFace.EAST && face != BlockFace.SOUTH && face != BlockFace.WEST) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!manager.isLockable(block) || !manager.canPlaceLock(block.getRelative(face).getType())) {
            return;
        }

        event.setCancelled(true);

        if (!manager.bypassPerm(player)) {
            if (!manager.hasPermission(player, "locket.lock")) {
                manager.sendHint(player, "needPerm", manager.mappingPerm("locket.lock"));
                return;
            }
            if (manager.otherProtected(player, block)) {
                manager.sendHint(player, "otherProtect");
                return;
            }
        }

        switch (manager.tryAccess(player, block, true)) {
            case SIGN_OWNER:
            case NOT_LOCKED:
                manager.placeLock(player, block, face, handType, stack.getType());
                return;
            case MULTI_OWNERS:
                manager.sendHint(player, "multiOwners");
                return;
            case MULTI_BLOCKS:
                manager.sendHint(player, "multiBlocks");
                return;
            case OTHER_PROTECT:
                manager.sendHint(player, "otherProtect");
                return;
            default:
                manager.sendHint(player, "noAccess");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeSign(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        Player temp = player;
        if (manager.isPrivate(lines[0])) {
            if (!lines[1].isEmpty() && !lines[1].equals(player.getName()) && manager.bypassPerm(player)) {
                Player user = Bukkit.getPlayer(lines[1]);
                if (user != null) {
                    temp = user;
                } else {
                    event.setLine(0, "");
                    event.setLine(1, "");
                    manager.sendKey(player, "invalidUsername", lines[1]);
                    return;
                }
            }
            if (!manager.bypassPerm(player)) {
                Block block = LocketManager.getAttached(event.getBlock());
                if (!manager.isLockable(block)) {
                    manager.sendHint(player, "notLockable");
                    event.setCancelled(true);
                    return;
                }
                if (!manager.hasPermission(player, "locket.lock")) {
                    manager.sendHint(player, "needPerm", manager.mappingPerm("locket.lock"));
                    event.setCancelled(true);
                    return;
                }
                if (manager.otherProtected(player, block)) {
                    manager.sendHint(player, "otherProtect");
                    event.setCancelled(true);
                    return;
                }
            }
            Player owner = temp;
            Bukkit.getScheduler().runTask(manager.getPlugin(), () -> Helper.touchSign(event.getBlock(), data -> {
                data.lines[0] = manager.getPrivateText();
                data.lines[1] = manager.getOwnerText(owner);
                data.lines[2] = manager.getUserText(lines[2]);
                data.lines[3] = manager.getUserText(lines[3]);
                return true;
            }));
            manager.asyncUpdateSign(event.getBlock());
            manager.sendHint(player, "manuLock");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSelectSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null && manager.isWallSign(block.getType())) {
            manager.setSelected(player, block.getLocation());
            manager.asyncUpdateSign(block);
            manager.sendHint(player, "selectSign");
        }
    }

    @EventHandler
    public void onLoadChunk(ChunkLoadEvent event) {
        Arrays.stream(event.getChunk().getTileEntities()).filter(tile -> tile instanceof Sign).forEach(tile -> manager.asyncUpdateSign(tile.getBlock()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.clearSelected(event.getPlayer().getUniqueId());
    }
}
