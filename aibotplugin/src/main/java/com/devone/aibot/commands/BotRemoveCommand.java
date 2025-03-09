package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.BotManager;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BotRemoveCommand implements CommandExecutor {
    private final AIBotPlugin plugin;
    private final BotManager botManager;

    public BotRemoveCommand(AIBotPlugin plugin, BotManager botManager) {
        this.plugin = plugin;
        this.botManager = botManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /bot-remove <bot_name>");
            return true;
        }

        String botName = args[0];
        NPC bot = botManager.getBot(botName);

        if (bot == null) {
            sender.sendMessage("§cBot '" + botName + "' not found.");
            return true;
        }

        bot.despawn();
        bot.destroy();
        botManager.removeBot(botName);

        sender.sendMessage("§aBot '" + botName + "' has been removed.");
        plugin.getLogger().info("[AIBotPlugin] Bot '" + botName + "' was removed.");

        return true;
    }
}
