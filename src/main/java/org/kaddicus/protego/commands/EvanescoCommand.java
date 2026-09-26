package org.kaddicus.protego.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.kaddicus.protego.managers.ConfigManager;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EvanescoCommand implements CommandExecutor {
    private final ConfigManager config;
    private final Logger logger;

    private static final Set<Class<? extends Entity>> PROJECTILE_CLASSES = Set.of(
            AbstractArrow.class, Fireball.class, Snowball.class, Egg.class,
            EnderPearl.class, ThrownPotion.class, Trident.class, WitherSkull.class,
            SmallFireball.class, DragonFireball.class, ShulkerBullet.class,
            LlamaSpit.class, Firework.class
    );

    private static final Set<Class<? extends Entity>> DISPLAY_CLASSES = Set.of(
            TextDisplay.class, BlockDisplay.class, ItemDisplay.class,
            Marker.class, Interaction.class
    );

    public EvanescoCommand(ConfigManager config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    /**
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player player)) {
            sender.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                    .append(Component.text("This command can only be used by players.", NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("protego.evanesco")) {
            player.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                    .append(Component.text("You do not have permission to use this command.", NamedTextColor.RED)));
            return true;
        }

        int radius = config.getEvanescoMaxRadius();
        StringBuilder flagsBuilder = new StringBuilder();

        for (String arg : args) {
            if (arg.startsWith("-")) {
                flagsBuilder.append(arg.substring(1).toLowerCase());
            } else {
                try {
                    radius = Integer.parseInt(arg);
                } catch (NumberFormatException ignored) {}
            }
        }

        String flags = flagsBuilder.toString();
        radius = Math.min(radius, config.getEvanescoMaxRadius());
        if (radius < 1) radius = 1;

        boolean hasFlags = !flags.isEmpty();
        boolean targetAllArmorStands = flags.contains("a");
        boolean targetUndeadArmorStands = flags.contains("u");
        boolean targetMinecarts = flags.contains("m");
        boolean targetProjectiles = flags.contains("p");
        boolean targetItemDrops = flags.contains("i");
        boolean targetDisplays = flags.contains("d");


        Map<EntityType, Integer> removedByType = new HashMap<>();
        Location loc = player.getLocation();
        int playerChunkX = loc.getChunk().getX();
        int playerChunkZ = loc.getChunk().getZ();
        World world = loc.getWorld();

        if (targetUndeadArmorStands) {
            int badStands = removeUndeadStandsNMS(world, playerChunkX, playerChunkZ, radius);
            if (badStands > 0) {
                removedByType.put(EntityType.ARMOR_STAND, badStands);
            }
        }

        for (int x = playerChunkX - radius; x <= playerChunkX + radius; x++) {
            for (int z = playerChunkZ - radius; z <= playerChunkZ + radius; z++) {
                if (!world.isChunkLoaded(x, z)) continue;

                Chunk chunk = world.getChunkAt(x, z);
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getType() == EntityType.PLAYER) continue;

                    // Skip armor stands here if -u was used, as the NMS method handles them separately
                    if (targetUndeadArmorStands && entity instanceof ArmorStand) {
                        continue;
                    }

                    boolean shouldRemove = false;

                    if (hasFlags) {
                        if (targetItemDrops && entity instanceof Item) {
                            shouldRemove = true;
                        } else if (targetProjectiles && isProjectile(entity)) {
                            shouldRemove = true;
                        } else if (targetMinecarts && entity instanceof Minecart) {
                            shouldRemove = true;
                        } else if (targetAllArmorStands && entity instanceof ArmorStand) {
                            shouldRemove = true;
                        } else if (targetDisplays && isDisplay(entity)) {
                            shouldRemove = true;
                        }
                    } else {
                        if (!config.getEvanescoKeepList().contains(entity.getType())) {
                            shouldRemove = true;
                        }
                    }

                    if (shouldRemove) {
                        entity.remove();
                        removedByType.merge(entity.getType(), 1, Integer::sum);
                    }
                }
            }
        }

        int removedCount = removedByType.values().stream().mapToInt(Integer::intValue).sum();
        Component countText = Component.text(removedCount, NamedTextColor.YELLOW);
        Component radiusText = Component.text(radius, NamedTextColor.YELLOW);

        String typeBreakdown = removedByType.entrySet().stream()
                .sorted(Map.Entry.<EntityType, Integer>comparingByValue().reversed())
                .map(entry -> entry.getValue() + " " + entry.getKey().toString())
                .collect(Collectors.joining(", "));

        if (removedCount == 0) {
            player.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                    .append(Component.text("Evanesco: No entities found in a ", NamedTextColor.GREEN))
                    .append(radiusText)
                    .append(Component.text(" chunk radius.", NamedTextColor.GREEN)));
            logger.info("Evanesco: No entities found in a " + radius + " chunk radius.");
        } else if (hasFlags) {
            player.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                    .append(Component.text("Evanesco: Removed ", NamedTextColor.GREEN))
                    .append(countText)
                    .append(Component.text(" targeted entities (", NamedTextColor.GREEN))
                    .append(Component.text(typeBreakdown, NamedTextColor.GRAY))
                    .append(Component.text(") in a ", NamedTextColor.GREEN))
                    .append(radiusText)
                    .append(Component.text(" chunk radius.", NamedTextColor.GREEN)));
            logger.info("Evanesco: Removed " + removedCount + " targeted entities (" + typeBreakdown + ") in a " + radius + " chunk radius.");
        } else {
            player.sendMessage(Component.text("[Protego] ", NamedTextColor.GOLD)
                    .append(Component.text("Evanesco: Removed ", NamedTextColor.GREEN))
                    .append(countText)
                    .append(Component.text(" entities (", NamedTextColor.GREEN))
                    .append(Component.text(typeBreakdown, NamedTextColor.GRAY))
                    .append(Component.text(") in a ", NamedTextColor.GREEN))
                    .append(radiusText)
                    .append(Component.text(" chunk radius.", NamedTextColor.GREEN)));
            logger.info("Evanesco: Removed " + removedCount + " entities (" + typeBreakdown + ") in a " + radius + " chunk radius.");
        }

        return true;
    }

    /**
     * Special removal method for bad/undead armor stands.
     * Bypasses Bukkit's API to guarantee removal of entities with health <= 0.
     * @param world        The world the player is in
     * @param playerChunkX X coordinate of the chunk the player is in
     * @param playerChunkZ Z coordinate of the chunk the player is in
     * @param radius       The radius of chunks to check
     * @return The number of entities removed
     */
    private int removeUndeadStandsNMS(World world, int playerChunkX, int playerChunkZ, int radius) {
        int count = 0;
        Set<UUID> processedUUIDs = new HashSet<>();

        try {
            net.minecraft.server.level.ServerLevel serverLevel = ((org.bukkit.craftbukkit.CraftWorld) world).getHandle();
            var armorStandType = net.minecraft.world.entity.EntityType.ARMOR_STAND;

            for (int x = playerChunkX - radius; x <= playerChunkX + radius; x++) {
                for (int z = playerChunkZ - radius; z <= playerChunkZ + radius; z++) {
                    if (!world.isChunkLoaded(x, z)) continue;

                    // Expand AABB slightly to ensure we catch entities on chunk borders
                    double minX = (x * 16) - 1.0;
                    double maxX = (x * 16) + 17.0;
                    double minZ = (z * 16) - 1.0;
                    double maxZ = (z * 16) + 17.0;

                    net.minecraft.world.phys.AABB aabb = new net.minecraft.world.phys.AABB(
                            minX, world.getMinHeight(), minZ,
                            maxX, world.getMaxHeight(), maxZ
                    );

                    for (net.minecraft.world.entity.decoration.ArmorStand nms : serverLevel.getEntities(armorStandType, aabb, null)) {
                        org.bukkit.entity.Entity bukkit = nms.getBukkitEntity();
                        if (bukkit instanceof ArmorStand stand) {
                            if (stand.getHealth() <= 0.0) {
                                if (processedUUIDs.add(stand.getUniqueId())) {
                                    nms.discard();
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback in case of NMS reflection/handle issues
            e.printStackTrace();
        }

        return count;
    }

    /**
     * @param entity The entity to check
     * @return true if the entity is a projectile
     */
    private boolean isProjectile(Entity entity) {
        for (Class<? extends Entity> projectileClass : PROJECTILE_CLASSES) {
            if (projectileClass.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param entity The entity to check
     * @return true if the entity is a display
     */
    private boolean isDisplay(Entity entity) {
        for (Class<? extends Entity> displayClass : DISPLAY_CLASSES) {
            if (displayClass.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }
}