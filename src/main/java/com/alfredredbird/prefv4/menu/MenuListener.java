package com.alfredredbird.prefv4.menu;

import com.alfredredbird.prefv4.abilities.AbilityManager;
import com.alfredredbird.prefv4.logging.EventLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class MenuListener implements Listener {

    private final JavaPlugin plugin;
    private final MenuManager menuManager;
    private final AbilityManager abilityManager;
    private final EventLogger eventLogger;

    public MenuListener(JavaPlugin plugin, MenuManager menuManager, AbilityManager abilityManager, EventLogger eventLogger) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.abilityManager = abilityManager;
        this.eventLogger = eventLogger;
    }

    /** Sensitive actions can be gated behind their own node so an op can delegate just this action to trusted staff. */
    private boolean checkPermission(Player player, String node) {
        if (player.hasPermission(node)) return true;
        player.sendMessage(Component.text("You don't have permission for that.").color(NamedTextColor.RED));
        return false;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        Object holder = topInventory.getHolder();

        if (!(holder instanceof MenuHolders.MainMenuHolder)
                && !(holder instanceof MenuHolders.ItemMenuHolder)
                && !(holder instanceof MenuHolders.CommandMenuHolder)
                && !(holder instanceof MenuHolders.AbilitiesMenuHolder)
                && !(holder instanceof MenuHolders.KickMenuHolder)
                && !(holder instanceof MenuHolders.KickReasonMenuHolder)) {
            return; // not one of our menus, ignore
        }

        // Always cancel clicks inside any of our menus so nothing gets taken/moved.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(topInventory)) return; // ignore clicks in the player's own inventory

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (holder instanceof MenuHolders.MainMenuHolder) {
            handleMainMenu(player, clicked);
        } else if (holder instanceof MenuHolders.ItemMenuHolder) {
            handleItemMenu(player, clicked, topInventory);
        } else if (holder instanceof MenuHolders.CommandMenuHolder) {
            handleCommandMenu(player, clicked);
        } else if (holder instanceof MenuHolders.AbilitiesMenuHolder) {
            handleAbilitiesMenu(player, clicked);
        } else if (holder instanceof MenuHolders.KickMenuHolder) {
            handleKickMenu(player, clicked, event.getClick());
        } else if (holder instanceof MenuHolders.KickReasonMenuHolder kickReasonHolder) {
            handleKickReasonMenu(player, clicked, kickReasonHolder);
        }
    }

    // ---------- MAIN MENU ----------

    private void handleMainMenu(Player player, ItemStack clicked) {
        switch (clicked.getType()) {
            case DIAMOND_BLOCK -> {
                player.setGameMode(GameMode.CREATIVE);
                player.closeInventory();
                eventLogger.logAdminAction(player, "set their game mode to Creative");
            }
            case GRASS_BLOCK -> {
                player.setGameMode(GameMode.SURVIVAL);
                player.closeInventory();
                eventLogger.logAdminAction(player, "set their game mode to Survival");
            }
            case CHEST -> menuManager.openItemMenu(player);
            case COMMAND_BLOCK -> menuManager.openCommandMenu(player);
            case DIAMOND_SWORD -> menuManager.openAbilitiesMenu(player);
            case PLAYER_HEAD -> menuManager.openKickMenu(player);
            default -> { /* filler glass, ignore */ }
        }
    }

    // ---------- ITEM MENU ----------

    private void handleItemMenu(Player player, ItemStack clicked, Inventory inv) {
        String name = displayName(clicked);
        if (clicked.getType() == Material.NETHER_STAR && "Shuffle".equals(name)) {
            menuManager.randomizeItems(inv);
            return;
        }
        if (clicked.getType() == Material.ARROW && "Back".equals(name)) {
            menuManager.openMainMenu(player);
            return;
        }
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Otherwise it's one of the random items: give a copy to the player.
        player.getInventory().addItem(clicked.clone());
    }

    // ---------- COMMAND MENU ----------

    private void handleCommandMenu(Player player, ItemStack clicked) {
        String name = displayName(clicked);
        if (name == null) return;

        switch (name) {
            case "Heal" -> {
                var maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealthAttr != null) {
                    player.setHealth(maxHealthAttr.getValue());
                }
                player.setFireTicks(0);
                eventLogger.logAdminAction(player, "healed themselves");
            }
            case "Feed" -> {
                player.setFoodLevel(20);
                player.setSaturation(20f);
                eventLogger.logAdminAction(player, "fed themselves");
            }
            case "Toggle Flight" -> {
                boolean nowFlying = !player.getAllowFlight();
                player.setAllowFlight(nowFlying);
                player.setFlying(nowFlying);
                menuManager.openCommandMenu(player); // refresh lore
                eventLogger.logAdminAction(player, "turned flight " + (nowFlying ? "on" : "off"));
            }
            case "Toggle God Mode" -> {
                if (!checkPermission(player, "prefv4.command.godmode")) return;
                boolean now = abilityManager.toggleGodMode(player);
                menuManager.openCommandMenu(player);
                eventLogger.logAdminAction(player, "turned god mode " + (now ? "on" : "off"));
            }
            case "Toggle Vanish" -> {
                if (!checkPermission(player, "prefv4.command.vanish")) return;
                boolean now = abilityManager.toggleVanish(player);
                menuManager.openCommandMenu(player);
                eventLogger.logAdminAction(player, "turned vanish " + (now ? "on" : "off"));
            }
            case "Clear Weather" -> {
                player.getWorld().setStorm(false);
                player.getWorld().setThundering(false);
                eventLogger.logAdminAction(player, "cleared the weather in " + player.getWorld().getName());
            }
            case "Set Day" -> {
                player.getWorld().setTime(1000L);
                eventLogger.logAdminAction(player, "set time to day in " + player.getWorld().getName());
            }
            case "Kill Nearby Mobs" -> {
                int killed = 0;
                for (Entity entity : player.getNearbyEntities(32, 32, 32)) {
                    if (entity instanceof Monster monster) {
                        monster.setHealth(0);
                        killed++;
                    }
                }
                eventLogger.logAdminAction(player, "killed " + killed + " nearby mob(s)");
            }
            case "Back" -> menuManager.openMainMenu(player);
            default -> { /* filler glass, ignore */ }
        }
    }

    // ---------- ABILITIES MENU ----------

    private void handleAbilitiesMenu(Player player, ItemStack clicked) {
        String name = displayName(clicked);
        if (name == null) return;

        switch (name) {
            case "Free Villager Trades" -> {
                boolean now = abilityManager.toggleFreeTrades(player);
                menuManager.openAbilitiesMenu(player);
                eventLogger.logAdminAction(player, "turned free villager trades " + (now ? "on" : "off"));
            }
            case "Infinite Breathing" -> {
                boolean now = abilityManager.toggleInfiniteBreathing(player);
                menuManager.openAbilitiesMenu(player);
                eventLogger.logAdminAction(player, "turned infinite breathing " + (now ? "on" : "off"));
            }
            case "Back" -> menuManager.openMainMenu(player);
            default -> { /* filler glass, ignore */ }
        }
    }

    // ---------- KICK MENU ----------

    private void handleKickMenu(Player admin, ItemStack clicked, ClickType click) {
        String name = displayName(clicked);

        if (clicked.getType() == Material.ARROW && "Back".equals(name)) {
            menuManager.openMainMenu(admin);
            return;
        }
        if (clicked.getType() != Material.PLAYER_HEAD) return; // filler glass, ignore

        UUID targetId = menuManager.readTargetUuid(clicked);
        if (targetId == null) return;
        Player target = plugin.getServer().getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            admin.sendMessage(Component.text("That player is no longer online."));
            menuManager.openKickMenu(admin);
            return;
        }

        if (click.isShiftClick()) {
            if (!checkPermission(admin, "prefv4.command.freeze")) return;
            boolean now = abilityManager.toggleFreeze(target);
            target.sendMessage(Component.text(now ? "You have been frozen by staff." : "You have been unfrozen.")
                    .color(now ? NamedTextColor.AQUA : NamedTextColor.GREEN));
            menuManager.openKickMenu(admin); // refresh so the head shows the new state
            eventLogger.logAdminAction(admin, (now ? "froze " : "unfroze ") + target.getName());
            return;
        }

        if (!checkPermission(admin, "prefv4.command.kick")) return;

        if (click == ClickType.RIGHT) {
            menuManager.openKickReasonMenu(admin, targetId);
        } else {
            admin.closeInventory();
            String targetName = target.getName();
            target.kick(Component.text("Kicked by an admin."));
            eventLogger.logAdminAction(admin, "kicked " + targetName);
        }
    }

    private void handleKickReasonMenu(Player admin, ItemStack clicked, MenuHolders.KickReasonMenuHolder holder) {
        String name = displayName(clicked);
        if (name == null) return;

        if ("Back".equals(name)) {
            menuManager.openKickMenu(admin);
            return;
        }

        String kickMessage = switch (name) {
            case "Flying" -> "Flying is not enabled on this server";
            case "Packet IO" -> "Internal Exception: io.netty.handler.timeout.ReadTimeoutException";
            case "Internal Exception" -> "Internal Exception: java.io.IOException: An existing connection was forcibly closed by the remote host";
            default -> null;
        };
        if (kickMessage == null) return; // filler glass, ignore
        if (!checkPermission(admin, "prefv4.command.kick")) return;

        Player target = plugin.getServer().getPlayer(holder.getTargetId());
        admin.closeInventory();
        if (target != null && target.isOnline()) {
            String targetName = target.getName();
            target.kick(Component.text(kickMessage));
            eventLogger.logAdminAction(admin, "kicked " + targetName + " (reason: " + name + ")");
        } else {
            admin.sendMessage(Component.text("That player is no longer online."));
        }
    }

    private String displayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return null;
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(meta.displayName());
    }
}
