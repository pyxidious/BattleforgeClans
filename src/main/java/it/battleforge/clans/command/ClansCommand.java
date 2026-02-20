package it.battleforge.clans.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClansCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(
                    Component.text("Usa /clans listacomandi")
                            .color(NamedTextColor.RED)
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("listacomandi")) {

            sender.sendMessage(
                    Component.text("===== Comandi Clans =====")
                            .color(NamedTextColor.GOLD)
            );

            sender.sendMessage(
                    Component.text("/clans listacomandi ")
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text("- Mostra questa lista")
                                    .color(NamedTextColor.GRAY))
            );

            sender.sendMessage(
                    Component.text("/clans create <nome> ")
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text("- Crea un clan")
                                    .color(NamedTextColor.GRAY))
            );

            sender.sendMessage(
                    Component.text("/clans invite <player> ")
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text("- Invita un player")
                                    .color(NamedTextColor.GRAY))
            );

            sender.sendMessage(
                    Component.text("=========================")
                            .color(NamedTextColor.GOLD)
            );

            return true;
        }

        sender.sendMessage(
                Component.text("Sottocomando sconosciuto.")
                        .color(NamedTextColor.RED)
        );

        return true;
    }
}