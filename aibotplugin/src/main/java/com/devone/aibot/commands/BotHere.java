package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.core.BotManager;
import com.devone.aibot.botlogic.BotNavigating;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;;

public class BotHere implements CommandExecutor {

    private final AIBotPlugin plugin;
    private final BotManager botManager;

    public BotHere(AIBotPlugin plugin, BotManager botManager) {
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
        NPC bot = botManager.getOrSelectBot(player.getUniqueId());

        if (bot == null || !bot.isSpawned()) {
            player.sendMessage("§cYou must select a spawned bot first.");
            return true;
        }

        // Вызываем навигацию бота
        new BotNavigating(plugin, bot, player).startNavigation();

        return true;
    }
}
