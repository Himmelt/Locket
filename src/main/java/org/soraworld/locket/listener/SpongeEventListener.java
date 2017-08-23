package org.soraworld.locket.listener;

import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.constant.Perms;
import org.soraworld.locket.constant.Result;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.item.inventory.AffectItemStackEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.TargetContainerEvent;
import org.spongepowered.api.event.item.inventory.TargetInventoryEvent;
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
import java.util.Optional;

public class SpongeEventListener {

    // FIRST 监听

    // 玩家方块交互事件(主要行为保护)
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        player.sendMessage(Text.of("===== onPlayerInteractBlock ====="));
        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if (location == null) return;
        IPlayer iPlayer = LocketAPI.getPlayer(player);
        Result result = iPlayer.canInteract(location);
        switch (result) {
            case OWNER:
                iPlayer.sendChat("你是所有者");
                return;
            case USER:
                iPlayer.sendChat("你是使用者");
                return;
            case NOT_LOCK:
                iPlayer.sendChat("方块没有上锁");
                return;
            case ADMIN_INTERACT:
                iPlayer.sendChat("你有admin.interact权限");
                return;
            case M_OWNERS:
                iPlayer.sendChat("这个方块有多个所有者,这是不允许的,请联系管理员!");
                event.setCancelled(true);
                return;
            case M_CHESTS:
                iPlayer.sendChat("这是一个多重箱子,这是不允许的,请联系管理员!");
                event.setCancelled(true);
                return;
            default:
                iPlayer.sendChat("你没有进行此操作的权限");
                event.setCancelled(true);
        }
    }

    // 玩家放置方块事件
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerPlaceBlock(ChangeBlockEvent.Place event, @First Player player) {
        player.sendMessage(Text.of("===== onPlayerPlaceBlock ====="));
        if (player.hasPermission(Perms.ADMIN_INTERFERE)) return;
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        IPlayer iPlayer = LocketAPI.getPlayer(player);
        for (Transaction<BlockSnapshot> transaction : transactions) {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            if (location != null && !iPlayer.canInterfere(location)) {
                event.setCancelled(true);
                iPlayer.sendChat("你不能在此处放置可能影响他人保护锁的方块!");
            }
        }
    }

    // 玩家破坏方块事件
    //@Listener(order = Order.AFTER_PRE, beforeModifications = true)
    public void onPlayer_BreakBlock(ChangeBlockEvent.Break event, @First Player player) {
        player.sendMessage(Text.of("===== onPlayerBreakBlock ====="));
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        IPlayer iPlayer = LocketAPI.getPlayer(player);
        for (Transaction<BlockSnapshot> transaction : transactions) {

            BlockSnapshot block = transaction.getOriginal();
            TileEntityArchetype archetype = block.createArchetype().orElse(null);
            DataContainer sd= archetype.getTileData();

            switch (iPlayer.canBreak(block)) {
                case NOT_LOCK:
                case ADMIN_UNLOCK:
                case ADMIN_BREAK:
                case OWNER:
                    iPlayer.sendChat(iPlayer.canBreak(block).name());
                    return;
                default:
                    event.setCancelled(true);
                    iPlayer.sendChat("你不能破坏此方块!");
            }
        }
    }

    // 活塞推出事件
    // Cause org.spongepowered.api.world.World
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
    // Cause org.spongepowered.api.world.World
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
    @Listener(order = Order.FIRST, beforeModifications = true)
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

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onRandom(TickBlockEvent.Random event) {
        System.out.println("===== onRandom =====");
        System.out.println(event.getTargetBlock());
    }

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onTargetInventoryEvent(TargetInventoryEvent event) {
        System.out.println("===== onTargetInventoryEvent =====");
        System.out.println(event.getClass());
        System.out.println(event.getCause());
        System.out.println(event.getTargetInventory());
    }

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onTargetContainerEvent(TargetContainerEvent event) {
        System.out.println("===== onTargetContainerEvent =====");
        System.out.println(event.getClass());
        System.out.println(event.getCause());
        System.out.println(event.getTargetInventory());
    }

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeDataHolderEvent(ChangeDataHolderEvent event) {
        System.out.println("===== onChangeDataHolderEvent =====");
        System.out.println(event.getClass());
        System.out.println(event.getCause());
        System.out.println(event.getTargetHolder());
    }

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onAffectItemStackEvent(AffectItemStackEvent event) {
        System.out.println("===== onAffectItemStackEvent =====");
        System.out.println(event.getClass());
        System.out.println(event.getCause());
        System.out.println(event.getTransactions());
    }

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onInventoryChange(ChangeInventoryEvent event) {
        System.out.println("===== onInventoryChange =====");
        System.out.println(event.getClass());
        System.out.println(event.getCause());
        System.out.println(event.getTargetInventory());
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

    // LAST 监听

    // 右键锁箱子
    @Listener(order = Order.LAST)
    public void onPlayerLockBlock(InteractBlockEvent.Secondary event, @First Player player) {
        player.sendMessage(Text.of("===== onPlayerLockBlock ====="));
        ItemStack stack = player.getItemInHand(event.getHandType()).orElse(null);
        if (stack == null || ItemTypes.SIGN != stack.getItem()) return;
        if (player.get(Keys.IS_SNEAKING).orElse(false)) return;
        Direction face = event.getTargetSide();
        if (!Constants.FACES.contains(face)) return;
        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if (location == null || !LocketAPI.isLockable(location)) return;
        IPlayer iPlayer = LocketAPI.getPlayer(player);
        if (location.getRelative(face).getBlockType() != BlockTypes.AIR) return;
        // 事件进入取消阶段
        event.setCancelled(true);
        if (!player.hasPermission(Perms.LOCK)) {
            iPlayer.sendChat("&a&l你需要权限 &b[" + Perms.LOCK + "]&a!");
            return;
        }
        if (iPlayer.isOtherProtect(location)) {
            iPlayer.sendChat("&c&l方块被其他插件保护,你没有操作权限!");
            return;
        }

        Result result = iPlayer.canLock(location);
        switch (result) {
            case OWNER:
                iPlayer.sendChat("你是所有者,放置");
                iPlayer.placeLock(location, face);
                iPlayer.removeSign(event.getHandType());
                return;
            case USER:
                iPlayer.sendChat("你是使用者,不放置");
                return;
            case NOT_LOCK:
                iPlayer.sendChat("方块没有上锁,放置");
                iPlayer.placeLock(location, face);
                iPlayer.removeSign(event.getHandType());
                return;
            case NO_LOCK:
                iPlayer.sendChat("你没有.lock权限");
                return;
            case M_OWNERS:
                iPlayer.sendChat("这个方块有多个所有者,这是不允许的,请联系管理员!");
                return;
            case M_CHESTS:
                iPlayer.sendChat("这是一个多重箱子,这是不允许的,请联系管理员!");
                return;
            case CANT_TYPE:
                iPlayer.sendChat("这个方块不可被锁");
            default:
                iPlayer.sendChat("你没有进行此操作的权限");
        }
    }

    // 右键选择告示牌
    @Listener(order = Order.LAST)
    public void onPlayerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if (location == null) return;
        BlockType type = location.getBlockType();
        if (type == BlockTypes.WALL_SIGN) {
            if (!player.hasPermission(Perms.EDIT)) {
                LocketAPI.getPlayer(player).sendChat("你没有 " + Perms.EDIT + " 权限");
                return;
            }
            LocketAPI.getPlayer(player).select(location);
            player.sendMessage(Text.of("你选择了一个告示牌:" + location));
        }
    }

    // 玩家修改告示牌事件
    @Listener(order = Order.LAST)
    public void onPlayerChangeSign(ChangeSignEvent event, @First Player player) {
        player.sendMessage(Text.of("===== onPlayerChangeSign ====="));
        SignData data = event.getText();
        String line_0 = data.get(0).orElse(Text.EMPTY).toPlain();
        if (LocketAPI.isPrivate(line_0)) {

            Sign sign = event.getTargetTile();
            IPlayer iPlayer = LocketAPI.getPlayer(player);
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
            {
                // 其他处理
            }
        }
    }

    // 普通监听

    // 玩家登出
    @Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect event, @First Player player) {
        LocketAPI.removePlayer(player);
    }

}
