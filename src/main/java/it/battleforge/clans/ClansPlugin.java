package it.battleforge.clans;

import it.battleforge.clans.command.ClansCommand;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.util.CombatManager;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class ClansPlugin extends JavaPlugin {
    private static ClansPlugin instance;

    private ClanService clanService;
    private MessageManager messageManager;
    private it.battleforge.clans.util.InputManager inputManager;
    private File dataFile;
    private BukkitTask autoSaveTask;
    private boolean autoSaveEnabled;
    private int autoSaveIntervalSeconds;
    private CombatManager combatManager;
    private int homeCombatSeconds;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);

        this.messageManager = new MessageManager(this);
        this.clanService = new ClanService();
        this.inputManager = new it.battleforge.clans.util.InputManager();
        this.dataFile = new File(getDataFolder(), "clans-data.yml");
        this.autoSaveEnabled = getConfig().getBoolean("autosave.enabled", false);
        this.autoSaveIntervalSeconds = Math.max(30, getConfig().getInt("autosave.interval-seconds", 300));
        this.homeCombatSeconds = Math.max(1, getConfig().getInt("home.teleport-combat-seconds", 20));
        this.combatManager = new CombatManager();

        clanService.loadFromFile(dataFile);

        if (autoSaveEnabled) {
            startAutoSaveTask();
        }

        ClansCommand clansCommand = new ClansCommand(this, clanService, messageManager, inputManager);
        Objects.requireNonNull(getCommand("clans"), "Command 'clans' missing in plugin.yml")
                .setExecutor(clansCommand);
        Objects.requireNonNull(getCommand("clans"), "Command 'clans' missing in plugin.yml")
                .setTabCompleter(clansCommand);

        getServer().getPluginManager().registerEvents(
                new it.battleforge.clans.listener.ChatListener(clanService, messageManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new it.battleforge.clans.gui.GuiManager(),
                this
        );
        getServer().getPluginManager().registerEvents(
                inputManager,
                this
        );
        getServer().getPluginManager().registerEvents(
                new it.battleforge.clans.listener.CombatListener(combatManager),
                this
        );

        getLogger().info("Clans abilitato!");
    }

    @Override
    public void onDisable() {
        stopAutoSaveTask();
        saveClanData();
        getLogger().info("Clans disabilitato!");
    }

    public static ClansPlugin getInstance() {
        return instance;
    }

    public void reloadClanData() {
        if (clanService != null && dataFile != null) {
            clanService.loadFromFile(dataFile);
        }
    }

    public void saveClanData() {
        if (clanService != null && dataFile != null) {
            clanService.saveToFile(dataFile);
        }
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public int getAutoSaveIntervalSeconds() {
        return autoSaveIntervalSeconds;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public int getHomeCombatSeconds() {
        return homeCombatSeconds;
    }

    public void setAutoSave(boolean enabled, int intervalSeconds) {
        this.autoSaveEnabled = enabled;
        this.autoSaveIntervalSeconds = Math.max(30, intervalSeconds);

        getConfig().set("autosave.enabled", this.autoSaveEnabled);
        getConfig().set("autosave.interval-seconds", this.autoSaveIntervalSeconds);
        saveConfig();

        if (this.autoSaveEnabled) {
            startAutoSaveTask();
        } else {
            stopAutoSaveTask();
        }
    }

    private void startAutoSaveTask() {
        stopAutoSaveTask();
        long periodTicks = autoSaveIntervalSeconds * 20L;
        autoSaveTask = getServer().getScheduler().runTaskTimer(this, this::saveClanData, periodTicks, periodTicks);
    }

    private void stopAutoSaveTask() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
    }
}
