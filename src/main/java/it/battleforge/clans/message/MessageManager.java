package it.battleforge.clans.message;

import it.battleforge.clans.ClansPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Objects;

public final class MessageManager {

    private final ClansPlugin plugin;
    private FileConfiguration config;
    private final MiniMessage mini = MiniMessage.miniMessage();

    public MessageManager(ClansPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public Component get(String path) {
        String prefix = Objects.requireNonNull(config.getString("prefix", ""));
        String message = config.getString(path, "<red>Messaggio non trovato: " + path);

        return mini.deserialize(prefix + message);
    }

    public Component get(String path, String placeholder, String value) {
        String prefix = Objects.requireNonNull(config.getString("prefix", ""));
        String message = config.getString(path, "<red>Messaggio non trovato: " + path);

        message = message.replace("{" + placeholder + "}", value);

        return mini.deserialize(prefix + message);
    }

    public String getRawPrefix() {
        return config.getString("prefix", "");
    }

    public String getRaw(String path) {
        return config.getString(path, "<red>Messaggio non trovato: " + path);
    }

    public java.util.List<String> getStringList(String path) {
        return config.getStringList(path);
    }
}