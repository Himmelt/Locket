package org.soraworld.locket.data;

import org.jetbrains.annotations.NotNull;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.util.ChatColor;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;

/**
 * @author Himmelt
 */
@Inject
public class LockData {

    private final HashSet<String> owners = new HashSet<>();
    private final HashSet<String> users = new HashSet<>();

    @Inject
    private static LocketManager manager;

    public LockData(@NotNull HashSet<Location<World>> signs) {
        signs.forEach(sign -> sign.getTileEntity().ifPresent(tile -> {
            if (tile instanceof Sign) {
                ListValue<Text> lines = ((Sign) tile).lines();
                // TODO support for UUID
                String line0 = ChatColor.stripAllColor(lines.get(0).toPlain()).trim();
                String line1 = ChatColor.stripAllColor(lines.get(1).toPlain()).trim();
                String line2 = ChatColor.stripAllColor(lines.get(2).toPlain()).trim();
                String line3 = ChatColor.stripAllColor(lines.get(3).toPlain()).trim();
                if (manager.isPrivate(line0)) {
                    owners.add(line1);
                    users.add(line2);
                    users.add(line3);
                }
            }
        }));
    }

    public Result tryAccess(@NotNull Player player) {
        if (owners.size() <= 0) {
            return Result.NOT_LOCKED;
        }
        if (owners.size() >= 2) {
            return Result.MULTI_OWNERS;
        }
        if (owners.contains(player.getName())) {
            return Result.SIGN_OWNER;
        }
        if (users.contains(player.getName())) {
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
