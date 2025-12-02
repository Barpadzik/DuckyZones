package pl.barpad.duckyzones.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import pl.barpad.duckyzones.Main;
import pl.barpad.duckyzones.cache.ZoneWorldCache;
import pl.barpad.duckyzones.managers.AfkRewardManager;
import pl.barpad.duckyzones.managers.MessagesManager;
import pl.barpad.duckyzones.managers.ZoneManager;
import pl.barpad.duckyzones.model.Zone;

import java.util.*;

public class ZoneListener implements Listener {

    private final Main plugin;
    private final ZoneManager zoneManager;
    private final MessagesManager messagesManager;
    private final AfkRewardManager afkRewardManager;
    private List<Zone> cachedZones;
    private final Map<Player, String> elytraMessageCache = new HashMap<>();
    private final Map<Player, Zone> playerZones = new HashMap<>();
    private final ZoneWorldCache zoneWorldCache;

    public ZoneListener(Main plugin, ZoneManager zoneManager, MessagesManager messagesManager, AfkRewardManager afkRewardManager) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;
        this.messagesManager = messagesManager;
        this.afkRewardManager = afkRewardManager;
        this.zoneWorldCache = new ZoneWorldCache(zoneManager);
        refreshZones();
    }

    public void refreshZones() {
        zoneManager.loadZones();
        this.cachedZones = zoneManager.getZones();
        this.zoneWorldCache.refresh(zoneManager);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(Objects.requireNonNull(event.getTo()).getBlock())) return;

        Player player = event.getPlayer();
        if (!zoneWorldCache.isWorldWithZones(player.getWorld())) return;

        Zone currentZone = cachedZones.stream().filter(zone -> zone.isInside(event.getTo())).findFirst().orElse(null);

        Zone previousZone = playerZones.get(player);

        if (!Objects.equals(previousZone, currentZone)) {
            if (currentZone != null) {
                for (Map.Entry<PotionEffectType, Integer> entry : currentZone.getZoneEffects().entrySet()) {
                    PotionEffectType type = entry.getKey();
                    int amplifier = entry.getValue();
                    if (!currentZone.getBlockedEffects().containsKey(type)) {
                        player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, true, false));
                    }
                }
                afkRewardManager.trackPlayerInZone(player, currentZone);
            }

            if (currentZone != null) {
                playerZones.put(player, currentZone);
            } else {
                if (previousZone != null) {
                    for (PotionEffectType type : previousZone.getZoneEffects().keySet()) {
                        player.removePotionEffect(type);
                    }
                    afkRewardManager.removePlayerFromZone(player, previousZone);
                }
                playerZones.remove(player);
            }
        }

        if (currentZone != null) {
            String zoneName = currentZone.getName().toLowerCase();

            boolean hasBypass = player.hasPermission("duckyzones.zone." + zoneName + ".bypass")
                    || player.hasPermission("duckyzones.zones.bypass")
                    || player.hasPermission("duckyzones.*")
                    || player.hasPermission("*")
                    || player.isOp();

            if (hasBypass) return;

            boolean blockedItem = currentZone.getBlockedItems().stream().anyMatch(item -> player.getInventory().contains(item));
            boolean hasBlockedEffect = player.getActivePotionEffects().stream().anyMatch(effect -> {
                PotionEffectType type = effect.getType();
                int amplifier = effect.getAmplifier();

                Integer blockedAmplifier = currentZone.getBlockedEffects().get(type);
                return blockedAmplifier != null && (blockedAmplifier == -1 || blockedAmplifier == amplifier);
            });
            boolean missingRequiredItem = !currentZone.getRequiredItems().isEmpty() &&
                    !currentZone.getRequiredItems().stream().allMatch(item -> player.getInventory().contains(item));
            boolean missingRequiredEffect = !currentZone.getRequiredEffects().isEmpty() &&
                    !currentZone.getRequiredEffects().entrySet().stream().allMatch(entry -> {
                        PotionEffectType type = entry.getKey();
                        int requiredAmplifier = entry.getValue();

                        return player.getActivePotionEffects().stream().anyMatch(effect ->
                                effect.getType().equals(type) && effect.getAmplifier() >= requiredAmplifier);
                    });
            boolean lowHealth = player.getHealth() < currentZone.getMinHealth();
            boolean highHealth = player.getHealth() > currentZone.getMaxHealth();
            boolean lowLevel = player.getLevel() < currentZone.getMinLevel();
            boolean highLevel = player.getLevel() > currentZone.getMaxLevel();
            boolean noPermission = !player.hasPermission("duckyzones.zone." + zoneName);

            boolean hasRestriction = blockedItem || hasBlockedEffect || missingRequiredItem || missingRequiredEffect
                    || lowHealth || highHealth || lowLevel || highLevel || noPermission;

            if (hasRestriction) {
                String reasonKey;

                if (blockedItem) reasonKey = "reason-blocked-blocks";
                else if (hasBlockedEffect) reasonKey = "reason-blocked-effects";
                else if (missingRequiredItem) reasonKey = "reason-missing-required-items";
                else if (missingRequiredEffect) reasonKey = "reason-missing-required-effects";
                else if (lowHealth) reasonKey = "reason-low-health";
                else if (highHealth) reasonKey = "reason-high-health";
                else if (lowLevel) reasonKey = "reason-low-level";
                else if (highLevel) reasonKey = "reason-high-level";
                else reasonKey = "reason-no-permission";

                String reasonMessage = color(messagesManager.getMessage(reasonKey));
                showDenialFeedback(player, currentZone, reasonMessage);

                int borderDistance = currentZone.distanceFromBorder(event.getTo());
                if (borderDistance >= 4) {
                    for (PotionEffectType type : currentZone.getZoneEffects().keySet()) {
                        player.removePotionEffect(type);
                    }
                    playerZones.remove(player);

                    String command = getTeleportBackCommand(currentZone.getName());
                    if (command != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                        player.sendMessage(color(messagesManager.getMessage("teleported-back")));
                    }
                } else {
                    Vector backward = event.getFrom().toVector().subtract(event.getTo().toVector()).normalize().multiply(2.8);
                    backward.setY(0.5);
                    player.setVelocity(backward);
                    player.sendMessage(color(currentZone.getDenyMessage().replace("%reason%", reasonMessage)));
                }
            }

            if (currentZone.isElytraDisabled()) {
                if (player.isGliding()) {
                    player.setGliding(false);
                }

                if (player.getInventory().getChestplate() != null &&
                        player.getInventory().getChestplate().getType() == Material.ELYTRA) {

                    ItemStack elytra = player.getInventory().getChestplate().clone();
                    player.getInventory().setChestplate(null);

                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(elytra);
                    if (!leftover.isEmpty()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), elytra);
                    }
                }

                if (!currentZone.getName().equalsIgnoreCase(elytraMessageCache.get(player))) {
                    player.sendMessage(color(messagesManager.getMessage("elytra-disabled-message")));
                    elytraMessageCache.put(player, currentZone.getName());
                }
            } else {
                elytraMessageCache.remove(player);
            }
        } else {
            elytraMessageCache.remove(player);
        }
    }

    private void showDenialFeedback(Player player, Zone zone, String reasonMessage) {
        FileConfiguration config = plugin.getConfig();
        String path = "zones." + zone.getName();

        String title = config.getString(path + ".entry-deny-title");
        String subtitle = config.getString(path + ".entry-deny-subtitle");
        String actionbar = config.getString(path + ".entry-deny-actionbar");
        String bossbarText = config.getString(path + ".entry-deny-bossbar");
        String soundName = config.getString(path + ".entry-deny-sound");

        if (title != null || subtitle != null) {
            assert title != null;
            assert subtitle != null;
            player.sendTitle(color(title.replace("%reason%", reasonMessage)), color(subtitle.replace("%reason%", reasonMessage)), 10, 40, 10);
        }

        if (actionbar != null) {
            TextComponent component = new TextComponent(color(actionbar.replace("%reason%", reasonMessage)));
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, component);
        }

        if (bossbarText != null) {
            BossBar bossBar = Bukkit.createBossBar(color(bossbarText.replace("%reason%", reasonMessage)), BarColor.RED, BarStyle.SEGMENTED_10);
            bossBar.addPlayer(player);
            bossBar.setVisible(true);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                bossBar.removePlayer(player);
                bossBar.setVisible(false);
            }, 60L);
        }

        if (soundName != null) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound in config for zone " + zone.getName() + ": " + soundName);
            }
        }
    }

    private String getTeleportBackCommand(String zoneName) {
        FileConfiguration config = plugin.getConfig();
        return config.getString("zones." + zoneName + ".zone-teleport-back-command");
    }

    private String color(String message) {
        return message == null ? "" : message.replace("&", "§");
    }
}