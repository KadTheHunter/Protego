package org.kaddicus.protego;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Protego extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Protego enabled!");

        if (getCommand("evanesco") != null) {
            getCommand("evanesco").setExecutor(this);
        }
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
    /**
     * Prevents Explosions from breaking hanging entities
     *
     * @param event EntityDamageByEntityEvent - Server thrown event
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Hanging) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                    event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("protego.evanesco")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        int count = removeBadStands();
        sender.sendMessage("§aRemoved " + count + " armor stands.");
        return true;
    }

    private int removeBadStands() {
        int count = 0;

        // Track UUIDs to ensure we never process the same entity twice
        Set<UUID> processedUUIDs = new HashSet<>();

        for (World world : Bukkit.getWorlds()) {
            try {
                ServerLevel serverLevel = ((CraftWorld) world).getHandle();

                for (Chunk chunk : world.getLoadedChunks()) {
                    double minX = (chunk.getX() * 16) - 1.0;
                    double maxX = (chunk.getX() * 16) + 17.0;
                    double minZ = (chunk.getZ() * 16) - 1.0;
                    double maxZ = (chunk.getZ() * 16) + 17.0;

                    AABB aabb = new AABB(minX, world.getMinHeight(), minZ, maxX, world.getMaxHeight(), maxZ);
                    var armorStandType = net.minecraft.world.entity.EntityType.ARMOR_STAND;

                    for (net.minecraft.world.entity.decoration.ArmorStand nms : serverLevel.getEntities(armorStandType, aabb, null)) {
                        org.bukkit.entity.Entity bukkit = nms.getBukkitEntity();

                        if (bukkit instanceof ArmorStand stand) {
                            if (isBadStand(stand)) {
                                // .add() returns false if the UUID is already in the set
                                if (processedUUIDs.add(stand.getUniqueId())) {
                                    nms.discard();
                                    count++;
                                    getLogger().info("Removed Armor Stand [UUID: " + stand.getUniqueId() + "] at " + stand.getLocation());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().log(java.util.logging.Level.SEVERE, "Error removing Armor Stand(s) in world " + world.getName(), e);
            }
        }
        return count;
    }

    /**
     * Checks if an Armor Stand has a negative Health value
     *
     * @param stand The Armor Stand to check
     */
    private boolean isBadStand(ArmorStand stand) {
        /*
         * Stands with negative Health values require a negative DeathTime value to be set,
         * otherwise they will be automatically cleaned by the game.
         *
         * The presence of one implies the presence of the other :D
         */
        return stand.getHealth() <= 0.0;
    }
}