package fr.wixymc.orinstonevoicechat;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VoiceChatPlugin implements VoicechatPlugin {

    private static final String PLUGIN_ID = "orinstonevoicechat";

    private final OrinStoneVoiceChat plugin;
    private VoicechatServerApi serverApi;
    private final Set<UUID> connectedClients = new HashSet<>();

    public VoiceChatPlugin(OrinStoneVoiceChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        // Initialization
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(PlayerConnectedEvent.class, this::onPlayerConnected);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    public void serverStarted(VoicechatServerApi api) {
        this.serverApi = api;
    }

    private void onPlayerConnected(PlayerConnectedEvent event) {
        UUID uuid = event.getConnection().getPlayer().getUuid();
        connectedClients.add(uuid);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                updatePlayerHelmet(player);
            }
        });
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        UUID uuid = event.getPlayerUuid();
        connectedClients.remove(uuid);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                updatePlayerHelmet(player);
            }
        });
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        UUID uuid = event.getSenderConnection().getPlayer().getUuid();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        // Si le joueur est mute, annuler le packet
        if (plugin.getMuteManager().isMuted(uuid)) {
            event.cancel();
        }
    }

    public void updatePlayerHelmet(Player player) {
        // Cette méthode est appelée depuis PlayerJoinListener aussi
        // Elle met à jour visuellement l'état du casque du joueur
        // (implémenté via le score / bossbar ou autre affichage côté serveur)
    }

    public boolean isConnected(UUID uuid) {
        return connectedClients.contains(uuid);
    }

    public VoicechatServerApi getServerApi() {
        return serverApi;
    }
}
