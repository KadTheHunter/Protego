package org.kaddicus.protego.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.kaddicus.protego.managers.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProtegoCommand implements CommandExecutor, TabCompleter {
    private final ConfigManager configManager;

    public ProtegoCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("protego.reload")) {
                sender.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                        .append(Component.text("You do not have permission to use this command.", NamedTextColor.RED)));
                return true;
            }

            configManager.reloadConfig();
            sender.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                    .append(Component.text("Configuration reloaded successfully.", NamedTextColor.GREEN)));
            return true;

        } else if (args.length > 0 && args[0].equalsIgnoreCase("evanesco")) {
            Component helpMessage = Component.text()
                    // Header
                    .append(Component.text("--- ", NamedTextColor.GOLD))
                    .append(Component.text("Protego Evanesco Help", NamedTextColor.YELLOW))
                    .append(Component.text(" ---\n", NamedTextColor.GOLD))

                    // Flag: -a
                    .append(Component.text("-a ", NamedTextColor.YELLOW))
                    .append(Component.text("» Remove all Armor Stands\n", NamedTextColor.GRAY))

                    // Flag: -u
                    .append(Component.text("-u ", NamedTextColor.YELLOW))
                    .append(Component.text("» Remove 'undead' Armor Stands (Health <= 0)\n", NamedTextColor.GRAY))

                    // Flag: -m
                    .append(Component.text("-m ", NamedTextColor.YELLOW))
                    .append(Component.text("» Remove all Minecarts\n", NamedTextColor.GRAY))

                    // Flag: -p
                    .append(Component.text("-p ", NamedTextColor.YELLOW))
                    .append(Component.text("» Remove all Projectiles (arrows, fireballs, etc.)\n", NamedTextColor.GRAY))

                    // Flag: -i
                    .append(Component.text("-i ", NamedTextColor.YELLOW))
                    .append(Component.text("» Remove all Dropped Items\n", NamedTextColor.GRAY))

                    // Flag: -d
                    .append(Component.text("-d ", NamedTextColor.YELLOW))
                    .append(Component.text("» Remove all Display Entities\n", NamedTextColor.GRAY))

                    // Spacer and footer
                    .append(Component.text("\n", NamedTextColor.GRAY))
                    .append(Component.text("Flags can be combined! e.g., ", NamedTextColor.GRAY))
                    .append(Component.text("/evanesco -pd", NamedTextColor.YELLOW))
                    .append(Component.text(" removes projectiles and drops.", NamedTextColor.GRAY))
                    .build();

            sender.sendMessage(helpMessage);
            return true;
        }

        sender.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                .append(Component.text("Entity Protection Plugin", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  ", NamedTextColor.GRAY)
                .append(Component.text("/protego reload", NamedTextColor.YELLOW))
                .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Reload the configuration", NamedTextColor.WHITE)));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            if (sender.hasPermission("protego.reload")) {
                suggestions.add("reload");
            }
            if (sender.hasPermission("protego.evanesco")) {
                suggestions.add("evanesco");
            }

            String currentArg = args[0].toLowerCase();
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}