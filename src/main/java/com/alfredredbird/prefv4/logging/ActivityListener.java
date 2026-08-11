package com.alfredredbird.prefv4.logging;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Mirrors chat and command usage (from any plugin, not just Prefv4) to
 * Discord via EventLogger. Runs at MONITOR priority so it only logs things
 * that actually happened - e.g. a chat message another plugin cancelled, or
 * a command another plugin blocked, won't get logged.
 */
public class ActivityListener implements Listener {

    private final EventLogger eventLogger;

    public ActivityListener(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        eventLogger.logCommand(event.getPlayer(), event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        eventLogger.logChat(event.getPlayer(), plainMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        eventLogger.logJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        eventLogger.logLeave(event.getPlayer());
    }
}
