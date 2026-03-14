package com.pyzatech.pyzanode.gameselector.placeholder;

import com.pyzatech.pyzanode.gameselector.PyzaNodeGameSelectorPlugin;
import com.pyzatech.pyzanode.gameselector.routing.PyzaNodeRouter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * Minimal PlaceholderAPI expansion to expose some PyzaNode-related placeholders.
 *
 * This class is only loaded if PlaceholderAPI is installed.
 */
public final class PyzaNodeExpansion extends PlaceholderExpansion {

    private final PyzaNodeGameSelectorPlugin plugin;
    @SuppressWarnings("unused")
    private final PyzaNodeRouter router;

    public PyzaNodeExpansion(PyzaNodeGameSelectorPlugin plugin, PyzaNodeRouter router) {
        this.plugin = plugin;
        this.router = router;
    }

    @Override
    public String getIdentifier() {
        return "pyzanode";
    }

    @Override
    public String getAuthor() {
        return "PyzaNode";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        // Simple demo: %pyzanode_api_url% returns controller base URL.
        if (params.equalsIgnoreCase("api_url")) {
            return plugin.getConfig().getString("pyzanode.controller-url", "http://127.0.0.1:9451");
        }
        // Owners can add more cases here (e.g. group stats, cached counts) as needed.
        return null;
    }
}

