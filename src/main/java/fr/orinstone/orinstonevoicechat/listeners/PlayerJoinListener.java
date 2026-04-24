package fr.orinstone.orinstonevoicechat.listeners;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import fr.orinstone.orinstonevoicechat.OrinStoneVoiceChat;
import fr.orinstone.orinstonevoicechat.managers.MuteEntry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final OrinStoneVoiceChat plugin;

    public PlayerJoinListener(OrinStoneVoiceChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Vérifier si le mute est expiré
        if (plugin.getMuteManager().isMuted(uuid)) {
            MuteEntry entry = plugin.getMuteManager().getMuteEntry(uuid);
            if (entry != null && entry.isExpired()) {
                plugin.getMuteManager().unmute(uuid);
            }
        }

        // Délai pour attendre la connexion du client voicechat
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            checkVoiceChat(player);
        }, 40L); // 2 secondes
    }

    private void checkVoiceChat(Player player) {
        boolean hasVoiceChat = false;

        if (plugin.getVoiceChatPlugin() != null) {
            VoicechatServerApi api = plugin.getVoiceChatPlugin().getServerApi();
            if (api != null) {
                hasVoiceChat = plugin.getVoiceChatPlugin().isConnected(player.getUniqueId());
            }
        }

        // Notifier les admins
        String msgKey = hasVoiceChat ? "join-has-voicechat" : "join-no-voicechat";
        Component msg = plugin.getConfigManager().getMessageComponent(msgKey,
                "{player}", player.getName());

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("orinstonevoicechat.admin")) {
                admin.sendMessage(msg);
            }
        }
    }
}
