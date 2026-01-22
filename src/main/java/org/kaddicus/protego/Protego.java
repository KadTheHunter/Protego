package org.kaddicus.protego;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Protego extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Protego enabled!");
    }

    /**
     * Prevents projectiles from breaking hanging entities
     *
     * @param event ProjectileHitEvent - Server thrown event
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Hanging) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }
}