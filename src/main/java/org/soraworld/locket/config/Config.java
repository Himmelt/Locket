package org.soraworld.locket.config;

/* Created by Himmelt on 2016/7/15.*/

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.soraworld.locket.Locket;
import org.soraworld.locket.log.Logger;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {
    private Locket locket;
    private static FileConfiguration langFile;

    private static Set<Material> lockables = new HashSet<>();

    private static String defaultPrivateSign = "[Private]";
    private static String defaultMoreSign = "[More Users]";

    private static boolean blockInterferePlacement = true;// 干涉放置,比如放置双开门,大箱子,漏斗之类的
    private static boolean blockItemTransferIn = false;
    private static boolean blockItemTransferOut = false;
    private static boolean explosionProtection = true;

    private static boolean blockHopperMinecart = true;//漏斗矿车

    public Config(Locket locket) {
        this.locket = locket;
        reload();
    }

    @SuppressWarnings("deprecation")
    public static void reload() {
        locket.saveDefaultConfig();
        initConfigFiles();
        FileConfiguration configFile = YamlConfiguration.loadConfiguration(new File(locket.getDataFolder(), "config.yml"));
        String langName = configFile.getString("language-file", "lang_en_us.yml");
        langFile = YamlConfiguration.loadConfiguration(new File(locket.getDataFolder(), langName));

        defaultPrivateSign = ChatColor.translateAlternateColorCodes('&', configFile.getString("private-sign"));
        defaultMoreSign = ChatColor.translateAlternateColorCodes('&', configFile.getString("more-sign"));

        blockInterferePlacement = configFile.getBoolean("block-interfere-placement", true);
        blockItemTransferIn = configFile.getBoolean("block-item-transfer-in", false);
        blockItemTransferOut = configFile.getBoolean("block-item-transfer-out", true);
        explosionProtection = configFile.getBoolean("explosion-protection", true);
        blockHopperMinecart = configFile.getBoolean("block-hopper-minecart", true);

        // 配置清单里的物品
        List<String> unprocessedItems = configFile.getStringList("lockables");
        lockables = new HashSet<>();
        for (String unprocessedItem : unprocessedItems) {
            if (unprocessedItem.equals("*")) {
                Collections.addAll(lockables, Material.values());
                Logger.info("All blocks are default to be lockable!");
                Logger.info("Add '-<Material>' to exempt a block, such as '-STONE'!");
                continue;
            }
            boolean add = true;
            if (unprocessedItem.startsWith("-")) {
                add = false;
                unprocessedItem = unprocessedItem.substring(1);
            }
            try { // Is it a number?
                int materialId = Integer.parseInt(unprocessedItem);
                // Hit here without error means yes it is
                if (add) {
                    lockables.add(Material.getMaterial(materialId));
                } else {
                    lockables.remove(Material.getMaterial(materialId));
                }
            } catch (Exception ex) {
                // It is not really a number...
                Material material = Material.getMaterial(unprocessedItem);
                if (material == null) {
                    Logger.info(unprocessedItem + " is not an item!");
                } else {
                    if (add) {
                        lockables.add(material);
                    } else {
                        lockables.remove(material);
                    }
                }
            }
        }
        lockables.remove(Material.WALL_SIGN);
    }

    public static void initConfigFiles() {
        String[] langFiles = {"lang_en_us.yml", "lang_zh_cn.yml"};
        for (String filename : langFiles) {
            File _file = new File(locket.getDataFolder(), filename);
            if (!_file.exists()) {
                locket.saveResource(filename, false);
            }
        }
    }

    public static boolean isInterferePlacementBlocked() {
        return blockInterferePlacement;
    }

    public static boolean isItemTransferInBlocked() {
        return blockItemTransferIn;
    }

    public static boolean isItemTransferOutBlocked() {
        return blockItemTransferOut;
    }

    public static boolean getHopperMinecartAction() {
        return blockHopperMinecart;
    }

    public static String getLang(String path) {
        return ChatColor.translateAlternateColorCodes('&', langFile.getString(path, ""));
    }

    public static boolean isLockable(Material material) {
        return lockables.contains(material);
    }

    public static boolean isPrivateSignString(String message) {
        return defaultPrivateSign.equals(message);
    }

    public static boolean isMoreSign(String message) {
        return defaultMoreSign.equals(message);
    }

    public static boolean isExplosionProtection() {
        return explosionProtection;
    }


    public static String getDefaultPrivateString() {
        return defaultPrivateSign;
    }

    public static String getDefaultMoreString() {
        return defaultMoreSign;
    }

}
