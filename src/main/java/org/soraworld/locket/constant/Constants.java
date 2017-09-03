package org.soraworld.locket.constant;

import com.google.common.reflect.TypeToken;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class Constants {
    public static final String MODID = "locket";
    public static final String NAME = "Locket";
    public static final String VERSION = "1.0.6";
    public static final Text DEFAULT_PRIVATE = Text.of("[Private]");
    public static final Cause PLUGIN_CAUSE = Cause.source(LocketAPI.PLUGIN).build();

    public static final List<Direction> FACES = Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public static final TypeToken<HashMap<String, String>> TOKEN_HASH_MAP = new TypeToken<HashMap<String, String>>() {
    };
    public static final TypeToken<Config> TOKEN_CONFIG = TypeToken.of(Config.class);
}
