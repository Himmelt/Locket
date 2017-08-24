package org.soraworld.locket.constant;

public enum Result {
    SIGN_USER,             // 用户
    SIGN_OWNER,            // 所有者
    SIGN_NOT_LOCK,         // 没有有效锁
    SIGN_M_OWNERS,         // 多重所有者
    SIGN_NO_ACCESS,        // 没有权限,既不是所有者也不是用户
    M_CHESTS         // 多重箱子
}
