package org.soraworld.locket.api;

import org.slf4j.Logger;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.constant.Result;
import org.soraworld.locket.data.LockSignData;
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
import java.util.HashMap;
import java.util.HashSet;

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

    public static Result isLocked(Location<World> block) {
        BlockType type = block.getBlockType();
        boolean isDBlock = LocketAPI.isDBlock(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        // 自身也将参与检查
        signs.add(block);

        // 检查4个方向是否是 WALL_SIGN 或 DChest
        for (Direction face : Constants.FACES) {
            Location<World> relative = block.getRelative(face);
            if (isDBlock && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) return Result.M_BLOCKS;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                signs.add(relative);
            }
        }
        // 检查相邻双联方块
        if (isDBlock && link != null) {
            count = 0;
            for (Direction face : Constants.FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return Result.M_BLOCKS;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    signs.add(relative);
                }
            }
        }

        if (signs.isEmpty()) return Result.SIGN_NOT_LOCK;
        LockSignData data = new LockSignData();
        for (Location<World> sign : signs) {
            TileEntity tile = sign.getTileEntity().orElse(null);
            if (tile != null && tile instanceof Sign) {
                data.append(parseSign((Sign) tile));
            }
        }
        return data.getAccess("Himmelt");
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

    static boolean isDBlock(BlockType type) {
        return CONFIG.isDBlock(type);
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

    public static Text formatText(String text) {
        return TextSerializers.FORMATTING_CODE.deserialize(text);
    }
}
