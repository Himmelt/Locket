package org.soraworld.locket.util;

import org.soraworld.locket.Locket;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.title.Title;
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
        player.sendMessage(Text.of("event target face:" + face));
        newSign.setBlockType(BlockTypes.WALL_SIGN, Cause.source(Locket.getLocket().getPlugin()).build());
        BlockState state = BlockTypes.WALL_SIGN.getDefaultState();
        player.sendMessage(Text.of("sign default face state:" + state.get(Keys.DIRECTION).orElse(Direction.NONE)));
        state = state.with(Keys.DIRECTION, face).orElse(state);
        player.sendMessage(Text.of("put sign face after:" + state.get(Keys.DIRECTION).orElse(Direction.NONE)));

        //newSign.setBlockType(BlockTypes.WALL_SIGN, BlockChangeFlag.NEIGHBOR, Cause.of(NamedCause.source(player)));
        // So this part is pretty much a Bukkit bug:
        // Signs' rotation is not correct with bukkit's set facing, below is the workaround.
        newSign.setBlock(state, Cause.source(Locket.getLocket().getPlugin()).build());
        player.sendMessage(Text.of("put sign face finish:" + newSign.get(Keys.DIRECTION).orElse(Direction.NONE)));
        player.sendMessage(Text.of("put sign state face finish:" + newSign.getBlock().get(Keys.DIRECTION).orElse(Direction.NONE)));

        TileEntity tile = newSign.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            player.sendMessage(Text.of("origin:" + data));
            data.setElement(0, Text.of("[Private]"));
            data.setElement(1, Text.of(player.getName()));
            tile.offer(data);
            player.sendMessage(Text.of("after:" + data));
        }
        player.playSound(SoundTypes.BLOCK_WOOD_PLACE, location.getPosition(), 5);
        //updateSign(newSign);
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

    public static void removeOne(Player player, HandType hand) {
        if (GameModes.CREATIVE.equals(player.gameMode().get())) return;
        ItemStack stack = player.getItemInHand(hand).orElse(null);
        if (stack != null && stack.getQuantity() > 1) {
            stack.setQuantity(stack.getQuantity() - 1);
            player.setItemInHand(hand, stack);
        } else {
            player.setItemInHand(hand, null);
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

    public static void sendChat(Player player, String message) {
        player.sendMessage(ChatTypes.CHAT, TextSerializers.FORMATTING_CODE.deserialize(message));
    }

    public static void sendActionBar(Player player, String message) {
        player.sendMessage(ChatTypes.ACTION_BAR, TextSerializers.FORMATTING_CODE.deserialize(message));
    }

    public static void sendActionBar(Player player, String message, int fadeIn, int stay, int fadeOut) {
        Text actionBar = TextSerializers.FORMATTING_CODE.deserialize(message);
        player.sendTitle(Title.builder().actionBar(actionBar).fadeIn(fadeIn).stay(stay).fadeOut(fadeOut).build());
    }

    public static boolean isFace(Direction face) {
        return face == Direction.NORTH || face == Direction.WEST || face == Direction.EAST || face == Direction.SOUTH;
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
}
