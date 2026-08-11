package com.alfredredbird.prefv4.abilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * hidePlayer() only affects players who are online at the moment it's called.
 * Without this, someone who vanishes and then a new player joins would be
 * visible to that new player. This re-applies the hide on join.
 */
public class VanishJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final AbilityManager abilityManager;

    public VanishJoinListener(JavaPlugin plugin, AbilityManager abilityManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        for (UUID vanishedId : abilityManager.getVanishedIds()) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedId);
            if (vanishedPlayer != null && !vanishedPlayer.equals(joined)) {
                joined.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }
}
