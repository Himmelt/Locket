package org.soraworld.locket.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.Locket;
import org.soraworld.locket.data.HandType;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.nms.InvUtil;
import org.soraworld.violet.command.*;
import org.soraworld.violet.inject.Command;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Himmelt
 */
@Command(name = Locket.PLUGIN_ID, aliases = {"lock"}, usage = "usage.lock")
public class CommandLocket {
    @Inject
    private LocketManager manager;

    @Sub(path = ".", perm = "locket.lock")
    public final SubExecutor<Player> lock = (cmd, player, args) -> {
        Location location = manager.getSelected(player);
        if (location != null) {
            Block selected = location.getBlock();
            if (args.empty()) {
                if (manager.bypassPerm(player)) {
                    manager.lockSign(player, selected, 0, null);
                } else if (manager.tryAccess(player, selected, true) == Result.NOT_LOCKED) {
                    if (manager.isLockable(LocketManager.getAttached(selected))) {
                        manager.lockSign(player, selected, 0, null);
                    } else {
                        manager.sendHint(player, "notLockable");
                    }
                } else {
                    manager.sendHint(player, "noOwnerAccess");
                }
            } else if (args.size() >= 2) {
                try {
                    int line = Integer.parseInt(args.get(0));
                    String name = args.get(1);
                    if (manager.bypassPerm(player)) {
                        manager.lockSign(player, selected, line, name);
                    } else if (manager.isLockable(LocketManager.getAttached(selected))) {
                        Result result = manager.tryAccess(player, selected, true);
                        if (result == Result.SIGN_OWNER || result == Result.NOT_LOCKED) {
                            manager.lockSign(player, selected, line, name);
                            manager.sendHint(player, "manuLock");
                        } else {
                            manager.sendHint(player, "noOwnerAccess");
                        }
                    } else {
                        manager.sendHint(player, "notLockable");
                    }
                } catch (Throwable e) {
                    manager.sendHint(player, "invalidInt");
                }
            }
        } else {
            manager.sendHint(player, "selectFirst");
        }
    };

    @Tab(path = ".")
    public final TabExecutor<Player> lock_tab = (cmd, player, args) -> {
        if (args.size() <= 1) {
            List<String> list = cmd.tabComplete(player, args, true);
            if (args.first().isEmpty()) {
                list.add(0, "3");
                list.add(0, "2");
            }
            return list;
        }
        if (args.size() >= 2) {
            VCommand sub = cmd.getSub(args.first());
            if (sub != null) {
                return sub.tabComplete(player, args.next());
            }
            List<String> players = new ArrayList<>();
            Bukkit.getServer().getOnlinePlayers().forEach(p -> players.add(p.getName()));
            return ListUtils.getMatchListIgnoreCase(args.get(1), players);
        }
        return new ArrayList<>();
    };

    @Sub(perm = "locket.lock", usage = "usage.remove")
    public final SubExecutor<Player> remove = (cmd, player, args) -> {
        Location selected = manager.getSelected(player);
        if (selected != null) {
            if (args.empty()) {
                if (manager.bypassPerm(player) || manager.tryAccess(player, selected.getBlock(), true) == Result.SIGN_OWNER) {
                    manager.unLockSign(selected, 0);
                } else {
                    manager.sendHint(player, "noOwnerAccess");
                }
            } else if (args.size() >= 1) {
                try {
                    int line = Integer.parseInt(args.first());
                    if (manager.bypassPerm(player)) {
                        manager.unLockSign(selected, line);
                    } else if ((line == 2 || line == 3) && manager.tryAccess(player, selected.getBlock(), true) == Result.SIGN_OWNER) {
                        manager.unLockSign(selected, line);
                        manager.sendHint(player, "manuRemove");
                    } else {
                        manager.sendHint(player, "cantRemove");
                    }
                } catch (Throwable ignored) {
                    manager.sendHint(player, "invalidInt");
                }
            }
        } else {
            manager.sendHint(player, "selectFirst");
        }
    };

    @Sub(perm = "admin", virtual = true, usage = "usage.type")
    public final SubExecutor<CommandSender> type = null;

    @Sub(path = "type.+", perm = "admin")
    public final SubExecutor<CommandSender> type_plus = (cmd, sender, args) -> processType(sender, args, manager::addType, "typeAdd");

    @Sub(path = "type.-", perm = "admin")
    public final SubExecutor<CommandSender> type_minus = (cmd, sender, args) -> processType(sender, args, manager::removeType, "typeRemove");

    @Sub(path = "type.++", perm = "admin")
    public final SubExecutor<CommandSender> type_dplus = (cmd, sender, args) -> processType(sender, args, manager::addDType, "dTypeAdd");

    @Sub(path = "type.--", perm = "admin")
    public final SubExecutor<CommandSender> type_dminus = (cmd, sender, args) -> processType(sender, args, manager::removeDType, "dTypeRemove");

    private void processType(@NotNull CommandSender sender, @NotNull Args args, @NotNull Consumer<Material> consumer, @NotNull String key) {
        Material type;
        // TODO +/-/++/-- block look at
        if (args.notEmpty()) {
            type = Material.getMaterial(args.first());
        } else if (sender instanceof Player) {
            ItemStack stack = InvUtil.getItemInHand(((Player) sender).getInventory(), HandType.MAIN_HAND);
            type = stack == null ? null : stack.getType();
        } else {
            manager.sendKey(sender, "emptyArgs");
            return;
        }
        if (type == null) {
            manager.sendKey(sender, "nullBlockType");
            return;
        }
        if (type == Material.AIR || type == Material.SIGN || type == Material.WALL_SIGN || "SIGN_POST".equalsIgnoreCase(type.name())) {
            manager.sendKey(sender, "illegalType");
            return;
        }
        consumer.accept(type);
        manager.sendKey(sender, key, type.name());
        manager.asyncSave(null);
    }
}
