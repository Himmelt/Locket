package org.soraworld.locket.util;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class Utils {

    public static Location<World> getAttached(Location<World> location) {
        return location.getRelative(location.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite());
    }

}
