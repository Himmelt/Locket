package org.soraworld.locket.listener;

import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.constant.Perms;
import org.soraworld.locket.constant.Result;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class SpongeEventListener {

    // 玩家方块交互事件(主要行为保护)
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        if (player.hasPermission(Perms.ADMIN_INTERACT)) return;
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block == null) return;
        IPlayer iPlayer = LocketAPI.getPlayer(player);
        switch (iPlayer.tryAccess(block)) {
            case SIGN_USER:
                iPlayer.sendChat("你是使用者");
                return;
            case SIGN_OWNER:
                iPlayer.sendChat("你是所有者");
                return;
            case SIGN_NOT_LOCK:
                iPlayer.sendChat("方块没有上锁");
                return;
            case SIGN_M_OWNERS:
                iPlayer.sendChat("这个方块有多个所有者,这是不允许的,请联系管理员!");
                event.setCancelled(true);
                return;
            case SIGN_NO_ACCESS:
                iPlayer.sendChat("你没有进行此操作的权限");
                event.setCancelled(true);
                return;
            case M_CHESTS:
                iPlayer.sendChat("这是一个多重箱子,这是不允许的,请联系管理员!");
                event.setCancelled(true);
        }
    }

    // 玩家放置方块事件
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerPlaceBlock(ChangeBlockEvent.Pre event, @Named(NamedCause.PLAYER_PLACE) Object world, @First Player player) {
        if (player.hasPermission(Perms.ADMIN_INTERFERE)) return;
        IPlayer iPlayer = LocketAPI.getPlayer(player);
        event.getLocations().forEach(location -> {
            if (player.hasPermission(Perms.INTERFERE)) {
                event.setCancelled(true);
                iPlayer.sendChat("你不能在此处放置可能影响他人保护锁的方块!");
            }
        });
    }

    // 玩家破坏方块事件
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerBreakBlock(ChangeBlockEvent.Pre event, @Named(NamedCause.PLAYER_BREAK) Object world, @First Player player) {
        IPlayer iPlayer = LocketAPI.getPlayer(player);
        boolean admin_unlock = player.hasPermission(Perms.ADMIN_UNLOCK);
        boolean admin_break = player.hasPermission(Perms.ADMIN_BREAK);
        for (Location<World> block : event.getLocations()) {
            if (iPlayer.analyzeSign(block) != Result.SIGN_NOT_LOCK) {
                if (!admin_unlock) event.setCancelled(true);
                return;
            }

            if (admin_break) return;
            switch (iPlayer.tryAccess(block)) {
                case SIGN_USER:
                    event.setCancelled(true);
                    iPlayer.sendChat("你是用户,不能破坏此方块(s)!");
                    return;
                case SIGN_OWNER:
                    event.setCancelled(true);
                    iPlayer.sendChat("你是所有者,请使用/locket unlock 解锁!");
                    return;
                case SIGN_NOT_LOCK:
                    iPlayer.sendChat("未上锁,继续!");
                    continue;
                case SIGN_M_OWNERS:
                case SIGN_NO_ACCESS:
                case M_CHESTS:
                    event.setCancelled(true);
                    iPlayer.sendChat("你不能破坏此方块(s)!");
                    return;
            }
        }
    }

    // 活塞推出事件
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPistonExtend(ChangeBlockEvent.Pre event, @Named(NamedCause.PISTON_EXTEND) Object world) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isLocked(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 活塞收回事件
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPistonRetract(ChangeBlockEvent.Pre event, @Named(NamedCause.PISTON_RETRACT) Object world) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isLocked(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 环境生长事件
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onStructureGrow(ChangeBlockEvent.Grow event) {
        System.out.println("===== onStructureGrow =====");
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            if (location != null && LocketAPI.isLocked(location)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onModify(ChangeBlockEvent.Modify event) {
        System.out.println("===== onModify =====");
        System.out.println(event.getClass());
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            if (location != null && LocketAPI.isLocked(location)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 容器传输事件(取消依然监控)
    //@Listener(order = Order.FIRST, beforeModifications = true)
    //@IsCancelled(value = Tristate.UNDEFINED)
    public void onInventoryMove(ChangeInventoryEvent.Transfer event) {
        System.out.println("===== onInventoryMove =====");
        /*if (Config.isItemTransferOutBlocked() || Config.getHopperMinecartAction()) {
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
        }*/
//        if (Config.isItemTransferInBlocked()) {
//            if (isInventoryLocked(event.getTargetInventory())) {
//                event.setCancelled(true);
//            }
//        }
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

    // 右键锁箱子
    @Listener(order = Order.LAST)
    public void onPlayerLockBlock(InteractBlockEvent.Secondary event, @First Player player) {
        ItemStack stack = player.getItemInHand(event.getHandType()).orElse(null);
        if (stack == null || ItemTypes.SIGN != stack.getItem()) return;
        if (player.get(Keys.IS_SNEAKING).orElse(false)) return;
        Direction face = event.getTargetSide();
        if (face == Direction.UP || face == Direction.DOWN || face == Direction.NONE) return;
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block == null) return;
        if (!LocketAPI.isLockable(block) || block.getRelative(face).getBlockType() != BlockTypes.AIR) return;

        event.setCancelled(true);

        IPlayer iPlayer = LocketAPI.getPlayer(player);
        if (player.hasPermission(Perms.ADMIN_LOCK)) {
            iPlayer.sendChat("你使用管理权限强制放置了锁");
            iPlayer.placeLock(block, face, event.getHandType());
            return;
        }
        if (!player.hasPermission(Perms.LOCK)) {
            iPlayer.sendChat("你需要 [" + Perms.LOCK + "] 权限才能进行此操作!");
            return;
        }
        if (iPlayer.isOtherProtect(block)) {
            iPlayer.sendChat("方块被其他插件保护,你没有操作权限!");
            return;
        }
        switch (iPlayer.tryAccess(block)) {
            case SIGN_USER:
                iPlayer.sendChat("你是使用者,不放置");
                return;
            case SIGN_OWNER:
                iPlayer.sendChat("你是所有者,放置");
                iPlayer.placeLock(block, face, event.getHandType());
                return;
            case SIGN_NOT_LOCK:
                iPlayer.sendChat("方块没有上锁,放置");
                iPlayer.placeLock(block, face, event.getHandType());
                return;
            case SIGN_M_OWNERS:
                iPlayer.sendChat("这个方块有多个所有者,这是不允许的,请联系管理员!");
                return;
            case SIGN_NO_ACCESS:
                iPlayer.sendChat("你没有进行此操作的权限,你既不是所有者也不是用户!");
                return;
            case M_CHESTS:
                iPlayer.sendChat("这是一个多重箱子,这是不允许的,请联系管理员!");
        }
    }

    // 右键选择告示牌
    @Listener(order = Order.LAST)
    public void onPlayerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block != null && block.getBlockType() == BlockTypes.WALL_SIGN) {
            IPlayer iPlayer = LocketAPI.getPlayer(player);
            if (!player.hasPermission(Perms.EDIT)) {
                iPlayer.sendChat("你没有 [" + Perms.EDIT + "] 权限");
                return;
            }
            iPlayer.select(block);
            iPlayer.sendChat("你选择了一个告示牌!");
        }
    }

    // 玩家修改告示牌事件
    @Listener(order = Order.LAST)
    public void onPlayerChangeSign(ChangeSignEvent event, @First Player player) {
        if (player.hasPermission(Perms.ADMIN_EDIT)) {
            LocketAPI.getPlayer(player).sendChat("ADMIN_EDIT");
            return;
        }
        SignData data = event.getText();
        String line_0 = data.get(0).orElse(Text.EMPTY).toPlain();
        if (LocketAPI.isPrivate(line_0)) {
            IPlayer iPlayer = LocketAPI.getPlayer(player);
            Sign sign = event.getTargetTile();
            Location<World> block = LocketAPI.getAttached(sign.getLocation());

            if (!LocketAPI.isLockable(block)) {
                iPlayer.sendChat("此方块无法用告示牌保护!");
                event.setCancelled(true);
                return;
            }
            if (!player.hasPermission(Perms.LOCK)) {
                iPlayer.sendChat("你没有手动锁牌的权限");
                event.setCancelled(true);
                return;
            }
            if (iPlayer.isOtherProtect(block)) {
                iPlayer.sendChat("方块被其他插件保护,且你没有操作权限!");
                event.setCancelled(true);
                return;
            }
            data.setElement(1, Text.of(player.getName()));
            sign.offer(data);
        }
    }

    // 玩家登出
    @Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect event, @First Player player) {
        LocketAPI.removePlayer(player);
    }

}
