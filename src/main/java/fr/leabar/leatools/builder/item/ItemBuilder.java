package fr.leabar.leatools.builder.item;

import fr.leabar.leatools.builder.AbstractItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemBuilder extends AbstractItemBuilder<ItemMeta, ItemBuilder> {
    public ItemBuilder(Material material) {
        super(material, ItemMeta.class);
    }

    public ItemBuilder(Material material, int amount) {
        super(material, ItemMeta.class);
    }

    public ItemBuilder(ItemStack item) {
        super(item, ItemMeta.class);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", item.getType().name());
        if (meta.hasDisplayName()) map.put("name", meta.getDisplayName());
        if (meta.hasLore()) map.put("lore", meta.getLore());

        if (!meta.getEnchants().isEmpty()) {
            Map<String, Integer> enchMap = new HashMap<>();
            meta.getEnchants().forEach((e, lvl) -> enchMap.put(e.getKey().toString(), lvl));
            map.put("enchants", enchMap);
        }
        if (!meta.getItemFlags().isEmpty()) {
            List<String> flags = meta.getItemFlags().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            map.put("flags", flags);
        }
        return map;
    }

}