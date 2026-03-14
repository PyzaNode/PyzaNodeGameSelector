package com.pyzatech.pyzanode.gameselector.routing;

import com.pyzatech.pyzanode.gameselector.PyzaNodeGameSelectorPlugin;
import com.pyzatech.pyzanode.gameselector.menu.MenuEntry;
import com.pyzatech.pyzanode.client.PyzaNodeClient;
import com.pyzatech.pyzanode.client.PyzaNodeClient.ServerConnectionInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Thin wrapper around the PyzaNode Java client and BungeeCord messaging.
 *
 * Responsibilities:
 * 1) Read controller URL / token / timeout from config.
 * 2) Use {@link PyzaNodeClient#getOrCreateServerForGroup} to pick or create a server.
 * 3) Forward the player to that server using the configured Bungee channel.
 */
public final class PyzaNodeRouter {

    private final PyzaNodeGameSelectorPlugin plugin;
    private final PyzaNodeClient client;
    private final ExecutorService executor;

    public PyzaNodeRouter(PyzaNodeGameSelectorPlugin plugin) {
        this.plugin = plugin;
        String baseUrl = plugin.getConfig().getString("pyzanode.controller-url", "http://127.0.0.1:9451");
        String token = plugin.getConfig().getString("pyzanode.api-token", "");
        int timeoutSec = plugin.getConfig().getInt("pyzanode.create-timeout-seconds", 90);
        client = new PyzaNodeClient(baseUrl, token, timeoutSec);
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "PyzaNodeGameSelector-API"));
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    /**
     * Handle a click on a menu entry:
     * 1) decide which group / preset to use,
     * 2) call the PyzaNode API on a background thread,
     * 3) once a target server is known, send the player over Bungee.
     */
    public void handleSelection(Player player, MenuEntry entry) {
        String group = entry.getGroup();
        String presetId = entry.getPresetIdOrNull();
        if (presetId == null || presetId.isEmpty()) {
            presetId = plugin.getConfig().getString("pyzanode.default-preset-id", "");
        }
        if (presetId == null || presetId.isEmpty()) {
            plugin.sendRaw(player, plugin.getConfig().getString("messages.prefix", "")
                + "§cPreset ID not configured for group \"" + group + "\".");
            return;
        }

        final String presetIdFinal = presetId;
        plugin.sendMessage(player, "messages.creating-server");

        executor.submit(() -> {
          try {
              ServerConnectionInfo info = client.getOrCreateServerForGroup(group, presetIdFinal);
              if (info == null || info.getHost() == null || info.getPort() <= 0) {
                  plugin.sendMessage(player, "messages.no-server-available");
                  return;
              }

              String serverName = info.getServerName();
              String msg = plugin.getConfig().getString("messages.sending-player", "");
              if (msg != null && !msg.isEmpty()) {
                  msg = msg.replace("%server_name%", serverName);
                  plugin.sendRaw(player, plugin.getConfig().getString("messages.prefix", "") + msg);
              }

              // Actually send the player via BungeeCord plugin messaging channel.
              connectPlayerViaBungee(player, serverName);
          } catch (Exception ex) {
              plugin.getLogger().log(Level.WARNING, "Failed to route player via PyzaNode", ex);
              plugin.sendMessage(player, "messages.controller-error");
          }
        });
    }

    /**
     * Use the classic BungeeCord plugin messaging channel to move a player.
     * Channel / subchannel are configurable so it can integrate with helper plugins.
     */
    private void connectPlayerViaBungee(Player player, String serverName) {
        String channel = plugin.getConfig().getString("bungee.channel", "BungeeCord");
        String sub = plugin.getConfig().getString("bungee.connect-subchannel", "Connect");

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (DataOutputStream out = new DataOutputStream(baos)) {
                    out.writeUTF(sub);
                    out.writeUTF(serverName);
                }
                player.sendPluginMessage(plugin, channel, baos.toByteArray());
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to send BungeeCord connect message for server " + serverName, ex);
                plugin.sendMessage(player, "messages.controller-error");
            }
        });
    }
}


