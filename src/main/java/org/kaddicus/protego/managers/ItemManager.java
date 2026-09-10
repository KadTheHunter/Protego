package org.kaddicus.protego.managers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public class ItemManager {
    private final ConfigManager config;
    private final Logger logger;

    public ItemManager(ConfigManager config, Logger logger) {
        this.config = config;
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

    private boolean isDangerous(CompoundTag data) {
        if (data == null) return false;

        Tag customName = data.get("CustomName");
        Tag text = data.get("text");

        return (customName != null && exceedsLimits(customName)) ||
                (text != null && exceedsLimits(text));
    }

    private boolean exceedsLimits(Tag root) {
        return exceedsLimits(root, 0, new int[1]) || root.toString().length() > config.getItemMaxCharacters();
    }

    private boolean exceedsLimits(Tag tag, int depth, int[] nodes) {
        if (depth > config.getItemMaxDepth() || ++nodes[0] > config.getItemMaxNodes()) {
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