package it.battleforge.clans.gui.impl;

import it.battleforge.clans.gui.Gui;
import it.battleforge.clans.gui.ItemBuilder;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.model.Clan;
import it.battleforge.clans.model.ClanPermission;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.util.InputManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;
import java.util.UUID;

public class MemberManageGui implements Gui {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;
    private final UUID targetId;
    private Inventory inventory;

    public MemberManageGui(ClanService service, MessageManager messages, InputManager inputManager, UUID targetId) {
        this.service = service;
        this.messages = messages;
        this.inputManager = inputManager;
        this.targetId = targetId;
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
        boolean canKick = isLeader || service.hasPermission(player.getUniqueId(), ClanPermission.KICK);

        // Non si può gestire se stessi in questa GUI
        if (targetId.equals(player.getUniqueId())) {
            new MembersGui(service, messages, inputManager).open(player);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        String roleDisplay = service.getRoleDisplay(targetId);

        this.inventory = Bukkit.createInventory(this, 27, MiniMessage.miniMessage().deserialize("<dark_gray>Gestione " + target.getName()));

        // Torna indietro
        inventory.setItem(0, new ItemBuilder(Material.ARROW)
                .name("<yellow>Torna Indietro")
                .build());

        // Info giocatore (al centro sopra)
        inventory.setItem(4, new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(target)
                .name("<yellow>" + target.getName())
                .lore("<gray>Ruolo: <white>" + roleDisplay)
                .build());

        // Modifica ruolo (solo leader)
        if (isLeader) {
            inventory.setItem(11, new ItemBuilder(Material.NAME_TAG)
                    .name("<aqua><bold>Modifica Ruolo")
                    .lore("<gray>Clicca per assegnare", "<gray>un nuovo ruolo a questo giocatore.")
                    .build());
        }

        // Espelli membro (se si ha il permesso e il target non è il leader)
        boolean isTargetLeader = clan.getLeader().equals(targetId);
        if (canKick && !isTargetLeader) {
            inventory.setItem(15, new ItemBuilder(Material.BARRIER)
                    .name("<red><bold>Espelli Membro")
                    .lore("<gray>Clicca per espellere", "<gray>questo giocatore dal clan.")
                    .build());
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

        Optional<Clan> clanOpt = service.getClanByPlayer(player.getUniqueId());
        if (clanOpt.isEmpty()) {
            player.closeInventory();
            return;
        }

        Clan clan = clanOpt.get();
        boolean isLeader = clan.getLeader().equals(player.getUniqueId());
        boolean canKick = isLeader || service.hasPermission(player.getUniqueId(), ClanPermission.KICK);
        boolean isTargetLeader = clan.getLeader().equals(targetId);

        switch (slot) {
            case 0 -> {
                new MembersGui(service, messages, inputManager).open(player);
            }
            case 11 -> {
                if (isLeader) {
                    player.closeInventory();
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<white>Scrivi in chat il nome del ruolo da assegnare a <yellow>" + Bukkit.getOfflinePlayer(targetId).getName() + "<white>, oppure scrivi <red>annulla<white>."));
                    
                    inputManager.requestInput(player, (input) -> {
                        if (input.equalsIgnoreCase("annulla")) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Operazione annullata."));
                            return;
                        }
                        
                        ClanService.AssignRoleResult res = service.assignRole(player.getUniqueId(), targetId, input);
                        switch (res) {
                            case OK -> player.sendMessage(messages.get("success.role-set", "role", input));
                            case ROLE_NOT_FOUND -> player.sendMessage(messages.get("error.role-not-found"));
                            case NOT_LEADER -> player.sendMessage(messages.get("error.no-permission"));
                        }
                    });
                }
            }
            case 15 -> {
                if (canKick && !isTargetLeader) {
                    player.closeInventory();
                    ClanService.KickResult res = service.kick(player.getUniqueId(), targetId);
                    if (res == ClanService.KickResult.OK) {
                        Player targetPlayer = Bukkit.getPlayer(targetId);
                        if (targetPlayer != null) {
                            targetPlayer.sendMessage(messages.get("error.kicked", "clan", clanOpt.get().getName()));
                        }
                        player.sendMessage(messages.get("success.player-kicked", "player", Bukkit.getOfflinePlayer(targetId).getName()));
                        // Torna alla lista membri dopo il kick
                        new MembersGui(service, messages, inputManager).open(player);
                    } else if (res == ClanService.KickResult.NOT_LEADER) {
                        player.sendMessage(messages.get("error.not-leader"));
                    } else if (res == ClanService.KickResult.CANNOT_KICK_SELF) {
                        player.sendMessage(messages.get("error.cannot-kick-self"));
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
