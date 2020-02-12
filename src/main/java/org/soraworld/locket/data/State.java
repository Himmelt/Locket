package org.soraworld.locket.data;

import java.util.UUID;

/**
 * The type State.
 *
 * @author Himmelt
 */
public class State {

    private final UUID uuid;

    /**
     * The constant NOT_LOCKED.
     */
    public static final State NOT_LOCKED = new State(null);
    /**
     * The constant MULTI_OWNERS.
     */
    public static final State MULTI_OWNERS = new State(null);
    /**
     * The constant MULTI_BLOCKS.
     */
    public static final State MULTI_BLOCKS = new State(null);

    /**
     * Instantiates a new State.
     *
     * @param owner the owner
     */
    public State(UUID owner) {
        this.uuid = owner;
    }

    /**
     * Same owner to boolean.
     *
     * @param target the target
     * @return the boolean
     */
    public boolean sameOwnerTo(State target) {
        if (target == null || uuid == null || target.uuid == null) {
            return false;
        }
        return uuid.equals(target.uuid);
    }
}
