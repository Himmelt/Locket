package org.soraworld.locket.command;

import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.I18n;
import org.soraworld.locket.constant.LangKeys;
import org.soraworld.locket.constant.Perms;
import org.soraworld.locket.constant.Result;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class CommandSpecs {

    private static final CommandSpec CMD_LOCKET_TYPE = CommandSpec.builder()
            .description(Text.of("Locket type +/-/++/--."))
            .permission(Perms.ADMIN_CONFIG)
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("action"))),
                    GenericArguments.optional(GenericArguments.catalogedElement(Text.of("block"), BlockType.class)))
            .executor((source, args) -> {
                String action = args.<String>getOne("action").orElse(null);
                Text HEAD = LocketAPI.CONFIG.HEAD();
                if (action != null && (action.equals("+") || action.equals("++") || action.equals("-") || action.equals("--"))) {
                    BlockType type = args.<BlockType>getOne("block").orElse(null);
                    if (type != null && type != BlockTypes.AIR && type != BlockTypes.WALL_SIGN && type != BlockTypes.STANDING_SIGN) {
                        switch (action) {
                            case "+":
                                LocketAPI.CONFIG.addType(type);
                                source.sendMessage(HEAD.concat(I18n.formatText(LangKeys.TYPE_ADD_SUCCESS)));
                                break;
                            case "++":
                                LocketAPI.CONFIG.addDType(type);
                                source.sendMessage(HEAD.concat(I18n.formatText(LangKeys.DTYPE_ADD_SUCCESS)));
                                break;
                            case "-":
                                LocketAPI.CONFIG.removeType(type);
                                source.sendMessage(HEAD.concat(I18n.formatText(LangKeys.TYPE_REMOVE_SUCCESS)));
                                break;
                            case "--":
                                LocketAPI.CONFIG.removeDType(type);
                                source.sendMessage(HEAD.concat(I18n.formatText(LangKeys.DTYPE_REMOVE_SUCCESS)));
                                break;
                        }
                        LocketAPI.CONFIG.save();
                        return CommandResult.success();
                    }
                    if (type == null) {
                        if (source instanceof Player) {
                            IPlayer iPlayer = LocketAPI.getPlayer((Player) source);
                            BlockType block = iPlayer.getHeldBlockType();
                            if (block != BlockTypes.AIR && block != BlockTypes.WALL_SIGN && block != BlockTypes.STANDING_SIGN) {
                                LocketAPI.CONFIG.addType(block);
                                source.sendMessage(HEAD.concat(I18n.formatText(LangKeys.TYPE_ADD_SUCCESS)));
                                LocketAPI.CONFIG.save();
                                return CommandResult.success();
                            }
                        } else {
                            source.sendMessage(HEAD.concat(I18n.formatText(LangKeys.ONLY_PLAYER)));
                        }
                    }
                }
                return CommandResult.empty();
            })
            .build();
    private static final CommandSpec CMD_LOCKET_RELOAD = CommandSpec.builder()
            .description(Text.of("Locket reload config."))
            .permission(Perms.ADMIN_CONFIG)
            .executor((source, args) -> {
                LocketAPI.CONFIG.load();
                source.sendMessage(LocketAPI.CONFIG.HEAD().concat(I18n.formatText(LangKeys.CFG_RELOAD)));
                return CommandResult.success();
            })
            .build();
    private static final CommandSpec CMD_LOCKET_REMOVE = CommandSpec.builder()
            .description(Text.of("Locket remove line."))
            .permission(Perms.LOCK)
            .arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("line"))))
            .executor((source, args) -> {
                if (!(source instanceof Player)) {
                    source.sendMessage(LocketAPI.CONFIG.HEAD().concat(I18n.formatText(LangKeys.ONLY_PLAYER)));
                    return CommandResult.empty();
                }
                IPlayer iPlayer = LocketAPI.getPlayer((Player) source);
                Location<World> selection = iPlayer.selection();
                if (selection == null) {
                    iPlayer.sendChat(I18n.formatText(LangKeys.SELECT_FIRST));
                    return CommandResult.empty();
                }
                Integer line = args.<Integer>getOne("line").orElse(null);
                if (line == null || (line != 3 && line != 4)) {
                    iPlayer.sendChat(I18n.formatText(LangKeys.CANT_UNLOCK));
                    return CommandResult.empty();
                }
                if (iPlayer.hasPerm(Perms.ADMIN_LOCK)) {
                    iPlayer.unLockSign(selection, line);
                    iPlayer.adminNotify(I18n.formatText(LangKeys.USING_ADMIN_PERM));
                    return CommandResult.success();
                }
                if (!iPlayer.hasPerm(Perms.LOCK)) {
                    iPlayer.sendChat(I18n.formatText(LangKeys.NEED_PERM, Perms.LOCK));
                    return CommandResult.empty();
                }
                Result result = iPlayer.tryAccess(selection);
                if (result == Result.SIGN_OWNER) {
                    iPlayer.unLockSign(selection, line);
                    iPlayer.sendChat(I18n.formatText(LangKeys.MANU_REMOVE));
                    return CommandResult.success();
                }
                iPlayer.sendChat(I18n.formatText(LangKeys.CANT_REMOVE));
                return CommandResult.empty();
            })
            .build();

    public static final CommandSpec CMD_LOCKET = CommandSpec.builder()
            .description(Text.of("Locket"))
            .permission(Perms.LOCK)
            .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("line"))),
                    GenericArguments.optional(GenericArguments.string(Text.of("name"))))
            .child(CMD_LOCKET_TYPE, "type")
            .child(CMD_LOCKET_REMOVE, "remove")
            .child(CMD_LOCKET_RELOAD, "reload")
            .executor((source, args) -> {
                if (!(source instanceof Player)) {
                    source.sendMessage(I18n.formatText(LangKeys.ONLY_PLAYER));
                    return CommandResult.empty();
                }
                IPlayer iPlayer = LocketAPI.getPlayer((Player) source);
                Location<World> selection = iPlayer.selection();
                if (selection == null) {
                    iPlayer.sendChat(I18n.formatText(LangKeys.SELECT_FIRST));
                    return CommandResult.empty();
                }
                Integer line = args.<Integer>getOne("line").orElse(null);
                String name = args.<String>getOne("name").orElse(null);
                if (iPlayer.hasPerm(Perms.ADMIN_LOCK)) {
                    iPlayer.lockSign(selection, line, name);
                    iPlayer.adminNotify(I18n.formatText(LangKeys.USING_ADMIN_PERM));
                    return CommandResult.success();
                }
                if (!LocketAPI.isLockable(LocketAPI.getAttached(selection))) {
                    iPlayer.sendChat(I18n.formatText(LangKeys.CANT_LOCK));
                    return CommandResult.empty();
                }
                if (!iPlayer.hasPerm(Perms.LOCK)) {
                    iPlayer.sendChat(I18n.formatText(LangKeys.NEED_PERM, Perms.LOCK));
                    return CommandResult.empty();
                }
                Result result = iPlayer.tryAccess(selection);
                if (result == Result.SIGN_OWNER || result == Result.SIGN_NOT_LOCK) {
                    iPlayer.lockSign(selection, line, name);
                    iPlayer.sendChat(I18n.formatText(LangKeys.MANU_LOCK));
                    return CommandResult.success();
                }
                iPlayer.sendChat(I18n.formatText(LangKeys.CANT_LOCK));
                return CommandResult.empty();
            })
            .build();
}
