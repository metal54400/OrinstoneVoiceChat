package fr.wixymc.orinstonevoicechat.placeholders;

import fr.wixymc.orinstonevoicechat.OrinStoneVoiceChat;
import fr.wixymc.orinstonevoicechat.managers.MuteEntry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Placeholders disponibles:
 *
 * %orinstonevoicechat_has_voicechat%   -> true / false
 * %orinstonevoicechat_is_muted%        -> true / false
 * %orinstonevoicechat_mute_reason%     -> raison ou ""
 * %orinstonevoicechat_mute_duration%   -> duree ou ""
 * %orinstonevoicechat_mute_expires%    -> timestamp ou "permanent"
 * %orinstonevoicechat_mute_remaining%  -> secondes restantes ou "0"
 * %orinstonevoicechat_status%          -> MUTED / ACTIVE / NO_VOICECHAT
 */
public class VoiceChatPlaceholders extends PlaceholderExpansion {

    private final OrinStoneVoiceChat plugin;

    public VoiceChatPlaceholders(OrinStoneVoiceChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "orinstonevoicechat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "WixyMC";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        UUID uuid = player.getUniqueId();

        return switch (params.toLowerCase()) {
            case "has_voicechat" -> {
                if (plugin.getVoiceChatPlugin() == null) yield "false";
                yield plugin.getVoiceChatPlugin().isConnected(uuid) ? "true" : "false";
            }
            case "voicechat_icon" -> {
                if (plugin.getMuteManager().isMuted(uuid)) yield "\u00A7c\uD83C\uDF99";
                if (plugin.getVoiceChatPlugin() != null
                        && plugin.getVoiceChatPlugin().isConnected(uuid)) yield "\u00A7a\uD83C\uDF99";
                yield "\u00A77\uD83C\uDF99";
            }
            case "is_muted" -> plugin.getMuteManager().isMuted(uuid) ? "true" : "false";
            case "mute_reason" -> {
                MuteEntry entry = plugin.getMuteManager().getMuteEntry(uuid);
                yield entry != null ? entry.getReason() : "";
            }
            case "mute_duration" -> {
                MuteEntry entry = plugin.getMuteManager().getMuteEntry(uuid);
                yield entry != null ? entry.getDuration() : "";
            }
            case "mute_expires" -> {
                MuteEntry entry = plugin.getMuteManager().getMuteEntry(uuid);
                if (entry == null) yield "";
                yield entry.isPermanent() ? "permanent" : String.valueOf(entry.getExpiresAt());
            }
            case "mute_remaining" -> {
                MuteEntry entry = plugin.getMuteManager().getMuteEntry(uuid);
                if (entry == null) yield "0";
                yield entry.isPermanent() ? "-1" : String.valueOf(entry.getRemainingSeconds());
            }
            case "status" -> {
                if (plugin.getMuteManager().isMuted(uuid)) yield "MUTED";
                if (plugin.getVoiceChatPlugin() != null
                        && plugin.getVoiceChatPlugin().isConnected(uuid)) yield "ACTIVE";
                yield "NO_VOICECHAT";
            }
            default -> null;
        };
    }
}