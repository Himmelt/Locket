package me.ryanhamshire.griefprevention.api.claim;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface ClaimManager {
    Claim getWildernessClaim();

    Claim getClaimAt(Location<World> location);
}
