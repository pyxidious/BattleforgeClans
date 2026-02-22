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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MembersGui implements Gui {

    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;
    private Inventory inventory;
    private final List<UUID> membersList = new ArrayList<>();

    public MembersGui(ClanService service, MessageManager messages, InputManager inputManager) {
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
        boolean canKick = isLeader || service.hasPermission(player.getUniqueId(), ClanPermission.KICK);

        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize("<dark_gray>Membri del Clan"));

        // Torna indietro
        inventory.setItem(0, new ItemBuilder(Material.ARROW)
                .name("<yellow>Torna Indietro")
                .build());

        // Invita giocatore
        inventory.setItem(4, new ItemBuilder(Material.EMERALD)
                .name("<green><bold>Invita Giocatore")
                .lore("<gray>Clicca per invitare", "<gray>un nuovo membro nel clan.")
                .build());

        // Lista membri (inizia dallo slot 9)
        membersList.clear();
        membersList.addAll(clan.getMembers());
        
        // Ordina mettendo il leader per primo
        membersList.remove(clan.getLeader());
        membersList.add(0, clan.getLeader());

        int slot = 9;
        for (UUID memberId : membersList) {
            if (slot >= 53) break; // Limite slots

            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
            String roleDisplay = service.getRoleDisplay(memberId);
            boolean isMemberLeader = clan.getLeader().equals(memberId);

            ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD)
                    .skullOwner(member)
                    .name("<yellow>" + member.getName());

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Ruolo: <white>" + roleDisplay);
            
            if (!isMemberLeader && !memberId.equals(player.getUniqueId())) {
                if (isLeader || canKick) {
                    lore.add("");
                    lore.add("<blue>Click per gestire");
                }
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
        boolean canKick = isLeader || service.hasPermission(player.getUniqueId(), ClanPermission.KICK);
        
        if (slot == 0) {
            new MainClanGui(service, messages, inputManager).open(player);
            return;
        }

        if (slot == 4) {
            player.closeInventory();
            player.sendMessage(MiniMessage.miniMessage().deserialize("<white>Scrivi in chat il nome del giocatore da invitare, oppure <red>annulla<white>."));
            
            inputManager.requestInput(player, (input) -> {
                if (input.equalsIgnoreCase("annulla")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Operazione annullata."));
                    return;
                }
                
                Player target = Bukkit.getPlayer(input);
                if (target == null) {
                    player.sendMessage(messages.get("error.player-not-found"));
                    return;
                }
                
                ClanService.InviteResult res = service.invite(player.getUniqueId(), target.getUniqueId());
                switch (res) {
                    case OK -> {
                        player.sendMessage(messages.get("success.invite-sent", "player", target.getName()));
                        target.sendMessage(messages.get("info.invite-received", "clan", clanOpt.get().getName()));
                    }
                    case TARGET_ALREADY_IN_CLAN -> player.sendMessage(messages.get("error.target-already-in-clan"));
                    case TARGET_ALREADY_INVITED -> player.sendMessage(messages.get("error.target-already-invited"));
                    case NOT_LEADER -> player.sendMessage(messages.get("error.not-leader"));
                }
            });
            return;
        }

        // Click su una testa (Slot tra 9 e 53)
        if (slot >= 9 && slot < 9 + membersList.size()) {
            UUID targetId = membersList.get(slot - 9);
            
            boolean isMemberLeader = clan.getLeader().equals(targetId);

            if (!isMemberLeader && !targetId.equals(player.getUniqueId())) {
                if (isLeader || canKick) {
                    new MemberManageGui(service, messages, inputManager, targetId).open(player);
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
