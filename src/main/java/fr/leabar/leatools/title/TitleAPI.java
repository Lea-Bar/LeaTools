package fr.leabar.leatools.title;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TitleAPI {
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null || !player.isOnline()) return;
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            nmsPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
            if (title != null && !title.isEmpty()) {
                nmsPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
            }
            if (subtitle != null && !subtitle.isEmpty()) {
                nmsPlayer.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
            }
        } catch (Exception e) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 60, 20);
    }

    public static void sendTitle(Player player, String title) {
        sendTitle(player, title, null);
    }

    public static void sendActionBar(Player player, String message) {
        if (player == null || !player.isOnline() || message == null) return;
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            nmsPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.literal(message)));
        } catch (Exception e) {
            player.sendActionBar(message);
        }
    }

    public static void clearTitle(Player player) {
        if (player == null || !player.isOnline()) return;
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            nmsPlayer.connection.send(new ClientboundClearTitlesPacket(false));
        } catch (Exception e) {
            player.sendTitle("", "", 0, 1, 0);
        }
    }

    public static void resetTitle(Player player) {
        if (player == null || !player.isOnline()) return;
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            nmsPlayer.connection.send(new ClientboundClearTitlesPacket(true));
        } catch (Exception ignored) {}
    }

    public static void sendTitleToAll(Collection<? extends Player> players, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (players == null || players.isEmpty()) return;

        ClientboundSetTitlesAnimationPacket timingPacket = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        ClientboundSetTitleTextPacket titlePacket = title != null && !title.isEmpty() ?
                new ClientboundSetTitleTextPacket(Component.literal(title)) : null;
        ClientboundSetSubtitleTextPacket subtitlePacket = subtitle != null && !subtitle.isEmpty() ?
                new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)) : null;

        for (Player player : players) {
            if (player == null || !player.isOnline()) continue;
            try {
                ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
                nmsPlayer.connection.send(timingPacket);
                if (titlePacket != null) nmsPlayer.connection.send(titlePacket);
                if (subtitlePacket != null) nmsPlayer.connection.send(subtitlePacket);

            } catch (Exception e) {
                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            }
        }
    }

    public static void sendTitleToAll(Collection<? extends Player> players, String title, String subtitle) {
        sendTitleToAll(players, title, subtitle, 10, 60, 20);
    }

    public static void sendActionBarToAll(Collection<? extends Player> players, String message) {
        if (players == null || players.isEmpty() || message == null) return;
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal(message));
        for (Player player : players) {
            if (player == null || !player.isOnline()) continue;
            try {
                ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
                nmsPlayer.connection.send(packet);
            } catch (Exception e) {
                player.sendActionBar(message);
            }
        }
    }

}
