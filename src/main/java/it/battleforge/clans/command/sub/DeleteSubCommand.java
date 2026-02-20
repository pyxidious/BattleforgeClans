package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class DeleteSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public DeleteSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "delete"; }
    @Override public List<String> aliases() { return List.of("del", "elimina"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player leader)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        var clanOpt = service.getClanByPlayer(leader.getUniqueId());
        if (clanOpt.isEmpty()) {
            sender.sendMessage(messages.get("error.not-in-clan"));
            return true;
        }

        var clanSnapshot = clanOpt.get();

        ClanService.DeleteResult res = service.deleteClan(leader.getUniqueId());
        switch (res) {
            case OK -> {
                sender.sendMessage(messages.get("success.clan-deleted"));

                for (var uuid : clanSnapshot.getMembers()) {
                    var p = Bukkit.getPlayer(uuid);
                    if (p != null && !p.getUniqueId().equals(leader.getUniqueId())) {
                        p.sendMessage(messages.get("info.clan-deleted-by-leader"));
                    }
                }
            }
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case NOT_LEADER -> sender.sendMessage(messages.get("error.not-leader"));
        }
        return true;
    }
}