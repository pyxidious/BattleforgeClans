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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public class SettingsGui implements Gui {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;
    private Inventory inventory;

    public SettingsGui(ClanService service, MessageManager messages, InputManager inputManager) {
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
        boolean canSetHome = isLeader || service.hasPermission(player.getUniqueId(), ClanPermission.SET_HOME);

        this.inventory = Bukkit.createInventory(this, 27, MiniMessage.miniMessage().deserialize("<dark_gray>Impostazioni Clan"));

        // Torna indietro
        inventory.setItem(0, new ItemBuilder(Material.ARROW)
                .name("<yellow>Torna Indietro")
                .build());

        // Vai in Home
        inventory.setItem(11, new ItemBuilder(Material.ENDER_PEARL)
                .name("<light_purple><bold>Vai in Home")
                .lore("<gray>Teletrasportati alla base", "<gray>del clan.")
                .build());

        // Sethome
        if (canSetHome) {
            inventory.setItem(13, new ItemBuilder(Material.CAMPFIRE)
                    .name("<gold><bold>Imposta Home")
                    .lore("<gray>Clicca per impostare la home", "<gray>nella tua posizione attuale.")
                    .build());
        }

        // Abbandona clan
        if (!isLeader) {
            inventory.setItem(15, new ItemBuilder(Material.OAK_DOOR)
                    .name("<red><bold>Abbandona Clan")
                    .lore("<gray>Clicca per uscire dal clan.")
                    .build());
        }

        // Elimina Clan o Trasferisci
        if (isLeader) {
            inventory.setItem(22, new ItemBuilder(Material.TNT)
                    .name("<dark_red><bold>Elimina Clan")
                    .lore("<gray>Clicca per distruggere", "<gray>definitivamente il clan.", "<dark_red>Questa azione è irreversibile!")
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

        switch (slot) {
            case 0 -> {
                new MainClanGui(service, messages, inputManager).open(player);
            }
            case 11 -> {
                player.closeInventory();
                ClanService.HomeTpResult res = service.canTeleportHome(player.getUniqueId());
                if (res == ClanService.HomeTpResult.OK) {
                    player.teleportAsync(service.getHomeLocation(player.getUniqueId()))
                            .thenAccept(success -> {
                                if (success) player.sendMessage(messages.get("success.home-teleport"));
                            });
                } else if (res == ClanService.HomeTpResult.HOME_NOT_SET) {
                    player.sendMessage(messages.get("error.home-not-set"));
                }
            }
            case 13 -> {
                if (isLeader || service.hasPermission(player.getUniqueId(), ClanPermission.SET_HOME)) {
                    player.closeInventory();
                    ClanService.SetHomeResult res = service.setHome(player.getUniqueId(), player.getLocation());
                    if (res == ClanService.SetHomeResult.OK) {
                        player.sendMessage(messages.get("success.home-set"));
                    } else {
                        player.sendMessage(messages.get("error.no-permission"));
                    }
                }
            }
            case 15 -> {
                if (!isLeader) {
                    player.closeInventory();
                    ClanService.LeaveResult res = service.leave(player.getUniqueId());
                    if (res == ClanService.LeaveResult.OK) {
                        player.sendMessage(messages.get("success.left-clan"));
                    }
                }
            }
            case 22 -> {
                if (isLeader) {
                    player.closeInventory();
                    ClanService.DeleteResult res = service.deleteClan(player.getUniqueId());
                    if (res == ClanService.DeleteResult.OK) {
                        player.sendMessage(messages.get("success.clan-deleted"));
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
