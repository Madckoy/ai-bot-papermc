package com.devone.aibot.botlogic;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class ResourceGathering {
    private final NPC bot;
    private final BotPatroling botPatroling; // Ссылка на патруль

    public ResourceGathering(NPC bot, BotPatroling botPatroling) {
        this.bot = bot;
        this.botPatroling = botPatroling;
    }

    // Команда добычи дерева
    public void chopTree(Location treeLoc) {
        bot.getNavigator().setTarget(treeLoc);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bot.getNavigator().isNavigating()) {
                    botPatroling.getBotManager().getPlugin().getLogger().warning("[AIBotPlugin] Bot failed to reach tree. Resuming patrol.");
                    botPatroling.continuePatrol(bot);
                    cancel();
                }
            }
        }.runTaskTimer(botPatroling.getBotManager().getPlugin(), 40L, 40L);
    }

    // Команда сбора предметов
    public void collectItem(Item item) {
        bot.getNavigator().setTarget(item.getLocation());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bot.getNavigator().isNavigating()) {
                    botPatroling.getBotManager().getPlugin().getLogger().warning("[AIBotPlugin] Bot failed to reach item. Resuming patrol.");
                    botPatroling.continuePatrol(bot);
                    cancel();
                }
            }
        }.runTaskTimer(botPatroling.getBotManager().getPlugin(), 40L, 40L);
    }

    // Атака на враждебного моба
    public void attackMob(LivingEntity mob) {
        bot.getNavigator().setTarget(mob.getLocation());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bot.getNavigator().isNavigating()) {
                    botPatroling.getBotManager().getPlugin().getLogger().warning("[AIBotPlugin] Bot failed to reach target. Resuming patrol.");
                    botPatroling.continuePatrol(bot);
                    cancel();
                }
            }
        }.runTaskTimer(botPatroling.getBotManager().getPlugin(), 40L, 40L);
    }
}
