package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;

import java.util.HashMap;
import java.util.Map;

import io.papermc.paper.event.player.AsyncChatEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PlayerCache extends BaseListener {

    private static Map<String, String> cache = new HashMap<>();
    
    public PlayerCache(Plugin plugin) {
        super(plugin);
    }

    public static Map<String, String> getCache() {
        return new HashMap<>(cache);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cache.put(PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName()), event.getPlayer().getName());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        cache.remove(PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName()));
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        cache.remove(PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName()));
    }

    @EventHandler
    public void onCommand(PlayerCommandSendEvent event) {
        var displayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());
        cache.computeIfAbsent(displayName, s -> event.getPlayer().getName());
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var displayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());
        cache.computeIfAbsent(displayName, s -> event.getPlayer().getName());
    }
}
