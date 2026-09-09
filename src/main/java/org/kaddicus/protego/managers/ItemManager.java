package org.kaddicus.protego.managers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public class ItemManager {
    private final Logger logger;

    private static final int MAX_CHARACTERS = 65_536;
    private static final int MAX_DEPTH = 12;
    private static final int MAX_NODES = 1_024;

    public ItemManager(Logger logger) {
        this.logger = logger;
    }

    public boolean isDangerous(ItemStack item) {
        if (item == null) {
            return false;
        }
        var nms = CraftItemStack.unwrap(item);
        var entity = nms.get(DataComponents.ENTITY_DATA);
        var bucket = nms.get(DataComponents.BUCKET_ENTITY_DATA);
        var block = nms.get(DataComponents.BLOCK_ENTITY_DATA);

        return isDangerous(entity == null ? null : entity.copyTagWithoutId())
                || isDangerous(bucket == null ? null : bucket.copyTag())
                || isDangerous(block == null ? null : block.copyTagWithoutId());
    }

    private static boolean isDangerous(CompoundTag data) {
        Tag customName = data == null ? null : data.get("CustomName");
        return customName != null && exceedsLimits(customName);
    }

    static boolean exceedsLimits(Tag root) {
        return exceedsLimits(root, 0, new int[1]) || root.toString().length() > MAX_CHARACTERS;
    }

    private static boolean exceedsLimits(Tag tag, int depth, int[] nodes) {
        if (depth > MAX_DEPTH || ++nodes[0] > MAX_NODES) {
            return true;
        }
        return switch (tag) {
            case CompoundTag compound ->
                    compound.values().stream().anyMatch(child -> exceedsLimits(child, depth + 1, nodes));
            case ListTag list -> list.stream().anyMatch(child -> exceedsLimits(child, depth + 1, nodes));
            default -> false;
        };
    }
}