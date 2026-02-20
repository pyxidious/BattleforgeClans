package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class RoleSetSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public RoleSetSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "roleset"; }
    @Override public List<String> aliases() { return List.of("ruoloassegna", "setruolo"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player leader)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(messages.get("error.usage", "usage", "/clans roleset <giocatore> <ruolo>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("error.target-not-found"));
            return true;
        }

        String role = args[2];

        var res = service.assignRole(leader.getUniqueId(), target.getUniqueId(), role);
        switch (res) {
            case OK -> sender.sendMessage(messages.get("success.role-assigned", java.util.Map.of(
                    "role", role,
                    "player", target.getName()
            )));
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case NOT_LEADER -> sender.sendMessage(messages.get("error.not-leader"));
            case ROLE_NOT_FOUND -> sender.sendMessage(messages.get("error.role-not-found"));
            case TARGET_NOT_IN_YOUR_CLAN -> sender.sendMessage(messages.get("error.target-not-in-your-clan"));
        }

        return true;
    }
}