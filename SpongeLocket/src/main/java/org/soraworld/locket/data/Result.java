package org.soraworld.locket.data;

public enum Result {
    SIGN_USER(false, true),
    SIGN_OWNER(true, true),
    LOCKED(false, false),
    NOT_LOCKED(true, true),
    MULTI_OWNERS(false, false),
    MULTI_BLOCKS(false, false),
    OTHER_PROTECT(false, false);

    private boolean edit, use;

    Result(boolean edit, boolean use) {
        this.edit = edit;
        this.use = use;
    }

    public boolean canEdit() {
        return edit;
    }

    public boolean canUse() {
        return use;
    }
}
