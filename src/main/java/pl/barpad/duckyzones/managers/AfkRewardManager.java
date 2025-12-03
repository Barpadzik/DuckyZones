package pl.barpad.duckyzones.managers;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
    private final Map<UUID, Map<AfkReward, BossBar>> playerBossbars = new HashMap<>();
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
            playerBossbars.putIfAbsent(playerId, new HashMap<>());

            for (AfkReward reward : zone.getAfkRewards()) {
                BossBar bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SEGMENTED_20);
                bossBar.addPlayer(player);
                playerBossbars.get(playerId).put(reward, bossBar);
            }
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

        if (playerBossbars.containsKey(playerId)) {
            for (AfkReward reward : zone.getAfkRewards()) {
                BossBar bossBar = playerBossbars.get(playerId).remove(reward);
                if (bossBar != null) {
                    bossBar.removePlayer(player);
                    bossBar.setVisible(false);
                }
            }
            if (playerBossbars.get(playerId).isEmpty()) {
                playerBossbars.remove(playerId);
            }
        }
    }

    public void clearPlayer(UUID playerId) {
        playerZoneEntryTime.remove(playerId);
        lastRewardTime.remove(playerId);

        if (playerBossbars.containsKey(playerId)) {
            for (BossBar bossBar : playerBossbars.get(playerId).values()) {
                bossBar.removeAll();
                bossBar.setVisible(false);
            }
            playerBossbars.remove(playerId);
        }
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

                    StringBuilder rewardsInfo = new StringBuilder();
                    Map<AfkReward, Long> rewardTimes = lastRewardTime.get(playerId).get(zone);

                    for (AfkReward reward : zone.getAfkRewards()) {
                        long lastTime = rewardTimes.getOrDefault(reward, zoneEntry.getValue());
                        long elapsedSeconds = (currentTime - lastTime) / 1000;
                        long remainingSeconds = reward.getIntervalSeconds() - elapsedSeconds;

                        BossBar bossBar = playerBossbars.get(playerId).get(reward);
                        if (bossBar != null) {
                            String timeFormat = formatTime(Math.max(0, remainingSeconds));
                            String bossbarText = color(reward.getBossbarFormat().replace("{time}", timeFormat));
                            bossBar.setTitle(bossbarText);
                            bossBar.setProgress(Math.max(0, (double) remainingSeconds / reward.getIntervalSeconds()));
                        }

                        if (rewardsInfo.length() > 0) {
                            rewardsInfo.append(" &7| ");
                        }
                        rewardsInfo.append("&e").append(reward.getName()).append(" &f").append(formatTime(Math.max(0, remainingSeconds)));

                        if (elapsedSeconds >= reward.getIntervalSeconds()) {
                            double playerChance = reward.getChanceForPlayer(player);
                            if (Math.random() * 100 <= playerChance) {
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

                    String actionbarText = zone.getAfkActionbarFormat().replace("{rewards}", rewardsInfo.toString());
                    TextComponent actionbarComponent = new TextComponent(color(actionbarText));
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, actionbarComponent);
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
        playerBossbars.clear();
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private String color(String message) {
        return message == null ? "" : message.replace("&", "§");
    }
}
