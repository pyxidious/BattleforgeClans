package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.Messages;
import it.battleforge.clans.service.ClanService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class InviteSubCommand implements SubCommand {

    private final ClanService service;

    public InviteSubCommand(ClanService service) {
        this.service = service;
    }

    @Override public String name() { return "invite"; }
    @Override public List<String> aliases() { return List.of("invita"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player leader)) {
            sender.sendMessage(Messages.err("Only players can use this command."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Messages.warn("Usage: /clans invite <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Messages.err("Player not found (must be online)."));
            return true;
        }

        ClanService.InviteResult res = service.invite(leader.getUniqueId(), target.getUniqueId());
        switch (res) {
            case OK -> {
                sender.sendMessage(Messages.ok("Invite sent to " + target.getName() + "."));
                target.sendMessage(Messages.info("You have been invited to a clan. Use /clans accept to join."));
            }
            case NOT_IN_CLAN -> sender.sendMessage(Messages.err("You are not in a clan."));
            case NOT_LEADER -> sender.sendMessage(Messages.err("Only the clan leader can invite."));
            case TARGET_ALREADY_IN_CLAN -> sender.sendMessage(Messages.err("That player is already in a clan."));
            case TARGET_ALREADY_INVITED -> sender.sendMessage(Messages.err("That player already has a pending invite."));
        }

        return true;
    }
}