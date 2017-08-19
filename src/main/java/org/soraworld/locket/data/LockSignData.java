package org.soraworld.locket.data;

import org.soraworld.locket.constant.AccessResult;

import java.util.Collections;
import java.util.HashSet;

public class LockSignData {

    private final HashSet<String> owners = new HashSet<>();
    private final HashSet<String> users = new HashSet<>();

    public void append(LockSignData data) {
        this.owners.addAll(data.owners);
        this.users.addAll(data.users);
    }

    public void addOwner(String owner) {
        owners.add(owner);
    }

    public void addUser(String... users) {
        Collections.addAll(this.users, users);
    }

    public AccessResult getAccess(String username) {
        if (owners.size() <= 0) return AccessResult.NOT_LOCK;
        if (owners.size() >= 2) return AccessResult.M_OWNERS;
        if (owners.contains(username)) return AccessResult.OWNER;
        if (users.contains(username)) return AccessResult.USER;
        return AccessResult.NO_ACCESS;
    }
}
