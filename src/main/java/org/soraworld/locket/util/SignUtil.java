package org.soraworld.locket.util;

import org.soraworld.locket.constant.Constants;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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

    public static void placeWallSign(Location<World> location, Direction face, Text... texts) {
        BlockState state = BlockTypes.WALL_SIGN.getDefaultState();
        location.setBlock(state.with(Keys.DIRECTION, face).orElse(state), Constants.PLUGIN_CAUSE);
        setSignText(location, texts);
        location.getExtent().playSound(SoundTypes.BLOCK_WOOD_PLACE, location.getPosition(), 1.0D);
    }

    public static void setSignText(Location<World> location, Text... texts) {
        TileEntity tile = location.getTileEntity().orElse(null);
        if (tile != null && texts != null && tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            for (int i = 0; i < texts.length && i < 4; i++) {
                data.setElement(i, texts[i]);
            }
            tile.offer(data);
        }
    }
}
