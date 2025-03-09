package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.core.BotManager;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BotPatrol implements CommandExecutor {
    @SuppressWarnings("unused")
    private final AIBotPlugin plugin;
    private final BotManager botManager;

    public BotPatrol(AIBotPlugin plugin, BotManager botManager) {
        this.plugin = plugin;
        this.botManager = botManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        botManager.getPlugin().getLogger().info("[AIBotPlugin] Active bots in memory: " + botManager.getAllBots());

        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }
    
        Player player = (Player) sender;
    

        NPC bot = botManager.getOrSelectBot(player.getUniqueId());

        if (bot == null || !bot.isSpawned()) {
            player.sendMessage("§cYou must select a spawned bot first.");
            return true;
        }

        if (botManager.getBotPatroling() == null) {
            sender.sendMessage(Component.text("Bot patrolling system is not initialized!", NamedTextColor.RED));
            return true;
        }
    
        Location botLocation = bot.getEntity().getLocation();
        botManager.getBotPatroling().setPatrolCenter(botLocation);
        botManager.getBotPatroling().startPatrol(bot);
    
        player.sendMessage(Component.text("Bot patrol center set to its location: ", NamedTextColor.GREEN)
            .append(Component.text(botLocation.getBlockX() + ", " + botLocation.getBlockY() + ", " + botLocation.getBlockZ(), NamedTextColor.YELLOW)));
    
        return true;
    }
    
}
