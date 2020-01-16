package com.griefdefender.api;

import com.griefdefender.api.claim.ClaimManager;

import java.util.UUID;

/**
 * The interface Core.
 *
 * @author bloodmc
 */
public interface Core {
    /**
     * Gets claim manager.
     *
     * @param worldUniqueId the world unique id
     * @return the claim manager
     */
    ClaimManager getClaimManager(UUID worldUniqueId);
}
