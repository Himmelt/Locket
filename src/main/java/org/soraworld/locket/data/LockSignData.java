package org.soraworld.locket.data;

import org.soraworld.locket.constant.Result;

import java.util.HashSet;

public class LockSignData {

    private final HashSet<String> owners = new HashSet<>();
    private final HashSet<String> users = new HashSet<>();

    public void append(LockSignData data) {
        this.owners.addAll(data.owners);
        this.users.addAll(data.users);
    }

    public void puts(String owner, String user1, String user2) {
        System.out.println("put owner:" + owner);
        owners.add(owner);
        users.add(user1);
        users.add(user2);
    }

    public Result getAccess(String username) {
        System.out.println("size" + owners.size());
        if (owners.size() <= 0) return Result.NOT_LOCK;
        if (owners.size() >= 2) return Result.M_OWNERS;
        for (String s : owners) {
            System.out.println("[" + s + "]");
        }
        System.out.println("[" + owners.contains(username) + "]");
        if (owners.contains(username)) return Result.OWNER;
        if (users.contains(username)) return Result.USER;
        return Result.NO_ACCESS;
    }

}
