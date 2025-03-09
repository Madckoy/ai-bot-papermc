package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.BotManager;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class BotListCommand implements CommandExecutor {
    @SuppressWarnings("unused")
    private final AIBotPlugin plugin;
    private final BotManager botManager;

    public BotListCommand(AIBotPlugin plugin, BotManager botManager) {
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

        if (botManager.getAllBots().isEmpty()) {
            player.sendMessage("§cNo active bots.");
        } else {
            player.sendMessage("§aActive Bots:");
            for (NPC bot : botManager.getAllBots()) {
                if (bot.isSpawned()) {
                    Location loc = bot.getStoredLocation();
                    player.sendMessage(" - " + bot.getName() + " at [X: " +
                        (int) loc.getX() + ", Y: " + (int) loc.getY() + ", Z: " + (int) loc.getZ() + "]");
                }
            }
        }
        return true;
    }
}
