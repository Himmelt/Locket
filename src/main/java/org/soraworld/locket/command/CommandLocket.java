package org.soraworld.locket.command;

import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.Locket;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.command.Args;
import org.soraworld.violet.command.Sub;
import org.soraworld.violet.command.SubExecutor;
import org.soraworld.violet.inject.Command;
import org.soraworld.violet.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.function.Consumer;

@Command(name = Locket.PLUGIN_ID, aliases = {"lock"}, usage = "usage.lock")
public class CommandLocket {
    @Inject
    private LocketManager locket;

    @Sub(path = ".", perm = "locket.lock")
    public final SubExecutor<Player> lock = (cmd, player, args) -> {
        Location<World> selected = locket.getSelected(player);
        if (selected != null) {
            if (args.empty()) {
                if (player.hasPermission(locket.defAdminPerm())) {
                    locket.lockSign(player, selected, 0, null);
                } else if (locket.tryAccess(player, selected) == Result.SIGN_NOT_LOCK) {
                    if (locket.isLockable(LocketManager.getAttached(selected))) {
                        locket.lockSign(player, selected, 0, null);
                    } else locket.sendKey(player, "cantLock");
                }
            } else if (args.size() >= 2) {
                try {
                    int line = Integer.valueOf(args.get(0));
                    String name = args.get(1);
                    if (player.hasPermission(locket.defAdminPerm())) {
                        locket.lockSign(player, selected, line, name);
                    } else if (locket.isLockable(LocketManager.getAttached(selected))) {
                        Result result = locket.tryAccess(player, selected);
                        if (result == Result.SIGN_OWNER || result == Result.SIGN_NOT_LOCK) {
                            locket.lockSign(player, selected, line, name);
                            locket.sendKey(player, "manuLock");
                        } else locket.sendKey(player, "noOwnerAccess");
                    } else locket.sendKey(player, "unLockable");
                } catch (Throwable e) {
                    locket.sendKey(player, "invalidInt");
                }
            }
        } else locket.sendKey(player, "selectFirst");
    };

    @Sub(perm = "locket.lock", usage = "usage.remove")
    public final SubExecutor<Player> remove = (cmd, player, args) -> {
        Location<World> selected = locket.getSelected(player);
        if (selected != null) {
            if (args.empty()) {
                if (player.hasPermission(locket.defAdminPerm()) || locket.tryAccess(player, selected) == Result.SIGN_OWNER) {
                    locket.unLockSign(selected, 0);
                }
            } else if (args.size() >= 1) {
                try {
                    int line = Integer.valueOf(args.first());
                    if (player.hasPermission(locket.defAdminPerm())) {
                        locket.unLockSign(selected, line);
                    } else if ((line == 2 || line == 3) && locket.tryAccess(player, selected) == Result.SIGN_OWNER) {
                        locket.unLockSign(selected, line);
                        locket.sendKey(player, "manuRemove");
                    } else locket.sendKey(player, "cantRemove");
                } catch (Throwable ignored) {
                    locket.sendKey(player, "invalidInt");
                }
            }
        } else locket.sendKey(player, "selectFirst");
    };

    @Sub(perm = "admin", virtual = true, usage = "usage.type")
    public final SubExecutor type = null;

    @Sub(path = "type.+", perm = "admin")
    public final SubExecutor type_plus = (cmd, sender, args) -> {
        processType(sender, args, locket::addType, "typeAdd");
    };

    @Sub(path = "type.-", perm = "admin")
    public final SubExecutor type_minus = (cmd, sender, args) -> {
        processType(sender, args, locket::removeType, "typeRemove");
    };

    @Sub(path = "type.++", perm = "admin")
    public final SubExecutor type_dplus = (cmd, sender, args) -> {
        processType(sender, args, locket::addDType, "dTypeAdd");
    };

    @Sub(path = "type.--", perm = "admin")
    public final SubExecutor type_dminus = (cmd, sender, args) -> {
        processType(sender, args, locket::removeDType, "dTypeRemove");
    };

    private void processType(@NotNull CommandSource sender, @NotNull Args args, @NotNull Consumer<BlockType> consumer, @NotNull String key) {
        BlockType type;
        if (args.notEmpty()) {
            type = Sponge.getRegistry().getType(BlockType.class, args.first()).orElse(null);
        } else if (sender instanceof Player) {
            ItemStack stack = ((Player) sender).getItemInHand(HandTypes.MAIN_HAND).orElse(null);
            type = stack == null ? null : stack.getType().getBlock().orElse(null);
        } else {
            locket.sendKey(sender, "emptyArgs");
            return;
        }
        if (type == null) {
            locket.sendKey(sender, "nullBlockType");
            return;
        }
        if (type == BlockTypes.AIR || type == BlockTypes.WALL_SIGN || type == BlockTypes.STANDING_SIGN) {
            locket.sendKey(sender, "illegalType");
            return;
        }
        String typeName = type.getTranslation().get();
        consumer.accept(type);
        locket.sendKey(sender, key, typeName);
        locket.asyncSave(null);
    }
}
