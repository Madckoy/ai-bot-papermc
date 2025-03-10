package com.devone.aibot.botlogic;

import com.devone.aibot.core.BotManager;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Random;
import org.bukkit.entity.Item;


public class BotPatroling {
    private final BotManager botManager;
    private Location patrolCenter;
    private static final int PATROL_RADIUS = 100; // Фиксированный радиус патруля
    private static final int CHECK_RADIUS = 5; // Радиус поиска земли
    private final Random random = new Random();

    public BotPatroling(BotManager mgr) {
        this.botManager = mgr;
    }

    public BotManager getBotManager() {
        return botManager;
    }

    public void startPatrol(NPC bot) {
        Location bot_loc = bot.getStoredLocation();

        if (patrolCenter == null) {
            patrolCenter = bot_loc;
            botManager.getPlugin().getLogger().info("[AIBotPlugin] Patrol center set to bot's current location.");
        }
    
        botManager.getPlugin().getLogger().info("[AIBotPlugin] Bot patrol started.");


        if (!bot.isSpawned() || bot.getEntity() == null) {
            botManager.getPlugin().getLogger().warning("[AIBotPlugin] Bot is not fully initialized. Trying to respawn...");
            bot.spawn(bot.getStoredLocation()); // Принудительный респавн
        }
    
        new BukkitRunnable() {
            @Override
            public void run() {
 
                Location patrolPoint = getSafePatrolPoint();
                if (patrolPoint == null) {
                    botManager.getPlugin().getLogger().info("[AIBotPlugin] No safe patrol point found! Retrying...");
                    startPatrol(bot); // Перезапускаем патруль с новой точкой
                    return;
                }
    
                botManager.getPlugin().getLogger().info("[AIBotPlugin] New patrol target: " + patrolPoint);
                try {
                    bot.getNavigator().setTarget(patrolPoint);
                } catch (Exception e) {
                    botManager.getPlugin().getLogger().warning("[AIBotPlugin] Failed to set target: " + e.getMessage());
                }
                        
                // Проверяем, двигается ли бот через 10 секунд (а не 2!)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!bot.getNavigator().isNavigating()) {
                            botManager.getPlugin().getLogger().info("[AIBotPlugin] Bot is stuck! Choosing another point.");
                            startPatrol(bot); // Перезапускаем патруль с новой точкой
                        }
                    }
                }.runTaskLater(botManager.getPlugin(), 100L); // Проверяем через 10 секунд
            }
        }.runTaskTimer(botManager.getPlugin(), 0L, 100L); // Бот меняет цель каждые 10 секунд
    }
    
    public void setPatrolCenter(Location location) {
        this.patrolCenter = location;
        botManager.getPlugin().getLogger().info("[AIBotPlugin] Patrol center set by command: " + location);
    }
    

    public Location getPatrolCenter() {
        return patrolCenter;
    }

    private Location getSafePatrolPoint() {
        for (int i = 0; i < 10; i++) { // Пробуем 10 раз найти безопасное место
            double offsetX = (random.nextDouble() * PATROL_RADIUS * 2) - PATROL_RADIUS;
            double offsetZ = (random.nextDouble() * PATROL_RADIUS * 2) - PATROL_RADIUS;
            Location potentialLoc = patrolCenter.clone().add(offsetX, 0, offsetZ);

            Location groundLoc = findGround(potentialLoc);
            if (groundLoc != null) {
                return groundLoc;
            }
        }
        return patrolCenter; // Если не нашли безопасное место, остаёмся в центре
    }

    private Location findGround(Location start) {
        for (int y = -CHECK_RADIUS; y <= CHECK_RADIUS; y++) {
            Location checkLoc = start.clone().add(0, y, 0);
            Block block = checkLoc.getBlock();
            if (block.getType().isSolid() && block.getType() != Material.WATER) {
                return checkLoc.add(0, 1, 0); // Возвращаем место чуть выше земли
            }
        }
        return null;
    }

    public void continuePatrol(NPC bot) {
        if (patrolCenter == null) {
            patrolCenter = bot.getStoredLocation();
        }
    
        Location patrolPoint = getSafePatrolPoint();
        if (patrolPoint == null) {
            botManager.getPlugin().getLogger().info("[AIBotPlugin] No safe patrol point found. Staying at patrol center.");
            patrolPoint = patrolCenter; // Если не нашли точку - остаёмся в центре
        }
    
        botManager.getPlugin().getLogger().info("[AIBotPlugin] Continuing patrol. New target: " + patrolPoint);
        bot.getNavigator().setTarget(patrolPoint);
    }
    
}
