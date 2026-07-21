package org.kaddicus.protego.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class SpawnerListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block.getType() == Material.SPAWNER || block.getType() == Material.TRIAL_SPAWNER) {
            event.setCancelled(true);
            player.sendMessage("§cSpawners are not allowed.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();
            if (block.getType() == Material.SPAWNER || block.getType() == Material.TRIAL_SPAWNER) {
                if (event.getItem() != null && event.getItem().getItemMeta() instanceof SpawnEggMeta) {
                    event.setCancelled(true);
                    player.sendMessage("§cSpawners cannot be updated.");
                }
            }
        }
    }
}