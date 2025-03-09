package com.devone.aibot.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.commands.BotHere;
import com.devone.aibot.commands.BotList;
import com.devone.aibot.commands.BotPatrol;
import com.devone.aibot.commands.BotRemoveAll;
import com.devone.aibot.commands.BotRemove;
import com.devone.aibot.commands.BotSelect;
import com.devone.aibot.commands.BotSpawn;
import com.devone.aibot.commands.ZoneAdd;
import com.devone.aibot.commands.ZoneList;
import com.devone.aibot.commands.ZoneRemove;

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
        registerCommand("bot-spawn", new BotSpawn(plugin, botManager));
        registerCommand("bot-select", new BotSelect(plugin, botManager));
        registerCommand("bot-list", new BotList(plugin, botManager));
        registerCommand("bot-remove", new BotRemove(plugin, botManager));
        registerCommand("bot-remove-all", new BotRemoveAll(plugin, botManager));
        registerCommand("bot-here", new BotHere(plugin, botManager));
        registerCommand("bot-patrol", new BotPatrol(plugin, botManager));

        registerCommand("zone-add", new ZoneAdd(plugin, zoneManager));
        registerCommand("zone-remove", new ZoneRemove(plugin, zoneManager));
        registerCommand("zone-list", new ZoneList(plugin, zoneManager));
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
