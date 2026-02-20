package it.battleforge.clans;

import it.battleforge.clans.command.ClansCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ClansPlugin extends JavaPlugin {

    private ClanService clanService;
    private MessageManager messageManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        saveResource("messages.yml", false);
        getServer().getPluginManager().registerEvents(new it.battleforge.clans.listener.ChatListener(clanService, messageManager), this);

        this.messageManager = new MessageManager(this);
        this.clanService = new ClanService();

        Objects.requireNonNull(getCommand("clans"))
                .setExecutor(new ClansCommand(clanService, messageManager));
    }

    @Override
    public void onDisable() {
        getLogger().info("Clans disabilitato!");
    }
}