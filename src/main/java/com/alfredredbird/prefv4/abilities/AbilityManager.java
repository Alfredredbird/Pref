package com.alfredredbird.prefv4.abilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Holds per-player toggle state for the abilities menu and the
 * background task that keeps "infinite breathing" topped up.
 * Everything here uses the Bukkit API directly (no command dispatch),
 * so none of it prints to console.
 */
public class AbilityManager {

    private final JavaPlugin plugin;

    private final Set<UUID> freeTrades = new HashSet<>();
    private final Set<UUID> infiniteBreathing = new HashSet<>();
    private final Set<UUID> vanished = new HashSet<>();
    private final Set<UUID> godMode = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();

    private BukkitTask breathingTask;

    public AbilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startBreathingTask();
    }

    private void startBreathingTask() {
        // Runs once a second; cheap enough for a handful of toggled players.
        breathingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID id : infiniteBreathing) {
                Player player = Bukkit.getPlayer(id);
                if (player != null && player.isOnline()) {
                    player.setRemainingAir(player.getMaximumAir());
                }
            }
        }, 20L, 20L);
    }

    public void shutdown() {
        if (breathingTask != null) {
            breathingTask.cancel();
        }
    }

    // --- Free villager trades ---

    public boolean toggleFreeTrades(Player player) {
        UUID id = player.getUniqueId();
        if (!freeTrades.add(id)) {
            freeTrades.remove(id);
            return false;
        }
        return true;
    }

    public boolean hasFreeTrades(Player player) {
        return freeTrades.contains(player.getUniqueId());
    }

    // --- Infinite breathing ---

    public boolean toggleInfiniteBreathing(Player player) {
        UUID id = player.getUniqueId();
        if (!infiniteBreathing.add(id)) {
            infiniteBreathing.remove(id);
            return false;
        }
        player.setRemainingAir(player.getMaximumAir());
        return true;
    }

    public boolean hasInfiniteBreathing(Player player) {
        return infiniteBreathing.contains(player.getUniqueId());
    }

    // --- Vanish (used by the command menu) ---

    public boolean toggleVanish(Player player) {
        UUID id = player.getUniqueId();
        boolean nowVanished = !vanished.contains(id);
        if (nowVanished) {
            vanished.add(id);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.hidePlayer(plugin, player);
                }
            }
            // Actual invisibility: no model, no equipment glint, no particles,
            // and mobs stop targeting/reacting to the player.
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, true, false, false));
            player.setCollidable(false);
        } else {
            vanished.remove(id);
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.setCollidable(true);
        }
        return nowVanished;
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    /** Used by VanishJoinListener to re-hide already-vanished players from someone who just joined. */
    public Set<UUID> getVanishedIds() {
        return vanished;
    }

    // --- God mode (used by the command menu) ---

    public boolean toggleGodMode(Player player) {
        UUID id = player.getUniqueId();
        boolean now = !godMode.contains(id);
        if (now) {
            godMode.add(id);
        } else {
            godMode.remove(id);
        }
        player.setInvulnerable(now);
        return now;
    }

    public boolean hasGodMode(Player player) {
        return godMode.contains(player.getUniqueId());
    }

    // --- Freeze (staff tool: locks a player in place while you investigate/talk to them) ---

    public boolean toggleFreeze(Player player) {
        UUID id = player.getUniqueId();
        boolean now = !frozen.contains(id);
        if (now) {
            frozen.add(id);
        } else {
            frozen.remove(id);
        }
        return now;
    }

    public boolean isFrozen(Player player) {
        return frozen.contains(player.getUniqueId());
    }
}
