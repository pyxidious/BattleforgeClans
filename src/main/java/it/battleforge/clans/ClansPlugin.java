package it.battleforge.clans;

import org.bukkit.plugin.java.JavaPlugin;

import it.battleforge.clans.command.ClansCommand;

public final class ClansPlugin extends JavaPlugin {

    private static ClansPlugin instance;

    public static ClansPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getCommand("clans").setExecutor(new ClansCommand());

        getLogger().info("Clans è stato abilitato correttamente!");
    }

    @Override
    public void onDisable() {
        // Qui in futuro chiuderemo database o salveremo dati

        getLogger().info("Clans è stato disabilitato!");
        instance = null;
    }
}