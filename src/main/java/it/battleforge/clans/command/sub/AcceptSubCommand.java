package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class AcceptSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public AcceptSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "accept"; }
    @Override public List<String> aliases() { return List.of("join", "accetta"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        ClanService.AcceptInviteResult res = service.acceptInvite(player.getUniqueId());
        switch (res) {
            case OK -> sender.sendMessage(messages.get("success.invite-accepted"));
            case NO_INVITE -> sender.sendMessage(messages.get("error.no-invite"));
            case ALREADY_IN_CLAN -> sender.sendMessage(messages.get("error.already-in-clan"));
            case CLAN_NO_LONGER_EXISTS -> sender.sendMessage(messages.get("error.clan-no-longer-exists"));
        }
        return true;
    }
}