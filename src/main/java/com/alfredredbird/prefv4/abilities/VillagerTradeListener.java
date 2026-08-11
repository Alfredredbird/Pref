package com.alfredredbird.prefv4.abilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Villager trade "result" slot is always index 2 (0 and 1 are the two
 * ingredient slots). Rather than editing the recipe's ingredient amounts
 * to zero -- which Paper rejects with IllegalArgumentException, since a
 * recipe can't have an empty ingredient -- we let the vanilla trade go
 * through normally (it consumes the ingredients as usual) and then hand
 * the player back exactly what the trade just took, one tick later. Net
 * cost ends up at zero without touching the recipe object at all.
 */
public class VillagerTradeListener implements Listener {

    private static final int RESULT_SLOT = 2;

    private final JavaPlugin plugin;
    private final AbilityManager abilityManager;

    public VillagerTradeListener(JavaPlugin plugin, AbilityManager abilityManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    @EventHandler
    public void onTradeClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory merchantInventory)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!abilityManager.hasFreeTrades(player)) return;
        if (event.getRawSlot() != RESULT_SLOT) return;

        MerchantRecipe recipe = merchantInventory.getSelectedRecipe();
        if (recipe == null) return;

        List<ItemStack> ingredients = recipe.getIngredients();

        // Run next tick, after the vanilla trade has actually consumed the ingredients.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            for (ItemStack ingredient : ingredients) {
                if (ingredient != null && ingredient.getAmount() > 0) {
                    player.getInventory().addItem(ingredient.clone());
                }
            }
        });
    }
}
