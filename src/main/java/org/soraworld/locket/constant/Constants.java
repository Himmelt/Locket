package org.soraworld.locket.constant;

import org.soraworld.locket.Locket;
import org.spongepowered.api.event.cause.Cause;

public final class Constants {
    public static final String MODID = "locket";
    public static final String NAME = "Locket";
    public static final String VERSION = "1.0.4";

    public static final Cause PLUGIN_CAUSE = Cause.source(Locket.getLocket().getPlugin()).build();


}
