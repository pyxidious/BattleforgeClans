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

public class InvitesGui implements Gui {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;
    private Inventory inventory;

    public InvitesGui(ClanService service, MessageManager messages, InputManager inputManager) {
        this.service = service;
        this.messages = messages;
        this.inputManager = inputManager;
    }

    @Override
    public void open(Player player) {
        this.inventory = Bukkit.createInventory(this, 27, MiniMessage.miniMessage().deserialize("<dark_gray>Inviti Clan"));

        // Torna indietro
        inventory.setItem(0, new ItemBuilder(Material.ARROW)
                .name("<yellow>Torna Indietro")
                .build());

        Optional<String> pendingInvite = service.getPendingInvite(player.getUniqueId());

        if (pendingInvite.isEmpty()) {
            inventory.setItem(13, new ItemBuilder(Material.BARRIER)
                    .name("<red>Nessun invito in sospeso")
                    .lore("<gray>Non hai ricevuto alcun", "<gray>invito da nessun clan.")
                    .build());
        } else {
            String clanKey = pendingInvite.get();
            Optional<Clan> clanOpt = service.getClanByName(clanKey);
            
            if (clanOpt.isPresent()) {
                String clanName = clanOpt.get().getName();
                inventory.setItem(13, new ItemBuilder(Material.PAPER)
                        .name("<yellow>Invito da <gold>" + clanName)
                        .lore(
                                "",
                                "<green>Click Sinistro per accettare",
                                "<red>Shift-Click per rifiutare"
                        )
                        .build());
            } else {
                // Il clan non esiste più, puliamo l'invito
                service.declineInvite(player.getUniqueId());
                inventory.setItem(13, new ItemBuilder(Material.BARRIER)
                        .name("<red>L'invito è scaduto")
                        .lore("<gray>Il clan che ti aveva", "<gray>invitato non esiste più.")
                        .build());
            }
        }

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

        if (slot == 0) {
            new NoClanGui(service, messages, inputManager).open(player);
            return;
        }

        if (slot == 13) {
            Optional<String> pendingInvite = service.getPendingInvite(player.getUniqueId());
            if (pendingInvite.isPresent()) {
                if (event.isLeftClick() && !event.isShiftClick()) {
                    // Accetta
                    player.closeInventory();
                    ClanService.AcceptInviteResult res = service.acceptInvite(player.getUniqueId());
                    if (res == ClanService.AcceptInviteResult.OK) {
                        player.sendMessage(messages.get("success.invite-accepted"));
                        new MainClanGui(service, messages, inputManager).open(player);
                    } else if (res == ClanService.AcceptInviteResult.CLAN_NO_LONGER_EXISTS) {
                        player.sendMessage(messages.get("error.clan-no-longer-exists"));
                    } else if (res == ClanService.AcceptInviteResult.ALREADY_IN_CLAN) {
                        player.sendMessage(messages.get("error.already-in-clan"));
                    } else {
                        player.sendMessage(messages.get("error.no-invite"));
                    }
                } else if (event.isShiftClick() || event.isRightClick()) {
                    // Rifiuta
                    if (service.declineInvite(player.getUniqueId())) {
                        player.sendMessage(messages.get("success.invite-declined"));
                        // Ricarica la pagina per mostrare che non ci sono più inviti
                        open(player);
                    }
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
