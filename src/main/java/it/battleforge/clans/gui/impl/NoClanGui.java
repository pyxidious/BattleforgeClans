package it.battleforge.clans.gui.impl;

import it.battleforge.clans.gui.Gui;
import it.battleforge.clans.gui.ItemBuilder;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.util.InputManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class NoClanGui implements Gui {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;
    private Inventory inventory;

    public NoClanGui(ClanService service, MessageManager messages, InputManager inputManager) {
        this.service = service;
        this.messages = messages;
        this.inputManager = inputManager;
    }

    @Override
    public void open(Player player) {
        this.inventory = Bukkit.createInventory(this, 27, MiniMessage.miniMessage().deserialize("<dark_gray>Gestione Clan"));
        
        inventory.setItem(11, new ItemBuilder(Material.WRITABLE_BOOK)
                .name("<green><bold>Crea Clan")
                .lore("<gray>Clicca per creare", "<gray>il tuo nuovo clan!")
                .build());
                
        inventory.setItem(15, new ItemBuilder(Material.PAPER)
                .name("<yellow><bold>Inviti Ricevuti")
                .lore("<gray>Clicca per visualizzare", "<gray>gli inviti in sospeso.")
                .build());

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("<gray>").build());
            }
        }
        
        player.openInventory(inventory);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 11) {
            player.closeInventory();
            player.sendMessage(MiniMessage.miniMessage().deserialize("<white>Scrivi in chat il nome del clan che vuoi creare, oppure scrivi <red>annulla<white>."));
            
            inputManager.requestInput(player, (input) -> {
                if (input.equalsIgnoreCase("annulla")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Operazione annullata."));
                    return;
                }
                
                ClanService.CreateResult res = service.createClan(player.getUniqueId(), input);
                switch (res) {
                    case OK -> player.sendMessage(messages.get("success.clan-created", "clan", input));
                    case INVALID_NAME -> player.sendMessage(messages.get("error.invalid-name"));
                    case NAME_TOO_LONG -> player.sendMessage(messages.get("error.clan-name-too-long"));
                    case NAME_TAKEN -> player.sendMessage(messages.get("error.name-taken"));
                    case ALREADY_IN_CLAN -> player.sendMessage(messages.get("error.already-in-clan"));
                }
            });
        } else if (slot == 15) {
            player.closeInventory();
            new InvitesGui(service, messages, inputManager).open(player);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
