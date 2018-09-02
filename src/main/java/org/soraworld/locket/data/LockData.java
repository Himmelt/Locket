package org.soraworld.locket.data;

import java.util.HashSet;

public class LockData {

    private final HashSet<String> owners = new HashSet<>();
    private final HashSet<String> users = new HashSet<>();

    public void append(LockData data) {
        this.owners.addAll(data.owners);
        this.users.addAll(data.users);
    }

    public void puts(String owner, String user1, String user2) {
        owners.add(owner);
        users.add(user1);
        users.add(user2);
    }

    public Result getAccess(String username) {
        if (owners.size() <= 0) return Result.SIGN_NOT_LOCK;
        if (owners.size() >= 2) return Result.SIGN_M_OWNERS;
        if (owners.contains(username)) return Result.SIGN_OWNER;
        if (users.contains(username)) return Result.SIGN_USER;
        return Result.SIGN_NO_ACCESS;
    }

}
