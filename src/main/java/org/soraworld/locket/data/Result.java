package org.soraworld.locket.data;

/**
 * 告示牌访问结果.
 */
public enum Result {
    /**
     * 用户.
     */
    SIGN_USER(false, true),
    /**
     * 所有者.
     */
    SIGN_OWNER(true, true),
    /**
     * 没有有效锁.
     */
    SIGN_NOT_LOCK(true, true),
    /**
     * 多重所有者.
     */
    SIGN_M_OWNERS(false, false),
    /**
     * 没有权限,既不是所有者也不是用户.
     */
    SIGN_NO_ACCESS(false, false),
    /**
     * 多重箱子(双联方块).
     */
    M_BLOCKS(false, false),
    /**
     * 被其他插件或 Mod 保护.
     */
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
