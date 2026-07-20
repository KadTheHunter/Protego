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

    public void sanitizeEntity(Entity bukkitEntity, net.minecraft.world.entity.Entity nmsEntity) {
        for (String key : config.getNbtBlacklist()) {
            switch (key.toLowerCase()) {
                // Tags used by all Entities, excluding Data, ID, Pos, and UUID
                case "air": nmsEntity.setAirSupply(300); break;
                case "customname": nmsEntity.setCustomName(null); break;
                case "customnamevisible": nmsEntity.setCustomNameVisible(false); break;
                case "fall_distance": nmsEntity.resetFallDistance(); break;
                case "fire": nmsEntity.setRemainingFireTicks(0); break;
                case "glowing": nmsEntity.setGlowingTag(false); break;
                case "hasvisualfire": nmsEntity.setSharedFlagOnFire(false); break;
                case "invulnerable": nmsEntity.setInvulnerable(false); break;
                case "motion": nmsEntity.setDeltaMovement(0, 0, 0); break;
                case "nogravity": nmsEntity.setNoGravity(false); break;
                case "onground": nmsEntity.setOnGround(false); break;
                case "portalcooldown": nmsEntity.setPortalCooldown(0); break;
                case "rotation": nmsEntity.setRot(0, 0); break;
                case "silent": nmsEntity.setSilent(false); break;
                case "tags": nmsEntity.getTags().clear(); break;
                case "ticksfrozen": nmsEntity.setTicksFrozen(0); break;
            }
        }
    }

    public void sanitizePassengers(net.minecraft.world.entity.Entity entity) {
        boolean nukeAllPassengers = config.getNbtBlacklist().stream()
                .anyMatch(s -> s.equalsIgnoreCase("Passengers"));

        List<net.minecraft.world.entity.Entity> passengers = new ArrayList<>(entity.getPassengers());

        for (net.minecraft.world.entity.Entity passenger : passengers) {
            Entity bukkitPassenger = passenger.getBukkitEntity();
            EntityType passengerType = bukkitPassenger.getType();
            boolean shouldDestroy = shouldDestroyPassenger(nukeAllPassengers, passengerType);

            if (shouldDestroy) {
                passenger.stopRiding();
                passenger.discard();
                logger.info("Destroyed passenger " + passengerType +
                        " riding " + entity.getBukkitEntity().getType());
                continue;
            }

            sanitizeEntity(bukkitPassenger, passenger);
            sanitizePassengers(passenger);
        }
    }

    private boolean shouldDestroyPassenger(boolean nukeAll, EntityType type) {
        if (nukeAll) return true;
        if (config.getBlockedEntityTypes().contains(type)) return true;
        if (config.getPassengerBlacklist().contains(type)) return true;
        return !config.getPassengerWhitelist().isEmpty() &&
                !config.getPassengerWhitelist().contains(type);
    }

    public boolean isChunkLimitExceeded(Entity entity) {
        int limit = config.getGlobalChunkLimit();
        if (limit <= 0) return false;
        if (config.getChunkLimitExclusions().contains(entity.getType())) return false;

        Chunk chunk = entity.getLocation().getChunk();
        long count = Arrays.stream(chunk.getEntities())
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