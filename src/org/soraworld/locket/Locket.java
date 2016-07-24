package org.soraworld.locket;

/* Created by Himmelt on 2016/7/15.*/

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.config.Config;
import org.soraworld.locket.depend.Depend;
import org.soraworld.locket.listener.*;
import org.soraworld.locket.log.Logger;
import org.soraworld.locket.util.Utils;

public class Locket extends JavaPlugin {

    @Override
    public void onLoad() {
        new Logger(this);// 初始化日志
    }

    @Override
    public void onEnable() {
        new Config(this);// 初始化配置
        new Depend(this);// 初始化依赖
        // 注册监听器
        getServer().getPluginManager().registerEvents(new BlockEventListener(), this);
        getServer().getPluginManager().registerEvents(new EntityEventListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
        getServer().getPluginManager().registerEvents(new WorldEventListener(), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("locket")) {
            if (args.length == 0) {
                Utils.sendMessages(sender, Config.getLang("command-usage"));
            } else {
                // 以下指令无需玩家也可执行
                switch (args[0]) {
                    case "reload":
                        if (sender.hasPermission("locket.reload")) {
                            Config.reload();
                            Utils.sendMessages(sender, Config.getLang("config-reloaded"));
                        } else {
                            Utils.sendMessages(sender, Config.getLang("no-permission"));
                        }
                        return true;
                    case "version":
                        if (sender.hasPermission("locket.version")) {
                            sender.sendMessage(this.getDescription().getFullName());
                        } else {
                            Utils.sendMessages(sender, Config.getLang("no-permission"));
                        }
                        return true;
                }
                // 以下指令只能玩家执行
                if (!(sender instanceof Player)) {
                    Utils.sendMessages(sender, Config.getLang("command-usage"));
                    return false;
                }
                Player player = (Player) sender;
                switch (args[0]) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                        if (player.hasPermission("locket.edit")) {
                            String message = "";
                            Block block = Utils.getSelectedSign(player);
                            if (block == null) {
                                Utils.sendMessages(player, Config.getLang("no-sign-selected"));
                            } else if (!LocketAPI.isSign(block) || !(player.hasPermission("locket.edit.admin") || LocketAPI.isOwnerOfSign(block, player))) {
                                Utils.sendMessages(player, Config.getLang("sign-need-reselect"));
                            } else {
                                for (int i = 1; i < args.length; i++) {
                                    message += args[i];
                                }
                                message = ChatColor.translateAlternateColorCodes('&', message);
                                if (message.length() > 16) {
                                    Utils.sendMessages(player, Config.getLang("line-is-too-long"));
                                    return true;
                                }
                                if (LocketAPI.isLockSigned(block)) {
                                    switch (args[0]) {
                                        case "1":
                                            Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                            break;
                                        case "2":
                                            if (!player.hasPermission("locket.admin.edit")) {
                                                Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                                break;
                                            }
                                        case "3":
                                        case "4":
                                            Utils.setSignLine(block, Integer.parseInt(args[0]) - 1, message);
                                            Utils.sendMessages(player, Config.getLang("sign-changed"));
                                            break;
                                    }
                                } else if (LocketAPI.isMoreSigned(block)) {
                                    switch (args[0]) {
                                        case "1":
                                            Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                            break;
                                        case "2":
                                        case "3":
                                        case "4":
                                            Utils.setSignLine(block, Integer.parseInt(args[0]) - 1, message);
                                            Utils.sendMessages(player, Config.getLang("sign-changed"));
                                            break;
                                    }
                                } else {
                                    Utils.sendMessages(player, Config.getLang("sign-need-reselect"));
                                }
                            }
                        } else {
                            Utils.sendMessages(player, Config.getLang("no-permission"));
                        }
                        break;
                    default:
                        Utils.sendMessages(player, Config.getLang("command-usage"));
                        break;
                }
            }
        }
        return true;
    }

}
