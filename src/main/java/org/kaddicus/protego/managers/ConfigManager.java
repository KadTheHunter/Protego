package org.kaddicus.protego.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import java.util.*;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Logger logger;

    private final Set<EntityType> blockedEntityTypes = new HashSet<>();
    private final Set<EntityType> stripFunctionalityTypes = new HashSet<>();
    private final Set<EntityType> passengerBlacklist = new HashSet<>();
    private final Set<EntityType> passengerWhitelist = new HashSet<>();
    private final List<String> nbtBlacklist = new ArrayList<>();
    private int globalChunkLimit = -1;
    private final Set<EntityType> chunkLimitExclusions = new HashSet<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        FileConfiguration config = plugin.getConfig();

        List<String> blockedTypes = config.getStringList("blocked-entity-types");
        for (String typeName : blockedTypes) {
            try { blockedEntityTypes.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type in config: " + typeName); }
        }

        List<String> stripTypes = config.getStringList("strip-functionality-types");
        for (String typeName : stripTypes) {
            try { stripFunctionalityTypes.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type in config: " + typeName); }
        }

        nbtBlacklist.addAll(config.getStringList("nbt-blacklist"));

        List<String> blockedPassengers = config.getStringList("passenger-blacklist");
        for (String typeName : blockedPassengers) {
            try { passengerBlacklist.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type in config: " + typeName); }
        }

        List<String> allowedPassengers = config.getStringList("passenger-whitelist");
        for (String typeName : allowedPassengers) {
            try { passengerWhitelist.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type in config: " + typeName); }
        }

        globalChunkLimit = config.getInt("global-entity-per-chunk-limit", -1);

        List<String> exclusions = config.getStringList("chunk-limit-exclusions");
        for (String typeName : exclusions) {
            try { chunkLimitExclusions.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type in chunk-limit-exclusions: " + typeName); }
        }

        logger.info("Loaded " + blockedEntityTypes.size() + " blocked entity types");
        logger.info("Loaded " + stripFunctionalityTypes.size() + " strip-functionality types");
        logger.info("Loaded " + passengerBlacklist.size() + " Passenger blacklist tags");
        logger.info("Loaded " + passengerWhitelist.size() + " Passenger whitelist tags");
        logger.info("Loaded " + nbtBlacklist.size() + " NBT blacklist tags");
        logger.info("Global entity per-chunk limit: " + (globalChunkLimit == -1 ? "unlimited" : globalChunkLimit));
        logger.info("Loaded " + chunkLimitExclusions.size() + " chunk limit exclusions");
    }

    public void reloadConfig() {
        plugin.reloadConfig();

        Set<EntityType> newBlockedTypes = new HashSet<>();
        Set<EntityType> newStripTypes = new HashSet<>();
        Set<EntityType> newPassengerBlacklist = new HashSet<>();
        Set<EntityType> newPassengerWhitelist = new HashSet<>();
        Set<EntityType> newChunkExclusions = new HashSet<>();
        int newGlobalLimit = -1;

        FileConfiguration config = plugin.getConfig();

        for (String typeName : config.getStringList("blocked-entity-types")) {
            try { newBlockedTypes.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type: " + typeName); }
        }

        for (String typeName : config.getStringList("strip-functionality-types")) {
            try { newStripTypes.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type: " + typeName); }
        }

        List<String> newNbtBlacklist = new ArrayList<>(config.getStringList("nbt-blacklist"));

        for (String typeName : config.getStringList("passenger-blacklist")) {
            try { newPassengerBlacklist.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type: " + typeName); }
        }

        for (String typeName : config.getStringList("passenger-whitelist")) {
            try { newPassengerWhitelist.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type: " + typeName); }
        }

        for (String typeName : config.getStringList("chunk-limit-exclusions")) {
            try { newChunkExclusions.add(EntityType.valueOf(typeName.toUpperCase())); }
            catch (IllegalArgumentException e) { logger.warning("Unknown entity type: " + typeName); }
        }

        newGlobalLimit = config.getInt("global-entity-per-chunk-limit", -1);

        blockedEntityTypes.clear();
        blockedEntityTypes.addAll(newBlockedTypes);

        stripFunctionalityTypes.clear();
        stripFunctionalityTypes.addAll(newStripTypes);

        passengerBlacklist.clear();
        passengerBlacklist.addAll(newPassengerBlacklist);

        passengerWhitelist.clear();
        passengerWhitelist.addAll(newPassengerWhitelist);

        nbtBlacklist.clear();
        nbtBlacklist.addAll(newNbtBlacklist);

        chunkLimitExclusions.clear();
        chunkLimitExclusions.addAll(newChunkExclusions);

        globalChunkLimit = newGlobalLimit;

        logger.info("Configuration reloaded successfully");
    }

    public Set<EntityType> getBlockedEntityTypes() { return blockedEntityTypes; }
    public Set<EntityType> getStripFunctionalityTypes() { return stripFunctionalityTypes; }
    public Set<EntityType> getPassengerBlacklist() { return passengerBlacklist; }
    public Set<EntityType> getPassengerWhitelist() { return passengerWhitelist; }
    public List<String> getNbtBlacklist() { return nbtBlacklist; }
    public int getGlobalChunkLimit() { return globalChunkLimit; }
    public Set<EntityType> getChunkLimitExclusions() { return chunkLimitExclusions; }
}