package org.soraworld.locket.command;

import org.soraworld.locket.constant.Permissions;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public final class CommandSpecs {

    public static final CommandSpec CMD_LOCKET = CommandSpec.builder()
            .description(Text.of("locket"))
            .permission(Permissions.ROOT)
            .executor(new CommandLocket())
            .build();
}
