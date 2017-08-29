package org.soraworld.locket.command;

import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.I18n;
import org.soraworld.locket.constant.LangKeys;
import org.soraworld.locket.constant.Perms;
import org.soraworld.locket.constant.Result;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class CommandLocket implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Text.of("This command can only be executed by an in-game player."));
            return CommandResult.empty();
        }
        IPlayer iPlayer = LocketAPI.getPlayer((Player) source);
        Location<World> selection = iPlayer.selection();
        if (selection == null) {
            iPlayer.sendChat("请先右键选择一个告示牌!");
            return CommandResult.empty();
        }
        Integer line = args.<Integer>getOne("line").orElse(null);
        String text = args.<String>getOne("name").orElse(null);
        if (iPlayer.hasPerm(Perms.ADMIN_LOCK)) {
            if (line == null || line == 0 || line > 3 || text == null || text.isEmpty()) {
                iPlayer.lockSign(selection);
            } else {
                iPlayer.lockSign(selection, line, text);
            }
            iPlayer.adminNotify("ADMIN_LOCK!");
            return CommandResult.success();
        }
        if (!iPlayer.hasPerm(Perms.LOCK)) {
            iPlayer.sendChat(I18n.formatText(LangKeys.NEED_PERM, Perms.LOCK));
            return CommandResult.empty();
        }
        Result result = iPlayer.tryAccess(selection);
        if (line == null && text == null) {
            if (result == Result.SIGN_OWNER || result == Result.SIGN_NOT_LOCK) {
                iPlayer.lockSign(selection);
                iPlayer.sendChat(I18n.formatText(LangKeys.MANU_LOCK));
                return CommandResult.success();
            } else {
                iPlayer.sendChat("你无法手动锁住此方块");
                return CommandResult.empty();
            }
        }
        if (line != null && line == 0) {
            iPlayer.sendChat("此行无法修改");
        }
        if (line != null && line >= 2 && line <= 3 && text != null && !text.isEmpty()) {
            iPlayer.lockSign(selection, line, text);
            iPlayer.sendChat(I18n.formatText(LangKeys.MANU_LOCK));
            return CommandResult.success();
        }
        return CommandResult.empty();
    }
}
