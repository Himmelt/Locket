package org.soraworld.locket.listener;

import org.soraworld.locket.Locket;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.constant.Permissions;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Hinges;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.minecart.HopperMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.explosive.TargetExplosiveEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class EventListener {

    // FIRST 监听

    // 玩家方块交互事件(主要行为保护)
    @Listener(order = Order.FIRST)
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        Location location = event.getTargetBlock().getLocation().orElse(null);
        if (((LocketAPI.isLocked(location) && !LocketAPI.isUser(location, player)))
                && !player.hasPermission("locket.admin.use")) {
            Utils.sendMessages(player, Config.getLang("block-is-locked"));
            event.setCancelled(true);
            return;
        }
        if (event instanceof InteractBlockEvent.Secondary && LocketAPI.isLocked(location)) {
            Location doorBlock = LocketAPI.getBottomDoorBlock(location);
            boolean shouldOpen = !(doorBlock.getBlock().get(Keys.HINGE_POSITION).get() == Hinges.LEFT); // Move to here
            List<Location> doors = new ArrayList<>();
            doors.add(doorBlock);
            if (doorBlock.getBlockType() == BlockTypes.IRON_DOOR) {
                LocketAPI.toggleDoor(doorBlock, shouldOpen);
            }
            for (Direction blockface : Constants.FACES) {
                Location relative = doorBlock.getRelative(blockface);
                if (relative.getBlockType() == doorBlock.getBlockType()) {
                    doors.add(relative);
                    LocketAPI.toggleDoor(relative, shouldOpen);
                }
            }
        }
    }

    // 玩家放置方块事件
    @Listener(order = Order.FIRST)
    public void onPlayerPlaceBlock(ChangeBlockEvent.Place event, @First Player player) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        if (!transactions.isEmpty()) {
            BlockSnapshot origin = transactions.get(0).getOriginal();
            BlockSnapshot target = transactions.get(0).getFinal();
            if (player.hasPermission("locket.admin.interfere")) return;
          /*  if (LocketAPI.mayInterfere(origin, player)) {
                player.sendMessage(Text.of("cannot-interfere-with-others"));
                event.setCancelled(true);
            }*/
        }
    }

    // 玩家破坏方块事件
    @Listener(order = Order.FIRST)
    public void onPlayerBreakBlock(ChangeBlockEvent.Break event, @First Player player) {
        Location<World> block = event.getTransactions().get(0).getOriginal().getLocation().get();
        if (player.hasPermission("locket.admin.break")) return;
        if (LocketAPI.isPrivateSign(block)) {
            if (LocketAPI.isOwnerOfSign(block, player)) {
                Utils.sendMessages(player, Config.getLang("break-own-lock-sign"));
                // Remove additional signs?
            } else {
                Utils.sendMessages(player, Config.getLang("cannot-break-this-lock-sign"));
                event.setCancelled(true);
            }
        } else if (LocketAPI.isMoreSigned(block)) {
            if (LocketAPI.isOwnerOfSign(block, player)) {
                Utils.sendMessages(player, Config.getLang("break-own-additional-sign"));
            } else if (!LocketAPI.isProtected(LocketAPI.getAttachedBlock(block))) {
                Utils.sendMessages(player, Config.getLang("break-redundant-additional-sign"));
            } else {
                Utils.sendMessages(player, Config.getLang("cannot-break-this-additional-sign"));
                event.setCancelled(true);
            }
        }
        if (LocketAPI.isLocked(block)) {
            Utils.sendMessages(player, Config.getLang("block-is-locked"));
            event.setCancelled(true);
        }
    }

    // 活塞推出事件
    @Listener(order = Order.FIRST)
    public void onPistonExtend(@Named(NamedCause.PISTON_EXTEND) ChangeBlockEvent.Pre event) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 活塞收回事件
    @Listener(order = Order.FIRST)
    public void onPistonRetract(@Named(NamedCause.PISTON_RETRACT) ChangeBlockEvent.Pre event) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 环境生长事件
    @Listener(order = Order.FIRST)
    public void onStructureGrow(ChangeBlockEvent.Grow event) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (LocketAPI.isProtected(transaction.getOriginal().getLocation().get())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 实体爆炸事件
    @Listener(order = Order.FIRST)
    public void onEntityExplode(TargetExplosiveEvent event) {
        if (!Config.isExplosionProtection()) return;
        /*Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Location<World> block = it.next();
            if (LocketAPI.isProtected(block)) it.remove();
        }*/
    }

    // 容器传输事件(取消依然监控)
    @Listener(order = Order.FIRST)
    @IsCancelled(value = Tristate.UNDEFINED)
    public void onInventoryMove(ChangeInventoryEvent.Transfer event) {
        if (Config.isItemTransferOutBlocked() || Config.getHopperMinecartAction()) {
            if (isInventoryLocked(event.getCause().first(HopperMinecart.class).get())) {
                if (Config.isItemTransferOutBlocked()) {
                    event.setCancelled(true);
                }
                // Additional Hopper Minecart Check
                if (event.getTargetInventory() instanceof HopperMinecart) {
                    boolean hopperMinecartAction = Config.getHopperMinecartAction();
                    if (hopperMinecartAction) {
                        // case 0 - Impossible
                        // Cancel only, it is not called if !Config.isItemTransferOutBlocked()
                        event.setCancelled(true);
                    }
                }
                return;
            }
        }
        if (Config.isItemTransferInBlocked()) {
            if (isInventoryLocked(event.getTargetInventory())) {
                event.setCancelled(true);
            }
        }
    }

    public boolean isInventoryLocked(Inventory inventory) {
        InventoryArchetype archetype = inventory.getArchetype();
        if (archetype == InventoryArchetypes.DOUBLE_CHEST) {
            inventory = inventory;
        }
        if (inventory instanceof CarriedInventory) {
            CarriedInventory carriedInventory = (CarriedInventory) inventory;
            if (carriedInventory.getCarrier().get() instanceof TileEntityCarrier) {
                TileEntityCarrier carrier = (TileEntityCarrier) carriedInventory.getCarrier().get();
                Location block = carrier.getLocation();
                return LocketAPI.isLocked(block);
            }
        }
        return false;
    }

    // LAST 监听

    // 右键锁箱子
    @Listener(order = Order.LAST)
    public void onPlayerLockBlock(InteractBlockEvent.Secondary event, @First Player player) {
        if (player.get(Keys.IS_SNEAKING).orElse(false)) return;
        Direction face = event.getTargetSide();
        if (!face.isCardinal()) return;
        ItemStack stack = player.getItemInHand(event.getHandType()).orElse(null);
        if (stack == null || ItemTypes.SIGN != stack.getItem()) return;
        event.setCancelled(true);
        if (!player.hasPermission(Permissions.LOCK)) {
            Utils.sendChat(player, "&a&lYou need permission &b[" + Permissions.LOCK + "]&a to do this!");
            return;
        }
        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if (location == null || !LocketAPI.isLockable(location)) {
            //Utils.sendChat(player, "&c&lYou can't lock this block from this side!");
            Locket.getLocket().getPlugin().getLogger().info("方块不存在或不属于可保护范围!");
            return;
        }
        if (location.getRelative(face).getBlockType() != BlockTypes.AIR) {
            Utils.sendChat(player, "&c&l此面空间被占用,无法放置保护锁!");
            return;
        }
        if (Depend.isProtectedFrom(location, player)) {
            Utils.sendChat(player, "&c&l方块被其他插件保护,你没有操作权限!");
            return;
        }

        if (Utils.canLock(player, location)) {
            Utils.placeLockSign(player, location, face);
            Utils.removeOne(player, event.getHandType());
        } else {
            Utils.sendChat(player, "你无法对此方块上锁!");
        }
    }

    // 右键选择告示牌
    @Listener(order = Order.LATE)
    public void onPlayerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        if (!player.hasPermission(Permissions.EDIT)) return;
        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if (location == null) return;
        BlockType type = location.getBlockType();
        if (type == BlockTypes.WALL_SIGN) {
            LocketAPI.getPlayer(player).setSelected(location);
            player.sendMessage(Text.of("sign-selected"));
        }
    }

    // 玩家修改告示牌事件
    @Listener(order = Order.LAST)
    public void onPlayerChangeSign(ChangeSignEvent event, @First Player player) {
        String topline = event.getText().get(0).get().toPlain();
        if (!player.hasPermission("locket.lock")) {
            if (LocketAPI.isPrivate(topline) || LocketAPI.isMoreString(topline)) {
                event.getText().setElement(0, Text.of(Config.getLang("sign-error")));
                Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
            }
            return;
        }
        if (LocketAPI.isPrivate(topline) || LocketAPI.isMoreString(topline)) {
            Location<World> block = LocketAPI.getAttachedBlock(event.getTargetTile().getLocation());
            if (LocketAPI.isLockable(block)) {
                // 检查其他插件保护
                if (Depend.isProtectedFrom(block, player)) {
                    event.getText().setElement(0, Text.of(Config.getLang("sign-error")));
                    Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
                    return;
                }
                boolean locked = LocketAPI.isLocked(block);
                if (!locked) {
                    if (LocketAPI.isPrivate(topline)) {
                        Utils.sendMessages(player, Config.getLang("locked-manual"));
                        if (!player.hasPermission("locket.lockothers")) {
                            // IPlayer with permission can lock with another name
                            event.getText().setElement(1, Text.of(player.getName()));
                        }
                    } else {
                        Utils.sendMessages(player, Config.getLang("not-locked-yet-manual"));
                        event.getText().setElement(0, Text.of(Config.getLang("sign-error")));
                    }
                } else if (LocketAPI.isOwner(block, player)) {
                    if (LocketAPI.isPrivate(topline)) {
                        Utils.sendMessages(player, Config.getLang("block-already-locked-manual"));
                        event.getText().setElement(0, Text.of(Config.getLang("sign-error")));
                    } else {
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-manual"));
                    }
                } else {
                    // Not possible to fall here except override
                    Utils.sendMessages(player, Config.getLang("block-already-locked-manual"));
                    //event.getTargetTile().getLocation().setBlockType()getBlock().breakNaturally();
                }
            } else {
                Utils.sendMessages(player, Config.getLang("block-is-not-lockable"));
                event.getText().setElement(0, Text.of(Config.getLang("sign-error")));
            }
        }
    }

    // 普通监听

    // 玩家登出
    @Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect event, @First Player player) {
        Utils.removeSelectedSign(player);
    }

}
