package com.devone.aibot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.devone.aibot.commands.BotFollowCommand;
import com.devone.aibot.commands.BotListCommand;
import com.devone.aibot.commands.BotRemoveAllCommand;
import com.devone.aibot.commands.BotRemoveCommand;
import com.devone.aibot.commands.BotSelectCommand;
import com.devone.aibot.commands.BotSpawnCommand;
import com.devone.aibot.commands.ZoneAddCommand;
import com.devone.aibot.commands.ZoneListCommand;
import com.devone.aibot.commands.ZoneRemoveCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Map<String, CommandExecutor> commandExecutors = new HashMap<>();

    public CommandDispatcher(AIBotPlugin plugin, BotManager botManager, ZoneManager zoneManager) {
        this.plugin = plugin;
        registerCommands(plugin, botManager, zoneManager);
    }

    // ✅ Registers all commands dynamically
    private void registerCommands(AIBotPlugin plugin, BotManager botManager, ZoneManager zoneManager) {
        registerCommand("bot-spawn", new BotSpawnCommand(plugin, botManager));
        registerCommand("bot-select", new BotSelectCommand(plugin, botManager));
        registerCommand("bot-list", new BotListCommand(plugin, botManager));
        registerCommand("bot-remove", new BotRemoveCommand(plugin, botManager));
        registerCommand("bot-remove-all", new BotRemoveAllCommand(plugin, botManager));
        registerCommand("bot-follow", new BotFollowCommand(plugin, botManager));
        //registerCommand("bot-stop", new BotStopCommand(plugin, botManager));

        registerCommand("zone-add", new ZoneAddCommand(zoneManager));
        registerCommand("zone-remove", new ZoneRemoveCommand(zoneManager));
        registerCommand("zone-list", new ZoneListCommand(zoneManager));
    }

    // ✅ Registers a single command and assigns an executor
    private void registerCommand(String command, CommandExecutor executor) {
        commandExecutors.put(command.toLowerCase(), executor);
        plugin.getCommand(command).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandExecutor executor = commandExecutors.get(command.getName().toLowerCase());

        if (executor != null) {
            return executor.onCommand(sender, command, label, args);
        }

        sender.sendMessage("§cUnknown command: " + command.getName());
        return false;
    }
}
