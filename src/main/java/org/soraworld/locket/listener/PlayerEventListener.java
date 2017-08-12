package org.soraworld.locket.listener;

import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.util.BlockFace;
import org.soraworld.locket.util.Utils;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Hinges;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class PlayerEventListener {

    // Quick protect for chests
    @Listener(order = Order.DEFAULT)
    public void onPlayerQuickLockChest(InteractBlockEvent.Secondary event, @First Player player) {
        // 右键手持木牌

        player.sendMessage(ChatTypes.ACTION_BAR, TextSerializers.FORMATTING_CODE.deserialize(("&a&lper&kmis&rsion:" + player.hasPermission("&block.&clock"))));
        player.sendMessage(ChatTypes.CHAT, TextSerializers.FORMATTING_CODE.deserialize(("&a&lper&kmis&rsion:" + player.hasPermission("&block.&clock"))));
        player.sendMessage(ChatTypes.SYSTEM, TextSerializers.FORMATTING_CODE.deserialize(("&a&lper&kmis&rsion:" + player.hasPermission("&block.&clock"))));

        player.sendMessage(Text.of(event + player.getName()));
        ItemStack itemStack = player.getItemInHand(event.getHandType()).orElse(null);

        player.sendMessage(Text.of(itemStack + ""));
        if (itemStack != null && ItemTypes.SIGN.equals(itemStack.getItem())) {
            // 潜行方式,取消
            if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                player.sendMessage(ChatTypes.SYSTEM, Text.builder("&cYou are sneaking").build());
                return;
            }
            // 如果玩家没有使用权限,取消
            if (!player.hasPermission("locket.lock")) {
                player.sendMessage(ChatTypes.ACTION_BAR, Text.builder("&bYou have no permission").build());
                //return;
            }
            Direction face = event.getTargetSide();
            player.sendMessage(Text.of("face:" + face.name()));
            // 木牌只能贴在四个侧面
            if (face == Direction.NORTH || face == Direction.WEST || face == Direction.EAST || face == Direction.SOUTH) {
                Location<World> location = event.getTargetBlock().getLocation().orElse(null);
                player.sendMessage(Text.of("location:" + location));
                if (location == null || Depend.isProtectedFrom(location, player) ||
                        location.getRelative(face).getBlockType() != BlockTypes.AIR ||
                        !LocketAPI.isLockable(location)) {
                    player.sendMessage(Text.of("this block cant lock"));
                    return;
                }
                // 在被保护方块列表内
                boolean locked = LocketAPI.isLocked(location);
                // 取消交互事件
                event.setCancelled(true);
                locked = false;
                if (!locked && !LocketAPI.isUpDownLockedDoor(location)) {
                    // 拿掉玩家一个木牌
                    player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize("&e remove your bag a sign"));
                    Utils.removeASign(player, event.getHandType());
                    // 显示消息
                    Utils.sendMessages(player, Config.getLang("locked-quick"));
                    Utils.putSignPrivate(player, location, face);
                    player.sendMessage(Text.of("putSignPrivate:" + location + face));
                } else if (!locked && LocketAPI.isOwnerUpDownLockedDoor(location, player)) {
                    Utils.removeASign(player, event.getHandType());
                    Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                    Utils.putSignMore(player, location, face);
                } else if (LocketAPI.isOwner(location, player)) {
                    Utils.removeASign(player, event.getHandType());
                    Utils.putSignMore(player, location, face);
                    Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                } else {
                    Utils.sendMessages(player, Config.getLang("cannot-lock-quick"));
                }
            }
        }
    }

    // Player select sign
    @Listener(order = Order.LATE)
    public void playerSelectSign(InteractBlockEvent.Secondary event, @First Player player) {
        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if (!player.hasPermission("locket.edit")) return;
        if (LocketAPI.isOwnerOfSign(location, player) || (LocketAPI.isLockOrMoreSign(location) && player.hasPermission("locket.edit.admin"))) {
            Utils.selectSign(player, location);
            Utils.sendMessages(player, Config.getLang("sign-selected"));
        }
    }

    // Protect block from being used & handle double doors
    // Bukkit-1.7.10 dose not support event.getHand()
    // 保护方块被使用
    @Listener(order = Order.FIRST)
    public void onAttemptInteractLockedBlocks(InteractBlockEvent event, @First Player player) {
        Location location = event.getTargetBlock().getLocation().orElse(null);
        if (((LocketAPI.isLocked(location) && !LocketAPI.isUser(location, player)) ||
                (LocketAPI.isUpDownLockedDoor(location) && !LocketAPI.isUserUpDownLockedDoor(location, player)))
                && !player.hasPermission("locket.admin.use")) {
            Utils.sendMessages(player, Config.getLang("block-is-locked"));
            event.setCancelled(true);
            return;
        }
        if (event instanceof InteractBlockEvent.Secondary && LocketAPI.isDoubleDoorBlock(location) && LocketAPI.isLocked(location)) {
            Location doorBlock = LocketAPI.getBottomDoorBlock(location);
            boolean shouldOpen = !(doorBlock.getBlock().get(Keys.HINGE_POSITION).get() == Hinges.LEFT); // Move to here
            List<Location> doors = new ArrayList<>();
            doors.add(doorBlock);
            if (doorBlock.getBlockType() == BlockTypes.IRON_DOOR) {
                LocketAPI.toggleDoor(doorBlock, shouldOpen);
            }
            for (BlockFace blockface : LocketAPI.newsFaces) {
                Location relative = doorBlock.getRelative(blockface.get());
                if (relative.getBlockType() == doorBlock.getBlockType()) {
                    doors.add(relative);
                    LocketAPI.toggleDoor(relative, shouldOpen);
                }
            }
        }
    }

}