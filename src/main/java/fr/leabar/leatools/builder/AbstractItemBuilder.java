package fr.leabar.leatools.builder;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public abstract class AbstractItemBuilder<T extends ItemMeta> {
    protected final ItemStack item;
    protected final T meta;

    protected AbstractItemBuilder(Material material, Class<T> metaClass) {
        this(material,1, metaClass);
    }

    protected AbstractItemBuilder(Material material, int amount, Class<T> metaClass) {
        this.item = new ItemStack(material);
        this.meta = metaClass.cast(item.getItemMeta());
    }

    public AbstractItemBuilder<T> name(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public AbstractItemBuilder<T> lore(String... lines) {
        meta.setLore(Arrays.asList(lines));
        return this;
    }

    public AbstractItemBuilder<T> enchant(Enchantment enc, int level) {
        meta.addEnchant(enc, level, true);
        return this;
    }

    public AbstractItemBuilder<T> flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemStack build(){
        item.setItemMeta(meta);
        return item;
    }

    public abstract Map<String, Object> serialize();

    public abstract void deserialize(Map<String, Object> data);
}