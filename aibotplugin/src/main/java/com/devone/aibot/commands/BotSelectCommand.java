package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.BotManager;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

public class BotSelectCommand implements CommandExecutor {

    private final AIBotPlugin plugin;
    private final BotManager botManager;

    public BotSelectCommand(AIBotPlugin plugin, BotManager botManager) {
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

        if (args.length != 1) {
            player.sendMessage("§cUsage: /bot-select <bot_name>");
            return true;
        }

        String botName = args[0];
        NPC bot = botManager.getBot(botName); // ✅ Using BotManager

        if (bot == null) {
            player.sendMessage("§cBot '" + botName + "' not found.");
            plugin.getLogger().warning("Bot '" + botName + "' not found.");
            return true;
        }

        // ✅ Store the selected bot in BotManager
        botManager.selectBot(player.getUniqueId(), botName);

        player.sendMessage("§aYou have selected bot '" + botName + "'.");
        plugin.getLogger().info("[AIBotPlugin] Player " + player.getName() + " selected bot: " + botName);

        // ✅ Use Adventure API to send modern chat messages
        String botMessage = "I am now under your command, " + player.getName() + "!";
        Component chatMessage = Component.text("§7[§b" + botName + "§7] " + botMessage);
        Bukkit.getServer().sendMessage(chatMessage);

        return true;
    }
}
