package org.kaddicus.protego.listeners;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.kaddicus.protego.managers.ItemManager;

public class MaliciousItemListener implements Listener {
    private final ItemManager itemManager;

    public MaliciousItemListener(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && itemManager.isDangerous(item)) {
            blockEvent(event.getPlayer(), item, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (itemManager.isDangerous(item)) {
            blockEvent(event.getPlayer(), item, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (itemManager.isDangerous(item)) {
            blockEvent(event.getPlayer(), item, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPreDispense(BlockPreDispenseEvent event) {
        if (itemManager.isDangerous(event.getItemStack())) {
            event.setCancelled(true);

            Location loc = event.getBlock().getLocation();
            Component msg = Component.text()
                    .append(Component.text("[Protego] ", NamedTextColor.GOLD))
                    .append(Component.text("Blocked dangerous item in dispenser at ", NamedTextColor.YELLOW))
                    .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())))
                    .build();

            notifyAdmins(msg);
        }
    }

    private void blockEvent(Player player, ItemStack item, Cancellable event) {
        event.setCancelled(true);
        player.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                .append(Component.text("Blocked an unsafe entity-data item.", NamedTextColor.RED)));

        Location loc = player.getLocation();

        Component msg = Component.text()
                .append(Component.text("[Protego] ", NamedTextColor.GOLD))
                .append(Component.text(player.getName() + " triggered malicious item block (", NamedTextColor.YELLOW))
                .append(Component.text(item.getType().toString(), NamedTextColor.RED))
                .append(Component.text(") at ", NamedTextColor.YELLOW))
                .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())))
                .build();

        notifyAdmins(msg);
    }

    private void notifyAdmins(Component msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("protego.notify")) {
                p.sendMessage(msg);
            }
        }
    }
}