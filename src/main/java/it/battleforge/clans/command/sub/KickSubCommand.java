package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.Messages;
import it.battleforge.clans.service.ClanService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class KickSubCommand implements SubCommand {

    private final ClanService service;

    public KickSubCommand(ClanService service) {
        this.service = service;
    }

    @Override public String name() { return "kick"; }
    @Override public List<String> aliases() { return List.of("espelli", "caccia"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player leader)) {
            sender.sendMessage(Messages.err("Only players can use this command."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Messages.warn("Usage: /clans kick <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Messages.err("Player not found (must be online)."));
            return true;
        }

        ClanService.KickResult res = service.kick(leader.getUniqueId(), target.getUniqueId());
        switch (res) {
            case OK -> {
                sender.sendMessage(Messages.ok("Kicked " + target.getName() + " from the clan."));
                target.sendMessage(Messages.warn("You have been kicked from the clan."));
            }
            case NOT_IN_CLAN -> sender.sendMessage(Messages.err("You are not in a clan."));
            case NOT_LEADER -> sender.sendMessage(Messages.err("Only the clan leader can kick."));
            case CANNOT_KICK_SELF -> sender.sendMessage(Messages.err("You cannot kick yourself."));
            case TARGET_NOT_IN_YOUR_CLAN -> sender.sendMessage(Messages.err("That player is not in your clan."));
        }

        return true;
    }
}