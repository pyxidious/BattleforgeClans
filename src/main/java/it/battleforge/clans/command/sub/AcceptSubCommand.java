package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.Messages;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class AcceptSubCommand implements SubCommand {

    private final ClanService service;

    public AcceptSubCommand(ClanService service) {
        this.service = service;
    }

    @Override public String name() { return "accept"; }
    @Override public List<String> aliases() { return List.of("join", "accetta"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.err("Only players can use this command."));
            return true;
        }

        ClanService.AcceptInviteResult res = service.acceptInvite(player.getUniqueId());
        switch (res) {
            case OK -> sender.sendMessage(Messages.ok("You joined the clan!"));
            case NO_INVITE -> sender.sendMessage(Messages.err("You have no pending invites."));
            case ALREADY_IN_CLAN -> sender.sendMessage(Messages.err("You are already in a clan."));
            case CLAN_NO_LONGER_EXISTS -> sender.sendMessage(Messages.err("That clan no longer exists."));
        }

        return true;
    }
}