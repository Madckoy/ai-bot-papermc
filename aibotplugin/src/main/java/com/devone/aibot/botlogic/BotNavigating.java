package com.devone.aibot.botlogic;

import com.devone.aibot.AIBotPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class BotNavigating {
    private final AIBotPlugin plugin;
    private final NPC bot;
    private final Player player;
    private final WaterHandling waterHandler;
    private final Set<Location> visitedLocations = new HashSet<>();
    private Location lastLocation;
    private long stuckTime = 0;

    public BotNavigating(AIBotPlugin plugin, NPC bot, Player player) {
        this.plugin = plugin;
        this.bot = bot;
        this.player = player;
        this.waterHandler = new WaterHandling(plugin);
    }

    public void startNavigation() {
        visitedLocations.clear();
        lastLocation = bot.getEntity().getLocation();
        stuckTime = System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bot.isSpawned() || !player.isOnline()) {
                    cancel();
                    return;
                }

                Location botLoc = bot.getEntity().getLocation();
                Location targetLoc = player.getLocation();

                // Проверяем, не застрял ли бот в воде
                if (waterHandler.isInWater(botLoc)) {
                    waterHandler.moveOutOfWater(bot);
                    return;
                }

                // Проверяем, не застрял ли бот перед препятствием
                if (isStuck(botLoc)) {
                    teleportThroughObstacle(botLoc);
                    return;
                }

                // Двигаем бота к игроку
                bot.getNavigator().setTarget(targetLoc);

                // Если бот достиг игрока, завершаем команду
                if (botLoc.distance(targetLoc) < 2) {
                    plugin.getLogger().info("[AIBotPlugin] Bot reached the player. Stopping.");
                    bot.getNavigator().cancelNavigation();
                    visitedLocations.clear();
                    cancel();
                }

                // Обновляем последнее местоположение для проверки застревания
                if (!botLoc.equals(lastLocation)) {
                    lastLocation = botLoc;
                    stuckTime = System.currentTimeMillis();
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private boolean isStuck(Location botLoc) {
        long elapsed = System.currentTimeMillis() - stuckTime;
        return visitedLocations.contains(botLoc) || elapsed > 1000; // 1 секунды
    }

    private void teleportThroughObstacle(Location botLoc) {
        Block frontBlock = botLoc.getBlock().getRelative(bot.getEntity().getFacing()); // Блок перед ботом
    
        // Если перед ботом нет препятствия – телепортация не нужна
        if (!isDoorOrGate(frontBlock) && !isFenceOrWall(frontBlock)) {
            plugin.getLogger().info("[AIBotPlugin] No obstacle detected, bot will continue walking.");
            return;
        }
    
        Location newLoc = botLoc.clone();
    
        // Определяем направление телепортации
        if (isVerticalDoor(frontBlock)) {
            newLoc.add(botLoc.getX() > player.getLocation().getX() ? -1 : 1, 0, 0);
        } else {
            newLoc.add(0, 0, botLoc.getZ() > player.getLocation().getZ() ? -1 : 1);
        }
    
        plugin.getLogger().info("[AIBotPlugin] Bot stuck at " + botLoc + ", teleporting to " + newLoc);
        bot.teleport(newLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    
        // После телепортации бот продолжает движение к игроку
        bot.getNavigator().setTarget(player.getLocation());
        stuckTime = System.currentTimeMillis();
    }
    
    // Проверяем, является ли блок дверью или калиткой
    private boolean isDoorOrGate(Block block) {
        return block.getBlockData() instanceof Openable;
    }
    
    // Проверяем, является ли блок забором или стеной
    private boolean isFenceOrWall(Block block) {
        Material type = block.getType();
        return type.name().endsWith("_FENCE") || type.name().endsWith("_WALL");
    }
    
    // Определяем, ориентирована ли дверь вертикально (север-юг)
    private boolean isVerticalDoor(Block block) {
        Material type = block.getType();
        return type == Material.OAK_DOOR || type == Material.SPRUCE_DOOR ||
               type == Material.BIRCH_DOOR || type == Material.JUNGLE_DOOR ||
               type == Material.ACACIA_DOOR || type == Material.DARK_OAK_DOOR ||
               type == Material.WARPED_DOOR || type == Material.CRIMSON_DOOR ||
               type == Material.IRON_DOOR || type == Material.OAK_FENCE_GATE ||
               type == Material.SPRUCE_FENCE_GATE || type == Material.BIRCH_FENCE_GATE ||
               type == Material.JUNGLE_FENCE_GATE || type == Material.ACACIA_FENCE_GATE ||
               type == Material.DARK_OAK_FENCE_GATE;
    }
    
}
