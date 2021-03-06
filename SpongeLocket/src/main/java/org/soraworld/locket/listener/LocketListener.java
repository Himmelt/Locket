package org.soraworld.locket.listener;

import org.soraworld.locket.data.State;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.inject.EventListener;
import org.soraworld.violet.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author Himmelt
 */
@EventListener
public class LocketListener {

    @Inject
    private LocketManager manager;

    @Listener(order = Order.PRE, beforeModifications = true)
    public void onChangeBlock(ChangeBlockEvent event) {
        Player player = event.getCause().first(Player.class).orElse(null);
        if (player == null) {
            event.filter(manager::notLocked);
            event.getTransactions().forEach(transaction -> {
                BlockState origin = transaction.getOriginal().getState();
                BlockState finals = transaction.getFinal().getState();
                if (origin.getType() == BlockTypes.FURNACE && finals.getType() == BlockTypes.LIT_FURNACE) {
                    transaction.setValid(true);
                } else if (origin.getType() == BlockTypes.LIT_FURNACE && finals.getType() == BlockTypes.FURNACE) {
                    transaction.setValid(true);
                }
            });
        } else if (!manager.bypassPerm(player)) {
            event.filter(location -> manager.tryAccess(player, location, true).canEdit());
        }
    }

    @Listener(order = Order.PRE, beforeModifications = true)
    public void onExplosion(ExplosionEvent.Detonate event) {
        if (manager.isPreventExplosion()) {
            event.getAffectedLocations().removeIf(location -> location != null && manager.isLocked(location));
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeBlockPre(ChangeBlockEvent.Pre event) {
        Player player = event.getCause().first(Player.class).orElse(null);
        for (Location<World> location : event.getLocations()) {
            if (location != null) {
                if (player == null) {
                    if (manager.isLocked(location)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (manager.bypassPerm(player)) {
                        return;
                    }
                    if (!manager.tryAccess(player, location, true).canEdit()) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent(location -> {
            if (manager.bypassPerm(player)) {
                return;
            }
            BlockType type = location.getBlockType();
            switch (manager.tryAccess(player, manager.isWallSign(type) ? LocketManager.getAttached(location) : location, false)) {
                case SIGN_USER:
                    if (event instanceof InteractBlockEvent.Primary || manager.isWallSign(type)) {
                        event.setCancelled(true);
                    }
                    break;
                case SIGN_OWNER:
                    if (event instanceof InteractBlockEvent.Primary && !manager.isWallSign(type) && player.gameMode().get().equals(GameModes.CREATIVE)) {
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
        });
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(value = Tristate.UNDEFINED)
    public void onInventoryTransfer(ChangeInventoryEvent.Transfer.Pre event) {
        if (manager.isPreventTransfer()) {
            Inventory sourceInv = event.getSourceInventory();
            Inventory targetInv = event.getTargetInventory();
            State source = State.NOT_LOCKED, target = State.NOT_LOCKED;

            if (sourceInv instanceof TileEntityInventory) {
                Carrier carrier = ((TileEntityInventory<?>) sourceInv).getCarrier().orElse(null);
                if (carrier instanceof BlockCarrier) {
                    source = manager.checkState(((BlockCarrier) carrier).getLocation());
                }
            }
            if (targetInv instanceof TileEntityInventory) {
                Carrier carrier = ((TileEntityInventory<?>) targetInv).getCarrier().orElse(null);
                if (carrier instanceof BlockCarrier) {
                    target = manager.checkState(((BlockCarrier) carrier).getLocation());
                }
            }

            // 允许情况: 相同所有者 或 都没上锁
            if ((source != State.NOT_LOCKED || target != State.NOT_LOCKED) && !source.sameOwnerTo(target)) {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerLockBlock(InteractBlockEvent.Secondary event, @First Player player) {
        ItemStack stack = player.getItemInHand(event.getHandType()).orElse(null);
        if (stack == null) {
            return;
        }
        ItemType signType = stack.getType();
        if (!manager.isSign(signType)) {
            return;
        }
        if (player.get(Keys.IS_SNEAKING).orElse(false)) {
            return;
        }
        Direction face = event.getTargetSide();
        if (face != Direction.NORTH && face != Direction.EAST && face != Direction.SOUTH && face != Direction.WEST) {
            return;
        }
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block == null) {
            return;
        }
        if (!manager.isLockable(block) || !manager.canPlaceLock(block.getRelative(face).getBlockType())) {
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
                manager.placeLock(player, block, face, event.getHandType(), signType);
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

    @Listener(order = Order.LAST)
    public void onPlayerChangeSign(ChangeSignEvent event, @First Player player) {
        SignData data = event.getText();
        ListValue<Text> lines = data.lines();
        Player owner = player;
        if (manager.isPrivate(lines.get(0).toPlain())) {
            String line1 = lines.get(1).toPlain().trim();
            if (!line1.isEmpty() && !line1.equals(player.getName()) && manager.bypassPerm(player)) {
                Player user = Sponge.getServer().getPlayer(line1).orElse(null);
                if (user != null) {
                    owner = user;
                } else {
                    data.setElement(0, Text.EMPTY);
                    data.setElement(1, Text.EMPTY);
                    manager.sendKey(player, "invalidUsername", line1);
                    return;
                }
            }
            if (!manager.bypassPerm(player)) {
                Location<World> block = LocketManager.getAttached(event.getTargetTile().getLocation());
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

            data.setElement(0, manager.getPrivateText());
            data.setElement(1, manager.getOwnerText(owner));
            data.setElement(2, manager.getUserText(lines.get(2).toPlain().trim()));
            data.setElement(3, manager.getUserText(lines.get(3).toPlain().trim()));
            manager.sendHint(player, "manuLock");
            manager.asyncUpdateSign(event.getTargetTile());
        }
    }

    @Listener(order = Order.POST)
    public void onPlayerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block != null && manager.isWallSign(block.getBlockType())) {
            manager.setSelected(player, block);
            block.getTileEntity().ifPresent(sign -> manager.asyncUpdateSign((Sign) sign));
            manager.sendHint(player, "selectSign");
        }
    }

    @Listener
    public void onLoadChunk(LoadChunkEvent event) {
        event.getTargetChunk().getTileEntities(tile -> tile instanceof Sign).forEach(sign -> manager.asyncUpdateSign((Sign) sign));
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        manager.clearSelected(event.getTargetEntity().getUniqueId());
    }
}
