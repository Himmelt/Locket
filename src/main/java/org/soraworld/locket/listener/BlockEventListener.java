package org.soraworld.locket.listener;

import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class BlockEventListener {

    @Listener(order = Order.FIRST)
    public void onBlockPlace(ChangeBlockEvent.Place event, @First Player player) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        if (!transactions.isEmpty()) {
            BlockSnapshot origin = transactions.get(0).getOriginal();
            BlockSnapshot target = transactions.get(0).getFinal();
            if (player.hasPermission("locket.admin.interfere")) return;
            if (LocketAPI.mayInterfere(origin, player)) {
                player.sendMessage(Text.of("cannot-interfere-with-others"));
                event.setCancelled(true);
            }
        }
    }

    // Tell player about locket
    @Listener(order = Order.FIRST)
    public void onPlaceFirstBlockNotify(ChangeBlockEvent.Place event, @First Player player) {
        Location<World> block = event.getTransactions().get(0).getOriginal().getLocation().get();
        if (!player.hasPermission("locket.lock")) return;
        if (Utils.shouldNotify(player) && Config.isLockable(block.getBlockType())) {
            Utils.sendMessages(player, Config.getLang("you-can-quick-lock-it"));
        }
    }

    // Player break sign
    @Listener(order = Order.EARLY)
    public void onAttemptBreakSign(ChangeBlockEvent.Break event, @First Player player) {
        Location<World> block = event.getTransactions().get(0).getOriginal().getLocation().get();
        if (player.hasPermission("locket.admin.break")) return;
        if (LocketAPI.isLockSigned(block)) {
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
    }

    // Protect block from being destroyed
    @Listener(order = Order.EARLY)
    public void onAttemptBreakLockedBlocks(ChangeBlockEvent.Break event, @First Player player) {
        Location<World> block = event.getTransactions().get(0).getOriginal().getLocation().get();
        if (LocketAPI.isLocked(block) || LocketAPI.isUpDownLockedDoor(block)) {
            Utils.sendMessages(player, Config.getLang("block-is-locked"));
            event.setCancelled(true);
        }
    }

    // 活塞推出事件
    @Listener(order = Order.EARLY)
    public void onPistonExtend(@Named(NamedCause.PISTON_EXTEND) ChangeBlockEvent.Pre event) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 活塞收回事件
    @Listener(order = Order.EARLY)
    public void onPistonRetract(@Named(NamedCause.PISTON_RETRACT) ChangeBlockEvent.Pre event) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Prevent redstone current open doors
    @Listener(order = Order.EARLY)
    public void onBlockRedstoneChange(ChangeBlockEvent.Pre event) {
        for (Location<World> block : event.getLocations()) {
            if (LocketAPI.isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Manual protection
    @Listener(order = Order.DEFAULT)
    public void onManualLock(ChangeSignEvent event,@First Player player) {
        String topline = event.getText().get(0).get().toPlain();
        if (!player.hasPermission("locket.lock")) {
            if (LocketAPI.isLockString(topline) || LocketAPI.isMoreString(topline)) {
                event.getText().setElement(0, Text.of(Config.getLang("sign-error")));
                Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
            }
            return;
        }
        if (LocketAPI.isLockString(topline) || LocketAPI.isMoreString(topline)) {
            Location<World> block = LocketAPI.getAttachedBlock(event.getTargetTile().getLocation());
            if (LocketAPI.isLockable(block)) {
                // 检查其他插件保护
                if (Depend.isProtectedFrom(block, player)) {
                    event.getText().setElement(0, Text.of(Config.getLang("sign-error")));
                    Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
                    return;
                }
                boolean locked = LocketAPI.isLocked(block);
                if (!locked && !LocketAPI.isUpDownLockedDoor(block)) {
                    if (LocketAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("locked-manual"));
                        if (!player.hasPermission("locket.lockothers")) {
                            // Player with permission can lock with another name
                            event.getText().setElement(1, Text.of(player.getName()));
                        }
                    } else {
                        Utils.sendMessages(player, Config.getLang("not-locked-yet-manual"));
                        event.getText().setElement(0, Text.of(Config.getLang("sign-error")));
                    }
                } else if (!locked && LocketAPI.isOwnerUpDownLockedDoor(block, player)) {
                    if (LocketAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("cannot-lock-door-nearby-manual"));
                        event.getText().setElement(0,Text.of(Config.getLang("sign-error")) );
                    } else {
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-manual"));
                    }
                } else if (LocketAPI.isOwner(block, player)) {
                    if (LocketAPI.isLockString(topline)) {
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

}
