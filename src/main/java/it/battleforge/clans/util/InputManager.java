package it.battleforge.clans.util;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InputManager implements Listener {

    private final Map<UUID, Consumer<String>> pendingInputs = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Consumer<String> callback = pendingInputs.remove(player.getUniqueId());

        if (callback != null) {
            event.setCancelled(true);
            
            // Converti il Component di Adventure in una stringa di testo normale
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            
            // Esegui la callback sul task principale in modo da poter accedere in sicurezza
            // ai metodi Bukkit e alla GUI
            Bukkit.getScheduler().runTask(
                JavaPlugin.getProvidingPlugin(InputManager.class), 
                () -> callback.accept(message)
            );
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingInputs.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Richiede un input testuale da parte del giocatore in chat.
     * @param player il giocatore a cui richiedere l'input
     * @param callback la logica da eseguire dopo che il giocatore ha inviato l'input
     */
    public void requestInput(Player player, Consumer<String> callback) {
        pendingInputs.put(player.getUniqueId(), callback);
    }
    
    /**
     * Annulla la richiesta di input per il giocatore specificato.
     * @param player il giocatore a cui annullare l'input
     */
    public void cancelInput(Player player) {
        pendingInputs.remove(player.getUniqueId());
    }
}
