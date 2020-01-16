package org.soraworld.locket.data;

/**
 * The enum Result.
 *
 * @author Himmelt
 */
public enum Result {
    /**
     * Sign user result.
     */
    SIGN_USER(false, true),
    /**
     * Sign owner result.
     */
    SIGN_OWNER(true, true),
    /**
     * Locked result.
     */
    LOCKED(false, false),
    /**
     * Not locked result.
     */
    NOT_LOCKED(true, true),
    /**
     * Multi owners result.
     */
    MULTI_OWNERS(false, false),
    /**
     * Multi blocks result.
     */
    MULTI_BLOCKS(false, false),
    /**
     * Other protect result.
     */
    OTHER_PROTECT(false, false);

    private boolean edit, use;

    Result(boolean edit, boolean use) {
        this.edit = edit;
        this.use = use;
    }

    /**
     * Can edit boolean.
     *
     * @return the boolean
     */
    public boolean canEdit() {
        return edit;
    }

    /**
     * Can use boolean.
     *
     * @return the boolean
     */
    public boolean canUse() {
        return use;
    }
}
