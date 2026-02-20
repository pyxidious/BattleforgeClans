package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class RoleCreateSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public RoleCreateSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "rolecreate"; }
    @Override public List<String> aliases() { return List.of("ruolocrea", "role", "ruolo"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        // /clans role create <nome> (noi lo modelliamo come: /clans rolecreate <nome>)
        if (args.length < 2) {
            sender.sendMessage(messages.get("error.usage", "usage", "/clans rolecreate <nome-ruolo>"));
            return true;
        }

        String roleName = args[1];
        var res = service.createRole(player.getUniqueId(), roleName);

        switch (res) {
            case OK -> sender.sendMessage(messages.get("success.role-created", "role", roleName));
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case NOT_LEADER -> sender.sendMessage(messages.get("error.not-leader"));
            case INVALID_NAME -> sender.sendMessage(messages.get("error.role-invalid-name"));
            case ALREADY_EXISTS -> sender.sendMessage(messages.get("error.role-already-exists"));
            case NAME_TOO_LONG -> sender.sendMessage(messages.get("error.role-name-too-long"));
        }
        return true;
    }
}