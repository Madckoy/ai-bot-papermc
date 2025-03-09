package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.BotManager;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.ai.Navigator;
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
        // Select bot for the player
        NPC sbot = botManager.getSelectedBot(player.getUniqueId());

        // Auto-select bot if only 1 bot exists
        Collection<NPC> bots = botManager.getAllBots();
        
        if (sbot == null && bots.size() == 1) {
            final NPC autoSelectedBot = bots.iterator().next();  // Auto-select if only one bot exists
            botManager.selectBot(player.getUniqueId(), autoSelectedBot);
            // Select bot for the player
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
            }
        }.runTaskTimer(plugin, 10L, 10L);  // Run every 0.5 seconds

        return true;
    }
}
