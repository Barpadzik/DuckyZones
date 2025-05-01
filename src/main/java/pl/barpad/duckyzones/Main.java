package pl.barpad.duckyzones;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import pl.barpad.duckyzones.listeners.ZoneListener;
import pl.barpad.duckyzones.managers.ConfigManager;
import pl.barpad.duckyzones.managers.MessagesManager;
import pl.barpad.duckyzones.managers.ZoneManager;
import pl.barpad.duckyzones.model.Zone;

public class Main extends JavaPlugin {

    private ZoneManager zoneManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        int serviceId = 25524;
        new MetricsLite(this, serviceId);

        ConfigManager configManager = new ConfigManager(this);
        this.zoneManager = new ZoneManager(this);
        MessagesManager messagesManager = new MessagesManager(this);
        ZoneListener zoneListener = new ZoneListener(this, zoneManager, messagesManager);

        getServer().getPluginManager().registerEvents(zoneListener, this);
        new Reload(this, configManager, messagesManager, zoneManager, zoneListener);
        new UpdateChecker(this).checkForUpdates();

        getLogger().info("DuckyZones Enabled | Author: Barpad!");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Zone zone : zoneManager.getZones()) {
                if (zone.isInside(player.getLocation())) {
                    for (PotionEffectType type : zone.getZoneEffects().keySet()) {
                        player.removePotionEffect(type);
                    }
                }
            }
        }
    }
}