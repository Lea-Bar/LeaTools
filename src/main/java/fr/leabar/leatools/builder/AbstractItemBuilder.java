package fr.leabar.leatools.builder;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public abstract class AbstractItemBuilder<T extends ItemMeta, U extends AbstractItemBuilder<T,U>> {
    protected final ItemStack item;
    protected final T meta;

    protected AbstractItemBuilder(Material material, Class<T> metaClass) {
        this(material,1, metaClass);
    }

    protected AbstractItemBuilder(Material material, int amount, Class<T> metaClass) {
        this(new ItemStack(material, amount), metaClass);
    }

    protected AbstractItemBuilder(ItemStack item, Class<T> metaClass) {
        this.item = item;
        this.meta = metaClass.cast(item.getItemMeta());
    }

    public U name(String name) {
        meta.setDisplayName(name);
        return (U) this;
    }

    public U lore(String... lines) {
        meta.setLore(Arrays.asList(lines));
        return (U) this;
    }

    public U enchant(Enchantment enc, int level) {
        meta.addEnchant(enc, level, true);
        return (U) this;
    }

    public U flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return (U) this;
    }

    public ItemStack build(){
        item.setItemMeta(meta);
        return item;
    }

    public abstract Map<String, Object> serialize();
}