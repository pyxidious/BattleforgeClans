package it.battleforge.clans.command;

import it.battleforge.clans.message.Messages;
import it.battleforge.clans.command.sub.HelpSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ClansCommand implements CommandExecutor {

    private final Map<String, SubCommand> byName = new HashMap<>();

    public ClansCommand() {
        register(new HelpSubCommand());
    }

    private void register(SubCommand cmd) {
        byName.put(cmd.name().toLowerCase(Locale.ROOT), cmd);
        for (String a : cmd.aliases()) {
            byName.put(a.toLowerCase(Locale.ROOT), cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Messages.usage("Usa /clans listacomandi"));
            return true;
        }

        SubCommand sub = byName.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            sender.sendMessage(Messages.error("Sottocomando sconosciuto. Usa /clans listacomandi"));
            return true;
        }

        String perm = sub.permission();
        if (perm != null && !sender.hasPermission(perm)) {
            sender.sendMessage(Messages.error("Non hai il permesso per usare questo comando."));
            return true;
        }

        return sub.execute(sender, args);
    }
}