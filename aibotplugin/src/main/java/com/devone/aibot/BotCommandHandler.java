package com.devone.aibot;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class BotCommandHandler implements CommandExecutor {

    private final AIBotPlugin plugin;
    private final Random random = new Random();
    private Map<String, NPC> botMap = new HashMap<>();

    // Store each player's selected bot
    private final Map<UUID, String> playerSelectedBot = new HashMap<>();

    // List of default Minecraft skins
    private final List<String> defaultSkins = Arrays.asList(
            "Steve", "Alex", "Ari", "Kai", "Noor", "Sunny", "Zuri", "Efe", "Makena"
    );

    public BotCommandHandler(AIBotPlugin plugin) {
        this.plugin = plugin;
    }

    public BotCommandHandler(AIBotPlugin plugin, Map<String, NPC> bMap) {
        this.plugin = plugin;
        this.botMap = bMap;
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
                case "bot-select":
                if (args.length != 1) {
                    player.sendMessage("§cUsage: /bot-select <bot_name>");
                    return true;
                }
            
                String botName = args[0];
                plugin.getLogger().info("Player " + player.getName() + " is trying to select bot: " + botName);
            
                NPC bot = botMap.get(botName);
                if (bot == null) {
                    player.sendMessage("§cBot '" + botName + "' not found.");
                    plugin.getLogger().warning("Bot '" + botName + "' not found in botMap.");
                    return true;
                }
            
                // Ensure bot has an entity
                if (bot.getEntity() == null) {
                    player.sendMessage("§cBot '" + botName + "' is not spawned.");
                    plugin.getLogger().warning("Bot '" + botName + "' exists in botMap but has no entity assigned.");
                    return true;
                }
            
                // Ensure bot is a Player
                if (!(bot.getEntity() instanceof Player)) {
                    player.sendMessage("§cBot '" + botName + "' is not a player entity.");
                    plugin.getLogger().warning("Bot '" + botName + "' is not a player entity, instead found: " + bot.getEntity().getType());
                    return true;
                }
            
                setPlayerSelectedBot(player, botName);
                player.sendMessage("§aYou have selected bot '" + botName + "'. All bot commands will now apply to this bot.");
                plugin.getLogger().info("Player " + player.getName() + " selected bot: " + botName);
            
                // ✅ Make the bot "speak" in chat
                Player botPlayer = (Player) bot.getEntity();
                String message = "§6[Bot] " + botPlayer.getName() + ": Understood! " + player.getName() + " is now my commander.";
            
                // Test if Adventure API is working
                try {
                    Bukkit.getServer().broadcast(Component.text(message));
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to send chat message using Adventure API: " + e.getMessage());
                    player.sendMessage("§cBot chat failed. Check logs for errors.");
                }        
        
            return true;

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

                case "bot-go-to":
                    if (args.length != 3) {
                        player.sendMessage("§cUsage: /bot-go-to <x> <y> <z>");
                        return true;
                    }

                    String selectedBot = getPlayerSelectedBot(player);
                    if (selectedBot == null) {
                        player.sendMessage("§cNo bot selected. Use /bot-select <bot_name> first.");
                        return true;
                    }

                    try {
                        double x = Double.parseDouble(args[0]);
                        double y = Double.parseDouble(args[1]);
                        double z = Double.parseDouble(args[2]);
                        moveBotToLocation(player, selectedBot, x, y, z);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid coordinates. Please enter valid numbers.");
                    }
                    return true;    
            default:
                return false;
        }
    }

    private void setPlayerSelectedBot(Player player, String botName) {
        playerSelectedBot.put(player.getUniqueId(), botName);
    }
    
    private String getPlayerSelectedBot(Player player) {
        return playerSelectedBot.get(player.getUniqueId());
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

    private void moveBotToLocation(Player player, String botName, double x, double y, double z) {
        NPC bot = botMap.get(botName);
        if (bot == null) {
            player.sendMessage("§cBot '" + botName + "' not found.");
            return;
        }
    
        Location targetLocation = new Location(player.getWorld(), x, y, z);
    
        // Ensure the target chunk is loaded before moving
        if (!targetLocation.getWorld().getChunkAt(targetLocation).isLoaded()) {
            player.sendMessage("§cTarget location is too far away. Move closer and try again.");
            return;
        }
    
        // Move the bot naturally to the target location
        bot.getNavigator().setTarget(targetLocation);
        bot.getNavigator().getLocalParameters().baseSpeed(0.3f); // Adjust walking speed
    
        player.sendMessage("§aBot '" + botName + "' is moving to (" + x + ", " + y + ", " + z + ").");
    }    

    @org.bukkit.event.EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        playerSelectedBot.remove(event.getPlayer().getUniqueId());
    }
}
