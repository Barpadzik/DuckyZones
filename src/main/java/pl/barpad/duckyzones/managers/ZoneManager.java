package pl.barpad.duckyzones.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;
import pl.barpad.duckyzones.Main;
import pl.barpad.duckyzones.model.AfkReward;
import pl.barpad.duckyzones.model.Zone;

import java.util.*;

public class ZoneManager {

    private final Main plugin;
    private final List<Zone> zones = new ArrayList<>();

    public ZoneManager(Main plugin) {
        this.plugin = plugin;
        loadZones();
    }

    public void loadZones() {
        zones.clear();
        FileConfiguration config = plugin.getConfig();

        if (!config.isConfigurationSection("zones")) return;

        for (String zoneName : Objects.requireNonNull(config.getConfigurationSection("zones")).getKeys(false)) {
            String path = "zones." + zoneName;

            try {
                String worldName = config.getString(path + ".world");
                if (worldName == null) {
                    plugin.getLogger().warning("Missing world for zone: " + zoneName);
                    continue;
                }

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("World \"" + worldName + "\" not found for zone: " + zoneName);
                    continue;
                }

                String rawCorner1 = config.getString(path + ".corner1");
                String rawCorner2 = config.getString(path + ".corner2");

                if (rawCorner1 == null || rawCorner2 == null) {
                    plugin.getLogger().warning("Missing corner1 or corner2 for zone: " + zoneName);
                    continue;
                }

                Location corner1 = parseLocation(rawCorner1, world);
                Location corner2 = parseLocation(rawCorner2, world);

                List<Material> blockedItems = new ArrayList<>();
                for (String item : config.getStringList(path + ".blocked-items")) {
                    Material material = Material.matchMaterial(item.toUpperCase());
                    if (material != null) blockedItems.add(material);
                }

                List<Material> requiredItems = new ArrayList<>();
                for (String item : config.getStringList(path + ".required-items")) {
                    Material material = Material.matchMaterial(item.toUpperCase());
                    if (material != null) requiredItems.add(material);
                }

                Map<PotionEffectType, Integer> blockedEffects = new HashMap<>();
                for (String effectString : config.getStringList(path + ".blocked-effects")) {
                    String[] parts = effectString.split(";");
                    if (parts.length == 2) {
                        PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                        int amplifier = Integer.parseInt(parts[1]);
                        if (type != null) {
                            blockedEffects.put(type, amplifier);
                        }
                    }
                }

                Map<PotionEffectType, Integer> requiredEffects = new HashMap<>();
                for (String effectString : config.getStringList(path + ".required-effects")) {
                    String[] parts = effectString.split(";");
                    if (parts.length == 2) {
                        PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                        int amplifier = Integer.parseInt(parts[1]);
                        if (type != null) {
                            requiredEffects.put(type, amplifier);
                        }
                    }
                }

                Map<PotionEffectType, Integer> zoneEffects = new HashMap<>();
                for (String effectString : config.getStringList(path + ".zone-effects")) {
                    String[] parts = effectString.split(";");
                    if (parts.length == 2) {
                        PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                        try {
                            int amplifier = Integer.parseInt(parts[1]);
                            if (type != null) {
                                zoneEffects.put(type, Math.max(0, amplifier - 1));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                boolean elytraDisabled = config.getBoolean(path + ".elytra-disabled", false);
                double minHealth = config.getDouble(path + ".min-health", 0);
                double maxHealth = config.getDouble(path + ".max-health", 20);
                int minLevel = config.getInt(path + ".min-level", 0);
                int maxLevel = config.getInt(path + ".max-level", 100);
                String message = config.getString(path + ".deny-message", "&cYou cannot enter this zone!");

                boolean isAfkZone = config.getBoolean(path + ".afk-zone.enabled", false);
                List<AfkReward> afkRewards = new ArrayList<>();

                if (isAfkZone && config.isConfigurationSection(path + ".afk-zone.rewards")) {
                    for (String rewardKey : Objects.requireNonNull(config.getConfigurationSection(path + ".afk-zone.rewards")).getKeys(false)) {
                        String rewardPath = path + ".afk-zone.rewards." + rewardKey;
                        int interval = config.getInt(rewardPath + ".interval", 60);
                        double defaultChance = config.getDouble(rewardPath + ".chance", 100.0);
                        List<String> commands = config.getStringList(rewardPath + ".commands");
                        String bossbarFormat = config.getString(rewardPath + ".bossbar-format", "&cReward in {time}");

                        Map<String, Double> permissionChances = new HashMap<>();
                        if (config.isConfigurationSection(rewardPath + ".permission-chances")) {
                            for (String permKey : Objects.requireNonNull(config.getConfigurationSection(rewardPath + ".permission-chances")).getKeys(false)) {
                                double permChance = config.getDouble(rewardPath + ".permission-chances." + permKey, 100.0);
                                permissionChances.put(permKey, permChance);
                            }
                        }

                        afkRewards.add(new AfkReward(interval, commands, defaultChance, permissionChances, bossbarFormat, rewardKey));
                    }
                }

                String afkActionbarFormat = config.getString(path + ".afk-zone.actionbar-format", "&eRewards: {rewards}");

                Zone zone = new Zone(
                        zoneName,
                        corner1, corner2,
                        blockedItems, blockedEffects,
                        requiredItems, requiredEffects,
                        minHealth, maxHealth,
                        minLevel, maxLevel,
                        message,
                        zoneEffects,
                        elytraDisabled,
                        isAfkZone,
                        afkRewards,
                        afkActionbarFormat
                );
                zones.add(zone);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load zone: " + zoneName + " → " + e.getMessage());
            }
        }
    }

    private Location parseLocation(String raw, World world) {
        String[] split = raw.split(",");
        if (split.length != 4) throw new IllegalArgumentException("Invalid location format: " + raw);

        return new Location(
                world,
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3])
        );
    }

    public List<Zone> getZones() {
        return zones;
    }
}