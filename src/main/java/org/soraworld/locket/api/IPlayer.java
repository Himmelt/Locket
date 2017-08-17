package org.soraworld.locket.api;

import org.soraworld.locket.config.Config;
import org.soraworld.locket.constant.AccessResult;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;

import static org.soraworld.locket.api.LocketAPI.isPrivate;


public class IPlayer {

    private final String name;
    private final Player player;
    private Location<World> selected;

    public IPlayer(Player player) {
        this.player = player;
        this.name = player.getName();
    }

    public String getName() {
        return name;
    }

    public AccessResult canLock(Location<World> location) {
        if (location == null) return AccessResult.NULL;
        BlockType type = location.getBlockType();
        if (!Config.isLockable(type)) return AccessResult.UNLOCKABLE;

        boolean isDChest = Utils.isDChest(type);
        int count = 0;
        Location<World> link = null;
        HashSet<Location<World>> signs = new HashSet<>();

        for (Direction face : Constants.FACES) {
            Location<World> relative = location.getRelative(face);
            if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                signs.add(relative);
            }
        }

        for (Direction face : Constants.FACES) {
            Location<World> relative = location.getRelative(face);
            if (isDChest && relative.getBlockType() == type) {
                link = relative;
                if (++count >= 2) return AccessResult.M_CHESTS;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                signs.add(relative);
            }
        }
        // 相邻箱子
        if (isDChest && link != null) {
            count = 0;
            for (Direction face : Constants.FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return AccessResult.M_CHESTS;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                    signs.add(relative);
                }
            }
        }
        return analyzeSign(signs);
    }

    public AccessResult analyzeSign(HashSet<Location<World>> locations) {
        if (locations == null || locations.isEmpty()) return AccessResult.SUCCESS;
        HashSet<String> owners = new HashSet<>(), users = new HashSet<>();
        for (Location<World> location : locations) {
            TileEntity tile = location.getTileEntity().orElse(null);
            if (tile != null && tile instanceof Sign) {
                Sign sign = ((Sign) tile);
                String line = sign.lines().get(0).toPlain();
                String owner = sign.lines().get(1).toPlain();
                String user1 = sign.lines().get(2).toPlain();
                String user2 = sign.lines().get(3).toPlain();
                if (isPrivate(line)) {
                    owners.add(owner);
                    users.add(user1);
                    users.add(user2);
                }
            }
        }
        if (owners.size() >= 2) return AccessResult.M_OWNERS;
        if (owners.size() == 0 || owners.contains(name) || users.contains(name)) return AccessResult.SUCCESS;
        return AccessResult.FAILED;
    }

    public Location<World> getSelected() {
        return selected;
    }

    public void setSelected(Location<World> selected) {
        this.selected = selected;
    }
}
