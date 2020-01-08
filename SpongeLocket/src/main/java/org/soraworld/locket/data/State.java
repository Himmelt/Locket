package org.soraworld.locket.data;

public class State {

    private final String owner;

    public static final State NOT_LOCKED = new State(null);
    public static final State MULTI_OWNERS = new State(null);
    public static final State MULTI_BLOCKS = new State(null);

    public State(String owner) {
        this.owner = owner;
    }

    public boolean sameOwnerTo(State target) {
        if (target == null || owner == null || target.owner == null || owner.isEmpty() || target.owner.isEmpty()) return false;
        return owner.equals(target.owner);
    }
}
