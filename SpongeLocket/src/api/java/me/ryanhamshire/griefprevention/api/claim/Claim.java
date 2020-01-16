package me.ryanhamshire.griefprevention.api.claim;

import java.util.UUID;

/**
 * The interface Claim.
 *
 * @author bloodmc
 */
public interface Claim {
    /**
     * Is user trusted boolean.
     *
     * @param uuid the uuid
     * @param type the type
     * @return the boolean
     */
    boolean isUserTrusted(UUID uuid, TrustType type);
}
