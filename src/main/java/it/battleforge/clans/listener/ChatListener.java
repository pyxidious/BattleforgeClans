package it.battleforge.clans.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {

    private final ClanService service;
    private final MessageManager messages;
    private final MiniMessage mini = MiniMessage.miniMessage();

    public ChatListener(ClanService service, MessageManager messages) {
        this.service = service;
        this.messages = messages;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // --- 1) Clan private chat (toggle ON) ---
        if (service.isClanChatEnabled(player.getUniqueId())) {
            var clanOpt = service.getClanByPlayer(player.getUniqueId());
            if (clanOpt.isEmpty()) return;

            var clan = clanOpt.get();

            // blocca la chat pubblica
            event.setCancelled(true);

            // prendiamo il contenuto del messaggio come plain text
            String plainMessage = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(event.message());

            String format = messages.getRaw("clanchat.format"); // vedi aggiunta sotto a MessageManager
            String rendered = format
                    .replace("{clan}", clan.getName())
                    .replace("{player}", player.getName())
                    .replace("{message}", plainMessage);

            Component out = mini.deserialize(messages.getRawPrefix() + rendered);

            // manda solo ai membri online
            for (var uuid : clan.getMembers()) {
                Player target = Bukkit.getPlayer(uuid);
                if (target != null) target.sendMessage(out);
            }
            return;
        }

        // --- 2) Public chat: add clan prefix if in clan ---
        var clanOpt = service.getClanByPlayer(player.getUniqueId());
        if (clanOpt.isEmpty()) return;

        String clanName = clanOpt.get().getName();

        // aggiunge prefisso al nome in chat pubblica
        event.renderer((source, sourceDisplayName, message, viewer) ->
                mini.deserialize("<gold>[" + clanName + "]</gold> ")
                        .append(sourceDisplayName)
                        .append(mini.deserialize("<gray>: </gray>"))
                        .append(message)
        );
    }
}