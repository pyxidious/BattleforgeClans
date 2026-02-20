package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.model.ClanPermission;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class RolePermSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public RolePermSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "roleperm"; }
    @Override public List<String> aliases() { return List.of("ruoloperm", "permruolo"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(messages.get("error.usage", "usage",
                    "/clans roleperm <ruolo> <SET_HOME|KICK> <on|off>"));
            return true;
        }

        String role = args[1];
        String permRaw = args[2];
        String toggle = args[3].toLowerCase();

        final ClanPermission perm;
        try {
            perm = ClanPermission.valueOf(permRaw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(messages.get("error.perm-invalid"));
            return true;
        }

        final boolean enabled;
        if (toggle.equals("on") || toggle.equals("true") || toggle.equals("si")) enabled = true;
        else if (toggle.equals("off") || toggle.equals("false") || toggle.equals("no")) enabled = false;
        else {
            sender.sendMessage(messages.get("error.toggle-required"));
            return true;
        }

        var res = service.setRolePermission(player.getUniqueId(), role, perm, enabled);

        switch (res) {
            case OK -> sender.sendMessage(messages.get("success.role-perm-set", Map.of(
                    "perm", perm.name(),
                    "role", role,
                    "value", enabled ? "on" : "off"
            )));
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case NOT_LEADER -> sender.sendMessage(messages.get("error.not-leader"));
            case ROLE_NOT_FOUND -> sender.sendMessage(messages.get("error.role-not-found"));
        }

        return true;
    }
}