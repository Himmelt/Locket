package org.soraworld.locket.util;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Utils {

    private static Map<Player, Location> selectedSign = new HashMap<>();
    private static Set<Player> notified = new HashSet<>();

    public static void putSignOn(Player player, Location location, Direction face) {
        Location newSign = location.getRelative(face);
        newSign.setBlockType(BlockTypes.WALL_SIGN, BlockChangeFlag.NEIGHBOR, Cause.of(NamedCause.source(player))).setType(Material.WALL_SIGN);
        byte data;
        // So this part is pretty much a Bukkit bug:
        // Signs' rotation is not correct with bukkit's set facing, below is the workaround.
        switch (face) {
            case NORTH:
                data = 2;
                break;
            case EAST:
                data = 5;
                break;
            case WEST:
                data = 4;
                break;
            case SOUTH:
                data = 3;
                break;
            default:
                return;
        }
        newSign.setData(data, true);
        updateSign(newSign);
        Sign sign = (Sign) newSign.getState();
        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.update();
    }

    public static void setSignLine(Block block, int line, String text) {
        // Requires isSign
        Sign sign = (Sign) block.getState();
        sign.setLine(line, text);
        sign.update();
    }

    public static void removeASign(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (player.getItemInHand().getAmount() == 1) {
            player.setItemInHand(null);
        } else {
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
        }
    }

    public static void updateSign(Block block) {
        block.getState().update();
    }

    public static Block getSelectedSign(Player player) {
        return selectedSign.get(player);
    }

    public static void selectSign(Player player, Location block) {
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
