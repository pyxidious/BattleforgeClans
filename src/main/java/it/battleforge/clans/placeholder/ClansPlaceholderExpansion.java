package it.battleforge.clans.placeholder;

import it.battleforge.clans.ClansPlugin;
import it.battleforge.clans.service.ClanService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ClansPlaceholderExpansion extends PlaceholderExpansion {
    private final ClansPlugin plugin;
    private final ClanService service;

    public ClansPlaceholderExpansion(ClansPlugin plugin, ClanService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "battleforgeclans";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || player.getUniqueId() == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "clan_name" -> service.getClanByPlayer(player.getUniqueId())
                    .map(clan -> clan.getName())
                    .orElse("");
            case "clan_role" -> service.getRoleDisplay(player.getUniqueId());
            case "in_clan" -> service.isInClan(player.getUniqueId()) ? "yes" : "no";
            default -> null;
        };
    }
}
