package org.kaddicus.protego.listeners;

import org.kaddicus.protego.managers.EntityManager;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class VehicleCreateListener implements Listener {
    private final EntityManager entityManager;

    public VehicleCreateListener(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleCreate(VehicleCreateEvent event) {
        Entity entity = event.getVehicle();

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

        entityManager.checkPassengers(((CraftEntity) entity).getHandle());
    }
}