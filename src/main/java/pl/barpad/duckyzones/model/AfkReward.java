package pl.barpad.duckyzones.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfkReward {

    private final int intervalSeconds;
    private final List<String> commands;
    private final double defaultChance;
    private final Map<String, Double> permissionChances;
    private final String bossbarFormat;
    private final String name;

    public AfkReward(int intervalSeconds, List<String> commands, double defaultChance,
                     Map<String, Double> permissionChances, String bossbarFormat, String name) {
        this.intervalSeconds = intervalSeconds;
        this.commands = commands;
        this.defaultChance = defaultChance;
        this.permissionChances = permissionChances != null ? permissionChances : new HashMap<>();
        this.bossbarFormat = bossbarFormat != null ? bossbarFormat : "&cReward in {time}";
        this.name = name;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public List<String> getCommands() {
        return commands;
    }

    public double getDefaultChance() {
        return defaultChance;
    }

    public Map<String, Double> getPermissionChances() {
        return permissionChances;
    }

    public String getBossbarFormat() {
        return bossbarFormat;
    }

    public String getName() {
        return name;
    }

    public double getChanceForPlayer(org.bukkit.entity.Player player) {
        for (Map.Entry<String, Double> entry : permissionChances.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                return entry.getValue();
            }
        }
        return defaultChance;
    }
}
