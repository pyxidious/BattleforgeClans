package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpSubCommand implements SubCommand {

    @Override public String name() { return "listacomandi"; }
    @Override public List<String> aliases() { return List.of("help", "comandi"); }
    @Override public String permission() { return null; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("===== Comandi Clans =====").color(NamedTextColor.GOLD));
        sender.sendMessage(Messages.usage("/clans listacomandi").append(Component.text(" - Mostra questa lista").color(NamedTextColor.GRAY)));
        // qui aggiungerai le righe man mano che crei i subcommands
        sender.sendMessage(Component.text("=========================").color(NamedTextColor.GOLD));
        return true;
    }
}