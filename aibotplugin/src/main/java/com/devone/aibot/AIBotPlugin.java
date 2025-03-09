package com.devone.aibot;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AIBotPlugin extends JavaPlugin implements Listener {

    private ZoneManager zoneManager;

    @Override
    public void onEnable() {
        getLogger().info("AI Bot Plugin with Citizens NPCs Enabled!");

        // Ensure the AIBotPlugin data folder exists inside /plugins
        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdirs()) {
                getLogger().info("Created plugin data folder: " + getDataFolder().getAbsolutePath());
            } else {
                getLogger().severe("Failed to create plugin data folder!");
            }
        }

        // Initialize Zone Manager
        zoneManager = new ZoneManager(getDataFolder());

        // Register Bot Commands
        BotCommandHandler botCommandHandler = new BotCommandHandler(this);
        registerCommand("bot-spawn", botCommandHandler);
        registerCommand("bot-list", botCommandHandler);
        registerCommand("bot-remove", botCommandHandler);
        registerCommand("bot-follow", botCommandHandler);
        registerCommand("bot-stop", botCommandHandler);

        // Register Zone Commands
        ZoneCommandHandler zoneCommandHandler = new ZoneCommandHandler(this, zoneManager);
        registerCommand("zone-add", zoneCommandHandler);
        registerCommand("zone-remove", zoneCommandHandler);
        registerCommand("zone-list", zoneCommandHandler);

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("AI Bot Plugin Disabled!");
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().warning("Command " + name + " not found in plugin.yml!");
        }
    }

    @org.bukkit.event.EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (zoneManager.isInProtectedZone(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot break blocks in a protected zone!");
        }
    }
}
