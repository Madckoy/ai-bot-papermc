package com.devone.aibot.core;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.botlogic.BotPatroling;

import java.util.*;

public class BotManager {
    private final AIBotPlugin plugin;
    private final Map<String, NPC> botMap = new HashMap<>();
    private final Map<UUID, NPC> selectedBots = new HashMap<>(); // Track selected bot per player (stores NPC directly)
    private BotPatroling botPatroling;

    public BotManager(AIBotPlugin plugin) {
        this.plugin = plugin;

        botPatroling = new BotPatroling(this);

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
        }, 60L); // Delay ensures Citizens is fully loaded
    }
    // ✅ Get the bot patroling instance
    public BotPatroling getBotPatroling() {
        return botPatroling;
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

    // ✅ Cleanup all bots
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
    
    public AIBotPlugin getPlugin() {
        return plugin;
    }

    public NPC getOrSelectBot(UUID playerId) {
        NPC bot = getSelectedBot(playerId);
        
        // Если бот не выбран и есть только один бот - выбираем его автоматически
        Collection<NPC> bots = getAllBots();
        if (bot == null && bots.size() == 1) {
            bot = bots.iterator().next();
            selectBot(playerId, bot);
            getPlugin().getLogger().info("[AIBotPlugin] Auto-selected bot: " + bot.getName() + " for player " + playerId);
        }
    
        return bot;
    }
 
    
    private void loadExistingBots() {
        plugin.getLogger().info("[AIBotPlugin] Loading existing bots...");
    
        botMap.clear();
    
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc == null) continue;
    
            String botName = npc.getName();
            botMap.put(botName, npc);

            Location loc = npc.getStoredLocation();

            botPatroling.setPatrolCenter(loc);
            botPatroling.startPatrol(npc);
        }
    
        plugin.getLogger().info("[AIBotPlugin] Bot loading complete. Total bots: " + botMap.size());

    }
    
}
