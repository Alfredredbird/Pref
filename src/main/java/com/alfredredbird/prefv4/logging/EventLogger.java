package com.alfredredbird.prefv4.logging;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Central place for turning in-game events into Discord log messages.
 * Settings are re-read from config.yml on every call (cheap in-memory
 * lookups) so /pref reload picks up changes without a restart.
 */
public class EventLogger {

    private static final int COLOR_COMMAND = 0x5865F2; // blurple
    private static final int COLOR_CHAT = 0x2ECC71;     // green
    private static final int COLOR_JOIN = 0x3498DB;     // blue
    private static final int COLOR_LEAVE = 0x95A5A6;    // gray
    private static final int COLOR_ADMIN = 0xE74C3C;    // red

    private final JavaPlugin plugin;

    public EventLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    private String webhookUrl() {
        return config().getString("discord.webhook-url", "");
    }

    private String botName() {
        return config().getString("discord.bot-name", "Prefv4 Logger");
    }

    public void logCommand(Player player, String fullCommand) {
        if (!config().getBoolean("discord.log-commands", true)) return;
        send("Command Used", player.getName() + " ran `" + sanitize(fullCommand) + "`", COLOR_COMMAND);
    }

    public void logChat(Player player, String message) {
        if (!config().getBoolean("discord.log-chat", true)) return;
        send("Chat Message", "**" + sanitize(player.getName()) + "**: " + sanitize(message), COLOR_CHAT);
    }

    public void logJoin(Player player) {
        if (!config().getBoolean("discord.log-joins-leaves", true)) return;
        send("Player Joined", sanitize(player.getName()) + " joined the server", COLOR_JOIN);
    }

    public void logLeave(Player player) {
        if (!config().getBoolean("discord.log-joins-leaves", true)) return;
        send("Player Left", sanitize(player.getName()) + " left the server", COLOR_LEAVE);
    }

    public void logAdminAction(CommandSender actor, String description) {
        if (!config().getBoolean("discord.log-admin-actions", true)) return;
        send("Admin Action", sanitize(actor.getName()) + " " + description, COLOR_ADMIN);
    }

    private void send(String title, String description, int color) {
        DiscordWebhook.sendEmbed(webhookUrl(), botName(), title, description, color, plugin.getLogger());
    }

    /** Strips Discord markdown control characters out of player-supplied text so it can't break formatting or ping @everyone. */
    private String sanitize(String input) {
        if (input == null) return "";
        return input
                .replace("@everyone", "@\u200Beveryone")
                .replace("@here", "@\u200Bhere")
                .replace("`", "'");
    }
}
