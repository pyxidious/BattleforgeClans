package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class ChatSubCommand implements SubCommand {

    private final ClanService service;
    private final MessageManager messages;

    public ChatSubCommand(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override public String name() { return "chat"; }

    // alias sia inglesi che italiani
    @Override public List<String> aliases() { return List.of("cc", "chatclan", "clanchat"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        if (!service.isInClan(player.getUniqueId())) {
            sender.sendMessage(messages.get("error.not-in-clan"));
            return true;
        }

        boolean enabled = service.toggleClanChat(player.getUniqueId());
        sender.sendMessage(messages.get(enabled ? "success.clan-chat-on" : "success.clan-chat-off"));
        return true;
    }
}