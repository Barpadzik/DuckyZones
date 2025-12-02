package pl.barpad.duckyzones.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.barpad.duckyzones.Main;
import pl.barpad.duckyzones.model.AfkReward;
import pl.barpad.duckyzones.model.Zone;

import java.util.*;

public class AfkRewardManager {

    private final Main plugin;
    private final Map<UUID, Map<Zone, Long>> playerZoneEntryTime = new HashMap<>();
    private final Map<UUID, Map<Zone, Map<AfkReward, Long>>> lastRewardTime = new HashMap<>();
    private BukkitTask rewardTask;

    public AfkRewardManager(Main plugin) {
        this.plugin = plugin;
        startRewardTask();
    }

    public void trackPlayerInZone(Player player, Zone zone) {
        if (!zone.isAfkZone()) return;

        UUID playerId = player.getUniqueId();
        playerZoneEntryTime.putIfAbsent(playerId, new HashMap<>());

        if (!playerZoneEntryTime.get(playerId).containsKey(zone)) {
            playerZoneEntryTime.get(playerId).put(zone, System.currentTimeMillis());
            lastRewardTime.putIfAbsent(playerId, new HashMap<>());
            lastRewardTime.get(playerId).put(zone, new HashMap<>());
        }
    }

    public void removePlayerFromZone(Player player, Zone zone) {
        UUID playerId = player.getUniqueId();

        if (playerZoneEntryTime.containsKey(playerId)) {
            playerZoneEntryTime.get(playerId).remove(zone);
            if (playerZoneEntryTime.get(playerId).isEmpty()) {
                playerZoneEntryTime.remove(playerId);
            }
        }

        if (lastRewardTime.containsKey(playerId)) {
            lastRewardTime.get(playerId).remove(zone);
            if (lastRewardTime.get(playerId).isEmpty()) {
                lastRewardTime.remove(playerId);
            }
        }
    }

    public void clearPlayer(UUID playerId) {
        playerZoneEntryTime.remove(playerId);
        lastRewardTime.remove(playerId);
    }

    private void startRewardTask() {
        rewardTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();

            for (Map.Entry<UUID, Map<Zone, Long>> entry : new HashMap<>(playerZoneEntryTime).entrySet()) {
                UUID playerId = entry.getKey();
                Player player = Bukkit.getPlayer(playerId);

                if (player == null || !player.isOnline()) {
                    clearPlayer(playerId);
                    continue;
                }

                for (Map.Entry<Zone, Long> zoneEntry : entry.getValue().entrySet()) {
                    Zone zone = zoneEntry.getKey();

                    if (!zone.isInside(player.getLocation())) {
                        removePlayerFromZone(player, zone);
                        continue;
                    }

                    for (AfkReward reward : zone.getAfkRewards()) {
                        Map<AfkReward, Long> rewardTimes = lastRewardTime.get(playerId).get(zone);
                        long lastTime = rewardTimes.getOrDefault(reward, zoneEntry.getValue());
                        long elapsedSeconds = (currentTime - lastTime) / 1000;

                        if (elapsedSeconds >= reward.getIntervalSeconds()) {
                            if (Math.random() * 100 <= reward.getChance()) {
                                for (String command : reward.getCommands()) {
                                    String processedCommand = command.replace("%player%", player.getName());
                                    Bukkit.getScheduler().runTask(plugin, () ->
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand)
                                    );
                                }
                            }
                            rewardTimes.put(reward, currentTime);
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    public void shutdown() {
        if (rewardTask != null) {
            rewardTask.cancel();
        }
        playerZoneEntryTime.clear();
        lastRewardTime.clear();
    }
}
