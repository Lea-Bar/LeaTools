package fr.leabar.leatools.builder.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.leabar.leatools.builder.AbstractItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SkullBuilder extends AbstractItemBuilder<SkullMeta> {

    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, PlayerProfile> profileCache = new ConcurrentHashMap<>();
    private static final UUID randomUUID = UUID.randomUUID();

    private static boolean isPaper;
    private static Field profileField;
    private static Method setProfileMethod;
    private static boolean initialized = false;

    static {
        initialize();
    }

    public SkullBuilder() {
        super(Material.PLAYER_HEAD, SkullMeta.class);
    }

    public SkullBuilder(ItemStack skull) {
        super(skull, SkullMeta.class);
    }

    private static void initialize() {
        if (initialized) return;

        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            isPaper = true;
        } catch (ClassNotFoundException e) {
            isPaper = false;
        }

        if (!isPaper) {
            try {
                String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                Class<?> skullMetaClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftMetaSkull");

                profileField = getField(skullMetaClass, "profile");
                if (profileField != null) {
                    profileField.setAccessible(true);
                }

                try {
                    setProfileMethod = getMethod(skullMetaClass, "setProfile", GameProfile.class);
                    if (setProfileMethod != null) {
                        setProfileMethod.setAccessible(true);
                    }
                } catch (Exception ignored) {}

            } catch (Exception ignored) {}
        }

        initialized = true;
    }

    private static Field getField(Class<?> clazz, String name) {
        String key = clazz.getName() + "." + name;
        return fieldCache.computeIfAbsent(key, k -> {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                return null;
            }
        });
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... params) {
        String key = clazz.getName() + "." + name + "(" + Arrays.toString(params) + ")";
        return methodCache.computeIfAbsent(key, k -> {
            try {
                return clazz.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException e) {
                return null;
            }
        });
    }

    public SkullBuilder owner(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return this;
        }

        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
            meta.setOwningPlayer(player);
            return this;
        } catch (Exception e) {
            if (isPaper) {
                return setPlayerProfile(createPlayerProfile(playerName, null));
            } else {
                return setGameProfile(createGameProfile(playerName, null));
            }
        }
    }

    public SkullBuilder texture(String textureValue) {
        if (textureValue == null || textureValue.trim().isEmpty()) {
            return this;
        }

        String profileId = "texture_" + textureValue.hashCode();

        if (isPaper) {
            PlayerProfile profile = profileCache.computeIfAbsent(profileId, k -> {
                PlayerProfile p = Bukkit.createProfile(randomUUID, "CustomSkull");
                p.getProperties().add(new ProfileProperty("textures", textureValue));
                return p;
            });
            return setPlayerProfile(profile);
        } else {
            GameProfile profile = createGameProfile("CustomSkull", textureValue);
            return setGameProfile(profile);
        }
    }

    public SkullBuilder skinUrl(String skinUrl) {
        if (skinUrl == null || skinUrl.trim().isEmpty()) {
            return this;
        }

        if (!isValidUrl(skinUrl)) {
            return this;
        }

        String textureJson = "{\"textures\":{\"SKIN\":{\"url\":\"" + skinUrl + "\"}}}";
        String encoded = Base64.getEncoder().encodeToString(textureJson.getBytes());

        return texture(encoded);
    }

    public SkullBuilder player(OfflinePlayer player) {
        if (player == null) {
            return this;
        }

        meta.setOwningPlayer(player);
        return this;
    }

    public SkullBuilder playerProfile(PlayerProfile profile) {
        if (profile == null || !isPaper) {
            return this;
        }

        return setPlayerProfile(profile);
    }

    private PlayerProfile createPlayerProfile(String name, String textureValue) {
        String cacheKey = name + (textureValue != null ? "_" + textureValue.hashCode() : "");

        return profileCache.computeIfAbsent(cacheKey, k -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(name.getBytes()), name);
            if (textureValue != null) {
                profile.getProperties().add(new ProfileProperty("textures", textureValue));
            }
            return profile;
        });
    }

    private GameProfile createGameProfile(String name, String textureValue) {
        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name);
        if (textureValue != null) {
            profile.getProperties().put("textures", new Property("textures", textureValue));
        }
        return profile;
    }

    private SkullBuilder setPlayerProfile(PlayerProfile profile) {
        if (profile == null) {
            return this;
        }

        try {
            Method setPlayerProfileMethod = meta.getClass().getMethod("setPlayerProfile", PlayerProfile.class);
            setPlayerProfileMethod.invoke(meta, profile);
        } catch (Exception e) {
            try {
                if (profile.getName() != null) {
                    meta.setOwningPlayer(Bukkit.getOfflinePlayer(profile.getName()));
                }
            } catch (Exception ignored) {}
        }

        return this;
    }

    private SkullBuilder setGameProfile(GameProfile profile) {
        if (profile == null) {
            return this;
        }

        try {
            if (setProfileMethod != null) {
                setProfileMethod.invoke(meta, profile);
                return this;
            }

            if (profileField != null) {
                profileField.set(meta, profile);
                return this;
            }
        } catch (Exception ignored) {}

        return this;
    }

    private boolean isValidUrl(String url) {
        try {
            URL u = new URL(url);
            return u.getProtocol().equals("http") || u.getProtocol().equals("https");
        } catch (Exception e) {
            return false;
        }
    }

    public static ItemStack createCustomSkull(String textureValue) {
        return new SkullBuilder().texture(textureValue).build();
    }

    public static ItemStack createSkullFromUrl(String skinUrl) {
        return new SkullBuilder().skinUrl(skinUrl).build();
    }

    public static ItemStack createPlayerSkull(String playerName) {
        return new SkullBuilder().owner(playerName).build();
    }

    public static ItemStack createSkullFromProfile(PlayerProfile profile) {
        return new SkullBuilder().playerProfile(profile).build();
    }

    public static boolean isValidTexture(String textureValue) {
        if (textureValue == null || textureValue.trim().isEmpty()) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(textureValue);
            String json = new String(decoded);
            return json.contains("textures") && json.contains("SKIN");
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractTextureUrl(String textureValue) {
        if (!isValidTexture(textureValue)) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(textureValue);
            String json = new String(decoded);

            int urlStart = json.indexOf("\"url\":\"") + 7;
            int urlEnd = json.indexOf("\"", urlStart);

            if (urlStart > 6 && urlEnd > urlStart) {
                return json.substring(urlStart, urlEnd);
            }
        } catch (Exception ignored) {}

        return null;
    }

    public static void clearCaches() {
        profileCache.clear();
    }

    public static Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("profiles", profileCache.size());
        stats.put("fields", fieldCache.size());
        stats.put("methods", methodCache.size());
        stats.put("paper", isPaper ? 1 : 0);
        return stats;
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
        if (meta.getOwningPlayer() != null) {
            map.put("owner", meta.getOwningPlayer().getName());
        }
        if (isPaper) {
            try {
                Method getPlayerProfileMethod = meta.getClass().getMethod("getPlayerProfile");
                PlayerProfile profile = (PlayerProfile) getPlayerProfileMethod.invoke(meta);
                if (profile != null) {
                    for (ProfileProperty property : profile.getProperties()) {
                        if ("textures".equals(property.getName())) {
                            map.put("texture", property.getValue());
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        } else {
            try {
                if (profileField != null) {
                    GameProfile profile = (GameProfile) profileField.get(meta);
                    if (profile != null && profile.getProperties().containsKey("textures")) {
                        Property textureProperty = profile.getProperties().get("textures").iterator().next();
                        map.put("texture", textureProperty.value());
                    }
                }
            } catch (Exception ignored) {}
        }
        return map;
    }
}