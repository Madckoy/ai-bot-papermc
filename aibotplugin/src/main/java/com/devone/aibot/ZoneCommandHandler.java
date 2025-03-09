package com.devone.aibot;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class ZoneCommandHandler implements CommandExecutor {

    private final AIBotPlugin plugin;
    private final ZoneManager zoneManager;

    public ZoneCommandHandler(AIBotPlugin plugin, ZoneManager zoneManager) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use these commands.");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "zone-add":
                return handleZoneAdd(player, args);

            case "zone-remove":
                return handleZoneRemove(player, args);

            case "zone-list":
                return handleZoneList(player);

            default:
                return false;
        }
    }

    private boolean handleZoneAdd(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("§cUsage: /zone-add <radius> <zone_name>");
            return true;
        }

        try {
            int radius = Integer.parseInt(args[0]);
            String zoneName = args[1];
            Location location = player.getLocation();

            zoneManager.addZone(zoneName, location, radius);
            player.sendMessage("§aProtected zone '" + zoneName + "' added at your location with radius " + radius);
            plugin.getLogger().info("Zone '" + zoneName + "' added at " + location + " with radius " + radius);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid radius. Please enter a number.");
        }

        return true;
    }

    private boolean handleZoneRemove(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage("§cUsage: /zone-remove <zone_name>");
            return true;
        }

        String zoneName = args[0];
        if (zoneManager.removeZone(zoneName)) {
            player.sendMessage("§aZone '" + zoneName + "' removed.");
            plugin.getLogger().info("Zone '" + zoneName + "' removed.");
        } else {
            player.sendMessage("§cZone '" + zoneName + "' not found.");
        }

        return true;
    }

    private boolean handleZoneList(Player player) {
        Set<String> zones = zoneManager.listZones();
        if (zones.isEmpty()) {
            player.sendMessage("§cNo protected zones found.");
        } else {
            player.sendMessage("§aProtected Zones:");
            for (String zoneName : zones) {
                int radius = zoneManager.getZoneRadius(zoneName);
                player.sendMessage(" - " + zoneName + " (Radius: " + radius + ")");
            }
        }
        return true;
    }
}
