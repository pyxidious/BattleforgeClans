package it.battleforge.clans.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public interface Gui extends InventoryHolder {

    /**
     * Apre la GUI per il giocatore specificato.
     * @param player il giocatore a cui aprire la GUI
     */
    void open(Player player);

    /**
     * Gestisce i click all'interno della GUI.
     * @param event l'evento di click
     */
    void onClick(InventoryClickEvent event);

    /**
     * Gestisce la chiusura della GUI.
     * @param event l'evento di chiusura
     */
    default void onClose(InventoryCloseEvent event) {}

    /**
     * Gestisce il trascinamento degli oggetti all'interno della GUI.
     * @param event l'evento di drag
     */
    default void onDrag(InventoryDragEvent event) {}
}
