package org.kaddicus.protego.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        Location loc = entity.getLocation();

        if (entity instanceof org.bukkit.entity.minecart.CommandMinecart command) {
            command.setCommand("");

            logger.warning("Sterilized Command Block Minecart at " + loc);

            Component msg = Component.text()
                    .append(Component.text("[Protego] ", NamedTextColor.GOLD))
                    .append(Component.text("Sterilized Command Block Minecart at ", NamedTextColor.YELLOW))
                    .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())))
                    .build();

            notifyAdmins(msg);

        }

        if (entity instanceof org.bukkit.entity.minecart.SpawnerMinecart spawner) {
            spawner.setSpawnedType(null);
            spawner.setSpawnedEntity((EntitySnapshot) null);
            spawner.setPotentialSpawns(Collections.emptyList());
            spawner.setSpawnCount(0);

            logger.warning("Sterilized Spawner Minecart at " + loc);

            Component msg = Component.text()
                    .append(Component.text("[Protego] ", NamedTextColor.GOLD))
                    .append(Component.text("Sterilized Spawner Minecart at ", NamedTextColor.YELLOW))
                    .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())))
                    .build();

            notifyAdmins(msg);
        }
    }

    public void checkPassengers(net.minecraft.world.entity.Entity entity) {
        List<EntityType> destroyedTypes = new ArrayList<>();

        checkPassengersRecursive(entity, destroyedTypes);

        if (!destroyedTypes.isEmpty()) {
            Location loc = entity.getBukkitEntity().getLocation();

            String types = destroyedTypes.stream()
                    .map(EntityType::toString)
                    .distinct()
                    .collect(Collectors.joining(", "));

            logger.warning("Destroyed " + destroyedTypes.size() +
                    " passenger(s) (" + types + ") riding " + entity.getBukkitEntity().getType() + " at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());

            Component msg = Component.text()
                    .append(Component.text("[Protego] ", NamedTextColor.GOLD))
                    .append(Component.text("Destroyed ", NamedTextColor.YELLOW))
                    .append(Component.text(destroyedTypes.size(), NamedTextColor.RED))
                    .append(Component.text(" passenger(s) (", NamedTextColor.YELLOW))
                    .append(Component.text(types, NamedTextColor.RED))
                    .append(Component.text(") riding ", NamedTextColor.YELLOW))
                    .append(Component.text(entity.getBukkitEntity().getType().toString(), NamedTextColor.RED))
                    .append(Component.text(" at ", NamedTextColor.YELLOW))
                    .append(Component.text(loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())))
                    .build();

            notifyAdmins(msg);
        }
    }

    /**
     * Recursively checks all passengers on an entity and destroys any that
     * are blocked or not on the whitelist.
     */
    private void checkPassengersRecursive(net.minecraft.world.entity.Entity entity, List<EntityType> destroyed) {
        List<net.minecraft.world.entity.Entity> passengers = new ArrayList<>(entity.getPassengers());
        for (net.minecraft.world.entity.Entity passenger : passengers) {
            EntityType passengerType = passenger.getBukkitEntity().getType();
            if (shouldDestroyPassenger(passengerType)) {
                passenger.stopRiding();
                passenger.discard();
                destroyed.add(passengerType);
                continue;
            }
            checkPassengersRecursive(passenger, destroyed);
        }
    }

    public void sanitizeCustomName(Entity bukkitEntity, net.minecraft.world.entity.Entity nmsEntity) {
        net.minecraft.network.chat.Component name = nmsEntity.getCustomName();
        if (containsMaliciousComponent(name)) {
            nmsEntity.setCustomName(null);

            Location loc = bukkitEntity.getLocation();

            logger.warning("Stripped malicious CustomName (nested Translatable/Selector) from " +
                    bukkitEntity.getType() + " at " + loc);

            Component msg = Component.text()
                    .append(Component.text("[Protego] ", NamedTextColor.GOLD))
                    .append(Component.text("Stripped malicious CustomName from ", NamedTextColor.YELLOW))
                    .append(Component.text(bukkitEntity.getType().toString(), NamedTextColor.RED))
                    .append(Component.text(" at ", NamedTextColor.YELLOW))
                    .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())))
                    .build();

            notifyAdmins(msg);
        }
    }

    private boolean containsMaliciousComponent(net.minecraft.network.chat.Component component) {
        if (component == null) return false;

        net.minecraft.network.chat.ComponentContents contents = component.getContents();
        if (contents instanceof net.minecraft.network.chat.contents.TranslatableContents ||
                contents instanceof net.minecraft.network.chat.contents.SelectorContents) {
            return true;
        }

        for (net.minecraft.network.chat.Component sibling : component.getSiblings()) {
            if (containsMaliciousComponent(sibling)) {
                return true;
            }
        }

        return false;
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

        Location loc = entity.getLocation();
        Chunk chunk = loc.getChunk();
        long count = Arrays.stream(chunk.getEntities())
                .filter(e -> e.getType() != EntityType.PLAYER)
                .filter(e -> !config.getChunkLimitExclusions().contains(e.getType()))
                .count();

        if (count >= limit) {
            logger.warning("Blocked " + entity.getType() + " spawn at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() +
                    " (chunk limit reached: " + count + "/" + limit + ")");

            Component msg = Component.text()
                    .append(Component.text("[Protego] ", NamedTextColor.GOLD))
                    .append(Component.text("Chunk limit reached at ", NamedTextColor.YELLOW))
                    .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())))
                    .append(Component.text(" (" + count + "/" + limit + ")", NamedTextColor.GRAY))
                    .build();

            notifyAdmins(msg);

            return true;
        }
        return false;
    }

    /**
     * Broadcasts a rich message to all online players with the 'protego.notify' permission.
     */
    public void notifyAdmins (Component msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("protego.notify")) {
                p.sendMessage(msg);
            }
        }
    }
}