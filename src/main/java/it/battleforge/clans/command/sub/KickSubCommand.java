package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class KickSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public KickSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "kick"; }
    @Override public List<String> aliases() { return List.of("espelli", "caccia"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player leader)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messages.get("error.usage", "usage", "/clans kick <giocatore>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("error.target-not-found"));
            return true;
        }

        ClanService.KickResult res = service.kick(leader.getUniqueId(), target.getUniqueId());
        switch (res) {
            case OK -> {
                sender.sendMessage(messages.get("success.player-kicked", "player", target.getName()));
                target.sendMessage(messages.get("info.kicked"));
            }
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case NOT_LEADER -> sender.sendMessage(messages.get("error.not-leader"));
            case CANNOT_KICK_SELF -> sender.sendMessage(messages.get("error.cannot-kick-self"));
            case TARGET_NOT_IN_YOUR_CLAN -> sender.sendMessage(messages.get("error.target-not-in-your-clan"));
        }
        return true;
    }
}