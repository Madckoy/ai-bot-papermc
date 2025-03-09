package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.BotManager;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.ai.Navigator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Openable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BotFollowCommand implements CommandExecutor {
    private final AIBotPlugin plugin;
    private final BotManager botManager;

    public BotFollowCommand(AIBotPlugin plugin, BotManager botManager) {
        this.plugin = plugin;
        this.botManager = botManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        NPC bot = botManager.getSelectedBot(player.getUniqueId());

        if (bot == null) {
            player.sendMessage("§cYou must first select a bot using /bot-select <bot_name>.");
            return true;
        }

        if (!bot.isSpawned()) {
            player.sendMessage("§cYour selected bot is not spawned.");
            return true;
        }

        // Enable pathfinding with advanced navigation
        Navigator navigator = bot.getNavigator();
        navigator.setTarget(player, true);
        navigator.getLocalParameters()
                .range(4.0f)
                .stuckAction(null)
                .useNewPathfinder(true);

        player.sendMessage("§aYour bot is now following you and can open doors.");
        plugin.getLogger().info("[AIBotPlugin] Bot '" + bot.getName() + "' is following player " + player.getName());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bot.isSpawned() || !player.isOnline()) {
                    cancel();
                    return;
                }

                Block botBlock = bot.getEntity().getLocation().getBlock();
                Block frontBlock = botBlock.getRelative(BlockFace.NORTH);

                if (isDoor(frontBlock)) {
                    openDoor(frontBlock);
                }

                if (navigator.isNavigating()) {
                    navigator.setTarget(player.getLocation());
                }
            }
        }.runTaskTimer(plugin, 10L, 10L); // Run every 0.5 seconds

        return true;
    }

    private boolean isDoor(Block block) {
        Material type = block.getType();
        return type == Material.OAK_DOOR || type == Material.SPRUCE_DOOR || // Add all door materials
               type == Material.BIRCH_DOOR || type == Material.IRON_DOOR;
    }

    private void openDoor(Block block) {
        if (!(block.getBlockData() instanceof Openable)) return;

        Openable door = (Openable) block.getBlockData();
        if (!door.isOpen()) {
            door.setOpen(true);
            block.setBlockData(door);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                door.setOpen(false);
                block.setBlockData(door);
            }, 60L); // Close the door after 3 seconds
        }
    }
}
