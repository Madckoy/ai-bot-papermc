package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.core.BotManager;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Iterator;

public class BotRemoveAll implements CommandExecutor {
    private final AIBotPlugin plugin;
    private final BotManager botManager;

    public BotRemoveAll(AIBotPlugin plugin, BotManager botManager) {
        this.plugin = plugin;
        this.botManager = botManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Collection<NPC> bots = botManager.getAllBots();

        if (bots.isEmpty()) {
            sender.sendMessage("§cNo active bots to remove.");
            return true;
        }

        int removedCount = 0;
        Iterator<NPC> iterator = bots.iterator();
        while (iterator.hasNext()) {
            NPC bot = iterator.next();
            bot.despawn();
            bot.destroy();
            iterator.remove();
            removedCount++;
        }

        botManager.clearAllBots();

        sender.sendMessage("§aAll " + removedCount + " bots have been removed.");
        plugin.getLogger().info("[AIBotPlugin] All bots have been removed.");

        return true;
    }
}
