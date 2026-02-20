package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.Messages;
import it.battleforge.clans.service.ClanService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class DeleteSubCommand implements SubCommand {

    private final ClanService service;

    public DeleteSubCommand(ClanService service) {
        this.service = service;
    }

    @Override public String name() { return "delete"; }
    @Override public List<String> aliases() { return List.of("del", "elimina"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player leader)) {
            sender.sendMessage(Messages.err("Only players can use this command."));
            return true;
        }

        var clanOpt = service.getClanByPlayer(leader.getUniqueId());
        if (clanOpt.isEmpty()) {
            sender.sendMessage(Messages.err("You are not in a clan."));
            return true;
        }

        var clan = clanOpt.get();
        ClanService.DeleteResult res = service.deleteClan(leader.getUniqueId());
        switch (res) {
            case OK -> {
                sender.sendMessage(Messages.ok("Clan deleted."));
                // Notifica membri online (dopo delete non abbiamo più mapping, quindi usiamo la snapshot di prima)
                for (var uuid : clan.getMembers()) {
                    var p = Bukkit.getPlayer(uuid);
                    if (p != null && !p.getUniqueId().equals(leader.getUniqueId())) {
                        p.sendMessage(Messages.warn("Your clan was deleted by the leader."));
                    }
                }
            }
            case NOT_IN_CLAN -> sender.sendMessage(Messages.err("You are not in a clan."));
            case NOT_LEADER -> sender.sendMessage(Messages.err("Only the clan leader can delete the clan."));
        }

        return true;
    }
}