package me.ryanhamshire.griefprevention.api.claim;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * The interface Claim manager.
 *
 * @author bloodmc
 */
public interface ClaimManager {
    /**
     * Gets wilderness claim.
     *
     * @return the wilderness claim
     */
    Claim getWildernessClaim();

    /**
     * Gets claim at.
     *
     * @param location the location
     * @return the claim at
     */
    Claim getClaimAt(Location<World> location);
}
