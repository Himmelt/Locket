package org.soraworld.locket.listener;

import org.soraworld.locket.core.WrappedPlayer;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.I18n;
import org.soraworld.locket.constant.LangKeys;
import org.soraworld.locket.constant.Perms;
import org.soraworld.locket.constant.Result;
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
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class SpongeEventListener {


    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockChange(ChangeBlockEvent event) {
        event.filter(loc -> {
            Result result = LocketAPI.isLocked(loc);
            System.out.println(">>" + result + " >> " + loc);
            return result == Result.SIGN_NOT_LOCK;
        });
    }

    // 玩家方块交互事件(主要行为保护)
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block == null) return;
        WrappedPlayer iPlayer = LocketAPI.getPlayer(player);
        Result result = iPlayer.tryAccess(block);
        iPlayer.adminNotify("==" + block + "|" + result);
        if (result != Result.SIGN_NOT_LOCK && iPlayer.hasPerm(Perms.ADMIN_INTERACT)) {
            /////////////////////////////////////////////////////////////////////
            //iPlayer.adminNotify(I18n.formatText(LangKeys.USING_ADMIN_PERM));
            iPlayer.adminNotify("line 49");
            return;
        }
        iPlayer.adminNotify(">>>>>>>>>>>>>>>>>>");
        switch (result) {
            case SIGN_USER:
            case SIGN_OWNER:
            case SIGN_NOT_LOCK:
                return;
            case SIGN_M_OWNERS:
                iPlayer.sendChat(I18n.formatText(LangKeys.MULTI_OWNERS));
                event.setCancelled(true);
                return;
            case M_BLOCKS:
                iPlayer.sendChat(I18n.formatText(LangKeys.MULTI_BLOCKS));
                event.setCancelled(true);
                return;
            case SIGN_NO_ACCESS:
                iPlayer.sendChat(I18n.formatText(LangKeys.NO_ACCESS));
                event.setCancelled(true);
        }
    }

    // TODO 玩家放置方块事件
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerPlaceBlock(ChangeBlockEvent.Place event, @First Player player) {
        WrappedPlayer iPlayer = LocketAPI.getPlayer(player);
        if (player.hasPermission(Perms.ADMIN_INTERFERE)) {
            //iPlayer.adminNotify(I18n.formatText(LangKeys.USING_ADMIN_PERM));
            iPlayer.adminNotify("line 77");
            return;
        }
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Location<World> block = transaction.getOriginal().getLocation().orElse(null);
        }
    }

    // 玩家破坏方块事件
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerBreakBlock(ChangeBlockEvent.Pre event, @ContextValue("PLAYER_BREAK") @First Player player) {
        WrappedPlayer iPlayer = LocketAPI.getPlayer(player);
        for (Location<World> block : event.getLocations()) {
            Result result = iPlayer.tryAccess(block);
            iPlayer.adminNotify("result:" + block + "|" + result);
            if (result != Result.SIGN_NOT_LOCK && iPlayer.hasPerm(Perms.ADMIN_LOCK)) {
                //iPlayer.adminNotify(I18n.formatText(LangKeys.USING_ADMIN_PERM));
                iPlayer.adminNotify("line 93");
                return;
                //break;
            }
            switch (result) {
                case SIGN_OWNER:
                    if (!iPlayer.hasPerm(Perms.LOCK)) {
                        iPlayer.sendChat(I18n.formatText(LangKeys.NEED_PERM, Perms.LOCK));
                        event.setCancelled(true);
                        return;
                    }
                    break;
                case SIGN_NOT_LOCK:
                    break;
                case M_BLOCKS:
                    iPlayer.sendChat(I18n.formatText(LangKeys.MULTI_BLOCKS));
                    event.setCancelled(true);
                    return;
                case SIGN_M_OWNERS:
                    iPlayer.sendChat(I18n.formatText(LangKeys.MULTI_OWNERS));
                    event.setCancelled(true);
                    return;
                default:
                    iPlayer.sendChat(I18n.formatText(LangKeys.NO_ACCESS));
                    event.setCancelled(true);
                    return;
            }
        }
    }

    // 活塞推出事件
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onPistonExtend(ChangeBlockEvent.Pre event, @ContextValue("PISTON_EXTEND") @First Object cause) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isLocked(block) != Result.SIGN_NOT_LOCK) {
                System.out.println("onPistonExtend Cancel:" + block);
                event.setCancelled(true);
                return;
            }
        }
    }

    // 活塞收回事件
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onPistonRetract(ChangeBlockEvent.Pre event, @ContextValue("PISTON_RETRACT") @First Object cause) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isLocked(block) != Result.SIGN_NOT_LOCK) {
                System.out.println("onPistonRetract Cancel");
                event.setCancelled(true);
                return;
            }
        }
    }

    // TODO 环境生长事件
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onStructureGrow(ChangeBlockEvent.Grow event) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            if (location != null && LocketAPI.isLocked(location) != Result.SIGN_NOT_LOCK) {
                System.out.println("onStructureGrow Cancel");
                event.setCancelled(true);
                return;
            }
        }
    }

    // TODO 方块变化事件
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onModify(ChangeBlockEvent.Modify event) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            if (location != null && LocketAPI.isLocked(location) != Result.SIGN_NOT_LOCK) {
                System.out.println("onModify Cancel");
                event.setCancelled(true);
                return;
            }
        }
    }

    // TODO 爆炸保护
    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedLocations().removeIf(location -> location != null && LocketAPI.isLocked(location) != Result.SIGN_NOT_LOCK);
    }

    // TODO 容器传输事件(取消依然监控)
    //@Listener(order = Order.FIRST, beforeModifications = true)
    //@IsCancelled(value = Tristate.UNDEFINED)
    public void onInventoryTransfer(ChangeInventoryEvent.Transfer event) {
        System.out.println("onInventoryTransfer");
    }

    // 右键锁箱子
    //@Listener(order = Order.LAST)
    public void onPlayerLockBlock(InteractBlockEvent.Secondary event, @First Player player) {
        ItemStack stack = player.getItemInHand(event.getHandType()).orElse(null);
        if (stack == null || ItemTypes.SIGN != stack.getType()) return;
        if (player.get(Keys.IS_SNEAKING).orElse(false)) return;
        Direction face = event.getTargetSide();
        if (face == Direction.UP || face == Direction.DOWN || face == Direction.NONE) return;
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block == null) return;
        if (!LocketAPI.isLockable(block) || block.getRelative(face).getBlockType() != BlockTypes.AIR) return;

        System.out.println("onPlayerLockBlock cancel");
        event.setCancelled(true);

        WrappedPlayer iPlayer = LocketAPI.getPlayer(player);
        if (player.hasPermission(Perms.ADMIN_LOCK)) {
            //iPlayer.adminNotify(I18n.formatText(LangKeys.USING_ADMIN_PERM));
            iPlayer.adminNotify("line 201");
            iPlayer.placeLock(block, face, event.getHandType());
            return;
        }
        if (!player.hasPermission(Perms.LOCK)) {
            iPlayer.sendChat(I18n.formatText(LangKeys.NEED_PERM, Perms.LOCK));
            return;
        }
        if (iPlayer.otherProtected(block)) {
            iPlayer.sendChat(I18n.formatText(LangKeys.OTHER_PROTECT));
            return;
        }
        switch (iPlayer.tryAccess(block)) {
            case SIGN_OWNER:
            case SIGN_NOT_LOCK:
                iPlayer.placeLock(block, face, event.getHandType());
                iPlayer.sendChat(I18n.formatText(LangKeys.QUICK_LOCK));
                return;
            case SIGN_M_OWNERS:
                iPlayer.sendChat(I18n.formatText(LangKeys.MULTI_OWNERS));
                return;
            case M_BLOCKS:
                iPlayer.sendChat(I18n.formatText(LangKeys.MULTI_BLOCKS));
                return;
            default:
                iPlayer.sendChat(I18n.formatText(LangKeys.NO_ACCESS));
        }
    }

    // 右键选择告示牌
    //@Listener(order = Order.LAST)
    public void onPlayerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        Location<World> block = event.getTargetBlock().getLocation().orElse(null);
        if (block != null && block.getBlockType() == BlockTypes.WALL_SIGN) {
            WrappedPlayer iPlayer = LocketAPI.getPlayer(player);
            if (!player.hasPermission(Perms.LOCK)) {
                iPlayer.sendChat(I18n.formatText(LangKeys.NEED_PERM, Perms.LOCK));
                return;
            }
            iPlayer.select(block);
            iPlayer.sendChat(I18n.formatText(LangKeys.SELECT_SIGN));
        }
    }

    // 玩家修改告示牌事件
    //@Listener(order = Order.LAST)
    public void onPlayerChangeSign(ChangeSignEvent event, @First Player player) {
        SignData data = event.getText();
        String line_0 = data.get(0).orElse(Text.EMPTY).toPlain();
        String line_1 = data.get(1).orElse(Text.EMPTY).toPlain();
        String line_2 = data.get(2).orElse(Text.EMPTY).toPlain();
        String line_3 = data.get(3).orElse(Text.EMPTY).toPlain();
        if (LocketAPI.isPrivate(line_0)) {
            Sign sign = event.getTargetTile();
            WrappedPlayer iPlayer = LocketAPI.getPlayer(player);
            Location<World> block = LocketAPI.getAttached(sign.getLocation());
            if (iPlayer.hasPerm(Perms.ADMIN_LOCK)) {
                //iPlayer.adminNotify(I18n.formatText(LangKeys.USING_ADMIN_PERM));
                iPlayer.adminNotify("line 259");
                data.setElement(0, LocketAPI.CONFIG.getPrivateText());
                data.setElement(1, LocketAPI.CONFIG.getOwnerText(line_1.isEmpty() ? player.getName() : line_1));
                data.setElement(2, LocketAPI.CONFIG.getUserText(line_2));
                data.setElement(3, LocketAPI.CONFIG.getUserText(line_3));
                sign.offer(data);
                return;
            }
            if (!LocketAPI.isLockable(block)) {
                iPlayer.sendChat(I18n.formatText(LangKeys.CANT_LOCK));
                event.setCancelled(true);
                return;
            }
            if (!player.hasPermission(Perms.LOCK)) {
                iPlayer.sendChat(I18n.formatText(LangKeys.NEED_PERM, Perms.LOCK));
                event.setCancelled(true);
                return;
            }
            if (iPlayer.otherProtected(block)) {
                iPlayer.sendChat(I18n.formatText(LangKeys.OTHER_PROTECT));
                event.setCancelled(true);
                return;
            }
            data.setElement(0, LocketAPI.CONFIG.getPrivateText());
            data.setElement(1, LocketAPI.CONFIG.getOwnerText(player.getName()));
            data.setElement(2, LocketAPI.formatText(line_2));
            data.setElement(3, LocketAPI.formatText(line_3));
            sign.offer(data);
        }
    }

    // 玩家登出
    //@Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect event, @First Player player) {
        LocketAPI.removePlayer(player);
    }

    // 重载配置
    //@Listener
    public void onReload(GameReloadEvent event) {
        LocketAPI.CONFIG.load();
    }

}
