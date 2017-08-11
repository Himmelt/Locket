package org.soraworld.locket.depend;

import org.soraworld.locket.log.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;

public class Depend {
    /*protected static WorldGuardPlugin worldGuard = null;
    protected static Plugin residence = null;
    protected static Plugin towny = null;
    protected static Plugin factions = null;*/

/*    public Depend(Plugin _plugin) {
        Plugin worldGuardPlugin = _plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin == null || !(worldGuardPlugin instanceof WorldGuardPlugin)) {
            worldGuard = null;
        } else {
            worldGuard = (WorldGuardPlugin) worldGuardPlugin;
        }
        // Residence
        residence = _plugin.getServer().getPluginManager().getPlugin("Residence");
        // Towny
        towny = _plugin.getServer().getPluginManager().getPlugin("Towny");
        // Factions
        factions = _plugin.getServer().getPluginManager().getPlugin("Factions");
    }*/


    public static boolean isProtectedFrom(Location location, Player player) {
        /*if (worldGuard != null) {
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
        }*/
        return false;
    }

    public static boolean isTownyTownOrNationOf(String line, Player player) {
        /*if (towny != null) {
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
        }*/
        return false;
    }

}
