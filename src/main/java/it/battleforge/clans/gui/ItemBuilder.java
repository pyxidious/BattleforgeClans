package it.battleforge.clans.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String mmString) {
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(mmString).decoration(TextDecoration.ITALIC, false));
        }
        return this;
    }

    public ItemBuilder lore(String... mmStrings) {
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String s : mmStrings) {
                lore.add(miniMessage.deserialize(s).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder lore(List<String> mmStrings) {
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String s : mmStrings) {
                lore.add(miniMessage.deserialize(s).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder flag(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        if (glow && meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder customModelData(int data) {
        if (meta != null) {
            meta.setCustomModelData(data);
        }
        return this;
    }

    public ItemBuilder skullOwner(OfflinePlayer player) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
