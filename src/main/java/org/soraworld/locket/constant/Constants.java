package org.soraworld.locket.constant;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;

import java.util.Arrays;
import java.util.List;

public final class Constants {
    public static final String PLUGIN_NAME = "Locket";
    public static final String PLUGIN_VERSION = "1.0.7";
    public static final Text DEFAULT_PRIVATE = Text.of("[Private]");

    public static final List<Direction> FACES = Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
}
