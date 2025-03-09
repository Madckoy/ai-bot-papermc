package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.core.BotManager;
import com.devone.aibot.botlogic.BotNavigating;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

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
        NPC bot = botManager.getSelectedBot(player.getUniqueId());

        // Авто-выбор бота, если только один
        Collection<NPC> bots = botManager.getAllBots();
        if (bot == null && bots.size() == 1) {
            bot = bots.iterator().next();
            botManager.selectBot(player.getUniqueId(), bot);
            player.sendMessage("§aAuto-selected bot: " + bot.getName());
        }

        if (bot == null || !bot.isSpawned()) {
            player.sendMessage("§cYou must select a spawned bot first.");
            return true;
        }

        // Вызываем навигацию бота
        new BotNavigating(plugin, bot, player).startNavigation();

        return true;
    }
}
