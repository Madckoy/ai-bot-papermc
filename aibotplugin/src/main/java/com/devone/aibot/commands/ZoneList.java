package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.core.ProtectedZone;
import com.devone.aibot.core.ZoneManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ZoneList implements CommandExecutor {
    private final ZoneManager zoneManager;

    public ZoneList(AIBotPlugin plugin, ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (zoneManager.listZones().isEmpty()) {
            sender.sendMessage("§cNo protected zones.");
        } else {
            sender.sendMessage("§aProtected Zones:");
            for (String name : zoneManager.listZones()) {
                int radius = zoneManager.getZoneRadius(name);
                ProtectedZone zone = zoneManager.getZone(name); // ✅ Get zone details
                if (zone != null) {
                    sender.sendMessage(" - " + name + " (Radius: " + radius + ") at [X: " +
                        (int) zone.getX() + ", Y: " + (int) zone.getY() + ", Z: " + (int) zone.getZ() + "]");
                }
            }
        }
        return true;
    }
}
