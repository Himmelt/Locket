package org.soraworld.locket.core;

import org.soraworld.locket.api.IPlayer;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.constant.Result;
import org.soraworld.locket.data.LockSignData;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;

public class WrappedPlayer implements IPlayer {

    private final String username;
    private final Player player;
    private Location<World> selected;
    private Result latest;
    private long last;
    private Location<World> target;
    private static final int delay = 1000;
    private static final Direction[] FACES = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public WrappedPlayer(@Nonnull Player player) {
        this.player = player;
        this.username = player.getName();
        this.latest = Result.SIGN_NO_ACCESS;
        this.last = System.currentTimeMillis();
    }

    public boolean canAccess(Location<World> loc) {
        if (otherProtected(loc)) return false;
        List<Location<World>> signs = LocketAPI.getSideSigns(loc);
        if (signs == null) return false;
        for (Location<World> sign : signs) {
            TileEntity tile = sign.getTileEntity().orElse(null);
            if (tile instanceof Sign && LocketAPI.isPrivate((Sign) tile)) return true;
        }
        return true;
    }

    public Role access(@Nonnull List<Location<World>> signs) {
        HashSet<String> users = new HashSet<>();
        HashSet<String> owners = new HashSet<>();
        for (Location<World> sign : signs) {
            TileEntity tile = sign.getTileEntity().orElse(null);
            if (tile instanceof Sign && LocketAPI.isPrivate(((Sign) tile).lines().get(0).toPlain())) {
                owners.add(((Sign) tile).lines().get(1).toPlain());
                users.add(((Sign) tile).lines().get(2).toPlain());
                users.add(((Sign) tile).lines().get(3).toPlain());
            }
        }
        if (owners.size() <= 0) return Role.NONE;
        if (owners.size() >= 2) return Role.SIGN_M_OWNERS;
        if (owners.contains(username)) return Role.SIGN_OWNER;
        if (users.contains(username)) return Role.SIGN_USER;
        return Role.NONE;
    }

    private Result analyzeSign(@Nonnull HashSet<Location<World>> signs) {
        if (signs.isEmpty()) return Result.SIGN_NOT_LOCK;
        LockSignData data = new LockSignData();
        for (Location<World> block : signs) {
            TileEntity tile = block.getTileEntity().orElse(null);
            if (tile != null && tile instanceof Sign) {
                data.append(LocketAPI.parseSign((Sign) tile));
            }
        }
        return data.getAccess(username);
    }

    private void removeOneSign(HandType hand) {
        if (GameModes.CREATIVE.equals(player.gameMode().get())) return;
        ItemStack stack = player.getItemInHand(hand).orElse(null);
        if (stack != null && stack.getQuantity() > 1) {
            stack.setQuantity(stack.getQuantity() - 1);
            player.setItemInHand(hand, stack);
        } else {
            player.setItemInHand(hand, null);
        }
    }

    public void placeLock(@Nonnull Location<World> loc, Direction face, HandType hand) {
    }


    public boolean otherProtected(Location<World> location) {
        return false;
    }

}
