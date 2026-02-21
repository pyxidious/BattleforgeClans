package it.battleforge.clans.command;

import it.battleforge.clans.gui.impl.NoClanGui;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.util.InputManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ClansCommand implements CommandExecutor {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;

    public ClansCommand(ClanService service, MessageManager messages, InputManager inputManager) {
        this.service = service;
        this.messages = messages;
        this.inputManager = inputManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.player-only"));
            return true;
        }

        // Se il giocatore ha un clan, in futuro apri MainClanGui
        // Altrimenti apri NoClanGui
        if (service.isInClan(player.getUniqueId())) {
            it.battleforge.clans.gui.impl.MainClanGui gui = new it.battleforge.clans.gui.impl.MainClanGui(service, messages, inputManager);
            gui.open(player);
        } else {
            NoClanGui gui = new NoClanGui(service, messages, inputManager);
            gui.open(player);
        }

        return true;
    }
}