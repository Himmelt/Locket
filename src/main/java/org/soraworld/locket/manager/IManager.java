package org.soraworld.locket.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.soraworld.hocon.node.Setting;
import org.soraworld.locket.Locket;
import org.soraworld.locket.data.SignData;
import org.soraworld.violet.api.*;
import org.soraworld.violet.command.Args;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.text.ChatColor;
import org.soraworld.violet.text.ChatType;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Himmelt
 */
public abstract class IManager implements IConfig {

    @Setting
    protected boolean protectTile = false;
    @Setting
    protected boolean protectCarrier = true;
    @Setting
    protected boolean preventTransfer = true;
    @Setting
    protected boolean preventExplosion = true;
    // TODO implementation
    @Setting
    protected boolean preventWorldEdit = false;
    @Setting
    protected ChatType chatType = ChatType.ACTION_BAR;
    @Setting(trans = 0b1000)
    protected String privateSign = ChatColor.DARK_RED.toString() + ChatColor.BOLD + "[Private]";
    @Setting(trans = 0b1000)
    protected String ownerFormat = ChatColor.GREEN + "{$owner}";
    @Setting(trans = 0b1000)
    protected String userFormat = "" + ChatColor.DARK_GRAY + ChatColor.ITALIC + "{$user}";
    @Setting(trans = 0b1000)
    protected Set<String> acceptSigns = new HashSet<>();

    private final HashMap<UUID, Object> selected = new HashMap<>();

    @Inject
    private static IPlugin plugin;

    @Override
    public void afterLoad() {
        if (privateSign == null) {
            privateSign = "[Private]";
        }
        acceptSigns.add(privateSign);
        HashSet<String> temp = new HashSet<>();
        acceptSigns.forEach(sign -> temp.add(ChatColor.stripAllColor(sign)));
        acceptSigns.clear();
        acceptSigns.addAll(temp);
    }

    public final @Nullable Object getSelected(@NotNull UUID uuid) {
        return selected.get(uuid);
    }

    public final void setSelected(@NotNull UUID uuid, Object block) {
        selected.put(uuid, block);
    }

    public final void clearSelected(@NotNull UUID uuid) {
        selected.remove(uuid);
    }

    public void sendHint(@NotNull IPlayer player, @NotNull String key, Object... args) {
        plugin.sendMessageKey(player, chatType, key, args);
    }

    public boolean isPrivate(@NotNull String line) {
        return acceptSigns.contains(ChatColor.stripAllColor(line).trim());
    }

    public String getPrivateText() {
        return privateSign;
    }

    public String getOwnerText(@NotNull IUser owner) {
        return ownerFormat.replace("{$owner}", owner.getName() + Locket.hideUuid(owner.uuid()));
    }

    public String getUserText(@NotNull IUser user) {
        return userFormat.replace("{$user}", user.getName() + Locket.hideUuid(user.uuid()));
    }

    public String getUserText(String name) {
        return name == null || name.isEmpty() ? "" : userFormat.replace("{$user}", name);
    }

    public final boolean bypassPerm(@NotNull IPlayer player) {
        return player.hasPermission(plugin.id() + ".bypass");
    }

    public abstract void unLockSign(Object select, int i);

    public abstract boolean isLockable(Object target);

    public abstract void lockSign(IPlayer player, Object selected, int line, String name);

    public abstract boolean isWallSign(Object block);

    public abstract Object getAttached(Object select);

    public abstract List<String> getMatchedPlayers(String s);

    public final boolean isPreventExplosion() {
        return preventExplosion;
    }

    public final boolean isPreventTransfer() {
        return preventTransfer;
    }

    public static String hideUuid(@NotNull UUID uuid) {
        String text = uuid.toString().replace("-", "");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            builder.append(ChatColor.TRUE_COLOR_CHAR).append(text.charAt(i));
        }
        return builder.toString();
    }

    public abstract void touchSign(Object block, @Nullable Predicate<String[]> sync, @Nullable Predicate<String[]> async);

    public abstract void processType(@NotNull ICommandSender sender, @NotNull Args args, @NotNull String key);

    public abstract void exeLock(IPlayer player);

    public abstract void exeRemove(IPlayer player, Args args);

    public abstract void exeInfo(IPlayer player);
}
