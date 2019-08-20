package me.ryanhamshire.griefprevention;

import me.ryanhamshire.griefprevention.api.GriefPreventionApi;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class GriefPrevention {
    public static GriefPreventionApi getApi() {
        return world -> new ClaimManager() {
            @Override
            public Claim getWildernessClaim() {
                return null;
            }

            @Override
            public Claim getClaimAt(Location<World> location) {
                return null;
            }
        };
    }
}
