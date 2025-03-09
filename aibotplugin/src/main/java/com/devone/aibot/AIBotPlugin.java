package com.devone.aibot;

import org.bukkit.plugin.java.JavaPlugin;

public class AIBotPlugin extends JavaPlugin {
    private ZoneManager zoneManager;
    private BotManager botManager;

    @Override
    public void onEnable() {
        getLogger().info("AI Bot Plugin is starting...");

        ensureDataFolderExists(); // ✅ Move folder check into its own method

        // ✅ Initialize managers
        botManager = new BotManager(this);
        zoneManager = new ZoneManager(this, getDataFolder());

        // ✅ Initialize command dispatcher
        new CommandDispatcher(this, botManager, zoneManager);

        // ✅ Initialize event handler manager
        new EventHandlerManager(this, zoneManager, botManager);

        getLogger().info("AI Bot Plugin has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AI Bot Plugin is shutting down...");
    
        // Cleanup all bots, including despawning any "AI-Bot" instances
        botManager.cleanupBots();
    
        getLogger().info("AI Bot Plugin has been disabled.");
    }
    

    private void ensureDataFolderExists() {
        if (!getDataFolder().exists() && getDataFolder().mkdirs()) {
            getLogger().info("Created plugin data folder: " + getDataFolder().getAbsolutePath());
        } else if (!getDataFolder().exists()) {
            getLogger().severe("Failed to create plugin data folder!");
        }
    }

    // ✅ Provide access to bot manager for other classes
    public BotManager getBotManager() {
        return botManager;
    }
}
