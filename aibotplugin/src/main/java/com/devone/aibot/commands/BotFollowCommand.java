package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.BotManager;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.ai.Navigator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        NPC bot = botManager.getSelectedBot(player.getUniqueId());

        if (bot == null) {
            sender.sendMessage("§cYou must first select a bot using /bot-select <bot_name>.");
            return true;
        }

        if (!bot.isSpawned()) {
            sender.sendMessage("§cYour selected bot is not spawned.");
            return true;
        }

        // ✅ Make bot follow the player using the Citizens Navigator API
        Navigator navigator = bot.getNavigator();
        navigator.setTarget(player, false); // Follow without attacking
        navigator.getLocalParameters().range(2.5f).stuckAction(null); // Follow distance

        sender.sendMessage("§aYour bot is now following you.");
        plugin.getLogger().info("[AIBotPlugin] Bot '" + bot.getName() + "' is following player " + player.getName());

        return true;
    }
}
