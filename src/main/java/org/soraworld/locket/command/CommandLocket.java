package org.soraworld.locket.command;

import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
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
        LocketAPI.CONFIG.load();
        LocketAPI.CONFIG.save();
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
        if (iPlayer.hasPerm(Perms.ADMIN_LOCK)) {
            iPlayer.lockSign(selection);
            iPlayer.sendChat("ADMIN_LOCK!");
            return CommandResult.success();
        }
        if (iPlayer.hasPerm(Perms.LOCK) && iPlayer.tryAccess(selection) == Result.SIGN_OWNER) {
            iPlayer.lockSign(selection);
            iPlayer.sendChat("tryAccess!");
            return CommandResult.success();
        }
        Integer line = args.<Integer>getOne("line").orElse(1);
        String text = args.<String>getOne("name").orElse("");
        return CommandResult.empty();
    }
}
