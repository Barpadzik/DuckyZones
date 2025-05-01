package pl.barpad.duckyzones;

import org.bukkit.plugin.java.JavaPlugin;
import pl.barpad.duckyzones.listeners.ZoneListener;
import pl.barpad.duckyzones.managers.ConfigManager;
import pl.barpad.duckyzones.managers.MessagesManager;
import pl.barpad.duckyzones.managers.ZoneManager;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        int serviceId = 25524;
        new MetricsLite(this, serviceId);
        ConfigManager configManager = new ConfigManager(this);
        ZoneManager zoneManager = new ZoneManager(this);
        MessagesManager messagesManager = new MessagesManager(this);
        ZoneListener zoneListener = new ZoneListener(this, zoneManager, messagesManager);
        getServer().getPluginManager().registerEvents(new ZoneListener(this, zoneManager, messagesManager), this);
        new Reload(this, configManager, messagesManager, zoneManager, zoneListener);
        new UpdateChecker(this).checkForUpdates();
        getLogger().info("DuckyZones Enabled | Author: Barpad!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DuckyZones Disabled | Author: Barpad");
    }
}