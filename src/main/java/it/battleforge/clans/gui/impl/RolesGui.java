package it.battleforge.clans.gui.impl;

import it.battleforge.clans.gui.Gui;
import it.battleforge.clans.gui.ItemBuilder;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.model.Clan;
import it.battleforge.clans.model.ClanPermission;
import it.battleforge.clans.model.ClanRole;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.util.InputManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolesGui implements Gui {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;
    private Inventory inventory;
    private final List<String> roleKeys = new ArrayList<>();

    public RolesGui(ClanService service, MessageManager messages, InputManager inputManager) {
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
        boolean isLeader = clan.getLeader().equals(player.getUniqueId());

        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize("<dark_gray>Ruoli del Clan"));

        // Torna indietro
        inventory.setItem(0, new ItemBuilder(Material.ARROW)
                .name("<yellow>Torna Indietro")
                .build());

        // Crea ruolo
        if (isLeader) {
            inventory.setItem(4, new ItemBuilder(Material.NAME_TAG)
                    .name("<aqua><bold>Crea Nuovo Ruolo")
                    .lore("<gray>Clicca per creare un nuovo", "<gray>ruolo personalizzato.")
                    .build());
        }

        roleKeys.clear();
        roleKeys.addAll(clan.getRoles().keySet());

        int slot = 9;
        for (String roleKey : roleKeys) {
            if (slot >= 53) break;

            ClanRole role = clan.getRoles().get(roleKey);

            ItemBuilder builder = new ItemBuilder(Material.PAPER)
                    .name("<yellow>" + role.getName());

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Permessi: ");
            
            boolean canSetHome = role.has(ClanPermission.SET_HOME);
            boolean canKick = role.has(ClanPermission.KICK);

            lore.add((canSetHome ? "<green>✔" : "<red>✖") + " SET_HOME");
            lore.add((canKick ? "<green>✔" : "<red>✖") + " KICK");

            if (isLeader) {
                lore.add("");
                lore.add("<dark_gray>Click Sinistro: <gray>Attiva/Disattiva SET_HOME");
                lore.add("<dark_gray>Shift-Click Sinistro: <gray>Attiva/Disattiva KICK");
            }

            builder.lore(lore);
            inventory.setItem(slot++, builder.build());
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null && i < 9) { // Solo la prima riga
                inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("<gray>").build());
            }
        }

        player.openInventory(inventory);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        Optional<Clan> clanOpt = service.getClanByPlayer(player.getUniqueId());
        if (clanOpt.isEmpty()) {
            player.closeInventory();
            return;
        }
        
        Clan clan = clanOpt.get();
        boolean isLeader = clan.getLeader().equals(player.getUniqueId());

        if (slot == 0) {
            new MainClanGui(service, messages, inputManager).open(player);
            return;
        }

        if (slot == 4 && isLeader) {
            player.closeInventory();
            player.sendMessage(MiniMessage.miniMessage().deserialize("<white>Scrivi in chat il nome del nuovo ruolo, oppure <red>annulla<white>."));
            
            inputManager.requestInput(player, (input) -> {
                if (input.equalsIgnoreCase("annulla")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Operazione annullata."));
                    return;
                }
                
                ClanService.CreateRoleResult res = service.createRole(player.getUniqueId(), input);
                switch (res) {
                    case OK -> player.sendMessage(messages.get("success.role-created", "role", input));
                    case INVALID_NAME -> player.sendMessage(messages.get("error.invalid-name"));
                    case NAME_TOO_LONG -> player.sendMessage(messages.get("error.role-name-too-long"));
                    case ALREADY_EXISTS -> player.sendMessage(messages.get("error.role-already-exists"));
                    case NOT_LEADER -> player.sendMessage(messages.get("error.not-leader"));
                }
            });
            return;
        }

        // Click su un ruolo (Slot tra 9 e 53)
        if (slot >= 9 && slot < 9 + roleKeys.size() && isLeader) {
            String roleKey = roleKeys.get(slot - 9);
            ClanRole role = clan.getRoles().get(roleKey);
            
            if (role != null) {
                if (event.isShiftClick()) {
                    // Toggle KICK
                    boolean current = role.has(ClanPermission.KICK);
                    service.setRolePermission(player.getUniqueId(), roleKey, ClanPermission.KICK, !current);
                } else if (event.isLeftClick()) {
                    // Toggle SET_HOME
                    boolean current = role.has(ClanPermission.SET_HOME);
                    service.setRolePermission(player.getUniqueId(), roleKey, ClanPermission.SET_HOME, !current);
                }
                open(player); // Aggiorna la GUI
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
