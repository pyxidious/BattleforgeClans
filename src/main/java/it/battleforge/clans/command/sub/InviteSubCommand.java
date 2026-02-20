package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class InviteSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public InviteSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "invite"; }
    @Override public List<String> aliases() { return List.of("invita"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player leader)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messages.get("error.usage", "usage", "/clans invite <giocatore>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("error.target-not-found"));
            return true;
        }

        ClanService.InviteResult res = service.invite(leader.getUniqueId(), target.getUniqueId());
        switch (res) {
            case OK -> {
                sender.sendMessage(messages.get("success.invite-sent", "player", target.getName()));
                target.sendMessage(messages.get("info.invited"));
            }
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case NOT_LEADER -> sender.sendMessage(messages.get("error.not-leader"));
            case TARGET_ALREADY_IN_CLAN -> sender.sendMessage(messages.get("error.target-already-in-clan"));
            case TARGET_ALREADY_INVITED -> sender.sendMessage(messages.get("error.target-already-invited"));
        }
        return true;
    }
}