package it.battleforge.clans.gui.impl;

import it.battleforge.clans.gui.Gui;
import it.battleforge.clans.gui.ItemBuilder;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.model.Clan;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.util.InputManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public class MainClanGui implements Gui {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;
    private Inventory inventory;

    public MainClanGui(ClanService service, MessageManager messages, InputManager inputManager) {
        this.service = service;
        this.messages = messages;
        this.inputManager = inputManager;
    }

    @Override
    public void open(Player player) {
        Optional<Clan> clanOpt = service.getClanByPlayer(player.getUniqueId());
        if (clanOpt.isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Errore: non sei in un clan."));
            return;
        }

        Clan clan = clanOpt.get();
        this.inventory = Bukkit.createInventory(this, 36, MiniMessage.miniMessage().deserialize("<dark_gray>Clan: <dark_red>" + clan.getName()));

        // Info Clan
        inventory.setItem(4, new ItemBuilder(Material.NETHER_STAR)
                .name("<dark_red><bold>" + clan.getName())
                .lore(
                        "<gray>Leader: <white>" + Bukkit.getOfflinePlayer(clan.getLeader()).getName(),
                        "<gray>Membri: <white>" + clan.getMembers().size()
                ).build());

        // Membri
        inventory.setItem(19, new ItemBuilder(Material.PLAYER_HEAD)
                .name("<yellow><bold>Gestione Membri")
                .lore("<gray>Visualizza o espelli membri.", "<gray>Invita nuovi giocatori.")
                .build());

        // Ruoli
        inventory.setItem(21, new ItemBuilder(Material.NAME_TAG)
                .name("<aqua><bold>Gestione Ruoli")
                .lore("<gray>Crea e modifica i ruoli", "<gray>e i relativi permessi.")
                .build());

        // Chat
        boolean chatOn = service.isClanChatEnabled(player.getUniqueId());
        inventory.setItem(23, new ItemBuilder(chatOn ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("<green><bold>Chat Clan: " + (chatOn ? "ATTIVA" : "DISATTIVA"))
                .lore("<gray>Clicca per " + (chatOn ? "disattivare" : "attivare"), "<gray>la chat privata del clan.")
                .build());

        // Impostazioni
        inventory.setItem(25, new ItemBuilder(Material.COMPARATOR)
                .name("<gold><bold>Impostazioni")
                .lore("<gray>Home, abbandona o elimina il clan.")
                .build());

        // Fill background
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

        switch (slot) {
            case 19 -> {
                new MembersGui(service, messages, inputManager).open(player);
            }
            case 21 -> {
                new RolesGui(service, messages, inputManager).open(player);
            }
            case 23 -> {
                boolean enabled = service.toggleClanChat(player.getUniqueId());
                player.sendMessage(messages.get("success.chat-mode-changed", "status", enabled ? "ATTIVA" : "DISATTIVA"));
                // Ricarica la GUI
                open(player);
            }
            case 25 -> {
                new SettingsGui(service, messages, inputManager).open(player);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
