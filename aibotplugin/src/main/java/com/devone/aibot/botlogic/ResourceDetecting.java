package com.devone.aibot.botlogic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import java.util.List;

public class ResourceDetecting {
    private static final int SEARCH_RADIUS = 1;

    // Поиск ближайшего дерева
    public static Location findNearbyTree(Location start) {
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                Location checkLoc = start.clone().add(dx, 0, dz);
                if (isTree(checkLoc.getBlock())) {
                    return checkLoc;
                }
            }
        }
        return null;
    }

    // Проверяем, является ли блок деревом
    private static boolean isTree(Block block) {
        Material type = block.getType();
        return type.name().endsWith("_LOG");
    }

    // Поиск ближайшего дропа
    public static Item findNearbyItem(Location start) {
        List<Entity> entities = start.getWorld().getNearbyEntities(start, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS)
                .stream().filter(e -> e instanceof Item).toList();
        return entities.isEmpty() ? null : (Item) entities.get(0);
    }

    // Поиск ближайшего враждебного моба
// Поиск ближайшего враждебного моба
public static LivingEntity findNearbyHostileMob(Location start) {
    List<LivingEntity> entities = start.getWorld().getNearbyEntities(start, SEARCH_RADIUS*5, SEARCH_RADIUS*5, SEARCH_RADIUS*5)
            .stream()
            .filter(e -> e instanceof LivingEntity) // Фильтруем только живые существа
            .map(e -> (LivingEntity) e) // Приводим к LivingEntity
            .filter(ResourceDetecting::isHostileMob) // Оставляем только враждебных
            .toList(); // Преобразуем в List

    return entities.isEmpty() ? null : entities.get(0);
}


    // Проверяем, является ли моб враждебным
    private static boolean isHostileMob(LivingEntity entity) {
        switch (entity.getType()) {
            case ZOMBIE, SKELETON, CREEPER, SPIDER, ENDERMAN, WITCH -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
