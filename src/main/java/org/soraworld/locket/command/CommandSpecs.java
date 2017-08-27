package org.soraworld.locket.command;

import org.soraworld.locket.constant.Perms;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public final class CommandSpecs {

    public static final CommandSpec CMD_LOCKET = CommandSpec.builder()
            .description(Text.of("locket"))
            .permission(Perms.LOCK)
            .executor(new CommandLocket())
            .build();
}
