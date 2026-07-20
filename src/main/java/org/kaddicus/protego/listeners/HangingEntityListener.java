package org.kaddicus.protego.listeners;

import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class HangingEntityListener implements Listener {
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Hanging) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Hanging) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                    event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }
}