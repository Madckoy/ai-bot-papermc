package com.devone.aibot;

import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;

public class BotMovement {

    private final Villager bot;
    private final AIBotPlugin plugin;

    public BotMovement(AIBotPlugin plugin, Villager bot) {
        this.plugin = plugin;
        this.bot = bot;
    }

    public void moveTo(Location target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bot.isDead() || bot.getLocation().distance(target) < 1) {
                    cancel();
                    return;
                }
                bot.teleport(bot.getLocation().add(target.toVector().subtract(bot.getLocation().toVector()).normalize().multiply(0.5)));
            }
        }.runTaskTimer(plugin, 0L, 10L); // Двигаем бота каждые 10 тиков
    }
}
