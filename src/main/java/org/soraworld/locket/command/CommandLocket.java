package org.soraworld.locket.command;

import org.soraworld.locket.Locket;
import org.soraworld.locket.data.Result;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.command.Sub;
import org.soraworld.violet.command.SubExecutor;
import org.soraworld.violet.inject.Command;
import org.soraworld.violet.inject.Inject;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Command(name = Locket.PLUGIN_ID, aliases = {"lock"}, usage = "/locket remove")
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

    @Sub(perm = "locket.lock")
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
}
