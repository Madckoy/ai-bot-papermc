package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.core.BotManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        botManager.getBotPatroling().setPatrolCenter(player.getLocation());
        player.sendMessage("§aBot patrol center set to your location.");
        return true;
    }
}
