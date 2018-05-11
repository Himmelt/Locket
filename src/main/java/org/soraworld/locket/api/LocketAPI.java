package org.soraworld.locket.api;

import org.slf4j.Logger;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.Perms;
import org.soraworld.locket.core.WrappedPlayer;
import org.soraworld.locket.data.LockSignData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocketAPI {

    public static Config CONFIG;
    public static Logger LOGGER;
    public static PluginContainer PLUGIN;
    private static final HashMap<Player, WrappedPlayer> players = new HashMap<>();
    private static final Direction[] FACES = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public static WrappedPlayer getPlayer(Player player) {
        WrappedPlayer iPlayer = players.get(player);
        if (iPlayer == null) {
            iPlayer = new WrappedPlayer(player);
            players.put(player, iPlayer);
        }
        return iPlayer;
    }

    public static void removePlayer(Player player) {
        players.remove(player);
    }

    public static boolean isLockable(Location<World> location) {
        return CONFIG.isLockable(location);
    }

    public static boolean isDuplex(BlockType type) {
        return CONFIG.isDBlock(type);
    }

    public static List<Location<World>> getSideSigns(Location<World> loc) {
        byte count = 0;
        Location<World> link = null, side;
        BlockType type = loc.getBlockType();
        boolean duplex = isDuplex(type);
        ArrayList<Location<World>> signs = new ArrayList<>();

        if (type == BlockTypes.WALL_SIGN || type == BlockTypes.STANDING_SIGN) signs.add(loc);
        for (Direction face : FACES) {
            side = loc.getRelative(face);
            if (duplex && side.getBlockType() == type) {
                link = side;
                if (++count >= 2) {
                    notifyAdmins("Found multi-blocks at " + loc);
                    return null;
                }
            } else if (side.getBlockType() == BlockTypes.WALL_SIGN && side.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(side);
            }
        }
        if (duplex && link != null) {
            count = 0;
            for (Direction face : FACES) {
                side = link.getRelative(face);
                if (side.getBlockType() == type && ++count >= 2) {
                    notifyAdmins("Found multi-blocks at " + loc);
                    return null;
                }
                if (side.getBlockType() == BlockTypes.WALL_SIGN && side.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(side);
                }
            }
        }
        return signs;
    }

    public static boolean isLocked(@Nonnull Location<World> loc) {
        List<Location<World>> signs = getSideSigns(loc);
        if (signs == null) return true;
        for (Location<World> sign : signs) {
            TileEntity tile = sign.getTileEntity().orElse(null);
            if (tile instanceof Sign && isPrivate((Sign) tile)) return true;
        }
        return false;
    }

    public static boolean isPrivate(@Nonnull Sign sign) {
        return sign.lines().size() >= 1 && isPrivate(sign.lines().get(0).toPlain());
    }

    public static boolean isPrivate(String text) {
        return CONFIG.isPrivate(text);
    }

    public static void notifyAdmins(String text) {
        if (CONFIG.isAdminNotify()) {
            Sponge.getServer().getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(Perms.ADMIN))
                    .forEach(player -> player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(text)));
        }
    }

    private static void notifyAdmins(Text text) {
        if (CONFIG.isAdminNotify()) {
            Sponge.getServer().getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(Perms.ADMIN))
                    .forEach(player -> player.sendMessage(text));
        }
    }

    public static Location<World> getAttached(Location<World> selection) {
        return null;
    }

    public static LockSignData parseSign(Sign tile) {
        return null;
    }

    public static Text formatText(String line_2) {
        return null;
    }
}
