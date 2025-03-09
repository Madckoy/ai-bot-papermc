package com.devone.aibot;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class BotCommandHandler implements CommandExecutor {

    private final AIBotPlugin plugin;
    private final Map<String, NPC> botMap = new HashMap<>();
    private final Random random = new Random();

    // List of default Minecraft skins
    private final List<String> defaultSkins = Arrays.asList(
            "Steve", "Alex", "Ari", "Kai", "Noor", "Sunny", "Zuri", "Efe", "Makena"
    );

    public BotCommandHandler(AIBotPlugin plugin) {
        this.plugin = plugin;
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
            case "bot-spawn":
                spawnBot(player.getLocation(), player);
                return true;

            case "bot-list":
                listBots(player);
                return true;

            case "bot-remove":
                if (args.length != 1) {
                    player.sendMessage("§cUsage: /bot-remove <name>");
                    return true;
                }
                removeBot(args[0], player);
                return true;

            case "bot-follow":
                if (args.length != 1) {
                    player.sendMessage("§cUsage: /bot-follow <name>");
                    return true;
                }
                makeBotFollow(args[0], player);
                return true;

            case "bot-stop":
                if (args.length != 1) {
                    player.sendMessage("§cUsage: /bot-stop <name>");
                    return true;
                }
                stopBotMovement(args[0], player);
                return true;

            default:
                return false;
        }
    }

    private void spawnBot(Location location, Player player) {
        // Select a random default skin
        String randomSkin = defaultSkins.get(random.nextInt(defaultSkins.size()));

        // Generate a unique bot name including the skin name
        String botName = "Bot_" + randomSkin + "_" + (random.nextInt(9000) + 1000);

        // Create NPC using Citizens API
        NPC bot = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, botName);
        bot.spawn(location);

        // Apply the random skin
        applySkin(bot, randomSkin);

        botMap.put(botName, bot);
        player.sendMessage("§aBot " + botName + " spawned with skin " + randomSkin + ".");
        plugin.getLogger().info("Bot " + botName + " spawned at " + location + " with skin " + randomSkin);
    }

    private void applySkin(NPC bot, String skinName) {
        if (!bot.hasTrait(SkinTrait.class)) {
            bot.addTrait(SkinTrait.class);
        }
        bot.getOrAddTrait(SkinTrait.class).setSkinName(skinName);
    }

    private void listBots(Player player) {
        if (botMap.isEmpty()) {
            player.sendMessage("§cNo active bots.");
        } else {
            player.sendMessage("§aActive Bots:");
            for (String botName : botMap.keySet()) {
                player.sendMessage(" - " + botName);
            }
        }
    }

    private void removeBot(String name, Player player) {
        NPC bot = botMap.get(name);

        // Check if bot exists in our botMap
        if (bot == null) {
            player.sendMessage("§cNo bot found with this name.");
            plugin.getLogger().warning("Attempted to remove bot '" + name + "', but it was not found in botMap.");
            return;
        }

        // Check if the bot is registered in Citizens' NPC registry
        boolean botExists = false;
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.getId() == bot.getId()) {
                botExists = true;
                break;
            }
        }

        if (!botExists) {
            player.sendMessage("§cBot '" + name + "' is not registered in Citizens.");
            botMap.remove(name); // Remove from our internal map
            return;
        }

        // Properly remove the NPC from Citizens
        bot.despawn();
        bot.destroy();

        // Remove from our bot tracking map
        botMap.remove(name);

        player.sendMessage("§aBot " + name + " removed.");
        plugin.getLogger().info("Bot " + name + " was completely removed.");
    }

    private void makeBotFollow(String botName, Player player) {
        NPC bot = botMap.get(botName);
        if (bot == null) {
            player.sendMessage("§cNo bot found with this name.");
            return;
        }
        bot.getNavigator().setTarget(player, false);
        player.sendMessage("§aBot " + botName + " is now following you.");
    }

    private void stopBotMovement(String botName, Player player) {
        NPC bot = botMap.get(botName);
        if (bot == null) {
            player.sendMessage("§cNo bot found with this name.");
            return;
        }
        bot.getNavigator().cancelNavigation();
        player.sendMessage("§aBot " + botName + " has stopped moving.");
    }
}
