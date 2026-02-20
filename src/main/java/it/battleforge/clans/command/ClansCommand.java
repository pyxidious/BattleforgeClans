package it.battleforge.clans.command;

import it.battleforge.clans.command.sub.*;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.message.Messages;
import org.bukkit.command.*;

import java.util.*;

public final class ClansCommand implements CommandExecutor {

    private final Map<String, SubCommand> map = new HashMap<>();
    private final MessageManager messages;

    public ClansCommand(ClanService service, MessageManager messages) {
        this.messages = messages;
        register(new HelpSubCommand());
        register(new CreateSubCommand(service));
        register(new InviteSubCommand(service));
        register(new AcceptSubCommand(service));
        register(new DeleteSubCommand(service));
        register(new KickSubCommand(service));
        register(new LeaveSubCommand(service));
    }

    private void register(SubCommand cmd) {
        map.put(cmd.name().toLowerCase(Locale.ROOT), cmd);
        for (String a : cmd.aliases()) map.put(a.toLowerCase(Locale.ROOT), cmd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Messages.warn("Use /clans listacomandi"));
            return true;
        }

        SubCommand sub = map.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            sender.sendMessage(Messages.err("Unknown subcommand. Use /clans listacomandi"));
            return true;
        }

        return sub.execute(sender, args);
    }
}