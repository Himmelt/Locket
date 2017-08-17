package org.soraworld.locket.util;

import org.slf4j.Logger;
import org.soraworld.locket.Locket;
import org.soraworld.locket.constant.Constants;
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
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;

import static org.soraworld.locket.api.LocketAPI.isPrivate;

public class Utils {

    private static final Locket locket = Locket.getLocket();
    private static final PluginContainer plugin = locket.getPlugin();
    private static final Logger LOGGER = plugin.getLogger();
    private static final HashMap<Player, Location<World>> SELECTED_SIGNS = new HashMap<>();


    public static void placeLockSign(Player player, Location<World> location, Direction face) {
        Location<World> relative = location.getRelative(face);
        relative.setBlockType(BlockTypes.WALL_SIGN, Cause.source(Locket.getLocket().getPlugin()).build());
        BlockState state = BlockTypes.WALL_SIGN.getDefaultState();
        relative.setBlock(state.with(Keys.DIRECTION, face).orElse(state), Cause.source(Locket.getLocket().getPlugin()).build());

        TileEntity tile = relative.getTileEntity().orElse(null);
        if (tile != null && tile instanceof Sign) {
            SignData data = ((Sign) tile).getSignData();
            data.setElement(0, Text.of("[Private]"));
            data.setElement(1, Text.of(player.getName()));
            data.setElement(2, TextSerializers.FORMATTING_CODE.deserialize("&b我是吴通,&c&l空境之主&b!&kWelcome&r&b to My &e&lSoraWorld&r!&r"));
            tile.offer(data);
        }
        player.playSound(SoundTypes.BLOCK_WOOD_PLACE, location.getPosition(), 5);
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

    public static Location<World> getSelectedSign(Player player) {
        return SELECTED_SIGNS.get(player);
    }

    public static void selectSign(Player player, Location<World> block) {
        SELECTED_SIGNS.put(player, block);
    }

    public static void sendMessages(CommandSource sender, String messages) {
        if (messages == null || messages.equals("")) return;
        sender.sendMessage(Text.of(messages));
    }

    public static boolean isPlayerOnLine(Player player, String text) {
        return text.equals(player.getName());
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

    public static boolean canLock(Player player, Location<World> location) {
        BlockType type = location.getBlockType();
        if (isDChest(type)) {
            int result = canOpenChest(player, location);
            if (result == 0) {
                player.sendMessage(Text.of("0-无法打开"));
                return false;
            } else if (result == 1) {
                player.sendMessage(Text.of("1-可以打开"));
                return true;
            } else if (result == 2) {
                player.sendMessage(Text.of("2-多重所有者"));
            } else if (result == 3) {
                player.sendMessage(Text.of("3-多重箱子"));
            } else {
                player.sendMessage(Text.of("NAN-无法打开"));
                return false;
            }
        } else if (isContainer(type)) {
            return canTouchBlock(player, location);
        }
        return false;
    }

    private static boolean canTouchBlock(Player player, Location<World> location) {
        HashSet<Location<World>> signs = new HashSet<>();
        for (Direction face : Constants.FACES) {
            Location<World> relative = location.getRelative(face);
            if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                signs.add(relative);
            }
        }
        int result = analyzeSign(player, signs);
        if (result == 1) return true;
        if (result == 0) {
            player.sendMessage(Text.of("canTouchBlock:0"));
            return false;
        }
        if (result == 2) {
            player.sendMessage(Text.of("canTouchBlock:2"));
            return false;
        }
        if (result == 3) {
            player.sendMessage(Text.of("canTouchBlock:3"));
            return false;
        }
        return false;
    }

    // 已确认是箱子
    // 0 --- 无名字
    // 1 --- 有名字 或 无有效锁
    // 2 --- 多重所有者
    // 3 --- 多重箱子
    private static int canOpenChest(Player player, @Nonnull Location<World> location) {
        BlockType type = location.getBlockType();
        HashSet<Location<World>> signs = new HashSet<>();
        Location<World> link = null;
        int count = 0;

        for (Direction face : Constants.FACES) {
            Location<World> relative = location.getRelative(face);
            if (relative.getBlockType() == type) {
                if (++count >= 2) return 3;
                link = relative;
            } else if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                signs.add(relative);
            }
        }

        if (link != null) {
            count = 0;
            for (Direction face : Constants.FACES) {
                Location<World> relative = link.getRelative(face);
                if (relative.getBlockType() == type && ++count >= 2) return 3;
                if (relative.getBlockType() == BlockTypes.WALL_SIGN && relative.get(Keys.DIRECTION).orElse(null) == face) {
                    player.sendMessage(Text.of("FACE:" + face + "   SIGN-FACE:" + relative.get(Keys.DIRECTION).orElse(null)));
                    signs.add(relative);
                }
            }
        }
        return analyzeSign(player, signs);
    }

    private static int analyzeSign(Player player, @Nonnull HashSet<Location<World>> locations) {
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
        if (owners.size() >= 2) return 2;
        if (owners.size() == 0 || owners.contains(player.getName()) || users.contains(player.getName())) return 1;
        return 0;
    }

    public static void removeSelectedSign(Player player) {
        SELECTED_SIGNS.remove(player);
    }
}
