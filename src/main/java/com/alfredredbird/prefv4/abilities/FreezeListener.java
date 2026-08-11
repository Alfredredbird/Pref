package com.alfredredbird.prefv4.abilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Frozen players (see AbilityManager#toggleFreeze) can still look around and
 * talk, but can't walk, break/place blocks, take damage, or deal damage.
 * Useful for staff to lock a suspected cheater in place while investigating
 * without having to kick them first.
 */
public class FreezeListener implements Listener {

    private final AbilityManager abilityManager;

    public FreezeListener(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!abilityManager.isFrozen(event.getPlayer())) return;
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return; // camera-only movement, allow it
        }
        event.setTo(event.getFrom());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (abilityManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (abilityManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageTaken(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && abilityManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageDealt(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && abilityManager.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You are frozen and cannot attack.").color(NamedTextColor.RED));
        }
    }
}
