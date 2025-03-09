package com.devone.aibot;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import net.citizensnpcs.api.npc.NPC;

public class EventHandlerManager implements Listener {
    private final AIBotPlugin plugin;
    private final ZoneManager zoneManager;
    private final BotManager botManager;

    public EventHandlerManager(AIBotPlugin plugin, ZoneManager zoneManager, BotManager botManager) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;
        this.botManager = botManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // ✅ Allow players to break blocks freely
        if (event.getPlayer() instanceof Player) {
            return;
        }

        // ✅ Restrict bots from breaking blocks in protected zones
        NPC bot = botManager.getSelectedBot(event.getPlayer().getUniqueId());
        if (bot != null && zoneManager.isInProtectedZone(event.getBlock().getLocation())) {
            event.setCancelled(true);
            plugin.getLogger().info("[AIBotPlugin] Bot '" + bot.getName() + "' tried to break a protected block!");
        }
    }
}
