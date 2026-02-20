package it.battleforge.clans.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    String name();
    List<String> aliases();
    boolean execute(CommandSender sender, String[] args);
}