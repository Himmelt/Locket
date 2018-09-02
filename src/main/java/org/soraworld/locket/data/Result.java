package org.soraworld.locket.data;

public enum Result {
    SIGN_USER,             // 用户
    SIGN_OWNER,            // 所有者
    SIGN_NOT_LOCK,         // 没有有效锁
    SIGN_M_OWNERS,         // 多重所有者
    SIGN_NO_ACCESS,        // 没有权限,既不是所有者也不是用户
    OTHER_PROTECT, M_BLOCKS         // 多重箱子(双联方块)
}
