package org.soraworld.locket.listener;

import org.soraworld.locket.manager.LocketManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class EventListener {

    private final LocketManager locket;

    public EventListener(LocketManager locket) {
        this.locket = locket;
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockChange(ChangeBlockEvent event) {
        event.filter(locket::isLocked);
    }

    /**
     * 玩家方块交互事件(主要行为保护).
     *
     * @param event  事件
     * @param player 玩家
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block == null || player.hasPermission(locket.defAdminPerm())) return;
        switch (locket.tryAccess(player, block)) {
            case SIGN_USER:
            case SIGN_OWNER:
            case SIGN_NOT_LOCK:
                return;
            case SIGN_M_OWNERS:
                locket.sendKey(player, "multiOwners");
                event.setCancelled(true);
                return;
            case M_BLOCKS:
                locket.sendKey(player, "multiBlocks");
                event.setCancelled(true);
                return;
            case SIGN_NO_ACCESS:
                locket.sendKey(player, "noAccess");
                event.setCancelled(true);
        }
    }

    /**
     * 玩家放置方块事件.
     *
     * @param event  事件
     * @param player 玩家
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerPlaceBlock(ChangeBlockEvent.Place event, @First Player player) {
        if (!player.hasPermission(locket.defAdminPerm())) {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                Location<World> block = transaction.getOriginal().getLocation().orElse(null);
                if (!locket.tryAccess(player, block).canUse()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * 玩家破坏方块事件.
     *
     * @param event  事件
     * @param player 玩家
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerBreakBlock(ChangeBlockEvent.Pre event, @ContextValue("PLAYER_BREAK") @First Player player) {
        if (!player.hasPermission(locket.defAdminPerm())) {
            for (Location<World> location : event.getLocations()) {
                if (!locket.tryAccess(player, location).canUse()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * 活塞推出事件.
     *
     * @param event 事件
     * @param cause 原因
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPistonExtend(ChangeBlockEvent.Pre event, @ContextValue("PISTON_EXTEND") @First Object cause) {
        for (Location<World> location : event.getLocations()) {
            if (!locket.tryAccess(null, location).canUse()) {
                System.out.println("onPistonExtend Cancel:" + location);
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * 活塞收回事件.
     *
     * @param event 事件
     * @param cause 原因
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPistonRetract(ChangeBlockEvent.Pre event, @ContextValue("PISTON_RETRACT") @First Object cause) {
        for (Location<World> location : event.getLocations()) {
            if (!locket.tryAccess(null, location).canUse()) {
                System.out.println("onPistonRetract Cancel");
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * TODO 环境生长事件.
     *
     * @param event 事件
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onStructureGrow(ChangeBlockEvent.Grow event) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Location<World> block = transaction.getOriginal().getLocation().orElse(null);
            if (!locket.tryAccess(null, block).canUse()) {
                System.out.println("onStructureGrow Cancel");
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * TODO 方块变化事件.
     *
     * @param event 事件
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onModify(ChangeBlockEvent.Modify event) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Location<World> block = transaction.getOriginal().getLocation().orElse(null);
            if (!locket.tryAccess(null, block).canUse()) {
                System.out.println("onModify Cancel");
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * TODO 爆炸保护.
     *
     * @param event 事件
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedLocations().removeIf(location -> location != null && locket.isLocked(location));
    }

    /**
     * TODO 容器传输事件(取消依然监控).
     *
     * @param event 事件
     */
    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(value = Tristate.UNDEFINED)
    public void onInventoryTransfer(ChangeInventoryEvent.Transfer.Pre event) {
        System.out.println("onInventoryTransfer");
        Inventory source = event.getSourceInventory();
        if (source instanceof TileEntityInventory) {
            Object carrier = ((TileEntityInventory) source).getCarrier().orElse(null);
            if (carrier instanceof BlockCarrier) {
                if (locket.isLocked(((BlockCarrier) carrier).getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        Inventory target = event.getTargetInventory();
        if (target instanceof TileEntityInventory) {
            Object carrier = ((TileEntityInventory) target).getCarrier().orElse(null);
            if (carrier instanceof BlockCarrier) {
                if (locket.isLocked(((BlockCarrier) carrier).getLocation())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * 右键锁箱子.
     *
     * @param event  事件
     * @param player 玩家
     */
    @Listener(order = Order.LAST)
    public void onPlayerLockBlock(InteractBlockEvent.Secondary event, @First Player player) {
        ItemStack stack = player.getItemInHand(event.getHandType()).orElse(null);
        if (stack == null || stack.getType() != ItemTypes.SIGN) return;
        if (player.get(Keys.IS_SNEAKING).orElse(false)) return;
        Direction face = event.getTargetSide();
        if (face == Direction.UP || face == Direction.DOWN || face == Direction.NONE) return;
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block == null) return;
        if (!locket.isLockable(block) || block.getRelative(face).getBlockType() != BlockTypes.AIR) return;

        System.out.println("onPlayerLockBlock cancel");
        event.setCancelled(true);

        if (player.hasPermission(locket.defAdminPerm())) {
            locket.placeLock(player, block, face, event.getHandType());
            return;
        }
        if (!player.hasPermission("locket.lock")) {
            locket.sendKey(player, "needPerm", "locket.lock");
            return;
        }
        if (locket.otherProtected(player, block)) {
            locket.sendKey(player, "otherProtect");
            return;
        }
        switch (locket.tryAccess(player, block)) {
            case SIGN_OWNER:
            case SIGN_NOT_LOCK:
                locket.placeLock(player, block, face, event.getHandType());
                locket.sendKey(player, "quickLock");
                return;
            case SIGN_M_OWNERS:
                locket.sendKey(player, "multiOwners");
                return;
            case M_BLOCKS:
                locket.sendKey(player, "multiBlocks");
                return;
            default:
                locket.sendKey(player, "noAccess");
        }
    }

    /**
     * 右键选择告示牌.
     *
     * @param event  事件
     * @param player 玩家
     */
    @Listener(order = Order.LAST)
    public void onPlayerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block != null && block.getBlockType() == BlockTypes.WALL_SIGN) {
            locket.setSelected(player, block);
            locket.sendKey(player, "selectSign");
        }
    }

    /**
     * 玩家修改告示牌事件.
     *
     * @param event  事件
     * @param player 玩家
     */
    @Listener(order = Order.LAST)
    public void onPlayerChangeSign(ChangeSignEvent event, @First Player player) {
        SignData data = event.getText();
        String line_0 = data.get(0).orElse(Text.EMPTY).toPlain();
        String line_1 = data.get(1).orElse(Text.EMPTY).toPlain();
        String line_2 = data.get(2).orElse(Text.EMPTY).toPlain();
        String line_3 = data.get(3).orElse(Text.EMPTY).toPlain();
        if (locket.isPrivate(line_0)) {
            Sign sign = event.getTargetTile();
            Location<World> block = LocketManager.getAttached(sign.getLocation());
            if (player.hasPermission(locket.defAdminPerm())) {
                data.setElement(0, locket.getPrivateText());
                data.setElement(1, locket.getOwnerText(line_1.isEmpty() ? player.getName() : line_1));
                data.setElement(2, locket.getUserText(line_2));
                data.setElement(3, locket.getUserText(line_3));
                sign.offer(data);
                return;
            }
            if (!locket.isLockable(block)) {
                locket.sendKey(player, "cantLock");
                event.setCancelled(true);
                return;
            }
            if (!player.hasPermission("locket.lock")) {
                locket.sendKey(player, "needPerm", "locket.lock");
                event.setCancelled(true);
                return;
            }
            if (locket.otherProtected(player, block)) {
                locket.sendKey(player, "otherProtect");
                event.setCancelled(true);
                return;
            }
            data.setElement(0, locket.getPrivateText());
            data.setElement(1, locket.getOwnerText(player.getName()));
            // TODO check format
            data.setElement(2, Text.of(line_2));
            data.setElement(3, Text.of(line_3));
            sign.offer(data);
        }
    }

    /**
     * 玩家登出.
     *
     * @param event  事件
     * @param player 玩家
     */
    @Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect event, @First Player player) {
        locket.cleanPlayer(player);
    }
}
