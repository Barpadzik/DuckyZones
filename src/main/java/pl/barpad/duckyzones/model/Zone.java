package pl.barpad.duckyzones.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public class Zone {

    private final String name;
    private final Location corner1;
    private final Location corner2;
    private final List<Material> blockedItems;
    private final List<PotionEffectType> blockedEffects;
    private final List<Material> requiredItems;
    private final List<PotionEffectType> requiredEffects;
    private final boolean elytraDisabled;
    private final double minHealth;
    private final double maxHealth;
    private final int minLevel;
    private final int maxLevel;
    private final String denyMessage;
    private final Map<PotionEffectType, Integer> zoneEffects;

    public Zone(String name,
                Location corner1, Location corner2,
                List<Material> blockedItems, List<PotionEffectType> blockedEffects,
                List<Material> requiredItems, List<PotionEffectType> requiredEffects,
                double minHealth, double maxHealth,
                int minLevel, int maxLevel,
                String denyMessage,
                Map<PotionEffectType, Integer> zoneEffects,
                boolean elytraDisabled) {
        this.name = name;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.blockedItems = blockedItems;
        this.blockedEffects = blockedEffects;
        this.requiredItems = requiredItems;
        this.requiredEffects = requiredEffects;
        this.minHealth = minHealth;
        this.maxHealth = maxHealth;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.denyMessage = denyMessage;
        this.zoneEffects = zoneEffects;
        this.elytraDisabled = elytraDisabled;
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return corner1.getWorld();
    }


    public boolean isInside(Location loc) {
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= Math.min(corner1.getX(), corner2.getX()) && x <= Math.max(corner1.getX(), corner2.getX())
                && y >= Math.min(corner1.getY(), corner2.getY()) && y <= Math.max(corner1.getY(), corner2.getY())
                && z >= Math.min(corner1.getZ(), corner2.getZ()) && z <= Math.max(corner1.getZ(), corner2.getZ());
    }

    public int distanceFromBorder(Location loc) {
        Location min = getMin();
        Location max = getMax();

        int dx = Math.min(Math.abs(loc.getBlockX() - min.getBlockX()), Math.abs(loc.getBlockX() - max.getBlockX()));
        int dz = Math.min(Math.abs(loc.getBlockZ() - min.getBlockZ()), Math.abs(loc.getBlockZ() - max.getBlockZ()));

        return Math.min(dx, dz);
    }

    public Location getMin() {
        return new Location(corner1.getWorld(),
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ()));
    }

    public Location getMax() {
        return new Location(corner1.getWorld(),
                Math.max(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY()),
                Math.max(corner1.getZ(), corner2.getZ()));
    }

    public List<Material> getBlockedItems() {
        return blockedItems;
    }

    public List<PotionEffectType> getBlockedEffects() {
        return blockedEffects;
    }

    public List<Material> getRequiredItems() {
        return requiredItems;
    }

    public List<PotionEffectType> getRequiredEffects() {
        return requiredEffects;
    }

    public boolean isElytraDisabled() {
        return elytraDisabled;
    }

    public double getMinHealth() {
        return minHealth;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public String getDenyMessage() {
        return denyMessage;
    }

    public Map<PotionEffectType, Integer> getZoneEffects() {
        return zoneEffects;
    }
}