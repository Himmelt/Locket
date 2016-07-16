package org.soraworld.locket.depend;

/* Created by Himmelt on 2016/7/15.*/

import com.bekvon.bukkit.residence.Residence;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.soraworld.locket.log.Logger;

public class Depend {
    protected static WorldGuardPlugin worldGuard = null;
    protected static Plugin residence = null;
    protected static Plugin towny = null;
    protected static Plugin factions = null;
    private static Plugin plugin;

    public Depend(Plugin _plugin) {
        plugin = _plugin;
        Plugin worldGuardPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin == null || !(worldGuardPlugin instanceof WorldGuardPlugin)) {
            worldGuard = null;
        } else {
            worldGuard = (WorldGuardPlugin) worldGuardPlugin;
        }
        // Residence
        residence = plugin.getServer().getPluginManager().getPlugin("Residence");
        // Towny
        towny = plugin.getServer().getPluginManager().getPlugin("Towny");
        // Factions
        factions = plugin.getServer().getPluginManager().getPlugin("Factions");
    }


    @SuppressWarnings("deprecation")
    public static boolean isProtectedFrom(Block block, Player player) {
        if (worldGuard != null) {
            if (!worldGuard.canBuild(player, block)) return true;
        }
        if (residence != null) {
            if (!Residence.getPermsByLoc(block.getLocation()).playerHas(player.getName(), player.getWorld().getName(), "build", true))
                return true;
        }
        if (towny != null) {
            try {
                if (TownyUniverse.getDataSource().getWorld(block.getWorld().getName()).isUsingTowny()) {
                    // In town only residents can
                    if (!PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getTypeId(), (byte) 0, TownyPermission.ActionType.BUILD))
                        return true;
                    // Wilderness permissions
                    if (TownyUniverse.isWilderness(block)) { // It is wilderness here
                        if (!player.hasPermission("locket.towny.wilds")) return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.info(e.toString());
            }
        }
        if (factions != null) {
            try {
                Faction faction = BoardColl.get().getFactionAt(PS.valueOf(block));
                if (faction != null && !faction.isNone()) {
                    MPlayer mplayer = MPlayer.get(player);
                    if (mplayer != null && !mplayer.isOverriding()) {
                        Faction playerFaction = mplayer.getFaction();
                        if (faction != playerFaction) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.info(e.toString());
            }
        }
        return false;
    }

    public static boolean isTownyTownOrNationOf(String line, Player player) {
        if (towny != null) {
            String name = player.getName();
            try {
                Resident resident = TownyUniverse.getDataSource().getResident(name);
                Town town = resident.getTown();
                if (line.equals("[" + town.getName() + "]")) return true;
                Nation nation = town.getNation();
                if (line.equals("[" + nation.getName() + "]")) return true;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.info(e.toString());
            }
        }
        if (factions != null) {
            try {
                MPlayer mplayer = MPlayer.get(player);
                if (mplayer != null) {
                    Faction faction = mplayer.getFaction();
                    if (faction != null) {
                        if (line.equals("[" + faction.getName() + "]")) return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.info(e.toString());
            }
        }
        return false;
    }

}
