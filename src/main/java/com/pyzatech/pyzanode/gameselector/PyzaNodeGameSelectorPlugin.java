package com.pyzatech.pyzanode.gameselector;

import com.pyzatech.pyzanode.gameselector.menu.GameSelectorMenu;
import com.pyzatech.pyzanode.gameselector.menu.MenuEntry;
import com.pyzatech.pyzanode.gameselector.placeholder.PyzaNodeExpansion;
import com.pyzatech.pyzanode.gameselector.routing.PyzaNodeRouter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Simple example plugin that wires a configurable GUI to the PyzaNode controller.
 *
 * The idea is:
 * 1) Owners describe their games / groups in config.yml.
 * 2) This plugin builds an inventory menu from that config.
 * 3) When a player clicks an entry, we ask PyzaNode for a suitable server and
 *    forward the player there via BungeeCord.
 *
 * The code is intentionally small and straightforward so it is easy to copy‑paste
 * into your own projects.
 */
public final class PyzaNodeGameSelectorPlugin extends JavaPlugin {

    private PyzaNodeRouter router;
    private GameSelectorMenu menu;
    /** All menu entries keyed by config key (typically "lobby", "survival", etc.). */
    private final Map<String, MenuEntry> entries = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadAll();

        // Register outgoing plugin channel for BungeeCord routing.
        Bukkit.getMessenger().registerOutgoingPluginChannel(
                this,
                getConfig().getString("bungee.channel", "BungeeCord")
        );

        // Optional PlaceholderAPI expansion for %pyzanode_*% placeholders.
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PyzaNodeExpansion(this, router).register();
            getLogger().info("Registered PlaceholderAPI expansion.");
        }
    }

    @Override
    public void onDisable() {
        entries.clear();
        if (router != null) {
            router.shutdown();
            router = null;
        }
    }

    /**
     * Reload configuration, rebuild menu entries and router.
     * Called on enable and via /gameselector reload.
     */
    private void reloadAll() {
        reloadConfig();

        router = new PyzaNodeRouter(this);
        entries.clear();

        ConfigurationSection section = getConfig().getConfigurationSection("menu.entries");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    MenuEntry entry = MenuEntry.fromConfig(key, section.getConfigurationSection(key));
                    entries.put(key.toLowerCase(), entry);
                } catch (Exception ex) {
                    getLogger().log(Level.WARNING,
                            "Failed to load menu entry \"" + key + "\": " + ex.getMessage(), ex);
                }
            }
        }

        menu = new GameSelectorMenu(this, entries);
        getLogger().info("Loaded " + entries.size() + " menu entries.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("gameselector")) {
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("pyzanode.gameselector.admin")) {
                sendMessage(player, "messages.no-permission");
                return true;
            }
            reloadAll();
            sendRaw(player, getConfig().getString("messages.prefix", "") + "Config reloaded.");
            return true;
        }

        if (!player.hasPermission("pyzanode.gameselector.open")) {
            sendMessage(player, "messages.no-permission");
            return true;
        }

        sendMessage(player, "messages.open-menu");
        menu.openFor(player);
        return true;
    }

    /**
     * Convenience wrapper that pulls a formatted message from config and applies the prefix.
     */
    public void sendMessage(Player player, String path) {
        String msg = getConfig().getString(path);
        if (msg == null || msg.isEmpty()) return;
        sendRaw(player, getConfig().getString("messages.prefix", "") + msg);
    }

    /**
     * Send a raw, colour‑coded message to a player.
     */
    public void sendRaw(Player player, String msg) {
        if (msg == null || msg.isEmpty()) return;
        player.sendMessage(msg.replace('&', '§'));
    }

    public PyzaNodeRouter getRouter() {
        return router;
    }
}


