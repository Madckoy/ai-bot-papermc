package com.devone.aibot.commands;

import com.devone.aibot.ZoneManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ZoneRemoveCommand implements CommandExecutor {
    private final ZoneManager zoneManager;

    public ZoneRemoveCommand(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /zone-remove <zone_name>");
            return true;
        }

        String zoneName = args[0];
        if (zoneManager.removeZone(zoneName)) {
            sender.sendMessage("§aZone '" + zoneName + "' removed.");
        } else {
            sender.sendMessage("§cZone '" + zoneName + "' not found.");
        }
        return true;
    }
}
