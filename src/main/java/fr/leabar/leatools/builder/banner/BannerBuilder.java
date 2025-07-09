package fr.leabar.leatools.builder.banner;

import fr.leabar.leatools.builder.AbstractItemBuilder;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BannerBuilder extends AbstractItemBuilder<BannerMeta, BannerBuilder> {

    public BannerBuilder() {
        this(DyeColor.WHITE);
    }

    public BannerBuilder(DyeColor baseColor) {
        super(getMaterialFromColor(baseColor), BannerMeta.class);
    }

    public BannerBuilder(DyeColor baseColor, int amount) {
        super(getMaterialFromColor(baseColor), amount, BannerMeta.class);
    }

    public BannerBuilder(ItemStack item) {
        super(item, BannerMeta.class);
    }

    public BannerBuilder pattern(Pattern pattern) {
        meta.addPattern(pattern);
        return this;
    }

    public BannerBuilder pattern(PatternType type, DyeColor color) {
        return pattern(new Pattern(color, type));
    }

    public BannerBuilder patterns(Pattern... patterns) {
        for (Pattern pattern : patterns) {
            meta.addPattern(pattern);
        }
        return this;
    }

    public BannerBuilder setPatterns(List<Pattern> patterns) {
        meta.setPatterns(patterns);
        return this;
    }

    public BannerBuilder removePattern(int index) {
        meta.removePattern(index);
        return this;
    }

    public BannerBuilder clearPatterns() {
        meta.setPatterns(new ArrayList<>());
        return this;
    }

    public List<Pattern> getPatterns() {
        return meta.getPatterns();
    }

    public BannerBuilder gradientTop(DyeColor topColor, DyeColor bottomColor) {
        return pattern(PatternType.GRADIENT, topColor)
                .pattern(PatternType.GRADIENT_UP, bottomColor);
    }

    public BannerBuilder border(DyeColor borderColor) {
        return pattern(PatternType.BORDER, borderColor);
    }

    public BannerBuilder stripes(DyeColor stripeColor) {
        return pattern(PatternType.STRIPE_TOP, stripeColor)
                .pattern(PatternType.STRIPE_BOTTOM, stripeColor);
    }

    public BannerBuilder cross(DyeColor crossColor) {
        return pattern(PatternType.CROSS, crossColor);
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

        if (!meta.getPatterns().isEmpty()) {
            List<Map<String, String>> patterns = new ArrayList<>();
            for (Pattern pattern : meta.getPatterns()) {
                Map<String, String> patternData = new HashMap<>();
                patternData.put("type", pattern.getPattern().name());
                patternData.put("color", pattern.getColor().name());
                patterns.add(patternData);
            }
            map.put("patterns", patterns);
        }

        return map;
    }

    private static Material getMaterialFromColor(DyeColor color) {
        switch (color) {
            case WHITE: return Material.WHITE_BANNER;
            case ORANGE: return Material.ORANGE_BANNER;
            case MAGENTA: return Material.MAGENTA_BANNER;
            case LIGHT_BLUE: return Material.LIGHT_BLUE_BANNER;
            case YELLOW: return Material.YELLOW_BANNER;
            case LIME: return Material.LIME_BANNER;
            case PINK: return Material.PINK_BANNER;
            case GRAY: return Material.GRAY_BANNER;
            case LIGHT_GRAY: return Material.LIGHT_GRAY_BANNER;
            case CYAN: return Material.CYAN_BANNER;
            case PURPLE: return Material.PURPLE_BANNER;
            case BLUE: return Material.BLUE_BANNER;
            case BROWN: return Material.BROWN_BANNER;
            case GREEN: return Material.GREEN_BANNER;
            case RED: return Material.RED_BANNER;
            case BLACK: return Material.BLACK_BANNER;
            default: return Material.WHITE_BANNER;
        }
    }
}