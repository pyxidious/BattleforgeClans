package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class RolesSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public RolesSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "roles"; }
    @Override public List<String> aliases() { return List.of("ruoli", "listaruoli"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        var clanOpt = service.getClanByPlayer(player.getUniqueId());
        if (clanOpt.isEmpty()) {
            sender.sendMessage(messages.get("error.not-in-clan"));
            return true;
        }

        var clan = clanOpt.get();

        sender.sendMessage(messages.get("info.roles-header"));
        for (var role : clan.getRoles().values()) {
            sender.sendMessage(messages.get("info.roles-line", Map.of("role", role.getName())));
        }
        return true;
    }
}