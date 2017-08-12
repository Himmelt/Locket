package org.soraworld.locket.util;

import org.soraworld.locket.Locket;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Utils {

    private static Map<Player, Location<World>> selectedSign = new HashMap<>();
    private static Set<Player> notified = new HashSet<>();

    public static void putSignPrivate(Player player, Location<World> location, Direction face) {
        Location<World> newSign = location.getRelative(face);
        newSign.setBlockType(BlockTypes.WALL_SIGN, Cause.source(Locket.getLocket()).build());
        //newSign.setBlockType(BlockTypes.WALL_SIGN, BlockChangeFlag.NEIGHBOR, Cause.of(NamedCause.source(player)));
        // So this part is pretty much a Bukkit bug:
        // Signs' rotation is not correct with bukkit's set facing, below is the workaround.
        newSign.getBlock().with(Keys.DIRECTION, face);
        updateSign(newSign);
        TileEntity tile = newSign.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            Sign sign = (Sign) tile;
            sign.getSignData().setElement(0, Text.of("[Private]"));
            sign.getSignData().setElement(1, Text.of(player.getName()));
        }
        //sign.update();
    }

    public static void putSignMore(Player player, Location<World> location, Direction face) {
        Location<World> newSign = location.getRelative(face);
        newSign.setBlockType(BlockTypes.WALL_SIGN, BlockChangeFlag.NEIGHBOR, Cause.of(NamedCause.source(player)));
        // So this part is pretty much a Bukkit bug:
        // Signs' rotation is not correct with bukkit's set facing, below is the workaround.
        newSign.getBlock().with(Keys.DIRECTION, face);
        updateSign(newSign);
        TileEntity tile = newSign.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            Sign sign = (Sign) tile;
            sign.getSignData().setElement(0, Text.of("[More]"));
            sign.getSignData().setElement(1, Text.of(player.getName()));
        }
        //sign.update();
    }

    public static void removeASign(Player player, HandType hand) {
        if (player.gameMode() == GameModes.CREATIVE) return;
        if (player.getItemInHand(hand).get().getQuantity() <= 1) {
            player.setItemInHand(hand, null);
        } else {
            player.getItemInHand(hand).get().setQuantity(player.getItemInHand(hand).get().getQuantity() - 1);
        }
    }

    public static void updateSign(Location<World> block) {
        block.addScheduledUpdate(3, 5);
        //.getState().update();
    }

    public static Location<World> getSelectedSign(Player player) {
        return selectedSign.get(player);
    }

    public static void selectSign(Player player, Location<World> block) {
        selectedSign.put(player, block);
    }

    public static void sendMessages(CommandSource sender, String messages) {
        if (messages == null || messages.equals("")) return;
        sender.sendMessage(Text.of(messages));
    }

    public static boolean shouldNotify(Player player) {
        if (notified.contains(player)) {
            return false;
        } else {
            notified.add(player);
            return true;
        }
    }

    public static boolean isUsernameUuidLine(String text) {
        if (text.contains("#")) {
            String[] splits = text.split("#", 2);
            if (splits[1].length() == 36) {
                return true;
            }
        }
        return false;
    }

    public static String getUsernameFromLine(String text) {
        if (isUsernameUuidLine(text)) {
            return text.split("#", 2)[0];
        } else {
            return text;
        }
    }

    public static boolean isPlayerOnLine(Player player, String text) {
        if (Utils.isUsernameUuidLine(text)) {
            return player.getName().equals(getUsernameFromLine(text));
        } else {
            return text.equals(player.getName());
        }
    }

}
