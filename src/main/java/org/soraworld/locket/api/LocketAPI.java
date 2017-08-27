package org.soraworld.locket.api;

import org.slf4j.Logger;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.data.LockSignData;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class LocketAPI {

    public static Config CONFIG;
    public static Logger LOGGER;
    public static PluginContainer PLUGIN;
    private static final HashMap<Player, IPlayer> PLAYERS = new HashMap<>();

    public static IPlayer getPlayer(Player player) {
        if (PLAYERS.containsKey(player)) {
            return PLAYERS.get(player);
        } else {
            IPlayer iPlayer = new IPlayer(player);
            PLAYERS.put(player, iPlayer);
            return iPlayer;
        }
    }

    public static void removePlayer(Player player) {
        PLAYERS.remove(player);
    }

    public static boolean isLocked(Location<World> location) {
        BlockType type = location.getBlockType();
        return false;
    }

    public static boolean isLockable(Location<World> location) {
        return CONFIG.isLockable(location);
    }

    public static boolean isPrivate(String line) {
        return CONFIG.isPrivate(line);
    }

    public static Location<World> getAttached(Location<World> sign) {
        return sign.getRelative(sign.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite());
    }

    static boolean isDChest(BlockType type) {
        return CONFIG.isDChest(type);
    }

    static LockSignData parseSign(@Nonnull Sign sign) {
        LockSignData data = new LockSignData();
        String line_0 = sign.lines().get(0).toPlain();
        String line_1 = sign.lines().get(1).toPlain();
        String line_2 = sign.lines().get(2).toPlain();
        String line_3 = sign.lines().get(3).toPlain();
        if (isPrivate(line_0)) data.puts(line_1, line_2, line_3);
        return data;
    }

}
