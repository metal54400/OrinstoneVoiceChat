# OrinStoneVoiceChat

Plugin Paper 1.21.1 — Intégration Voice Chat avec système de mute avancé.

## 📦 Dépendances
- [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) `2.5.x`
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

## 🔨 Compilation
```bash
./gradlew build
```
Le JAR se trouve dans `build/libs/OrinStoneVoiceChat-1.0.0.jar`

---

## ⚙️ Configuration — `config.yml`

| Clé | Description |
|-----|-------------|
| `prefix` | Préfixe de tous les messages du plugin |
| `messages.*` | Tous les messages modifiables (codes couleurs `&`) |

---

## 🎙️ Icônes du micro (ActionBar)

| Icône | Signification |
|-------|--------------|
| `&7🎙️` | Joueur sans Voice Chat |
| `&a🎙️` | Joueur avec Voice Chat actif |
| `&c🎙️` | Joueur mute |

---

## 🛠️ Commandes

### `/originstonevcadmin`
Permission : `orinstonevoicechat.admin` (op par défaut)

| Sous-commande | Usage | Description |
|--------------|-------|-------------|
| `reload` | `/originstonevcadmin reload` | Recharge le config.yml |
| `mute` | `/originstonevcadmin mute <joueur> <durée> <raison>` | Mute un joueur |
| `unmute` | `/originstonevcadmin unmute <joueur>` | Unmute un joueur |

### Exemples de durées
```
30s       → 30 secondes
5m        → 5 minutes
2h        → 2 heures
1d / 1j   → 1 jour
1w / 1sem → 1 semaine
1mo       → 1 mois
1y / 1an  → 1 an
permanent → Mute permanent
```

### Exemples complets
```
/originstonevcadmin mute Steve 2h Spam micro
/originstonevcadmin mute Alex 7d Comportement toxique
/originstonevcadmin mute Notch permanent Récidiviste
/originstonevcadmin unmute Steve
/originstonevcadmin reload
```

---

## 📊 Placeholders PlaceholderAPI

| Placeholder | Valeur |
|-------------|--------|
| `%orinstonevoicechat_has_voicechat%` | `true` / `false` |
| `%orinstonevoicechat_voicechat_icon%` | Icône 🎙️ colorée selon l'état |
| `%orinstonevoicechat_is_muted%` | `true` / `false` |
| `%orinstonevoicechat_mute_reason%` | Raison du mute (vide si non mute) |
| `%orinstonevoicechat_mute_duration%` | Durée du mute (ex: `2h`) |
| `%orinstonevoicechat_mute_expires%` | Timestamp d'expiration ou `permanent` |
| `%orinstonevoicechat_mute_remaining%` | Secondes restantes (`-1` si permanent) |
| `%orinstonevoicechat_status%` | `MUTED` / `ACTIVE` / `NO_VOICECHAT` |

---

## 📁 Fichiers générés
```
plugins/OrinStoneVoiceChat/
├── config.yml    ← Messages et préfixe
└── mutes.yml     ← Données des mutes (auto-géré)
```

---

## 🔐 Permissions

| Permission | Description | Défaut |
|-----------|-------------|--------|
| `orinstonevoicechat.admin` | Accès aux commandes admin | op |
| `orinstonevoicechat.bypass` | Bypass le mute vocal | false |
