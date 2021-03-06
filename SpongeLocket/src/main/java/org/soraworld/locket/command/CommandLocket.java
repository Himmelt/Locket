package org.soraworld.locket.command;

import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.Locket;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.util.Helper;
import org.soraworld.locket.util.Util;
import org.soraworld.violet.command.*;
import org.soraworld.violet.inject.Command;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.util.ChatColor;
import org.soraworld.violet.util.ListUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
        Location<World> selected = manager.getSelected(player);
        if (selected == null) {
            manager.sendHint(player, "selectFirst");
            return;
        }
        if (!manager.isWallSign(selected.getBlockType())) {
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

        Location<World> target = LocketManager.getAttached(selected);

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
            Sponge.getServer().getOnlinePlayers().forEach(p -> players.add(p.getName()));
            return ListUtils.getMatchListIgnoreCase(args.get(1), players);
        }
        return new ArrayList<>();
    };

    @Sub(perm = "locket.lock", usage = "usage.remove")
    public final SubExecutor<Player> remove = (cmd, player, args) -> {
        Location<World> selected = manager.getSelected(player);
        if (selected != null) {
            if (manager.isWallSign(selected.getBlockType())) {
                Location<World> target = LocketManager.getAttached(selected);
                if (args.empty()) {
                    if (manager.bypassPerm(player) || manager.tryAccess(player, target, true) == Result.SIGN_OWNER) {
                        manager.unLockSign(selected, 0);
                    } else {
                        manager.sendHint(player, "noOwnerAccess");
                    }
                } else if (args.size() >= 1) {
                    try {
                        int line = Integer.parseInt(args.first());
                        if (manager.bypassPerm(player) || manager.tryAccess(player, target, true) == Result.SIGN_OWNER) {
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
                manager.clearSelected(player.getUniqueId());
            }
        } else {
            manager.sendHint(player, "selectFirst");
        }
    };

    @Sub(perm = "admin")
    public final SubExecutor<Player> info = ((cmd, player, args) -> {
        Location<World> select = manager.getSelected(player);
        if (select == null) {
            manager.sendHint(player, "selectFirst");
            return;
        }
        BlockState selected = select.getBlock();
        if (!manager.isWallSign(selected.getType())) {
            manager.sendHint(player, "notSignTile");
            manager.clearSelected(player.getUniqueId());
            return;
        }
        select.getTileEntity().filter(tile -> tile instanceof Sign).ifPresent(tile -> {
            SignData data = ((Sign) tile).getSignData();
            for (int i = 1; i <= 3; i++) {
                String plain = data.get(i).orElse(Text.EMPTY).toPlain();
                String text = Util.HIDE_UUID.matcher(plain).replaceAll("");
                UUID uuid = Util.parseUuid(plain).orElse(null);
                manager.send(player, "[" + i + "]: " + text + ChatColor.RESET + " -> " + uuid);
            }
        });
    });

    @Sub(perm = "admin", virtual = true, usage = "usage.type")
    public final SubExecutor<CommandSource> type = null;

    @Sub(path = "type.+", perm = "admin", tabs = {"look"})
    public final SubExecutor<CommandSource> type_plus = (cmd, sender, args) -> processType(sender, args, manager::addType, "typeAdd");

    @Sub(path = "type.-", perm = "admin", tabs = {"look"})
    public final SubExecutor<CommandSource> type_minus = (cmd, sender, args) -> processType(sender, args, manager::removeType, "typeRemove");

    @Sub(path = "type.++", perm = "admin", tabs = {"look"})
    public final SubExecutor<CommandSource> type_dplus = (cmd, sender, args) -> processType(sender, args, manager::addDType, "dTypeAdd");

    @Sub(path = "type.--", perm = "admin", tabs = {"look"})
    public final SubExecutor<CommandSource> type_dminus = (cmd, sender, args) -> processType(sender, args, manager::removeDType, "dTypeRemove");

    private void processType(@NotNull CommandSource sender, @NotNull Args args, @NotNull Consumer<BlockType> consumer, @NotNull String key) {
        BlockType type;
        if (args.notEmpty()) {
            if ("look".equals(args.first()) && sender instanceof Player) {
                Player player = (Player) sender;
                Location<World> block = Helper.getLookAt(player, 25);
                if (block != null) {
                    type = block.getBlockType();
                } else {
                    manager.sendKey(player, "notLookBlock");
                    return;
                }
            } else {
                type = Sponge.getRegistry().getType(BlockType.class, args.first()).orElse(null);
            }
        } else if (sender instanceof Player) {
            ItemStack stack = ((Player) sender).getItemInHand(HandTypes.MAIN_HAND).orElse(null);
            type = stack == null ? null : manager.getSignBlockType(stack.getType());
        } else {
            manager.sendKey(sender, "emptyArgs");
            return;
        }
        if (type == null) {
            manager.sendKey(sender, "nullBlockType");
            return;
        }
        if (type == BlockTypes.AIR || manager.isSign(type)) {
            manager.sendKey(sender, "illegalType");
            return;
        }
        String typeName = type.getTranslation().get();
        consumer.accept(type);
        manager.sendKey(sender, key, typeName);
        manager.asyncSave(null);
    }
}
