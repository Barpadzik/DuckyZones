package pl.barpad.duckyzones.model;

import java.util.List;

public class AfkReward {

    private final int intervalSeconds;
    private final List<String> commands;
    private final double chance;

    public AfkReward(int intervalSeconds, List<String> commands, double chance) {
        this.intervalSeconds = intervalSeconds;
        this.commands = commands;
        this.chance = chance;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public List<String> getCommands() {
        return commands;
    }

    public double getChance() {
        return chance;
    }
}
