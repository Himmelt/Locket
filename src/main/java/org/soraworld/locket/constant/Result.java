package org.soraworld.locket.constant;

public enum Result {
    USER,             // 用户
    OWNER,            // 所有者
    NO_LOCK,          // 没有.lock权限
    NOT_LOCK,         // 没有有效锁
    M_OWNERS,         // 多重所有者
    M_CHESTS,         // 多重箱子
    CANT_TYPE,       // 不可锁类型
    NO_ACCESS,        // 没有权限,既不是所有者也不是用户
    CANT_UNLOCK,      // 不能用破坏方式解锁
    ADMIN_BREAK,      // 管理权限:强制破坏方块
    ADMIN_UNLOCK,     // 管理权限:强制破坏锁牌
    ADMIN_INTERACT    // 管理权限:强制交互
}
