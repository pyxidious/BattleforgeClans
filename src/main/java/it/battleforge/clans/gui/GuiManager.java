package it.battleforge.clans.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class GuiManager implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof Gui gui) {
            // Annulla qualsiasi click mentre la GUI è aperta per evitare exploit con lo shift-click
            event.setCancelled(true);
            
            // Gestisci l'evento specifico della GUI solo se il click è avvenuto nel top inventory
            if (event.getClickedInventory() == inventory) {
                gui.onClick(event);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof Gui gui) {
            // Se il drag coinvolge slot dell'inventario top (la GUI), annullalo.
            for (int slot : event.getRawSlots()) {
                if (slot < inventory.getSize()) {
                    event.setCancelled(true);
                    break;
                }
            }
            gui.onDrag(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof Gui gui) {
            gui.onClose(event);
        }
    }
}
