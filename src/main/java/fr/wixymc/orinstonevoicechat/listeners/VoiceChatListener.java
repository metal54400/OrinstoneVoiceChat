package fr.wixymc.orinstonevoicechat.listeners;

import fr.wixymc.orinstonevoicechat.OrinStoneVoiceChat;
import org.bukkit.event.Listener;

/**
 * Listener voicechat.
 * L'actionbar a été supprimée — l'état du micro est disponible
 * uniquement via les placeholders PlaceholderAPI.
 */
public class VoiceChatListener implements Listener {

    private final OrinStoneVoiceChat plugin;

    public VoiceChatListener(OrinStoneVoiceChat plugin) {
        this.plugin = plugin;
    }
}
