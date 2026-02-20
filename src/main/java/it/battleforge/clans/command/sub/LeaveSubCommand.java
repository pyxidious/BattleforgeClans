package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.Messages;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class LeaveSubCommand implements SubCommand {

    private final ClanService service;

    public LeaveSubCommand(ClanService service) {
        this.service = service;
    }

    @Override public String name() { return "leave"; }
    @Override public List<String> aliases() { return List.of("esci", "abbandona"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.err("Only players can use this command."));
            return true;
        }

        ClanService.LeaveResult res = service.leave(player.getUniqueId());
        switch (res) {
            case OK -> sender.sendMessage(Messages.ok("You left the clan."));
            case NOT_IN_CLAN -> sender.sendMessage(Messages.err("You are not in a clan."));
            case LEADER_CANNOT_LEAVE -> sender.sendMessage(Messages.err("The leader cannot leave. Use /clans delete (or implement transfer)."));
        }

        return true;
    }
}