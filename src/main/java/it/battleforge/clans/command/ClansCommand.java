package it.battleforge.clans.command;

import it.battleforge.clans.command.sub.*;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ClansCommand implements CommandExecutor {

    private final Map<String, SubCommand> map = new HashMap<>();
    private final MessageManager messages;

    public ClansCommand(ClanService service, MessageManager messages) {
        this.messages = messages;

        register(new HelpSubCommand(messages));
        register(new CreateSubCommand(service, messages));
        register(new InviteSubCommand(service, messages));
        register(new AcceptSubCommand(service, messages));
        register(new KickSubCommand(service, messages));
        register(new LeaveSubCommand(service, messages));
        register(new DeleteSubCommand(service, messages));
    }

    private void register(SubCommand cmd) {
        map.put(cmd.name().toLowerCase(Locale.ROOT), cmd);
        for (String a : cmd.aliases()) map.put(a.toLowerCase(Locale.ROOT), cmd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(messages.get("error.usage", "usage", "/clans listacomandi"));
            return true;
        }

        SubCommand sub = map.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            sender.sendMessage(messages.get("error.unknown-subcommand"));
            return true;
        }

        return sub.execute(sender, args);
    }
}