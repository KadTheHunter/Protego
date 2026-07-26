package org.kaddicus.protego.managers;

import org.bukkit.Chunk;
import org.bukkit.entity.*;

import java.util.*;
import java.util.logging.Logger;

public class EntityManager {
    private final ConfigManager config;
    private final Logger logger;

    public EntityManager(ConfigManager config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public boolean isBlocked(EntityType type) {
        return config.getBlockedEntityTypes().contains(type);
    }

    public boolean shouldStrip(EntityType type) {
        return config.getStripFunctionalityTypes().contains(type);
    }

    public void stripEntityFunctionality(Entity entity) {
        if (entity instanceof org.bukkit.entity.minecart.CommandMinecart command) {
            command.setCommand("");
            logger.info("Stripped Command Minecart at " + entity.getLocation());
        }

        if (entity instanceof org.bukkit.entity.minecart.SpawnerMinecart spawner) {
            spawner.setSpawnedType(null);
            spawner.setSpawnedEntity((EntitySnapshot) null);
            spawner.setPotentialSpawns(Collections.emptyList());
            spawner.setSpawnCount(0);
            logger.info("Stripped Spawner Minecart at " + entity.getLocation());
        }
    }

    /**
     * Recursively checks all passengers on an entity and destroys any that
     * are blocked or not on the whitelist.
     */
    public void checkPassengers(net.minecraft.world.entity.Entity entity) {
        List<net.minecraft.world.entity.Entity> passengers = new ArrayList<>(entity.getPassengers());

        for (net.minecraft.world.entity.Entity passenger : passengers) {
            EntityType passengerType = passenger.getBukkitEntity().getType();
            if (shouldDestroyPassenger(passengerType)) {
                passenger.stopRiding();
                passenger.discard();
                logger.info("Destroyed passenger " + passengerType +
                        " riding " + entity.getBukkitEntity().getType());
                continue;
            }
            checkPassengers(passenger);
        }
    }

    private boolean shouldDestroyPassenger(EntityType type) {
        if (config.getBlockedEntityTypes().contains(type)) return true;
        if (config.getPassengerBlacklist().contains(type)) return true;
        return !config.getPassengerWhitelist().isEmpty() &&
                !config.getPassengerWhitelist().contains(type);
    }

    public boolean isChunkLimitExceeded(Entity entity) {
        int limit = config.getGlobalChunkLimit();
        if (limit <= 0) return false;
        if (entity.getType() == EntityType.PLAYER) return false;
        if (config.getChunkLimitExclusions().contains(entity.getType())) return false;

        Chunk chunk = entity.getLocation().getChunk();
        long count = Arrays.stream(chunk.getEntities())
                .filter(e -> e.getType() != EntityType.PLAYER)
                .filter(e -> !config.getChunkLimitExclusions().contains(e.getType()))
                .count();

        if (count >= limit) {
            logger.info("Blocked " + entity.getType() + " spawn at " + entity.getLocation() +
                    " (chunk limit reached: " + count + "/" + limit + ")");
            return true;
        }
        return false;
    }
}