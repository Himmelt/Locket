package org.soraworld.locket.util;

import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashMap;

public class SignUtil {

    private static HashMap<Player, Sign> signsMap = new HashMap<>();


    public static Sign getSelected(Player player) {
        return signsMap.get(player);
    }

    public static void setSelected(Player player, Sign sign) {
        signsMap.put(player, sign);
    }

    public static boolean canLock(Sign sign) {
        return sign.lines().get(0).toPlain().contains("Private");
    }

    public static boolean moreLock(Sign sign) {
        return sign.lines().get(0).toPlain().contains("More");
    }
}
