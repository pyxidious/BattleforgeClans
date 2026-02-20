package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.Messages;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class HelpSubCommand implements SubCommand {

    @Override public String name() { return "listacomandi"; }
    @Override public List<String> aliases() { return List.of("help", "comandi"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(Messages.info("Commands:"));
        sender.sendMessage(Messages.info("/clans create <name>"));
        sender.sendMessage(Messages.info("/clans invite <player>"));
        sender.sendMessage(Messages.info("/clans accept"));
        sender.sendMessage(Messages.info("/clans kick <player>"));
        sender.sendMessage(Messages.info("/clans leave"));
        sender.sendMessage(Messages.info("/clans delete"));
        return true;
    }
}