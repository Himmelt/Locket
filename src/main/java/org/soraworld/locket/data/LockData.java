package org.soraworld.locket.data;

import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.Locket;
import org.soraworld.locket.manager.IManager;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.world.BlockPos;

import java.util.HashSet;
import java.util.UUID;

/**
 * @author Himmelt
 */
@Inject
public final class LockData {

    private final HashSet<UUID> owners = new HashSet<>();
    private final HashSet<UUID> users = new HashSet<>();

    @Inject
    private static IManager manager;

    public LockData(@NotNull HashSet<BlockPos> signs) {
        signs.forEach(sign -> manager.touchSign(sign, data -> {
            if (manager.isPrivate(data.lines[0])) {
                Locket.parseUuid(data.lines[1]).ifPresent(owners::add);
                Locket.parseUuid(data.lines[2]).ifPresent(users::add);
                Locket.parseUuid(data.lines[3]).ifPresent(users::add);
            }
            return false;
        }, null));
    }

    public Result tryAccess(@NotNull UUID uuid) {
        if (owners.size() <= 0) {
            return Result.NOT_LOCKED;
        }
        if (owners.size() >= 2) {
            return Result.MULTI_OWNERS;
        }
        if (owners.contains(uuid)) {
            return Result.SIGN_OWNER;
        }
        if (users.contains(uuid)) {
            return Result.SIGN_USER;
        }
        return Result.LOCKED;
    }

    public State getState() {
        if (owners.size() <= 0) {
            return State.NOT_LOCKED;
        }
        if (owners.size() >= 2) {
            return State.MULTI_OWNERS;
        }
        return new State(owners.iterator().next());
    }
}
