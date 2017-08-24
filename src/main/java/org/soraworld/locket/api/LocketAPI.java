package org.soraworld.locket.api;

import org.soraworld.locket.Locket;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.data.LockSignData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
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

    private static final Locket locket = Locket.getLocket();
    private static final PluginContainer plugin = locket.getPlugin();

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

    public static IPlayer getPlayer(String name) {
        Player player = Sponge.getServer().getPlayer(name).orElse(null);
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

    public static boolean isLockable(Location location) {
        BlockType type = location.getBlockType();
        return type != BlockTypes.WALL_SIGN && type != BlockTypes.STANDING_SIGN && Config.isLockable(type);
    }

    public static boolean isPrivate(String line) {
        return getPrivate().equals(line);
    }

    public static Location<World> getAttached(Location<World> sign) {
        return sign.getRelative(sign.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite());
    }

    public static boolean isDChest(BlockType type) {
        return type == BlockTypes.CHEST || type == BlockTypes.TRAPPED_CHEST;
    }

    public static boolean isContainer(BlockType type) {
        return isDChest(type)
                || type == BlockTypes.FURNACE
                || type == BlockTypes.LIT_FURNACE
                || type == BlockTypes.BLACK_SHULKER_BOX
                || type == BlockTypes.BLUE_SHULKER_BOX
                || type == BlockTypes.BROWN_SHULKER_BOX
                || type == BlockTypes.CYAN_SHULKER_BOX
                || type == BlockTypes.GRAY_SHULKER_BOX
                || type == BlockTypes.GREEN_SHULKER_BOX
                || type == BlockTypes.LIME_SHULKER_BOX
                || type == BlockTypes.MAGENTA_SHULKER_BOX
                || type == BlockTypes.ORANGE_SHULKER_BOX
                || type == BlockTypes.PINK_SHULKER_BOX
                || type == BlockTypes.PURPLE_SHULKER_BOX
                || type == BlockTypes.RED_SHULKER_BOX
                || type == BlockTypes.SILVER_SHULKER_BOX
                || type == BlockTypes.WHITE_SHULKER_BOX
                || type == BlockTypes.YELLOW_SHULKER_BOX
                || type == BlockTypes.BREWING_STAND
                || type == BlockTypes.DISPENSER
                || type == BlockTypes.HOPPER
                || type == BlockTypes.DROPPER;
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

    public static String getPrivate() {
        return "[Private]";
    }

}
