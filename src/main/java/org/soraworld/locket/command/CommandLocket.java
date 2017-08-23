package org.soraworld.locket.command;

import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
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
        if (source instanceof Player) {
            Player player = (Player) source;
            IPlayer iPlayer = LocketAPI.getPlayer(player);
            Location<World> sign = iPlayer.selection();
            Integer line = args.<Integer>getOne("line").orElse(1);
            Text text = args.<Text>getOne("name").orElse(Text.of(""));
            if (sign == null) {
                player.sendMessage(Text.of("no-sign-selected"));
            } else if (!(player.hasPermission("locket.edit.admin") || iPlayer.canLock(iPlayer.selection()) == Result.OWNER)) {
                player.sendMessage(Text.of("sign-need-reselect"));
            } else if (iPlayer.canLock(sign) == Result.OWNER) {
                switch (line) {
                    case 1:
                        player.sendMessage(Text.of("cannot-change-this-line"));
                        break;
                    case 2:
                        if (!player.hasPermission("locket.admin.edit")) {
                            player.sendMessage(Text.of("cannot-change-this-line"));
                            break;
                        }
                    case 3:
                    case 4:
                        //sign.getSignData().setElement(line - 1, text);
                        player.sendMessage(Text.of("sign-changed"));
                        return CommandResult.success();
                    default:
                        player.sendMessage(Text.of("行数输入无效"));
                }
            } else {
                player.sendMessage(Text.of("无效锁牌"));
            }
        } else {
            source.sendMessage(Text.of("This command can only be executed by an in-game player."));
        }
        return CommandResult.empty();
    }
}
