package org.soraworld.locket.data;

/**
 * 告示牌访问结果.
 */
public enum Result {
    /**
     * 用户.
     */
    SIGN_USER,
    /**
     * 所有者.
     */
    SIGN_OWNER,
    /**
     * 没有有效锁.
     */
    SIGN_NOT_LOCK,
    /**
     * 多重所有者.
     */
    SIGN_M_OWNERS,
    /**
     * 没有权限,既不是所有者也不是用户.
     */
    SIGN_NO_ACCESS,
    /**
     * 多重箱子(双联方块).
     */
    M_BLOCKS,
    /**
     * 被其他插件或 Mod 保护.
     */
    OTHER_PROTECT
}
