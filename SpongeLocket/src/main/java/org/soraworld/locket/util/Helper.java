package org.soraworld.locket.util;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class Helper {
    @Nullable
    public static Location<World> getLookAt(Player player, double distance) {
        BlockRay<World> ray = BlockRay.from(player)
                .skipFilter(BlockRay.onlyAirFilter())
                .stopFilter(BlockRay.allFilter()).build();
        if (ray.hasNext()) {
            return ray.next().getLocation();
        } else {
            return null;
        }
    }
}
