package com.alfredredbird.prefv4;

import com.alfredredbird.prefv4.abilities.AbilityManager;
import com.alfredredbird.prefv4.abilities.FreezeListener;
import com.alfredredbird.prefv4.abilities.VanishJoinListener;
import com.alfredredbird.prefv4.abilities.VillagerTradeListener;
import com.alfredredbird.prefv4.logging.ActivityListener;
import com.alfredredbird.prefv4.logging.ConsoleCommandFilter;
import com.alfredredbird.prefv4.logging.EventLogger;
import com.alfredredbird.prefv4.menu.MenuListener;
import com.alfredredbird.prefv4.menu.MenuManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Prefv4 extends JavaPlugin {

    private static Prefv4 instance;

    private MenuManager menuManager;
    private AbilityManager abilityManager;
    private EventLogger eventLogger;
    private ConsoleCommandFilter consoleCommandFilter;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.abilityManager = new AbilityManager(this);
        this.menuManager = new MenuManager(this, abilityManager);
        this.eventLogger = new EventLogger(this);
        this.consoleCommandFilter = new ConsoleCommandFilter();
        this.consoleCommandFilter.register();
        applyConsoleHideSetting();

        getServer().getPluginManager().registerEvents(new MenuListener(this, menuManager, abilityManager, eventLogger), this);
        getServer().getPluginManager().registerEvents(new VillagerTradeListener(this, abilityManager), this);
        getServer().getPluginManager().registerEvents(new VanishJoinListener(this, abilityManager), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(abilityManager), this);
        getServer().getPluginManager().registerEvents(new ActivityListener(eventLogger), this);
    }

    @Override
    public void onDisable() {
        if (abilityManager != null) {
            abilityManager.shutdown();
        }
        if (consoleCommandFilter != null) {
            consoleCommandFilter.unregister();
        }
    }

    /** Reads config's hide-pref-from-console flag and applies it to the filter. Called on enable and on /pref reload. */
    private void applyConsoleHideSetting() {
        if (getConfig().getBoolean("hide-pref-from-console", true)) {
            consoleCommandFilter.hide("pref");
        } else {
            consoleCommandFilter.unhide("pref");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pref")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("prefv4.reload")) {
                    sender.sendMessage(Component.text("You don't have permission for that.").color(NamedTextColor.RED));
                    return true;
                }
                reloadConfig();
                applyConsoleHideSetting();
                sender.sendMessage(Component.text("Prefv4 config reloaded.").color(NamedTextColor.GREEN));
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            menuManager.openMainMenu(player);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("pref") && args.length == 1 && sender.hasPermission("prefv4.reload")) {
            return List.of("reload").stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }

    /**
     * Public entry point so other plugins/mods on the same server can open
     * the menu for a player, e.g.:
     *
     *   Prefv4.getInstance().openMenuFor(somePlayer);
     */
    public void openMenuFor(Player player) {
        menuManager.openMainMenu(player);
    }

    public static Prefv4 getInstance() {
        return instance;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public EventLogger getEventLogger() {
        return eventLogger;
    }
}
