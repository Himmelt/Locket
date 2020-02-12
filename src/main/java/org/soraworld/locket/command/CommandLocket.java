package org.soraworld.locket.command;

import org.soraworld.locket.Locket;
import org.soraworld.locket.manager.IManager;
import org.soraworld.violet.api.ICommandSender;
import org.soraworld.violet.api.IPlayer;
import org.soraworld.violet.command.CommandCore;
import org.soraworld.violet.command.SubExecutor;
import org.soraworld.violet.command.TabExecutor;
import org.soraworld.violet.inject.Cmd;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.inject.Tab;

import java.util.List;

/**
 * @author Himmelt
 */
@Cmd(name = Locket.PLUGIN_ID, aliases = {"lock"}, usage = "usage.lock")
public final class CommandLocket {

    @Inject
    private IManager manager;

    @Cmd(name = ".", perm = "locket.lock")
    public final SubExecutor<IPlayer> lock = (cmd, player, args) -> manager.exeLock(player);

    @Tab(path = ".")
    public final TabExecutor<IPlayer> lock_tab = (cmd, player, args) -> {
        if (args.size() <= 1) {
            List<String> list = cmd.tabComplete0(player, args);
            if (args.first().isEmpty()) {
                list.add(0, "3");
                list.add(0, "2");
            }
            return list;
        }
        CommandCore sub = cmd.getSub(args.first());
        if (sub != null) {
            return sub.tabComplete(player, args.next());
        }
        return manager.getMatchedPlayers(args.get(1));
    };

    @Cmd(perm = "locket.lock", usage = "usage.remove")
    public final SubExecutor<IPlayer> remove = (cmd, player, args) -> manager.exeRemove(player, args);

    @Cmd(perm = "admin")
    public final SubExecutor<IPlayer> info = (cmd, player, args) -> manager.exeInfo(player);

    @Cmd(name = "type.+", perm = "admin", tabs = {"look"})
    public final SubExecutor<ICommandSender> type_plus = (cmd, sender, args) -> manager.processType(sender, args, "typeAdd");

    @Cmd(name = "type.-", perm = "admin", tabs = {"look"})
    public final SubExecutor<ICommandSender> type_minus = (cmd, sender, args) -> manager.processType(sender, args, "typeRemove");

    @Cmd(name = "type.++", perm = "admin", tabs = {"look"})
    public final SubExecutor<ICommandSender> type_dplus = (cmd, sender, args) -> manager.processType(sender, args, "dTypeAdd");

    @Cmd(name = "type.--", perm = "admin", tabs = {"look"})
    public final SubExecutor<ICommandSender> type_dminus = (cmd, sender, args) -> manager.processType(sender, args, "dTypeRemove");
}
