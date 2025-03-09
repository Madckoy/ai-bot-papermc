package com.devone.aibot.botlogic;

import com.devone.aibot.AIBotPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class WaterHandling {
    private final AIBotPlugin plugin;

    public WaterHandling(AIBotPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isInWater(Location botLoc) {
        return botLoc.clone().add(0, -1, 0).getBlock().getType() == Material.WATER;
    }

    public void moveOutOfWater(NPC bot) {
        Location botLoc = bot.getEntity().getLocation();
        Location landLoc = findLandTowardsPlayer(botLoc, 10);

        if (landLoc != null) {
            plugin.getLogger().info("[AIBotPlugin] Bot found land towards player, teleporting.");
            bot.teleport(landLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
            plugin.getLogger().info("[AIBotPlugin] No land found, applying forward boost.");
            pushBotForward(bot);
        }
    }

    private Location findLandTowardsPlayer(Location start, int radius) {
        Location playerLoc = plugin.getServer().getPlayer("PlayerName").getLocation(); // Получаем координаты игрока
        Vector direction = playerLoc.toVector().subtract(start.toVector()).normalize();

        for (int i = 1; i <= radius; i++) {
            Location checkLoc = start.clone().add(direction.clone().multiply(i));
            if (checkLoc.getBlock().getType().isSolid()) {
                return checkLoc.add(0, 1, 0);
            }
        }
        return null;
    }

    private void pushBotForward(NPC bot) {
        Entity entity = bot.getEntity();
        Vector velocity = entity.getVelocity();

        // Добавляем толчок вперёд и вверх
        velocity.setX(velocity.getX() + 0.3);
        velocity.setY(velocity.getY() + 0.2);
        velocity.setZ(velocity.getZ() + 0.3);

        entity.setVelocity(velocity);
    }
}
