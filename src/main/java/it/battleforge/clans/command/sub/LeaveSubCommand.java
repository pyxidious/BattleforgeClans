package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class LeaveSubCommand implements SubCommand {


    private final ClanService service;
    private final MessageManager messages;

    public LeaveSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "leave"; }
    @Override public List<String> aliases() { return List.of("esci", "abbandona"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        ClanService.LeaveResult res = service.leave(player.getUniqueId());
        switch (res) {
            case OK -> sender.sendMessage(messages.get("success.clan-left"));
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case LEADER_CANNOT_LEAVE -> sender.sendMessage(messages.get("error.leader-cannot-leave"));
        }

        return true;
    }
}