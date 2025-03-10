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

        if (bot.getEntity() == null) {
            botManager.getPlugin().getLogger().warning("[AIBotPlugin] Bot entity is null after restart!");
            return;
        }
        //--------------------
        Location tree = ResourceDetecting.findNearbyTree(bot.getEntity().getLocation());
        if (tree != null) {
            new ResourceGathering(bot, this).chopTree(tree);
        } else {
            Item item = ResourceDetecting.findNearbyItem(bot.getEntity().getLocation());
            if (item != null) {
                new ResourceGathering(bot, this).collectItem(item);
            } else {
                LivingEntity mob = ResourceDetecting.findNearbyHostileMob(bot.getEntity().getLocation());
                if (mob != null) {
                    new ResourceGathering(bot, this).attackMob(mob);
                } else {
                    // Если ничего не найдено, продолжаем патрулирование
                    botManager.getBotPatroling().continuePatrol(bot);
                }
            }
        }

        //--------------------

        if (patrolCenter == null) {
            patrolCenter = bot.getEntity().getLocation();
            botManager.getPlugin().getLogger().info("[AIBotPlugin] Patrol center set to bot's current location.");
        }
    
        botManager.getPlugin().getLogger().info("[AIBotPlugin] Bot patrol started.");
    
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bot.isSpawned() || patrolCenter == null) {
                    botManager.getPlugin().getLogger().info("[AIBotPlugin] Bot not spawned or patrol center is null. Stopping patrol.");
                    cancel();
                    return;
                }
    
                Location patrolPoint = getSafePatrolPoint();
                if (patrolPoint == null) {
                    botManager.getPlugin().getLogger().info("[AIBotPlugin] No safe patrol point found! Retrying...");
                    return;
                }
    
                botManager.getPlugin().getLogger().info("[AIBotPlugin] New patrol target: " + patrolPoint);
                bot.getNavigator().setTarget(patrolPoint);
    
                // Проверяем, двигается ли бот через 10 секунд (а не 2!)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!bot.getNavigator().isNavigating()) {
                            botManager.getPlugin().getLogger().info("[AIBotPlugin] Bot is stuck! Choosing another point.");
                            startPatrol(bot); // Перезапускаем патруль с новой точкой
                        }
                    }
                }.runTaskLater(botManager.getPlugin(), 200L); // Проверяем через 10 секунд
            }
        }.runTaskTimer(botManager.getPlugin(), 0L, 200L); // Бот меняет цель каждые 10 секунд
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
            patrolCenter = bot.getEntity().getLocation();
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
