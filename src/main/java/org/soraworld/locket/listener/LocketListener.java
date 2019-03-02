package org.soraworld.locket.listener;

import org.soraworld.locket.data.Result;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.inject.EventListener;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.util.ChatColor;
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

@EventListener
public class LocketListener {

    @Inject
    private LocketManager manager;

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeBlock(ChangeBlockEvent event) {
        event.filter(manager::notLocked);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent(location -> {
            if (player.hasPermission(manager.defAdminPerm())) return;
            switch (manager.tryAccess(player, location)) {
                case SIGN_USER:
                case SIGN_OWNER:
                case NOT_LOCKED:
                    return;
                case MULTI_OWNERS:
                    manager.sendHint(player, "multiOwners");
                    event.setCancelled(true);
                    return;
                case MULTI_BLOCKS:
                    manager.sendHint(player, "multiBlocks");
                    event.setCancelled(true);
                    return;
                case LOCKED:
                    manager.sendHint(player, "noAccess");
                    event.setCancelled(true);
            }
        });
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(Tristate.UNDEFINED)
    public void onPlayerPlaceBlock(ChangeBlockEvent.Place event, @First Player player) {
        if (!player.hasPermission(manager.defAdminPerm())) {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                transaction.getOriginal().getLocation().ifPresent(location -> {
                    Result result = manager.tryAccess(player, location);
                    if (!result.canUse()) event.setCancelled(true);
                });
                if (event.isCancelled()) return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerBreakBlock(ChangeBlockEvent.Pre event, @ContextValue("PLAYER_BREAK") @First Player player) {
        if (!player.hasPermission(manager.defAdminPerm())) {
            for (Location<World> location : event.getLocations()) {
                Result result = manager.tryAccess(player, location);
                System.out.println(result);
                // TODO BUG 可以直接打掉告示牌
                if (!result.canEdit()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPistonExtend(ChangeBlockEvent.Pre event, @ContextValue("PISTON_EXTEND") @First Object cause) {
        for (Location<World> location : event.getLocations()) {
            if (manager.isLocked(location)) {
                System.out.println("onPistonExtend Cancel:" + location);
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPistonRetract(ChangeBlockEvent.Pre event, @ContextValue("PISTON_RETRACT") @First Object cause) {
        for (Location<World> location : event.getLocations()) {
            if (manager.isLocked(location)) {
                System.out.println("onPistonRetract Cancel:" + location);
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedLocations().removeIf(location -> location != null && manager.isLocked(location));
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(value = Tristate.UNDEFINED)
    public void onInventoryTransfer(ChangeInventoryEvent.Transfer.Pre event) {
        System.out.println("onInventoryTransfer");
        Inventory source = event.getSourceInventory();
        if (source instanceof TileEntityInventory) {
            ((TileEntityInventory<?>) source).getCarrier().ifPresent(carrier -> {
                if (carrier instanceof BlockCarrier && manager.isLocked(carrier.getLocation())) event.setCancelled(true);
            });
            if (event.isCancelled()) return;
        }
        Inventory target = event.getTargetInventory();
        if (target instanceof TileEntityInventory) {
            ((TileEntityInventory<?>) target).getCarrier().ifPresent(carrier -> {
                if (carrier instanceof BlockCarrier && manager.isLocked(carrier.getLocation())) event.setCancelled(true);
            });
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerLockBlock(InteractBlockEvent.Secondary event, @First Player player) {
        ItemStack stack = player.getItemInHand(event.getHandType()).orElse(null);
        if (stack == null || stack.getType() != ItemTypes.SIGN) return;
        if (player.get(Keys.IS_SNEAKING).orElse(false)) return;
        Direction face = event.getTargetSide();
        if (face == Direction.UP || face == Direction.DOWN || face == Direction.NONE) return;
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block == null) return;
        if (!manager.isLockable(block) || block.getRelative(face).getBlockType() != BlockTypes.AIR) return;

        System.out.println("onPlayerLockBlock cancel");
        event.setCancelled(true);

        if (player.hasPermission(manager.defAdminPerm())) {
            manager.placeLock(player, block, face, event.getHandType());
            return;
        }
        if (!player.hasPermission("locket.lock")) {
            manager.sendHint(player, "needPerm", "locket.lock");
            return;
        }
        if (manager.otherProtected(player, block)) {
            manager.sendHint(player, "otherProtect");
            return;
        }
        switch (manager.tryAccess(player, block)) {
            case SIGN_OWNER:
            case NOT_LOCKED:
                manager.placeLock(player, block, face, event.getHandType());
                manager.sendHint(player, "quickLock");
                return;
            case MULTI_OWNERS:
                manager.sendHint(player, "multiOwners");
                return;
            case MULTI_BLOCKS:
                manager.sendHint(player, "multiBlocks");
                return;
            default:
                manager.sendHint(player, "noAccess");
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block != null && block.getBlockType() == BlockTypes.WALL_SIGN) {
            manager.setSelected(player, block);
            manager.sendHint(player, "selectSign");
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerChangeSign(ChangeSignEvent event, @First Player player) {
        SignData data = event.getText();
        String line_0 = ChatColor.stripAllColor(data.get(0).orElse(Text.EMPTY).toPlain()).trim();
        String line_1 = ChatColor.stripAllColor(data.get(1).orElse(Text.EMPTY).toPlain()).trim();
        String line_2 = ChatColor.stripAllColor(data.get(2).orElse(Text.EMPTY).toPlain()).trim();
        String line_3 = ChatColor.stripAllColor(data.get(3).orElse(Text.EMPTY).toPlain()).trim();
        if (manager.isPrivate(line_0)) {
            Sign sign = event.getTargetTile();
            if (player.hasPermission(manager.defAdminPerm())) {
                data.setElement(0, manager.getPrivateText());
                data.setElement(1, manager.getOwnerText(line_1.isEmpty() ? player.getName() : line_1));
                data.setElement(2, manager.getUserText(line_2));
                data.setElement(3, manager.getUserText(line_3));
                sign.offer(data);
                manager.sendHint(player, "manuLock");
                return;
            }
            Location<World> block = LocketManager.getAttached(sign.getLocation());
            if (!manager.isLockable(block)) {
                manager.sendHint(player, "cantLock");
                event.setCancelled(true);
                return;
            }
            if (!player.hasPermission("locket.lock")) {
                manager.sendHint(player, "needPerm", "locket.lock");
                event.setCancelled(true);
                return;
            }
            if (manager.otherProtected(player, block)) {
                manager.sendHint(player, "otherProtect");
                event.setCancelled(true);
                return;
            }
            data.setElement(0, manager.getPrivateText());
            data.setElement(1, manager.getOwnerText(player.getName()));
            data.setElement(2, manager.getUserText(line_2));
            data.setElement(3, manager.getUserText(line_3));
            sign.offer(data);
            manager.sendHint(player, "manuLock");
        }
    }
}
