package it.battleforge.clans.command.sub;

import it.battleforge.clans.command.SubCommand;
import it.battleforge.clans.message.MessageManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class HelpSubCommand implements SubCommand {

    private final MessageManager messages;
    private final MiniMessage mini = MiniMessage.miniMessage();

    public HelpSubCommand(MessageManager messages) {
        this.messages = messages;
    }

    @Override public String name() { return "listacomandi"; }
    @Override public List<String> aliases() { return List.of("help", "comandi"); }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        // header/footer come Component già con prefix (quindi qui meglio SENZA prefix)
        // Per mantenere semplice: li mandiamo comunque con prefix.
        sender.sendMessage(messages.get("help.header"));

        // lines è una lista nel file, quindi qui serve una piccola estensione al MessageManager
        // Se non vuoi toccare MessageManager, ti do subito la versione "rapida" qui sotto.
        // -> vedi "Aggiunta a MessageManager" più giù

        for (String line : messages.getStringList("help.lines")) {
            sender.sendMessage(mini.deserialize(messages.getRawPrefix() + line));
        }

        sender.sendMessage(messages.get("help.footer"));
        return true;
    }
}