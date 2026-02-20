package it.battleforge.clans;

import it.battleforge.clans.command.ClansCommand;
import it.battleforge.clans.service.ClanService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ClansPlugin extends JavaPlugin {

    private ClanService clanService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.clanService = new ClanService();

        Objects.requireNonNull(getCommand("clans"), "Command 'clans' missing in plugin.yml")
                .setExecutor(new ClansCommand(clanService));

        getLogger().info("Clans abilitato!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Clans disabilitato!");
    }
}