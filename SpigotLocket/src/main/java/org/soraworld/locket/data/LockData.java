package org.soraworld.locket.data;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.nms.Helper;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.util.ChatColor;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Himmelt
 */
@Inject
public class LockData {

    private final HashSet<UUID> owners = new HashSet<>();
    private final HashSet<UUID> users = new HashSet<>();

    @Inject
    private static LocketManager manager;
    private static final Pattern HIDE_UUID = Pattern.compile("(\u00A7[0-9a-f]){32}");

    public LockData(@NotNull HashSet<Block> signs) {
        signs.forEach(sign -> Helper.touchSign(sign, data -> {
            if (manager.isPrivate(data.line0)) {
                parseUuid(data.line1).ifPresent(owners::add);
                parseUuid(data.line2).ifPresent(users::add);
                parseUuid(data.line3).ifPresent(users::add);
                manager.asyncUpdateSign(sign);
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

    private static Optional<UUID> parseUuid(String text) {
        Matcher matcher = HIDE_UUID.matcher(text);
        if (matcher.find()) {
            String hex = matcher.group().replace(ChatColor.TRUE_COLOR_STRING, "");
            if (hex.length() == 32) {
                long most = Long.parseUnsignedLong(hex.substring(0, 16), 16);
                long least = Long.parseUnsignedLong(hex.substring(16), 16);
                return Optional.of(new UUID(most, least));
            }
        }
        return Optional.empty();
    }
}
