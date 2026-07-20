package org.kaddicus.protego.commands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class EvanescoCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public EvanescoCommand(JavaPlugin plugin) {
        this.plugin = plugin;
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
                                    plugin.getLogger().info("Removed Armor Stand [UUID: " + stand.getUniqueId() + "] at " + stand.getLocation());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error removing Armor Stand(s) in world " + world.getName(), e);
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