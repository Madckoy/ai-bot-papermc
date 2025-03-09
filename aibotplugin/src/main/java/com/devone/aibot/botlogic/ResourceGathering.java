package com.devone.aibot.botlogic;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

public class ResourceGathering {
    private final NPC bot;

    public ResourceGathering(NPC bot) {
        this.bot = bot;
    }

    // Команда добычи дерева
    public void chopTree(Location treeLoc) {
        bot.getNavigator().setTarget(treeLoc);
        // TODO: Добавить анимацию или команду для рубки
    }

    // Команда сбора предметов
    public void collectItem(Item item) {
        bot.getNavigator().setTarget(item.getLocation());
        // TODO: Добавить логику подбора предметов
    }

    // Атака на враждебного моба
    public void attackMob(LivingEntity mob) {
        bot.getNavigator().setTarget(mob.getLocation());
        // TODO: Добавить механику атаки
    }
}
