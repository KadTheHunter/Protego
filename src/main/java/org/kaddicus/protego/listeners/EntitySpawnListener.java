package org.kaddicus.protego.listeners;

import org.kaddicus.protego.managers.EntityManager;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {
    private final EntityManager entityManager;

    public EntitySpawnListener(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();

        if (entityManager.isBlocked(entity.getType())) {
            event.setCancelled(true);
            return;
        }

        if (entityManager.isChunkLimitExceeded(entity)) {
            event.setCancelled(true);
            return;
        }

        if (entityManager.shouldStrip(entity.getType())) {
            entityManager.stripEntityFunctionality(entity);
        }

        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        entityManager.sanitizeEntity(entity, nmsEntity);
        entityManager.sanitizePassengers(nmsEntity);
    }
}