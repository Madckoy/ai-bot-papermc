package com.devone.aibot.botlogic;

import com.devone.aibot.core.BotManager;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;

import org.bukkit.scheduler.BukkitRunnable;

public class BotPatroling {
    private final BotManager BotManager;
    private Location patrolCenter;
    private static final int MAX_RADIUS = 200;

    public BotPatroling(BotManager mgr) {
        this.BotManager = mgr;
    }

    public void startPatrol(NPC bot) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bot.isSpawned() || patrolCenter == null) {
                    cancel();
                    return;
                }

                Location randomPatrolPoint = getRandomPatrolPoint();
                bot.getNavigator().setTarget(randomPatrolPoint);
            }
        }.runTaskTimer(BotManager.getPlugin(), 0L, 200L); // Каждые 10 секунд
    }

    public void setPatrolCenter(Location location) {
        this.patrolCenter = location;
    }

    private Location getRandomPatrolPoint() {
        double offsetX = (Math.random() * MAX_RADIUS * 2) - MAX_RADIUS;
        double offsetZ = (Math.random() * MAX_RADIUS * 2) - MAX_RADIUS;
        return patrolCenter.clone().add(offsetX, 0, offsetZ);
    }

    public Location getPatrolCenter() {
        return patrolCenter;
    }
}
