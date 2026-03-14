package com.pyzatech.pyzanode.gameselector.menu;

import com.pyzatech.pyzanode.gameselector.PyzaNodeGameSelectorPlugin;
import com.pyzatech.pyzanode.gameselector.routing.PyzaNodeRouter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Small helper around a Bukkit inventory that renders the configured menu entries
 * and handles click events.
 */
public final class GameSelectorMenu implements Listener {

    private final PyzaNodeGameSelectorPlugin plugin;
    private final Map<Integer, MenuEntry> bySlot = new HashMap<>();

    private final String title;
    private final int rows;
    private final boolean useFiller;
    private final Material fillerMaterial;
    private final String fillerName;
    private final List<String> fillerLore;

    public GameSelectorMenu(PyzaNodeGameSelectorPlugin plugin, Map<String, MenuEntry> entries) {
        this.plugin = plugin;
        title = color(plugin.getConfig().getString("menu.title", "&aSelect a game"));
        rows = Math.max(1, Math.min(6, plugin.getConfig().getInt("menu.rows", 3)));
        useFiller = plugin.getConfig().getBoolean("menu.filler.enabled", true);
        fillerMaterial = Material.matchMaterial(
                plugin.getConfig().getString("menu.filler.material", "GRAY_STAINED_GLASS_PANE"));
        fillerName = color(plugin.getConfig().getString("menu.filler.name", "&7"));
        fillerLore = plugin.getConfig().getStringList("menu.filler.lore");

        bySlot.clear();
        for (MenuEntry e : entries.values()) {
            if (e.getSlot() >= 0 && e.getSlot() < rows * 9) {
                bySlot.put(e.getSlot(), e);
            }
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Open the selector inventory for a player.
     */
    public void openFor(Player player) {
        Inventory inv = Bukkit.createInventory(player, rows * 9, title);

        if (useFiller && fillerMaterial != null) {
            ItemStack filler = new ItemStack(fillerMaterial);
            ItemMeta meta = filler.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(fillerName);
                if (fillerLore != null && !fillerLore.isEmpty()) {
                    meta.setLore(color(fillerLore));
                }
                meta.addItemFlags(ItemFlag.values());
                filler.setItemMeta(meta);
            }
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, filler);
            }
        }

        for (Map.Entry<Integer, MenuEntry> en : bySlot.entrySet()) {
            MenuEntry entry = en.getValue();
            ItemStack item = new ItemStack(entry.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color(entry.getDisplayName()));
                if (entry.getLore() != null && !entry.getLore().isEmpty()) {
                    meta.setLore(color(entry.getLore()));
                }
                meta.addItemFlags(ItemFlag.values());
                item.setItemMeta(meta);
            }
            inv.setItem(en.getKey(), item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getView().getTitle() == null || !event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        MenuEntry entry = bySlot.get(slot);
        if (entry == null) return;

        Player player = (Player) event.getWhoClicked();
        if (entry.getPermission() != null && !entry.getPermission().isEmpty()
            && !player.hasPermission(entry.getPermission())) {
            plugin.sendMessage(player, "messages.no-permission");
            return;
        }

        // Play click sound (client-side only).
        Sound s = entry.getClickSound();
        if (s != null) {
            player.playSound(player.getLocation(), s, 1.0f, 1.0f);
        }

        // Route through PyzaNode.
        PyzaNodeRouter router = plugin.getRouter();
        if (router == null) {
            plugin.sendMessage(player, "messages.controller-error");
            return;
        }
        router.handleSelection(player, entry);
    }

    private static String color(String input) {
        if (input == null) return null;
        return input.replace('&', '§');
    }

    private static List<String> color(List<String> input) {
        return input.stream().map(GameSelectorMenu::color).toList();
    }
}


