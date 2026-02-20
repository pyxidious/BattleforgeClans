package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.service.ClanService.CreateResult;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class CreateSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public CreateSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "create"; }
    @Override public List<String> aliases() { return List.of("crea"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messages.get("error.usage", "usage", "/clans create <nome>"));
            return true;
        }

        String clanNameRaw = args[1];
        String clanName = clanNameRaw == null ? "" : clanNameRaw.trim();

        ClanService.CreateResult res = service.createClan(player.getUniqueId(), clanName);
        switch (res) {
            case OK -> sender.sendMessage(messages.get("success.clan-created", "clan", clanName));
            case INVALID_NAME -> sender.sendMessage(messages.get("error.invalid-name"));
            case NAME_TOO_LONG -> sender.sendMessage(messages.get("error.clan-name-too-long"));
            case NAME_TAKEN -> sender.sendMessage(messages.get("error.name-taken"));
            case ALREADY_IN_CLAN -> sender.sendMessage(messages.get("error.already-in-clan"));
        }
        return true;
    }
}