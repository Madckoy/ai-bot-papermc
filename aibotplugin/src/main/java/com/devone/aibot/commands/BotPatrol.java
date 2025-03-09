package com.devone.aibot.commands;

import com.devone.aibot.botlogic.BotPatroling;
import com.devone.aibot.core.BotManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BotPatrol implements CommandExecutor {
    private final BotPatroling patroling;

    public BotPatrol(BotManager botManage) {
        this.patroling = botManage.getBotPatroling();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        patroling.setPatrolCenter(player.getLocation());
        player.sendMessage("§aBot patrol center set to your location.");
        return true;
    }
}
