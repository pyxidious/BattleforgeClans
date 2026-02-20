package it.battleforge.clans;

import it.battleforge.clans.command.ClansCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ClansPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Objects.requireNonNull(getCommand("clans"), "Comando 'clans' mancante in plugin.yml")
                .setExecutor(new ClansCommand());

        getLogger().info("Clans abilitato!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Clans disabilitato!");
    }
}