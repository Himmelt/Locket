package org.soraworld.locket.data;

import java.util.UUID;

public class State {

    private final UUID uuid;

    public static final State NOT_LOCKED = new State(null);
    public static final State MULTI_OWNERS = new State(null);
    public static final State MULTI_BLOCKS = new State(null);

    public State(UUID owner) {
        this.uuid = owner;
    }

    public boolean sameOwnerTo(State target) {
        if (target == null || uuid == null || target.uuid == null) {
            return false;
        }
        return uuid.equals(target.uuid);
    }
}
