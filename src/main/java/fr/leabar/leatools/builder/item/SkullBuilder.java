package fr.leabar.leatools.builder.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import fr.leabar.leatools.builder.AbstractItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkullBuilder extends AbstractItemBuilder<SkullMeta, SkullBuilder> {

    private static final Map<String, PlayerProfile> profileCache = new ConcurrentHashMap<>();

    public SkullBuilder() {
        super(Material.PLAYER_HEAD, SkullMeta.class);
    }

    public SkullBuilder(ItemStack skull) {
        super(skull, SkullMeta.class);
    }

    public SkullBuilder owner(String playerName) {
        if (playerName == null || playerName.isBlank()) return this;

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        meta.setOwningPlayer(player);
        return this;
    }

    public SkullBuilder player(OfflinePlayer player) {
        if (player == null) return this;
        meta.setOwningPlayer(player);
        return this;
    }

    public SkullBuilder texture(String textureValue) {
        if (textureValue == null || textureValue.isBlank()) return this;

        String cacheKey = "texture_" + textureValue.hashCode();
        PlayerProfile profile = profileCache.computeIfAbsent(cacheKey, k -> {
            PlayerProfile p = Bukkit.createProfile(UUID.randomUUID(), "CustomSkull");
            p.getProperties().add(new ProfileProperty("textures", textureValue));
            return p;
        });

        meta.setPlayerProfile(profile);
        return this;
    }


    public SkullBuilder playerProfile(PlayerProfile profile) {
        if (profile == null) return this;
        meta.setPlayerProfile(profile);
        return this;
    }

    public static ItemStack createCustomSkull(String textureValue) {
        return new SkullBuilder().texture(textureValue).build();
    }


    public static ItemStack createPlayerSkull(String playerName) {
        return new SkullBuilder().owner(playerName).build();
    }

    public static ItemStack createSkullFromProfile(PlayerProfile profile) {
        return new SkullBuilder().playerProfile(profile).build();
    }

    public static boolean isValidTexture(String textureValue) {
        try {
            if (textureValue == null || textureValue.isBlank()) return false;
            String json = new String(Base64.getDecoder().decode(textureValue));
            return json.contains("textures") && json.contains("SKIN");
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractTextureUrl(String textureValue) {
        if (!isValidTexture(textureValue)) return null;

        try {
            String json = new String(Base64.getDecoder().decode(textureValue));
            int start = json.indexOf("\"url\":\"") + 7;
            int end = json.indexOf("\"", start);
            return (start > 6 && end > start) ? json.substring(start, end) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void clearCaches() {
        profileCache.clear();
    }

    public static Map<String, Integer> getCacheStats() {
        return Map.of("profiles", profileCache.size());
    }

    private boolean isValidUrl(String url) {
        try {
            URL u = new URL(url);
            return u.getProtocol().equals("http") || u.getProtocol().equals("https");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", item.getType().name());
        if (meta.hasDisplayName()) map.put("name", meta.getDisplayName());
        if (meta.hasLore()) map.put("lore", meta.getLore());
        if (!meta.getEnchants().isEmpty()) {
            Map<String, Integer> ench = new HashMap<>();
            meta.getEnchants().forEach((e, lvl) -> ench.put(e.getKey().toString(), lvl));
            map.put("enchants", ench);
        }
        if (!meta.getItemFlags().isEmpty()) {
            map.put("flags", meta.getItemFlags().stream().map(Enum::name).toList());
        }
        if (meta.getOwningPlayer() != null) {
            map.put("owner", meta.getOwningPlayer().getName());
        }

        PlayerProfile profile = meta.getPlayerProfile();
        if (profile != null) {
            for (ProfileProperty prop : profile.getProperties()) {
                if ("textures".equals(prop.getName())) {
                    map.put("texture", prop.getValue());
                    break;
                }
            }
        }

        return map;
    }
}