package com.alfredredbird.prefv4.logging;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Minimal Discord webhook client. Sends a single embed per call using Java's
 * built-in HttpClient so the plugin doesn't need to ship a JSON/HTTP
 * dependency. Every send is async and swallows its own failures - a broken
 * or unset webhook should never affect gameplay or spam the console.
 */
public final class DiscordWebhook {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private DiscordWebhook() {}

    public static void sendEmbed(String webhookUrl, String botName, String title, String description, int color, Logger logger) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        String json = "{"
                + "\"username\":\"" + escape(botName) + "\","
                + "\"embeds\":[{"
                + "\"title\":\"" + escape(title) + "\","
                + "\"description\":\"" + escape(description) + "\","
                + "\"color\":" + color
                + "}]"
                + "}";

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
        } catch (IllegalArgumentException ex) {
            logger.warning("Discord webhook URL is invalid: " + ex.getMessage());
            return;
        }

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .whenComplete((response, error) -> {
                    if (error != null) {
                        logger.log(Level.WARNING, "Failed to deliver Discord webhook log", error);
                    } else if (response.statusCode() >= 300) {
                        logger.warning("Discord webhook responded with status " + response.statusCode());
                    }
                });
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
