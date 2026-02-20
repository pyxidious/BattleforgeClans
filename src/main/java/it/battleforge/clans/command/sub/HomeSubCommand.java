package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class HomeSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public HomeSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "home"; }
    @Override public List<String> aliases() { return List.of("base", "ritrovo"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        var res = service.canTeleportHome(player.getUniqueId());
        switch (res) {
            case OK -> {
                var loc = service.getHomeLocation(player.getUniqueId());
                player.teleportAsync(loc);
                sender.sendMessage(messages.get("success.teleported-home"));
            }
            case NOT_IN_CLAN -> sender.sendMessage(messages.get("error.not-in-clan"));
            case HOME_NOT_SET -> sender.sendMessage(messages.get("error.home-not-set"));
        }
        return true;
    }
}