package com.devone.aibot.botlogic;

import com.devone.aibot.AIBotPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WaterHandling {
    private final AIBotPlugin plugin;

    public WaterHandling(AIBotPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isInWater(Location botLoc) {
        return botLoc.getBlock().getType() == Material.WATER;
    }

    public void moveOutOfWater(NPC bot) {
        Location botLoc = bot.getEntity().getLocation();
        Location landLoc = findNearestLand(botLoc, 10);
        
        if (landLoc != null) {
            plugin.getLogger().info("[AIBotPlugin] Bot found land, teleporting.");
            bot.teleport(landLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
            plugin.getLogger().info("[AIBotPlugin] No land found, bot will attempt to swim.");
            bot.getNavigator().setTarget(botLoc.clone().add(0, 1, 0)); // Пытается всплыть
        }
    }

    private Location findNearestLand(Location start, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Location checkLoc = start.clone().add(dx, 0, dz);
                if (checkLoc.getBlock().getType().isSolid()) {
                    return checkLoc.add(0, 1, 0);
                }
            }
        }
        return null;
    }
}
