package com.alfredredbird.prefv4.menu;

import com.alfredredbird.prefv4.abilities.AbilityManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MenuManager {

    // Pool of "fun" items the item menu picks randomly from.
    public static final List<Material> ITEM_POOL = Arrays.asList(
            Material.DIAMOND, Material.NETHERITE_INGOT, Material.EMERALD,
            Material.ENCHANTED_GOLDEN_APPLE, Material.GOLDEN_APPLE, Material.TOTEM_OF_UNDYING,
            Material.ELYTRA, Material.NETHER_STAR, Material.BEACON,
            Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.BOW, Material.CROSSBOW, Material.TRIDENT,
            Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE, Material.DIAMOND_AXE,
            Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.DIAMOND_HELMET,
            Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS, Material.SHIELD,
            Material.ENDER_PEARL, Material.ENDER_EYE, Material.EXPERIENCE_BOTTLE,
            Material.GOLDEN_CARROT, Material.CAKE, Material.SADDLE, Material.NAME_TAG,
            Material.FIREWORK_ROCKET, Material.TNT, Material.OBSIDIAN, Material.BEDROCK,
            Material.SPAWNER, Material.DRAGON_EGG, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE
    );

    private final JavaPlugin plugin;
    private final AbilityManager abilityManager;
    private final Random random = new Random();
    private final NamespacedKey targetUuidKey;

    public MenuManager(JavaPlugin plugin, AbilityManager abilityManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
        this.targetUuidKey = new NamespacedKey(plugin, "target_uuid");
    }

    // ---------- MAIN MENU ----------

    public void openMainMenu(Player player) {
        MenuHolders.MainMenuHolder holder = new MenuHolders.MainMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 9, Component.text("Pref Menu").color(NamedTextColor.DARK_PURPLE));
        holder.setInventory(inv);

        inv.setItem(2, namedItem(Material.DIAMOND_BLOCK, NamedTextColor.AQUA, "Creative Mode", null));
        inv.setItem(3, namedItem(Material.GRASS_BLOCK, NamedTextColor.GREEN, "Survival Mode", null));
        inv.setItem(4, namedItem(Material.CHEST, NamedTextColor.YELLOW, "Item Menu", List.of("Random items, click Shuffle for more")));
        inv.setItem(5, namedItem(Material.COMMAND_BLOCK, NamedTextColor.RED, "Admin Commands", null));
        inv.setItem(6, namedItem(Material.DIAMOND_SWORD, NamedTextColor.LIGHT_PURPLE, "Abilities", null));
        inv.setItem(7, namedItem(Material.PLAYER_HEAD, NamedTextColor.DARK_RED, "Player Menu", List.of("Left-click: kick", "Right-click: kick with a reason", "Shift-click: toggle freeze")));

        fillEmpty(inv);
        player.openInventory(inv);
    }

    // ---------- ITEM MENU ----------

    public void openItemMenu(Player player) {
        MenuHolders.ItemMenuHolder holder = new MenuHolders.ItemMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("Item Menu").color(NamedTextColor.YELLOW));
        holder.setInventory(inv);

        randomizeItems(inv);

        inv.setItem(49, namedItem(Material.NETHER_STAR, NamedTextColor.GOLD, "Shuffle", List.of("Click to re-roll these items")));
        inv.setItem(53, namedItem(Material.ARROW, NamedTextColor.GRAY, "Back", null));

        player.openInventory(inv);
    }

    /** Refills slots 0-44 with random items from the pool, leaves the bottom control row alone. */
    public void randomizeItems(Inventory inv) {
        for (int slot = 0; slot < 45; slot++) {
            Material mat = ITEM_POOL.get(random.nextInt(ITEM_POOL.size()));
            inv.setItem(slot, new ItemStack(mat));
        }
        // bottom row filler around the control buttons
        ItemStack filler = namedItem(Material.GRAY_STAINED_GLASS_PANE, NamedTextColor.GRAY, " ", null);
        for (int slot = 45; slot < 54; slot++) {
            if (slot != 49 && slot != 53) {
                inv.setItem(slot, filler);
            }
        }
    }

    // ---------- COMMAND MENU ----------

    public void openCommandMenu(Player player) {
        MenuHolders.CommandMenuHolder holder = new MenuHolders.CommandMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, Component.text("Admin Commands").color(NamedTextColor.RED));
        holder.setInventory(inv);

        inv.setItem(10, namedItem(Material.GOLDEN_APPLE, NamedTextColor.GREEN, "Heal", null));
        inv.setItem(11, namedItem(Material.COOKED_BEEF, NamedTextColor.GOLD, "Feed", null));
        inv.setItem(12, toggleItem(Material.FEATHER, "Toggle Flight", player.getAllowFlight()));
        inv.setItem(13, toggleItem(Material.POTION, "Toggle God Mode", abilityManager.hasGodMode(player)));
        inv.setItem(14, toggleItem(Material.ENDER_EYE, "Toggle Vanish", abilityManager.isVanished(player)));
        inv.setItem(15, namedItem(Material.SUNFLOWER, NamedTextColor.YELLOW, "Clear Weather", null));
        inv.setItem(16, namedItem(Material.CLOCK, NamedTextColor.AQUA, "Set Day", null));
        inv.setItem(17, namedItem(Material.WOODEN_SWORD, NamedTextColor.DARK_RED, "Kill Nearby Mobs", null));

        inv.setItem(22, namedItem(Material.ARROW, NamedTextColor.GRAY, "Back", null));

        fillEmpty(inv);
        player.openInventory(inv);
    }

    // ---------- ABILITIES MENU ----------

    public void openAbilitiesMenu(Player player) {
        MenuHolders.AbilitiesMenuHolder holder = new MenuHolders.AbilitiesMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 9, Component.text("Abilities").color(NamedTextColor.LIGHT_PURPLE));
        holder.setInventory(inv);

        inv.setItem(2, toggleItem(Material.EMERALD, "Free Villager Trades", abilityManager.hasFreeTrades(player)));
        inv.setItem(3, toggleItem(Material.PUFFERFISH_BUCKET, "Infinite Breathing", abilityManager.hasInfiniteBreathing(player)));
        inv.setItem(8, namedItem(Material.ARROW, NamedTextColor.GRAY, "Back", null));

        fillEmpty(inv);
        player.openInventory(inv);
    }

    // ---------- KICK MENU ----------

    public void openKickMenu(Player viewer) {
        List<Player> targets = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(viewer)) {
                targets.add(online);
            }
        }

        int headRows = (targets.size() + 8) / 9; // rows needed just for player heads
        int size = Math.max(9, Math.min(54, (headRows + 1) * 9)); // +1 row reserved for controls
        MenuHolders.KickMenuHolder holder = new MenuHolders.KickMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, size, Component.text("Player Menu").color(NamedTextColor.DARK_RED));
        holder.setInventory(inv);

        int maxHeadSlots = size - 9;
        int slot = 0;
        for (Player target : targets) {
            if (slot >= maxHeadSlots) break; // more than 45 other players online, ran out of room
            inv.setItem(slot++, playerHeadItem(target));
        }

        int backSlot = size - 1;
        inv.setItem(backSlot, namedItem(Material.ARROW, NamedTextColor.GRAY, "Back", null));
        fillEmpty(inv);

        viewer.openInventory(inv);
    }

    public void openKickReasonMenu(Player viewer, UUID targetId) {
        MenuHolders.KickReasonMenuHolder holder = new MenuHolders.KickReasonMenuHolder(targetId);
        Player target = Bukkit.getPlayer(targetId);
        String targetName = target != null ? target.getName() : "Unknown Player";

        Inventory inv = Bukkit.createInventory(holder, 9, Component.text("Kick: " + targetName).color(NamedTextColor.DARK_RED));
        holder.setInventory(inv);

        inv.setItem(2, namedItem(Material.ELYTRA, NamedTextColor.RED, "Flying", List.of("Kick for Flying")));
        inv.setItem(4, namedItem(Material.REDSTONE, NamedTextColor.RED, "Packet IO", List.of("Kick for Packet IO issues")));
        inv.setItem(6, namedItem(Material.TNT, NamedTextColor.RED, "Internal Exception", List.of("Kick for an internal exception")));
        inv.setItem(8, namedItem(Material.ARROW, NamedTextColor.GRAY, "Back", null));

        fillEmpty(inv);
        viewer.openInventory(inv);
    }

    private ItemStack playerHeadItem(Player target) {
        boolean frozen = abilityManager.isFrozen(target);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        NamedTextColor nameColor = frozen ? NamedTextColor.AQUA : NamedTextColor.WHITE;
        String suffix = frozen ? " (Frozen)" : "";
        meta.displayName(Component.text(target.getName() + suffix).color(nameColor).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Left-click: kick").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Right-click: kick with a reason").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Shift-click: " + (frozen ? "unfreeze" : "freeze")).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(targetUuidKey, PersistentDataType.STRING, target.getUniqueId().toString());
        head.setItemMeta(meta);
        return head;
    }

    public UUID readTargetUuid(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String raw = meta.getPersistentDataContainer().get(targetUuidKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // ---------- helpers ----------

    private ItemStack namedItem(Material material, NamedTextColor color, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(color).decoration(TextDecoration.ITALIC, false));
        if (lore != null) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(Component.text(line).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(loreComponents);
        }
        item.setItemMeta(meta);
        return item;
    }

    /** Builds a toggle button whose lore reflects current ON/OFF state. */
    public ItemStack toggleItem(Material material, String name, boolean enabled) {
        NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
        String state = enabled ? "ON" : "OFF";
        return namedItem(material, color, name, List.of("Status: " + state, "Click to toggle"));
    }

    private void fillEmpty(Inventory inv) {
        ItemStack filler = namedItem(Material.GRAY_STAINED_GLASS_PANE, NamedTextColor.GRAY, " ", null);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }
}
