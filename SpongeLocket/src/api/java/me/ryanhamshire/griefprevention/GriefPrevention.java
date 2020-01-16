package me.ryanhamshire.griefprevention;

import me.ryanhamshire.griefprevention.api.GriefPreventionApi;

/**
 * The type Grief prevention.
 *
 * @author bloodmc
 */
public class GriefPrevention {
    /**
     * Gets api.
     *
     * @return the api
     */
    public static GriefPreventionApi getApi() {
        return world -> null;
    }
}
