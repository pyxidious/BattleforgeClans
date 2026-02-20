package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class SetHomeSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public SetHomeSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "sethome"; }
    @Override public List<String> aliases() { return List.of("impostabase", "setbase"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        var res = service.setHome(player.getUniqueId(), player.getLocation());
        switch (res) {
            case OK -> sender.sendMessage(messages.get("success.home-set"));
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case NO_PERMISSION -> sender.sendMessage(messages.get("error.no-permission"));
        }
        return true;
    }
}