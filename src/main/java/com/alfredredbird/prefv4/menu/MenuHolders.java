package com.alfredredbird.prefv4.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * One small InventoryHolder per menu screen. MenuListener checks
 * "getInventory().getHolder() instanceof X" to know which screen
 * a click happened in and how to route it.
 */
public final class MenuHolders {

    private MenuHolders() {}

    public static class MainMenuHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override
        public @NotNull Inventory getInventory() { return inventory; }
    }

    public static class ItemMenuHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override
        public @NotNull Inventory getInventory() { return inventory; }
    }

    public static class CommandMenuHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override
        public @NotNull Inventory getInventory() { return inventory; }
    }

    public static class AbilitiesMenuHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override
        public @NotNull Inventory getInventory() { return inventory; }
    }

    public static class KickMenuHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override
        public @NotNull Inventory getInventory() { return inventory; }
    }

    /** One of these is created per "pick a kick reason" screen, tied to a single target player. */
    public static class KickReasonMenuHolder implements InventoryHolder {
        private Inventory inventory;
        private final java.util.UUID targetId;
        public KickReasonMenuHolder(java.util.UUID targetId) { this.targetId = targetId; }
        public java.util.UUID getTargetId() { return targetId; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override
        public @NotNull Inventory getInventory() { return inventory; }
    }
}
