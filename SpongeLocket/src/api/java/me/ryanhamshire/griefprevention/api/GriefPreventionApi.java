package me.ryanhamshire.griefprevention.api;

import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import org.spongepowered.api.world.World;

/**
 * The interface Grief prevention api.
 *
 * @author bloodmc
 */
public interface GriefPreventionApi {
    /**
     * Gets claim manager.
     *
     * @param world the world
     * @return the claim manager
     */
    ClaimManager getClaimManager(World world);
}