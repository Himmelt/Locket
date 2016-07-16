package org.soraworld.locket.util;

/* Created by Himmelt on 2016/7/15.*/

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.soraworld.locket.log.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Utils {
    public static final String usernamePattern = "^[a-zA-Z0-9_]*$";

    private static Map<Player, Block> selectedSign = new HashMap<>();
    private static Set<Player> notified = new HashSet<>();

    // Helper functions
    @SuppressWarnings("deprecation")
    public static void putSignOn(Block block, BlockFace blockface, String line1, String line2) {
        Block newSign = block.getRelative(blockface);
        newSign.setType(Material.WALL_SIGN);
        byte data;
        // So this part is pretty much a Bukkit bug:
        // Signs' rotation is not correct with bukkit's set facing, below is the workaround.
        switch (blockface) {
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

    public static void selectSign(Player player, Block block) {
        selectedSign.put(player, block);
    }

    public static void sendMessages(CommandSender sender, String messages) {
        if (messages == null || messages.equals("")) return;
        sender.sendMessage(messages);
    }

    public static boolean shouldNotify(Player player) {
        if (notified.contains(player)) {
            return false;
        } else {
            notified.add(player);
            return true;
        }
    }

    public static void updateLineByPlayer(Block block, int line, Player player) {
        setSignLine(block, line, player.getName() + "#" + player.getUniqueId().toString());
    }

    public static boolean isUserName(String text) {
        return text.length() < 17 && text.length() > 2 && text.matches(usernamePattern);
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

    public static String getSignLineFromUnknown(String json) {
        try { // 1.8-
            JsonObject line = new JsonParser().parse(json).getAsJsonObject();
            return line.get("extra").getAsJsonArray().get(0).getAsString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.info(ex.toString());
        }
        try { // 1.9+
            JsonObject line = new JsonParser().parse(json).getAsJsonObject();
            return line.get("extra").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.info(ex.toString());
        }
        return json;
    }

}
