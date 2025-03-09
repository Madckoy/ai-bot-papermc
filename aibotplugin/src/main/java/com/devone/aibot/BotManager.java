package com.devone.aibot;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;

import java.util.*;

public class BotManager {
    private final AIBotPlugin plugin;
    private final Map<String, NPC> botMap = new HashMap<>();
    private final Map<UUID, NPC> selectedBots = new HashMap<>(); // Track selected bot per player (stores NPC directly)

    public BotManager(AIBotPlugin plugin) {
        this.plugin = plugin;

        // Ensure botMap is repopulated after a restart
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!plugin.isEnabled()) return; // Ensure plugin is still enabled

            if (Bukkit.getPluginManager().getPlugin("Citizens") == null || !Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
                plugin.getLogger().severe("Citizens plugin is not installed or failed to load! Disabling AI Bot Plugin.");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }

            // Reload existing bots from Citizens API after restart
            loadExistingBots();
        }, 40L); // Delay ensures Citizens is fully loaded
    }

    // ✅ Get a bot by name
    public NPC getBot(String name) {
        return botMap.get(name);
    }

    // ✅ Add a bot to the map
    public void addBot(String name, NPC bot) {
        botMap.put(name, bot);
        plugin.getLogger().info("Added bot: " + name);
    }

    // ✅ Remove a bot from the map
    public void removeBot(String name) {
        botMap.remove(name);
        plugin.getLogger().info("Removed bot: " + name);
    }

    // ✅ Check if a bot exists
    public boolean botExists(String name) {
        return botMap.containsKey(name);
    }

    private void loadExistingBots() {
        plugin.getLogger().info("[AIBotPlugin] Loading existing bots...");
        
        // Clear the existing bot map to avoid duplicates
        botMap.clear();
    
        // Load bots from Citizens API
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc == null) continue; // Avoid null references
    
            String botName = npc.getName(); // Ensure we get the correct bot name
    
            // Skip default bots or unwanted bots (AI-Bot example)
            if (botName.startsWith("AI-Bot")) {
                plugin.getLogger().info("[AIBotPlugin] Skipping bot: " + botName); // Skipping unwanted bots
                continue;
            }
    
            // Add the valid bot to the map if it's not already in the map
            if (!botMap.containsKey(botName)) {
                botMap.put(botName, npc); // Add to bot map if it's not already there
                plugin.getLogger().info("[AIBotPlugin] Reloaded bot: " + botName + " (ID: " + npc.getId() + ")");
            }
        }
    
        plugin.getLogger().info("[AIBotPlugin] Bot loading complete. Total bots: " + botMap.size());
    }    

    public void cleanupBots() {
        for (NPC npc : botMap.values()) {
            if (npc != null && npc.isSpawned()) {
                npc.despawn();
                plugin.getLogger().info("[AIBotPlugin] Despawned bot: " + npc.getName());
            }
        }
        botMap.clear();
        plugin.getLogger().info("All bots have been cleaned up.");
    }    

    public Collection<NPC> getAllBots() {
        return botMap.values();
    }

    public void clearAllBots() {
        botMap.clear();
        plugin.getLogger().info("[AIBotPlugin] Cleared all bots.");
    }

    // ✅ Select a bot for a player
    public void selectBot(UUID playerUUID, NPC bot) {
        selectedBots.put(playerUUID, bot);
        plugin.getLogger().info("[AIBotPlugin] Player " + playerUUID + " selected bot: " + bot.getName());
    }

    // ✅ Get the selected bot for a player
    public NPC getSelectedBot(UUID playerUUID) {
        return selectedBots.get(playerUUID);
    }

    // ✅ Clear the selected bot for a player
    public void clearSelectedBot(UUID playerUUID) {
        selectedBots.remove(playerUUID);
    }
}
