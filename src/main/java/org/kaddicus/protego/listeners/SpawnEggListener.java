package org.kaddicus.protego.listeners;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SpawnEggListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG")) return;

        Player player = event.getPlayer();

        if (player.hasPermission("protego.spawnegg")) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                .append(Component.text("You do not have permission to use spawn eggs.", NamedTextColor.RED)));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPreDispense(BlockPreDispenseEvent event) {
        if (event.getItemStack().getType().name().endsWith("_SPAWN_EGG")) {
            event.setCancelled(true);
        }
    }
}