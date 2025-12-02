package pl.barpad.duckyzones;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import pl.barpad.duckyzones.listeners.ZoneListener;
import pl.barpad.duckyzones.managers.ConfigManager;
import pl.barpad.duckyzones.managers.MessagesManager;
import pl.barpad.duckyzones.managers.ZoneManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reload extends AbstractCommand implements TabExecutor {

    private final Main plugin;
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final ZoneManager zoneManager;
    private final ZoneListener zoneListener;

    public Reload(Main plugin, ConfigManager configManager, MessagesManager messagesManager, ZoneManager zoneManager, ZoneListener zoneListener) {
        super("duckyzones", "/duckyzones reload", "Reload command");
        this.plugin = plugin;
        this.configManager = configManager;
        this.messagesManager = messagesManager;
        this.zoneManager = zoneManager;
        this.zoneListener = zoneListener;
        this.register();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("duckyzones.reload")) {
                sender.sendMessage(color(messagesManager.getMessage("no-permission")));
                return true;
            }

            plugin.reloadConfig();
            configManager.reloadConfig();
            messagesManager.reload();
            zoneManager.loadZones();
            zoneListener.refreshZones();

            sender.sendMessage(color(messagesManager.getMessage("reload-success")));
            return true;
        }

        sender.sendMessage(color(messagesManager.getMessage("usage")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("duckyzones.reload")) {
                return Collections.singletonList("reload");
            }
        }
        return new ArrayList<>();
    }

    private String color(String message) {
        return message == null ? "" : message.replace("&", "§");
    }
}