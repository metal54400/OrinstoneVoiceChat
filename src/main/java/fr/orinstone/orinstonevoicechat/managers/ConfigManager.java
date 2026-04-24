package fr.orinstone.orinstonevoicechat.managers;

import fr.wixymc.orinstonevoicechat.OrinStoneVoiceChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final OrinStoneVoiceChat plugin;
    private FileConfiguration config;

    public ConfigManager(OrinStoneVoiceChat plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getPrefix() {
        return config.getString("prefix", "&8[&6OrinStone&eVC&8] ");
    }

    public String getRawMessage(String key) {
        return config.getString("messages." + key, "&cMessage introuvable: " + key);
    }

    /**
     * Récupère un message avec les placeholders remplacés.
     * Placeholders supportés: {player}, {duration}, {reason}
     */
    public String getMessage(String key, String... replacements) {
        String msg = getPrefix() + getRawMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    /**
     * Retourne le message en Component (Adventure)
     */
    public Component getMessageComponent(String key, String... replacements) {
        return LegacyComponentSerializer.legacyAmpersand()
                .deserialize(getMessage(key, replacements));
    }

    /**
     * Message sans prefix
     */
    public String getMessageNoPrefix(String key, String... replacements) {
        String msg = getRawMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    public Component getMessageNoPrefixComponent(String key, String... replacements) {
        return LegacyComponentSerializer.legacyAmpersand()
                .deserialize(getMessageNoPrefix(key, replacements));
    }
}
