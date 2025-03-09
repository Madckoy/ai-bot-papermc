package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.BotManager;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.ai.Navigator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Openable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class BotFollowCommand implements CommandExecutor {

    private final AIBotPlugin plugin;
    private final BotManager botManager;

    public BotFollowCommand(AIBotPlugin plugin, BotManager botManager) {
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

        // Auto-select bot if only 1 bot exists
        Collection<NPC> bots = botManager.getAllBots();
        if (sbot == null && bots.size() == 1) {
            final NPC autoSelectedBot = bots.iterator().next();  // Auto-select if only one bot exists
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

        // Enable pathfinding with advanced navigation
        Navigator navigator = bot.getNavigator();
        navigator.setTarget(player, true);
        navigator.getLocalParameters()
                .range(32.0f)  // Set the range the bot follows
                .stuckAction(null)  // Define what to do if the bot gets stuck
                .useNewPathfinder(true);  // Use new pathfinding if available

        player.sendMessage("§aYour bot is now following you and can open doors.");
        plugin.getLogger().info("[AIBotPlugin] Bot '" + bot.getName() + "' is following player " + player.getName());

        // Path update loop (runs every 10 ticks = 0.5 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bot.isSpawned() || !player.isOnline()) {
                    cancel();
                    return;
                }

                // Keep updating bot movement
                if (navigator.isNavigating()) {
                    navigator.setTarget(player.getLocation());  // Keep setting the target to the player's location
                }

                // Check the blocks in front of the bot (e.g., 5 blocks)
                for (int i = 1; i <= 5; i++) {
                    Block frontBlock = bot.getEntity().getLocation().getBlock().getRelative(BlockFace.NORTH, i);

                    // If there's a door in the way, move closer to it and open it
                    if (isDoor(frontBlock)) {
                        plugin.getLogger().info("[AIBotPlugin] Bot " + bot.getName() + " is moving towards and opening a door.");

                        // Move closer to the door
                        moveTowardsDoor(bot, frontBlock);

                        // Open the door if the bot is near it
                        openDoor(frontBlock);
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);  // Run every 0.5 seconds

        return true;
    }

    private boolean isDoor(Block block) {
        Material type = block.getType();
        return type == Material.OAK_DOOR || type == Material.SPRUCE_DOOR ||
               type == Material.BIRCH_DOOR || type == Material.JUNGLE_DOOR ||
               type == Material.ACACIA_DOOR || type == Material.DARK_OAK_DOOR ||
               type == Material.IRON_DOOR || type == Material.WARPED_DOOR ||
               type == Material.CRIMSON_DOOR || type == Material.OAK_TRAPDOOR ||
               type == Material.SPRUCE_TRAPDOOR || type == Material.BIRCH_TRAPDOOR ||
               type == Material.JUNGLE_TRAPDOOR || type == Material.ACACIA_TRAPDOOR ||
               type == Material.DARK_OAK_TRAPDOOR || type == Material.IRON_TRAPDOOR ||
               type == Material.OAK_FENCE_GATE || type == Material.SPRUCE_FENCE_GATE ||
               type == Material.BIRCH_FENCE_GATE || type == Material.JUNGLE_FENCE_GATE ||
               type == Material.ACACIA_FENCE_GATE || type == Material.DARK_OAK_FENCE_GATE;
    }

    private void openDoor(Block block) {
        if (!(block.getBlockData() instanceof Openable)) return;

        Openable door = (Openable) block.getBlockData();
        if (!door.isOpen()) {
            door.setOpen(true);
            block.setBlockData(door);

            // Close the door after 3 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                door.setOpen(false);
                block.setBlockData(door);
            }, 60L); // 3 seconds
        }
    }

    private void moveTowardsDoor(NPC bot, Block doorBlock) {
        // Move towards the door's location before trying to open it
        bot.getNavigator().setTarget(doorBlock.getLocation());
    }
}
