package fr.wixymc.orinstonevoicechat;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import fr.wixymc.orinstonevoicechat.commands.AdminCommand;
import fr.wixymc.orinstonevoicechat.listeners.PlayerJoinListener;
import fr.wixymc.orinstonevoicechat.listeners.VoiceChatListener;
import fr.wixymc.orinstonevoicechat.managers.ConfigManager;
import fr.wixymc.orinstonevoicechat.managers.MuteManager;
import fr.wixymc.orinstonevoicechat.placeholders.VoiceChatPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class OrinStoneVoiceChat extends JavaPlugin {

    private static OrinStoneVoiceChat instance;
    private ConfigManager configManager;
    private MuteManager muteManager;
    private VoiceChatPlugin voiceChatPlugin;

    @Override
    public void onEnable() {
        instance = this;

        // Config
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.muteManager = new MuteManager(this);

        // Voice Chat API
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            this.voiceChatPlugin = new VoiceChatPlugin(this);
            service.registerPlugin(voiceChatPlugin);
            getLogger().info("Simple Voice Chat API chargée avec succès.");
        } else {
            getLogger().warning("Impossible de trouver l'API Simple Voice Chat !");
        }

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new VoiceChatListener(this), this);

        // Commands
        AdminCommand adminCommand = new AdminCommand(this);
        getCommand("originstonevcadmin").setExecutor(adminCommand);
        getCommand("originstonevcadmin").setTabCompleter(adminCommand);

        // PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new VoiceChatPlaceholders(this).register();
            getLogger().info("PlaceholderAPI détecté, placeholders enregistrés.");
        }

        getLogger().info("OrinStoneVoiceChat activé avec succès !");
    }

    @Override
    public void onDisable() {
        if (muteManager != null) {
            muteManager.saveMutes();
        }
        getLogger().info("OrinStoneVoiceChat désactivé.");
    }

    public static OrinStoneVoiceChat getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public VoiceChatPlugin getVoiceChatPlugin() {
        return voiceChatPlugin;
    }
}
