package org.soraworld.locket.data;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.Locket;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.nms.Helper;
import org.soraworld.locket.util.Util;
import org.soraworld.violet.inject.Inject;

import java.util.HashSet;
import java.util.UUID;

/**
 * @author Himmelt
 */
@Inject
public class LockData {

    private final HashSet<UUID> owners = new HashSet<>();
    private final HashSet<UUID> users = new HashSet<>();

    @Inject
    private static LocketManager manager;

    public LockData(@NotNull HashSet<Block> signs) {
        signs.forEach(sign -> Helper.touchSign(sign, data -> {
            if (manager.isPrivate(data.lines[0])) {
                Util.parseUuid(data.lines[1]).ifPresent(owners::add);
                Util.parseUuid(data.lines[2]).ifPresent(users::add);
                Util.parseUuid(data.lines[3]).ifPresent(users::add);
            }
            return false;
        }));
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
