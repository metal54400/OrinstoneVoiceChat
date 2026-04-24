package fr.orinstone.orinstonevoicechat.commands;

import fr.wixymc.orinstonevoicechat.OrinStoneVoiceChat;
import fr.wixymc.orinstonevoicechat.managers.MuteEntry;
import fr.wixymc.orinstonevoicechat.managers.MuteManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final OrinStoneVoiceChat plugin;

    public AdminCommand(OrinStoneVoiceChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("orinstonevoicechat.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "mute" -> handleMute(sender, args);
            case "unmute" -> handleUnmute(sender, args);
            default -> sendUsage(sender);
        }

        return true;
    }

    // ── RELOAD ──────────────────────────────────────────────────────────────

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        sender.sendMessage(plugin.getConfigManager().getMessageComponent("reload-success"));
    }

    // ── MUTE ────────────────────────────────────────────────────────────────

    private void handleMute(CommandSender sender, String[] args) {
        // /originstonevcadmin mute <joueur> <durée> <raison...>
        if (args.length < 4) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("invalid-usage-mute"));
            return;
        }

        String targetName = args[1];
        String durationStr = args[2];
        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        MuteManager muteManager = plugin.getMuteManager();

        // Valider la durée
        if (!muteManager.isValidDuration(durationStr)) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("invalid-duration"));
            return;
        }

        // Chercher le joueur (online ou offline via UUID)
        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUuid;
        String resolvedName;

        if (target != null) {
            targetUuid = target.getUniqueId();
            resolvedName = target.getName();
        } else {
            // Essayer offline player
            @SuppressWarnings("deprecation")
            var offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() == null) {
                sender.sendMessage(plugin.getConfigManager()
                        .getMessageComponent("player-not-found", "{player}", targetName));
                return;
            }
            targetUuid = offlinePlayer.getUniqueId();
            resolvedName = offlinePlayer.getName() != null ? offlinePlayer.getName() : targetName;
        }

        // Vérifier si déjà mute
        if (muteManager.isMuted(targetUuid)) {
            sender.sendMessage(plugin.getConfigManager()
                    .getMessageComponent("mute-already-muted", "{player}", resolvedName));
            return;
        }

        // Appliquer le mute
        boolean success = muteManager.mute(targetUuid, resolvedName, durationStr, reason);
        if (success) {
            sender.sendMessage(plugin.getConfigManager()
                    .getMessageComponent("mute-success",
                            "{player}", resolvedName,
                            "{duration}", durationStr,
                            "{reason}", reason));

            // Notifier le joueur s'il est en ligne
            if (target != null && target.isOnline()) {
                target.sendMessage(plugin.getConfigManager()
                        .getMessageComponent("mute-notify-player",
                                "{duration}", durationStr,
                                "{reason}", reason));
            }
        }
    }

    // ── UNMUTE ──────────────────────────────────────────────────────────────

    private void handleUnmute(CommandSender sender, String[] args) {
        // /originstonevcadmin unmute <joueur>
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("invalid-usage-unmute"));
            return;
        }

        String targetName = args[1];
        MuteManager muteManager = plugin.getMuteManager();

        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUuid;
        String resolvedName;

        if (target != null) {
            targetUuid = target.getUniqueId();
            resolvedName = target.getName();
        } else {
            @SuppressWarnings("deprecation")
            var offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() == null) {
                sender.sendMessage(plugin.getConfigManager()
                        .getMessageComponent("player-not-found", "{player}", targetName));
                return;
            }
            targetUuid = offlinePlayer.getUniqueId();
            resolvedName = offlinePlayer.getName() != null ? offlinePlayer.getName() : targetName;
        }

        // Vérifier si mute
        if (!muteManager.isMuted(targetUuid)) {
            sender.sendMessage(plugin.getConfigManager()
                    .getMessageComponent("unmute-not-muted", "{player}", resolvedName));
            return;
        }

        boolean success = muteManager.unmute(targetUuid);
        if (success) {
            sender.sendMessage(plugin.getConfigManager()
                    .getMessageComponent("unmute-success", "{player}", resolvedName));

            // Notifier le joueur s'il est en ligne
            if (target != null && target.isOnline()) {
                target.sendMessage(plugin.getConfigManager()
                        .getMessageComponent("unmute-notify-player"));
            }
        }
    }

    // ── USAGE ────────────────────────────────────────────────────────────────

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(color("&8&m                                        "));
        sender.sendMessage(color("&6OrinStoneVoiceChat &7- Commandes Admin"));
        sender.sendMessage(color("&8&m                                        "));
        sender.sendMessage(color("&e/originstonevcadmin reload &7- Recharge la config"));
        sender.sendMessage(color("&e/originstonevcadmin mute &f<joueur> <durée> <raison>"));
        sender.sendMessage(color("  &7Ex: mute Steve 2h Spam micro"));
        sender.sendMessage(color("  &7Unités: s m h d j w sem mo y ans"));
        sender.sendMessage(color("&e/originstonevcadmin unmute &f<joueur>"));
        sender.sendMessage(color("&8&m                                        "));
    }

    private Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    // ── TAB COMPLETE ─────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("orinstonevoicechat.admin")) return List.of();

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("reload", "mute", "unmute"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("mute") || sub.equals("unmute")) {
                if (sub.equals("mute")) {
                    Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
                } else {
                    // Unmute: suggérer les joueurs mutés
                    plugin.getMuteManager().getMutes().values()
                            .forEach(e -> completions.add(e.getPlayerName()));
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("mute")) {
            completions.addAll(List.of("30s", "5m", "1h", "12h", "1d", "7d", "1mo", "permanent"));
        } else if (args.length >= 4 && args[0].equalsIgnoreCase("mute")) {
            completions.add("<raison du mute>");
        }

        // Filtrer par ce qui est déjà tapé
        String current = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(current));
        return completions;
    }
}