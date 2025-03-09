package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.BotManager;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.ai.Navigator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BotHereCommand implements CommandExecutor {

    private final AIBotPlugin plugin;
    private final BotManager botManager;

    // Храним двери, через которые бот уже прошёл
    private final Set<Location> passedDoors = new HashSet<>();

    public BotHereCommand(AIBotPlugin plugin, BotManager botManager) {
        this.plugin = plugin;
        this.botManager = botManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        NPC sbot = botManager.getSelectedBot(player.getUniqueId());

        // Авто-выбор бота, если только один
        Collection<NPC> bots = botManager.getAllBots();
        if (sbot == null && bots.size() == 1) {
            final NPC autoSelectedBot = bots.iterator().next();
            botManager.selectBot(player.getUniqueId(), autoSelectedBot);
            sbot = botManager.getSelectedBot(player.getUniqueId());
            player.sendMessage("§aAuto-selected bot: " + autoSelectedBot.getName());
        }

        final NPC bot = sbot;

        if (bot == null) {
            player.sendMessage("§cYou must first select a bot using /bot-select <bot_name>.");
            return true;
        }

        if (!bot.isSpawned()) {
            player.sendMessage("§cYour selected bot is not spawned.");
            return true;
        }

        // Начинаем движение, очищаем список дверей
        passedDoors.clear();
        processNextDoor(bot, player);

        return true;
    }

    /**
     * Управляет движением бота: проходит через все двери на пути к игроку.
     */
    private void processNextDoor(NPC bot, Player player) {
        Block nearestDoor = findNearestDoor(bot, 10);

        if (nearestDoor != null) {
            Location doorLocation = nearestDoor.getLocation();

            // Проверяем, не проходил ли бот уже через эту дверь
            if (passedDoors.contains(doorLocation)) {
                plugin.getLogger().info("[AIBotPlugin] Bot " + bot.getName() + " already passed this door, skipping.");
                bot.getNavigator().setTarget(player.getLocation());
                return;
            }

            plugin.getLogger().info("[AIBotPlugin] Bot " + bot.getName() + " found a door and is moving towards it.");
            moveTowardsDoor(bot, doorLocation);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (bot.isSpawned() && bot.getEntity().getLocation().distance(doorLocation) < 2) {
                        openDoor(nearestDoor);
                        plugin.getLogger().info("[AIBotPlugin] Bot " + bot.getName() + " opened a door, teleporting forward.");

                        // Добавляем дверь в список пройденных
                        passedDoors.add(doorLocation);

                        // Телепортируем бота за дверь
                        moveForwardThroughDoor(bot, nearestDoor, player);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 10L, 10L);
        } else {
            // Дверей больше нет — бот идёт к игроку
            plugin.getLogger().info("[AIBotPlugin] No more doors. Moving to player.");
            bot.getNavigator().setTarget(player.getLocation());

            // Очищаем список дверей после завершения пути
            passedDoors.clear();
        }
    }

    /**
     * Ищет ближайшую дверь к боту.
     */
    private Block findNearestDoor(NPC bot, int searchRadius) {
        Location botLoc = bot.getEntity().getLocation();
        Block nearestDoor = null;
        double minDistance = Double.MAX_VALUE;

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    Block block = botLoc.clone().add(dx, dy, dz).getBlock();
                    if (isDoor(block)) {
                        double distance = botLoc.distance(block.getLocation());
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestDoor = block;
                        }
                    }
                }
            }
        }
        return nearestDoor;
    }

    /**
     * Проверяет, является ли блок дверью.
     */
    private boolean isDoor(Block block) {
        return block.getBlockData() instanceof Openable;
    }

    /**
     * Перемещает бота к двери перед её открытием.
     */
    private void moveTowardsDoor(NPC bot, Location doorLocation) {
        if (!bot.isSpawned()) return;

        plugin.getLogger().info("[DEBUG] Moving bot to door at: " + doorLocation);

        Navigator navigator = bot.getNavigator();
        navigator.cancelNavigation();
        navigator.getLocalParameters().avoidWater(false);
        navigator.getLocalParameters().stuckAction(null);
        navigator.getLocalParameters().useNewPathfinder(true);

        Location safeLocation = doorLocation.clone().add(0, 0, -1);
        navigator.setTarget(safeLocation);

        if (navigator.getPathStrategy() == null) {
            plugin.getLogger().warning("[ERROR] Bot " + bot.getName() + " couldn't find a path to the door!");
        } else {
            plugin.getLogger().info("[DEBUG] Bot " + bot.getName() + " is navigating to door.");
        }
    }

    /**
     * Телепортирует бота через дверь и продолжает движение.
     */
    private void moveForwardThroughDoor(NPC bot, Block doorBlock, Player player) {
        Location doorLoc = doorBlock.getLocation();
        Location botLoc = bot.getEntity().getLocation();

        // Вычисляем направление движения
        double dx = doorLoc.getX() - botLoc.getX();
        double dz = doorLoc.getZ() - botLoc.getZ();

        // Определяем новую точку за дверью
        Location forwardLocation = doorLoc.clone().add(dx * 1.5, 0, dz * 1.5);

        // Телепортируем бота за дверь
        plugin.getLogger().info("[DEBUG] Teleporting bot through door to: " + forwardLocation);
        bot.teleport(forwardLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

        // Продолжаем движение
        new BukkitRunnable() {
            @Override
            public void run() {
                processNextDoor(bot, player);
                cancel();
            }
        }.runTaskLater(plugin, 10L);
    }

    /**
     * Открывает дверь и автоматически закрывает её через 3 секунды.
     */
    private void openDoor(Block block) {
        if (!(block.getBlockData() instanceof Openable)) return;

        Openable door = (Openable) block.getBlockData();
        if (!door.isOpen()) {
            door.setOpen(true);
            block.setBlockData(door);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                door.setOpen(false);
                block.setBlockData(door);
            }, 60L);
        }
    }
}
