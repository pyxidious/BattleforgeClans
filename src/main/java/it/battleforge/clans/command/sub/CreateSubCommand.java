package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.Messages;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class CreateSubCommand implements SubCommand {

    private final ClanService service;

    public CreateSubCommand(ClanService service) {
        this.service = service;
    }

    @Override public String name() { return "create"; }
    @Override public List<String> aliases() { return List.of("crea"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.err("Only players can use this command."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Messages.warn("Usage: /clans create <name>"));
            return true;
        }

        String clanName = args[1];

        ClanService.CreateResult res = service.createClan(player.getUniqueId(), clanName);
        switch (res) {
            case OK -> sender.sendMessage(Messages.ok("Clan created: " + clanName));
            case INVALID_NAME -> sender.sendMessage(Messages.err("Invalid clan name."));
            case NAME_TAKEN -> sender.sendMessage(Messages.err("A clan with that name already exists."));
            case ALREADY_IN_CLAN -> sender.sendMessage(Messages.err("You are already in a clan."));
        }

        return true;
    }
}