package fr.orinstone.orinstonevoicechat.managers;

import fr.wixymc.orinstonevoicechat.OrinStoneVoiceChat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MuteManager {

    private final OrinStoneVoiceChat plugin;
    private final Map<UUID, MuteEntry> mutes = new ConcurrentHashMap<>();
    private File mutesFile;
    private FileConfiguration mutesConfig;

    // Pattern: nombre suivi d'unité (s, sec, m, min, h, d, w, mo, y, ans, etc.)
    private static final Pattern DURATION_PATTERN =
            Pattern.compile("(\\d+)(s|sec|secondes?|m|min|minutes?|h|heure?s?|heures?|d|j|jours?|w|sem|semaines?|mo|mois|y|an|ans|annees?)");

    public MuteManager(OrinStoneVoiceChat plugin) {
        this.plugin = plugin;
        loadMutes();
        startExpiryTask();
    }

    private void loadMutes() {
        mutesFile = new File(plugin.getDataFolder(), "mutes.yml");
        if (!mutesFile.exists()) {
            try { mutesFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        mutesConfig = YamlConfiguration.loadConfiguration(mutesFile);

        if (mutesConfig.contains("mutes")) {
            for (String key : mutesConfig.getConfigurationSection("mutes").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = mutesConfig.getString("mutes." + key + ".name", "Unknown");
                    String reason = mutesConfig.getString("mutes." + key + ".reason", "Aucune raison");
                    long mutedAt = mutesConfig.getLong("mutes." + key + ".mutedAt");
                    long expiresAt = mutesConfig.getLong("mutes." + key + ".expiresAt");
                    String duration = mutesConfig.getString("mutes." + key + ".duration", "?");

                    MuteEntry entry = new MuteEntry(uuid, name, reason, mutedAt, expiresAt, duration);
                    if (!entry.isExpired()) {
                        mutes.put(uuid, entry);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Erreur lors du chargement du mute: " + key);
                }
            }
        }
        plugin.getLogger().info("Chargé " + mutes.size() + " mute(s) depuis mutes.yml");
    }

    public void saveMutes() {
        mutesConfig.set("mutes", null);
        for (Map.Entry<UUID, MuteEntry> entry : mutes.entrySet()) {
            String key = "mutes." + entry.getKey().toString();
            MuteEntry me = entry.getValue();
            mutesConfig.set(key + ".name", me.getPlayerName());
            mutesConfig.set(key + ".reason", me.getReason());
            mutesConfig.set(key + ".mutedAt", me.getMutedAt());
            mutesConfig.set(key + ".expiresAt", me.getExpiresAt());
            mutesConfig.set(key + ".duration", me.getDuration());
        }
        try {
            mutesConfig.save(mutesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            List<UUID> toRemove = new ArrayList<>();
            for (Map.Entry<UUID, MuteEntry> entry : mutes.entrySet()) {
                if (entry.getValue().isExpired()) {
                    toRemove.add(entry.getKey());
                }
            }
            for (UUID uuid : toRemove) {
                MuteEntry me = mutes.remove(uuid);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage(plugin.getConfigManager()
                                .getMessageComponent("mute-expired", "{player}", me.getPlayerName()));
                        plugin.getVoiceChatPlugin().updatePlayerHelmet(p);
                    }
                });
            }
            if (!toRemove.isEmpty()) {
                saveMutes();
            }
        }, 20L * 30, 20L * 30); // toutes les 30 secondes
    }

    /**
     * Mute un joueur.
     * @param uuid UUID du joueur
     * @param name Nom du joueur
     * @param durationStr ex: "2h", "30m", "1d"
     * @param reason Raison
     * @return true si muté avec succès, false si déjà muté
     */
    public boolean mute(UUID uuid, String name, String durationStr, String reason) {
        if (isMuted(uuid)) return false;

        long durationSeconds = parseDuration(durationStr);
        long expiresAt = durationSeconds == -1 ? -1 : Instant.now().getEpochSecond() + durationSeconds;

        MuteEntry entry = new MuteEntry(uuid, name, reason, expiresAt, durationStr);
        mutes.put(uuid, entry);
        saveMutes();
        return true;
    }

    /**
     * Unmute un joueur.
     * @return true si unmuté, false si pas muté
     */
    public boolean unmute(UUID uuid) {
        if (!isMuted(uuid)) return false;
        mutes.remove(uuid);
        saveMutes();
        return true;
    }

    public boolean isMuted(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry == null) return false;
        if (entry.isExpired()) {
            mutes.remove(uuid);
            return false;
        }
        return true;
    }

    public MuteEntry getMuteEntry(UUID uuid) {
        return mutes.get(uuid);
    }

    /**
     * Parse une durée vers des secondes.
     * Supporte: 10s, 5m, 2h, 1d, 1w, 1mo, 1y, permanent/-1
     */
    public long parseDuration(String input) {
        if (input == null) return -1;
        String lower = input.toLowerCase().trim();
        if (lower.equals("permanent") || lower.equals("perm") || lower.equals("-1")) return -1;

        Matcher matcher = DURATION_PATTERN.matcher(lower);
        long totalSeconds = 0;
        boolean found = false;

        while (matcher.find()) {
            found = true;
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            totalSeconds += switch (unit) {
                case "s", "sec", "seconde", "secondes" -> value;
                case "m", "min", "minute", "minutes" -> value * 60;
                case "h", "heure", "heures", "hr" -> value * 3600;
                case "d", "j", "jour", "jours" -> value * 86400;
                case "w", "sem", "semaine", "semaines" -> value * 604800;
                case "mo", "mois" -> value * 2592000L; // 30 jours
                case "y", "an", "ans", "annee", "annees" -> value * 31536000L;
                default -> 0;
            };
        }

        return found ? totalSeconds : -2; // -2 = invalide
    }

    public boolean isValidDuration(String input) {
        long result = parseDuration(input);
        return result != -2;
    }

    public Map<UUID, MuteEntry> getMutes() {
        return Collections.unmodifiableMap(mutes);
    }
}
