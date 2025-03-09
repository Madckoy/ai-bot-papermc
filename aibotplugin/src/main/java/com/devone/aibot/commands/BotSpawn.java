package com.devone.aibot.commands;

import com.devone.aibot.AIBotPlugin;
import com.devone.aibot.core.BotManager;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BotSpawn implements CommandExecutor {
    private final AIBotPlugin plugin;
    private final BotManager botManager;
    private final Random random = new Random();

    // ✅ List of Default Minecraft Skins
    private final List<String> defaultSkins = Arrays.asList(
        "Steve", "Alex", "Ari", "Kai", "Noor", "Sunny", "Zuri", "Efe", "Makena"
    );

    public BotSpawn(AIBotPlugin plugin, BotManager botManager) {
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
        Location location = player.getLocation();

        // ✅ Select a random skin from the list
        String randomSkin = defaultSkins.get(random.nextInt(defaultSkins.size()));

        // ✅ Generate a unique bot name including the skin name
        String botName = "AI_" + randomSkin + "_" + (random.nextInt(9) + random.nextInt(9));

        // ✅ Create NPC using Citizens API
        NPC bot = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, botName);
        bot.spawn(location);

        // Запускаем патрулирование после спавна
        botManager.getBotPatroling().startPatrol(bot);

        // ✅ Apply the random skin
        applySkin(bot, randomSkin);

        // ✅ Add the bot to BotManager so it persists across restarts
        botManager.addBot(botName, bot);

        player.sendMessage("§aBot " + botName + " spawned with skin " + randomSkin + ".");
        plugin.getLogger().info("Bot " + botName + " spawned at " + location + " with skin " + randomSkin);

        return true;
    }

    private void applySkin(NPC bot, String skinName) {
        if (!bot.hasTrait(SkinTrait.class)) {
            bot.addTrait(SkinTrait.class);
        }
        bot.getOrAddTrait(SkinTrait.class).setSkinName(skinName);
    }
}
