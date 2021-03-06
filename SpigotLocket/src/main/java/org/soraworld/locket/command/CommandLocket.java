package org.soraworld.locket.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.Locket;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.nms.HandType;
import org.soraworld.locket.nms.Helper;
import org.soraworld.locket.util.Util;
import org.soraworld.violet.command.*;
import org.soraworld.violet.inject.Command;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.util.ChatColor;
import org.soraworld.violet.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
        Location select = manager.getSelected(player);
        if (select == null) {
            manager.sendHint(player, "selectFirst");
            return;
        }
        Block selected = select.getBlock();
        if (!manager.isWallSign(selected.getType())) {
            manager.sendHint(player, "notSignTile");
            manager.clearSelected(player.getUniqueId());
            return;
        }
        int line = 0;
        String name;
        if (args.size() < 2) {
            name = null;
        } else {
            try {
                line = Integer.parseInt(args.get(0));
            } catch (Throwable ignored) {
                manager.sendHint(player, "invalidInt");
                return;
            }
            name = args.get(1);
        }

        Block target = Helper.getAttached((Sign) selected.getState());

        if (!manager.bypassPerm(player)) {
            if (!manager.hasPermission(player, "locket.lock")) {
                manager.sendHint(player, "needPerm", manager.mappingPerm("locket.lock"));
                return;
            }
            if (manager.otherProtected(player, target)) {
                manager.sendHint(player, "otherProtect");
                return;
            }
            if (!manager.isLockable(target)) {
                manager.sendHint(player, "notLockable");
                return;
            }
        }

        switch (manager.tryAccess(player, target, true)) {
            case SIGN_OWNER:
            case NOT_LOCKED:
                manager.lockSign(player, selected, line, name);
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
            Block select = selected.getBlock();
            if (manager.isWallSign(select.getType())) {
                Block target = Helper.getAttached((Sign) select.getState());
                if (args.empty()) {
                    if (manager.bypassPerm(player) || manager.tryAccess(player, target, true) == Result.SIGN_OWNER) {
                        manager.unLockSign(select, 0);
                    } else {
                        manager.sendHint(player, "noOwnerAccess");
                    }
                } else if (args.size() >= 1) {
                    try {
                        int line = Integer.parseInt(args.first());
                        if (manager.bypassPerm(player) || manager.tryAccess(player, target, true) == Result.SIGN_OWNER) {
                            manager.unLockSign(select, line);
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
                manager.clearSelected(player.getUniqueId());
            }
        } else {
            manager.sendHint(player, "selectFirst");
        }
    };

    @Sub(perm = "admin")
    public final SubExecutor<Player> info = ((cmd, player, args) -> {
        Location select = manager.getSelected(player);
        if (select == null) {
            manager.sendHint(player, "selectFirst");
            return;
        }
        Block selected = select.getBlock();
        if (!manager.isWallSign(selected.getType())) {
            manager.sendHint(player, "notSignTile");
            manager.clearSelected(player.getUniqueId());
            return;
        }
        Helper.touchSign(selected, data -> {
            for (int i = 1; i <= 3; i++) {
                String text = Util.HIDE_UUID.matcher(data.lines[i]).replaceAll("");
                UUID uuid = Util.parseUuid(data.lines[i]).orElse(null);
                manager.send(player, "[" + i + "]: " + text + ChatColor.RESET + " -> " + uuid);
            }
            return false;
        }, null);
    });

    @Sub(perm = "admin", virtual = true, usage = "usage.type")
    public final SubExecutor<CommandSender> type = null;

    @Sub(path = "type.+", perm = "admin", tabs = {"look"})
    public final SubExecutor<CommandSender> type_plus = (cmd, sender, args) -> processType(sender, args, manager::addType, "typeAdd");

    @Sub(path = "type.-", perm = "admin", tabs = {"look"})
    public final SubExecutor<CommandSender> type_minus = (cmd, sender, args) -> processType(sender, args, manager::removeType, "typeRemove");

    @Sub(path = "type.++", perm = "admin", tabs = {"look"})
    public final SubExecutor<CommandSender> type_dplus = (cmd, sender, args) -> processType(sender, args, manager::addDType, "dTypeAdd");

    @Sub(path = "type.--", perm = "admin", tabs = {"look"})
    public final SubExecutor<CommandSender> type_dminus = (cmd, sender, args) -> processType(sender, args, manager::removeDType, "dTypeRemove");

    private void processType(@NotNull CommandSender sender, @NotNull Args args, @NotNull Consumer<Material> consumer, @NotNull String key) {
        Material type;
        if (args.notEmpty()) {
            if ("look".equals(args.first()) && sender instanceof Player) {
                Player player = (Player) sender;
                Block block = Helper.getLookAt(player, 25);
                if (block != null) {
                    type = block.getType();
                } else {
                    manager.sendKey(player, "notLookBlock");
                    return;
                }
            } else {
                type = Material.getMaterial(args.first());
            }
        } else if (sender instanceof Player) {
            ItemStack stack = Helper.getItemInHand(((Player) sender).getInventory(), HandType.MAIN_HAND);
            type = stack == null ? null : stack.getType();
        } else {
            manager.sendKey(sender, "emptyArgs");
            return;
        }
        if (type == null) {
            manager.sendKey(sender, "nullBlockType");
            return;
        }
        if (type == Material.AIR || manager.isSign(type)) {
            manager.sendKey(sender, "illegalType");
            return;
        }
        consumer.accept(type);
        manager.sendKey(sender, key, type.name());
        manager.asyncSave(null);
    }
}
