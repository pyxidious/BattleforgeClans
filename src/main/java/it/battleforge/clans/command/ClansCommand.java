package it.battleforge.clans.command;

import it.battleforge.clans.ClansPlugin;
import it.battleforge.clans.gui.impl.NoClanGui;
import it.battleforge.clans.message.MessageManager;
import it.battleforge.clans.service.ClanService;
import it.battleforge.clans.util.InputManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClansCommand implements CommandExecutor, TabCompleter {

    private final ClansPlugin plugin;
    private final ClanService service;
    private final MessageManager messages;
    private final InputManager inputManager;

    public ClansCommand(ClansPlugin plugin, ClanService service, MessageManager messages, InputManager inputManager) {
        this.plugin = plugin;
        this.service = service;
        this.messages = messages;
        this.inputManager = inputManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String sub = args[0].toLowerCase();

            if (sub.equals("save")) {
                if (!sender.hasPermission("clans.admin.save")) {
                    sender.sendMessage(messages.get("error.no-permission"));
                    return true;
                }
                plugin.saveClanData();
                sender.sendMessage(messages.get("success.data-saved"));
                return true;
            }

            if (sub.equals("reload")) {
                if (!sender.hasPermission("clans.admin.reload")) {
                    sender.sendMessage(messages.get("error.no-permission"));
                    return true;
                }
                plugin.reloadClanData();
                sender.sendMessage(messages.get("success.data-reloaded"));
                return true;
            }

            if (sub.equals("autosave")) {
                if (!sender.hasPermission("clans.admin.autosave")) {
                    sender.sendMessage(messages.get("error.no-permission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(messages.get("error.usage-clans-autosave"));
                    return true;
                }

                String action = args[1].toLowerCase();
                if (action.equals("status")) {
                    String status = plugin.isAutoSaveEnabled() ? "ON" : "OFF";
                    sender.sendMessage(messages.get("success.autosave-status",
                            java.util.Map.of(
                                    "status", status,
                                    "seconds", Integer.toString(plugin.getAutoSaveIntervalSeconds())
                            )));
                    return true;
                }

                if (action.equals("off")) {
                    plugin.setAutoSave(false, plugin.getAutoSaveIntervalSeconds());
                    sender.sendMessage(messages.get("success.autosave-disabled"));
                    return true;
                }

                if (action.equals("on")) {
                    int seconds = plugin.getAutoSaveIntervalSeconds();
                    if (args.length >= 3) {
                        try {
                            seconds = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(messages.get("error.autosave-invalid-seconds"));
                            return true;
                        }
                    }

                    if (seconds < 30) {
                        sender.sendMessage(messages.get("error.autosave-seconds-min"));
                        return true;
                    }

                    plugin.setAutoSave(true, seconds);
                    sender.sendMessage(messages.get("success.autosave-enabled",
                            java.util.Map.of("seconds", Integer.toString(seconds))));
                    return true;
                }

                sender.sendMessage(messages.get("error.usage-clans-autosave"));
                return true;
            }

            if (sub.equals("home")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(messages.get("error.player-only"));
                    return true;
                }

                if (!player.hasPermission("clans.command.home")) {
                    player.sendMessage(messages.get("error.no-permission"));
                    return true;
                }

                ClanService.HomeTpResult res = service.canTeleportHome(player.getUniqueId());
                if (res == ClanService.HomeTpResult.NO_PERMISSION) {
                    player.sendMessage(messages.get("error.no-permission"));
                    return true;
                }
                if (res == ClanService.HomeTpResult.HOME_NOT_SET) {
                    player.sendMessage(messages.get("error.home-not-set"));
                    return true;
                }
                if (res == ClanService.HomeTpResult.NOT_IN_CLAN) {
                    player.sendMessage(messages.get("error.not-in-clan"));
                    return true;
                }

                int combatSeconds = plugin.getHomeCombatSeconds();
                long remaining = plugin.getCombatManager().getRemainingSeconds(player.getUniqueId(), combatSeconds);
                if (remaining > 0) {
                    player.sendMessage(messages.get("error.home-combat-cooldown", "seconds", Long.toString(remaining)));
                    return true;
                }

                player.teleportAsync(service.getHomeLocation(player.getUniqueId()))
                        .thenAccept(success -> {
                            if (success) player.sendMessage(messages.get("success.home-teleport"));
                        });
                return true;
            }
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("error.usage-clans-admin"));
            return true;
        }

        if (service.isInClan(player.getUniqueId())) {
            it.battleforge.clans.gui.impl.MainClanGui gui = new it.battleforge.clans.gui.impl.MainClanGui(service, messages, inputManager);
            gui.open(player);
        } else {
            NoClanGui gui = new NoClanGui(service, messages, inputManager);
            gui.open(player);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            String prefix = args[0].toLowerCase();

            if (sender.hasPermission("clans.admin.save") && "save".startsWith(prefix)) out.add("save");
            if (sender.hasPermission("clans.admin.reload") && "reload".startsWith(prefix)) out.add("reload");
            if (sender.hasPermission("clans.admin.autosave") && "autosave".startsWith(prefix)) out.add("autosave");
            if (sender.hasPermission("clans.command.home") && "home".startsWith(prefix)) out.add("home");
            return out;
        }

        if (args.length == 2 && "autosave".equalsIgnoreCase(args[0]) && sender.hasPermission("clans.admin.autosave")) {
            String prefix = args[1].toLowerCase();
            List<String> out = new ArrayList<>();
            if ("on".startsWith(prefix)) out.add("on");
            if ("off".startsWith(prefix)) out.add("off");
            if ("status".startsWith(prefix)) out.add("status");
            return out;
        }

        if (args.length == 3
                && "autosave".equalsIgnoreCase(args[0])
                && "on".equalsIgnoreCase(args[1])
                && sender.hasPermission("clans.admin.autosave")) {
            return List.of("30", "60", "120", "300");
        }

        return Collections.emptyList();
    }
}
