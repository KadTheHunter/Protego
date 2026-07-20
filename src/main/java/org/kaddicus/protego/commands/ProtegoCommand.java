package org.kaddicus.protego.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.kaddicus.protego.managers.ConfigManager;

import java.util.*;

public class ProtegoCommand implements CommandExecutor {
    private final ConfigManager configManager;

    public ProtegoCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("protego.reload")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }

            configManager.reloadConfig();
            sender.sendMessage("§aProtego configuration reloaded successfully.");
            return true;
        }

        sender.sendMessage("§6Protego §7- Entity Protection Plugin");
        sender.sendMessage("§7/protego reload §8- §fReload the configuration");
        return true;
    }
}