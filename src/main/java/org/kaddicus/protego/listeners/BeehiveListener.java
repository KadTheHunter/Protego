package org.kaddicus.protego.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.logging.Logger;

public class BeehiveListener implements Listener {
    private final Logger logger;

    public BeehiveListener(Logger logger) {
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        checkAndSanitizeHive(event.getBlock(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        checkAndSanitizeHive(event.getBlock(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShear(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getItem() != null && event.getItem().getType() == Material.SHEARS) {
                checkAndSanitizeHive(event.getClickedBlock(), event.getPlayer());
            }
        }
    }

    private void checkAndSanitizeHive(Block block, Player player) {
        if (block.getType() != Material.BEEHIVE && block.getType() != Material.BEE_NEST) return;

        BlockState state = block.getState();
        if (state instanceof Beehive hive && hive.getEntityCount() > 3) {
            hive.clearEntities();
            hive.update(true, false);
            player.sendMessage("§cHive sanitized: cleared all bees.");

            Location loc = hive.getLocation();

            logger.warning(player.getName() + " triggered hive sanitization at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());

            Component msg = Component.text()
                    .append(Component.text("[Protego] ", NamedTextColor.GOLD))
                    .append(Component.text(player.getName() + " triggered hive sanitization at ", NamedTextColor.YELLOW))
                    .append(Component.text(loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())))
                    .build();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("protego.notify") && !onlinePlayer.equals(player)) {
                    onlinePlayer.sendMessage(msg);
                }
            }
        }
    }
}