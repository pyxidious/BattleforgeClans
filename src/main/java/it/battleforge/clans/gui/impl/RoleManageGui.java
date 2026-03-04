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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RoleManageGui implements Gui {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;
    private final String roleKey;
    private Inventory inventory;
    private final Map<Integer, ClanPermission> permissionBySlot = new LinkedHashMap<>();

    public RoleManageGui(ClanService service, MessageManager messages, InputManager inputManager, String roleKey) {
        this.service = service;
        this.messages = messages;
        this.inputManager = inputManager;
        this.roleKey = roleKey.toLowerCase();
    }

    @Override
    public void open(Player player) {
        Optional<Clan> clanOpt = service.getClanByPlayer(player.getUniqueId());
        if (clanOpt.isEmpty()) {
            player.sendMessage(messages.get("error.not-in-clan"));
            return;
        }

        Clan clan = clanOpt.get();
        if (!clan.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(messages.get("error.not-leader"));
            return;
        }

        ClanRole role = clan.getRoles().get(roleKey);
        if (role == null) {
            player.sendMessage(messages.get("error.role-not-found"));
            new RolesGui(service, messages, inputManager).open(player);
            return;
        }

        this.inventory = Bukkit.createInventory(this, 36,
                MiniMessage.miniMessage().deserialize("<dark_gray>Gestione Ruolo: <yellow>" + role.getName()));

        inventory.setItem(0, new ItemBuilder(Material.ARROW).name("<yellow>Torna ai Ruoli").build());
        inventory.setItem(4, new ItemBuilder(Material.NAME_TAG)
                .name("<aqua><bold>" + role.getName())
                .lore(
                        "<gray>Rango attuale: <white>" + role.getWeight(),
                        "<gray>Rango massimo: <white>" + ClanService.MAX_ROLE_WEIGHT,
                        "<gray>Il capo clan resta sempre sopra ogni ruolo."
                )
                .build());

        permissionBySlot.clear();
        int slot = 10;
        for (ClanPermission permission : ClanPermission.values()) {
            if (slot > 16) break;

            boolean enabled = role.has(permission);
            Material wool = enabled ? Material.LIME_WOOL : Material.RED_WOOL;
            inventory.setItem(slot, new ItemBuilder(wool)
                    .name("<yellow>" + permission.name())
                    .lore(
                            enabled ? "<green>Stato: ATTIVO" : "<red>Stato: DISATTIVO",
                            "<gray>" + describePermission(permission),
                            "",
                            "<dark_gray>Click: <gray>Attiva/Disattiva"
                    )
                    .build());
            permissionBySlot.put(slot, permission);
            slot += 2;
        }

        inventory.setItem(30, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name("<red><bold>Diminuisci Rango")
                .lore("<gray>Riduce di 1 il rango del ruolo.")
                .build());

        inventory.setItem(32, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name("<green><bold>Aumenta Rango")
                .lore("<gray>Aumenta di 1 il rango del ruolo.")
                .build());

        if ("membro".equals(roleKey)) {
            inventory.setItem(31, new ItemBuilder(Material.BARRIER)
                    .name("<red>Rango bloccato")
                    .lore("<gray>Il ruolo base non puo cambiare gerarchia.")
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

        if (slot == 0) {
            new RolesGui(service, messages, inputManager).open(player);
            return;
        }

        Optional<Clan> clanOpt = service.getClanByPlayer(player.getUniqueId());
        if (clanOpt.isEmpty()) {
            player.closeInventory();
            return;
        }

        Clan clan = clanOpt.get();
        ClanRole role = clan.getRoles().get(roleKey);
        if (role == null) {
            player.sendMessage(messages.get("error.role-not-found"));
            new RolesGui(service, messages, inputManager).open(player);
            return;
        }

        if (permissionBySlot.containsKey(slot)) {
            ClanPermission permission = permissionBySlot.get(slot);
            boolean newEnabled = !role.has(permission);
            ClanService.RolePermResult result = service.setRolePermission(
                    player.getUniqueId(),
                    roleKey,
                    permission,
                    newEnabled
            );

            if (result == ClanService.RolePermResult.OK) {
                if (newEnabled) {
                    player.sendMessage(messages.get("success.permission-enabled", java.util.Map.of("permission", permission.name())));
                } else {
                    player.sendMessage(messages.get("success.permission-disabled", java.util.Map.of("permission", permission.name())));
                }
            } else if (result == ClanService.RolePermResult.NOT_LEADER) {
                player.sendMessage(messages.get("error.not-leader"));
            } else {
                player.sendMessage(messages.get("error.role-not-found"));
            }

            open(player);
            return;
        }

        if (slot == 30 || slot == 32) {
            int delta = (slot == 32) ? 1 : -1;
            ClanService.RoleWeightResult result = service.adjustRoleWeight(player.getUniqueId(), roleKey, delta);

            if (result == ClanService.RoleWeightResult.OK) {
                int newWeight = clan.getRoles().get(roleKey).getWeight();
                player.sendMessage(messages.get("success.role-weight-updated",
                        java.util.Map.of(
                                "role", clan.getRoles().get(roleKey).getName(),
                                "weight", Integer.toString(newWeight)
                        )));
            } else if (result == ClanService.RoleWeightResult.ROLE_LOCKED) {
                player.sendMessage(messages.get("error.role-weight-locked"));
            } else if (result == ClanService.RoleWeightResult.MIN_WEIGHT_REACHED) {
                player.sendMessage(messages.get("error.role-weight-min"));
            } else if (result == ClanService.RoleWeightResult.MAX_WEIGHT_REACHED) {
                player.sendMessage(messages.get("error.role-weight-max", "max", Integer.toString(ClanService.MAX_ROLE_WEIGHT)));
            } else if (result == ClanService.RoleWeightResult.NOT_LEADER) {
                player.sendMessage(messages.get("error.not-leader"));
            } else {
                player.sendMessage(messages.get("error.role-not-found"));
            }

            open(player);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private String describePermission(ClanPermission permission) {
        return switch (permission) {
            case SET_HOME -> "Permette di impostare la home del clan.";
            case TP_HOME -> "Permette di teletrasportarsi alla home del clan.";
            case KICK -> "Permette di espellere membri di rango inferiore.";
        };
    }
}
