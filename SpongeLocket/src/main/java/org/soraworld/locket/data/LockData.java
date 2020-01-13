package org.soraworld.locket.data;

import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.util.ChatColor;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.UUID;

import static org.soraworld.locket.manager.LocketManager.parseUUID;

/**
 * @author Himmelt
 */
@Inject
public class LockData {

    private final HashSet<UUID> owners = new HashSet<>();
    private final HashSet<UUID> users = new HashSet<>();

    @Inject
    private static LocketManager manager;

    public LockData(@NotNull HashSet<Location<World>> signs) {
        signs.forEach(sign -> sign.getTileEntity().ifPresent(tile -> {
            if (tile instanceof Sign) {
                ListValue<Text> lines = ((Sign) tile).lines();
                String line0 = ChatColor.stripAllColor(lines.get(0).toPlain()).trim();
                if (manager.isPrivate(line0)) {
                    String raw1 = lines.get(1).toPlain().trim();
                    try {
                        UUID owner = parseUUID(raw1.substring(raw1.length() - 64).replace(ChatColor.TRUE_COLOR_STRING, ""));
                        if (owner != null) {
                            owners.add(owner);
                        }
                    } catch (Throwable ignored) {
                    }
                    String raw2 = lines.get(2).toPlain().trim();
                    try {
                        UUID user = parseUUID(raw2.substring(raw2.length() - 64).replace(ChatColor.TRUE_COLOR_STRING, ""));
                        if (user != null) {
                            users.add(user);
                        }
                    } catch (Throwable ignored) {
                    }
                    String raw3 = lines.get(3).toPlain().trim();
                    try {
                        UUID user = parseUUID(raw3.substring(raw3.length() - 64).replace(ChatColor.TRUE_COLOR_STRING, ""));
                        if (user != null) {
                            users.add(user);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
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
