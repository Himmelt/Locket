package org.soraworld.locket.command;

import org.soraworld.locket.constant.Perms;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public final class CommandSpecs {

    public static final CommandSpec CMD_LOCKET = CommandSpec.builder()
            .description(Text.of("locket"))
            .permission(Perms.LOCK)
            .arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("line"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("name"))))
            .executor(new CommandLocket())
            .build();
}
