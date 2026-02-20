package it.battleforge.clans.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Messages {
    private Messages() {}

    private static final Component PREFIX =
            Component.text("Battleforge Clans > ").color(NamedTextColor.GOLD);

    public static Component ok(String text) {
        return PREFIX.append(Component.text(text).color(NamedTextColor.GREEN));
    }

    public static Component info(String text) {
        return PREFIX.append(Component.text(text).color(NamedTextColor.GRAY));
    }

    public static Component warn(String text) {
        return PREFIX.append(Component.text(text).color(NamedTextColor.YELLOW));
    }

    public static Component err(String text) {
        return PREFIX.append(Component.text(text).color(NamedTextColor.RED));
    }
}