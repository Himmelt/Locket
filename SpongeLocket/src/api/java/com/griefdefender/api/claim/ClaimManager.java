package com.griefdefender.api.claim;

import com.flowpowered.math.vector.Vector3i;

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
     * @param pos the pos
     * @return the claim at
     */
    Claim getClaimAt(Vector3i pos);
}
