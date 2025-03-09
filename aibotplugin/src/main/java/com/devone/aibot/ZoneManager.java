package com.devone.aibot;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ZoneManager {

    private final File zonesFile;
    private final FileConfiguration config;
    private final Map<String, ProtectedZone> protectedZones = new HashMap<>();

    public ZoneManager(File pluginFolder) {
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        zonesFile = new File(pluginFolder, "zones.yml");
        config = YamlConfiguration.loadConfiguration(zonesFile);
        loadZones();
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
            }
        }
    }

    public void saveZones() {
        config.set("zones", null); // Clear old data
        for (Map.Entry<String, ProtectedZone> entry : protectedZones.entrySet()) {
            String zoneName = entry.getKey();
            ProtectedZone zone = entry.getValue();
            config.set("zones." + zoneName + ".x", zone.getX());
            config.set("zones." + zoneName + ".y", zone.getY());
            config.set("zones." + zoneName + ".z", zone.getZ());
            config.set("zones." + zoneName + ".radius", zone.getRadius());
        }
        try {
            config.save(zonesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addZone(String name, Location center, int radius) {
        protectedZones.put(name, new ProtectedZone(center.getX(), center.getY(), center.getZ(), radius));
        saveZones();
    }

    public boolean removeZone(String name) {
        if (protectedZones.remove(name) != null) {
            saveZones();
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
}
