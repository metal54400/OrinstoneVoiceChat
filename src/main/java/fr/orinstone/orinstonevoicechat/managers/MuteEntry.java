package fr.orinstone.orinstonevoicechat.managers;

import java.time.Instant;
import java.util.UUID;

public class MuteEntry {

    private final UUID playerUuid;
    private final String playerName;
    private final String reason;
    private final long mutedAt;       // timestamp unix (secondes)
    private final long expiresAt;     // timestamp unix (secondes), -1 = permanent
    private final String duration;    // lisible ex: "2h"

    public MuteEntry(UUID playerUuid, String playerName, String reason, long expiresAt, String duration) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.reason = reason;
        this.mutedAt = Instant.now().getEpochSecond();
        this.expiresAt = expiresAt;
        this.duration = duration;
    }

    // Reconstruction depuis fichier
    public MuteEntry(UUID playerUuid, String playerName, String reason, long mutedAt, long expiresAt, String duration) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.reason = reason;
        this.mutedAt = mutedAt;
        this.expiresAt = expiresAt;
        this.duration = duration;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public String getReason() { return reason; }
    public long getMutedAt() { return mutedAt; }
    public long getExpiresAt() { return expiresAt; }
    public String getDuration() { return duration; }

    public boolean isPermanent() {
        return expiresAt == -1;
    }

    public boolean isExpired() {
        if (isPermanent()) return false;
        return Instant.now().getEpochSecond() >= expiresAt;
    }

    public long getRemainingSeconds() {
        if (isPermanent()) return Long.MAX_VALUE;
        long remaining = expiresAt - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
}
