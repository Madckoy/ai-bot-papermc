package com.devone.aibot.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.devone.aibot.AIBotPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ZoneManager {

    private final AIBotPlugin plugin;
    private final File zonesFile;
    private final FileConfiguration config;
    private final Map<String, ProtectedZone> protectedZones = new HashMap<>();
    private static final long AUTO_SAVE_INTERVAL = 5 * 60 * 20; // 5 minutes in ticks

    public ZoneManager(AIBotPlugin plugin, File pluginFolder) {
        this.plugin = plugin;

        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        zonesFile = new File(pluginFolder, "zones.yml");
        config = YamlConfiguration.loadConfiguration(zonesFile);
        loadZones();

        // ✅ Schedule auto-save every 5 minutes
        scheduleAutoSave();
    }

    private void loadZones() {
        protectedZones.clear();
        if (config.contains("zones")) {
            for (String zoneName : config.getConfigurationSection("zones").getKeys(false)) {
                double x = config.getDouble("zones." + zoneName + ".x");
                double y = config.getDouble("zones." + zoneName + ".y");
                double z = config.getDouble("zones." + zoneName + ".z");
                int radius = config.getInt("zones." + zoneName + ".radius");
                protectedZones.put(zoneName, new ProtectedZone(x, y, z, radius));
                plugin.getLogger().info("[AIBotPlugin] Loaded zone: " + zoneName + " at (" + x + ", " + y + ", " + z + ") with radius " + radius);
            }
        }
        plugin.getLogger().info("[AIBotPlugin] Total zones loaded: " + protectedZones.size());
    }

    public void saveZones() {
        config.set("zones", null); // Clear old data
        for (Map.Entry<String, ProtectedZone> entry : protectedZones.entrySet()) {
            String zoneName = entry.getKey();
            ProtectedZone zone = entry.getValue();
            config.set("zones." + zoneName + ".x", (int) Math.round(zone.getX())); // ✅ Round to int
            config.set("zones." + zoneName + ".y", (int) Math.round(zone.getY()));
            config.set("zones." + zoneName + ".z", (int) Math.round(zone.getZ()));
            config.set("zones." + zoneName + ".radius", zone.getRadius());
        }
        try {
            config.save(zonesFile);
            plugin.getLogger().info("[AIBotPlugin] Zones saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().severe("[AIBotPlugin] Failed to save zones: " + e.getMessage());
        }
    }    

    public void addZone(String name, Location center, int radius) {
        protectedZones.put(name, new ProtectedZone(center.getX(), center.getY(), center.getZ(), radius));
        plugin.getLogger().info("[AIBotPlugin] Added new zone: " + name + " at " + center.toString() + " with radius " + radius);
    }

    public boolean removeZone(String name) {
        if (protectedZones.remove(name) != null) {
            plugin.getLogger().info("[AIBotPlugin] Removed zone: " + name);
            return true;
        }
        return false;
    }

    public boolean isInProtectedZone(Location location) {
        for (ProtectedZone zone : protectedZones.values()) {
            if (zone.isInside(location)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> listZones() {
        return protectedZones.keySet();
    }

    public int getZoneRadius(String zoneName) {
        ProtectedZone zone = protectedZones.get(zoneName);
        return (zone != null) ? zone.getRadius() : -1; // Return -1 if zone not found
    }

    public ProtectedZone getZone(String zoneName) {
        return protectedZones.get(zoneName);
    }    

    // ✅ Schedule periodic auto-save every 5 minutes
    private void scheduleAutoSave() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveZones, AUTO_SAVE_INTERVAL, AUTO_SAVE_INTERVAL);
    }
}
