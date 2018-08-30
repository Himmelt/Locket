package org.soraworld.locket.command;

import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.LocketManager;
import org.soraworld.locket.constant.LangKeys;
import org.soraworld.locket.constant.Perms;
import org.soraworld.locket.constant.Result;
import org.soraworld.locket.core.WrappedPlayer;
import org.soraworld.violet.command.Paths;
import org.soraworld.violet.command.SpongeCommand;
import org.soraworld.violet.command.Sub;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class CommandSpecs {

    @Sub(perm = "locket.lock", onlyPlayer = true)
    public static void remove(SpongeCommand self, CommandSource sender, Paths args) {
        LocketManager locket = (LocketManager) self.manager;
        Player player = (Player) sender;
        Location<World> selection = locket.getSelected(player);

        if (selection != null) {
            try {
                int line = Integer.valueOf(args.first());
                if (line != 3 && line != 4) {
                    locket.sendKey(sender, "cantRemoveLine");
                } else if (player.hasPermission(locket.defAdminPerm())) {
                    locket.removeLine(player, line);
                    //iPlayer.unLockSign(selection, line);
                } else if (!player.hasPermission(Perms.LOCK)) {
                    locket.sendKey(sender, LangKeys.NEED_PERM, Perms.LOCK);
                    return;
                }
                Result result = locket.tryAccess(player);
                if (result == Result.SIGN_OWNER) {
                    locket.removeLine(player, line);
                    locket.sendKey(player, LangKeys.MANU_REMOVE);
                    return;
                }
                locket.sendKey(player, LangKeys.CANT_REMOVE);
            } catch (Throwable ignored) {
                locket.sendKey(sender, "invalidInt");
            }
        } else locket.sendKey(player, "selectFirst");
    }

    @Sub(perm = "locket.lock", onlyPlayer = true)
    public static void lock(SpongeCommand self, CommandSource sender, Paths args) {
        LocketManager locket = (LocketManager) self.manager;
        Player player = (Player) sender;

        WrappedPlayer iPlayer = LocketAPI.getPlayer((Player) sender);
        Location<World> selection = locket.getSelected(player);//iPlayer.selection();
        if (selection == null) {
            locket.sendKey(player, "LangKeys.SELECT_FIRST");
            return;
        }
        int line = Integer.valueOf(args.get(0));
        String name = args.get(1);

        if (player.hasPermission(Perms.ADMIN_LOCK)) {
            locket.lockSign(player, line, name);
            //iPlayer.lockSign(selection, line, name);
            return;
        }
        if (!LocketAPI.isLockable(LocketAPI.getAttached(selection))) {
            locket.sendKey(player, LangKeys.CANT_LOCK);
            return;
        }
        if (!iPlayer.hasPerm(Perms.LOCK)) {
            locket.sendKey(player, LangKeys.NEED_PERM, Perms.LOCK);
            return;
        }
        Result result = iPlayer.tryAccess(selection);
        if (result == Result.SIGN_OWNER || result == Result.SIGN_NOT_LOCK) {
            locket.lockSign(player, line, name);
            //iPlayer.lockSign(selection, line, name);
            locket.sendKey(player, LangKeys.MANU_LOCK);
            return;
        }
        locket.sendKey(player, LangKeys.CANT_LOCK);
    }
}
