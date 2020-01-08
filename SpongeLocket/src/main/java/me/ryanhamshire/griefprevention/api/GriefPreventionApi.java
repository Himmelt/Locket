package me.ryanhamshire.griefprevention.api;

import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import org.spongepowered.api.world.World;

public interface GriefPreventionApi {
    ClaimManager getClaimManager(World world);
}
