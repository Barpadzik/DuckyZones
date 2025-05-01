package pl.barpad.duckyzones.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.barpad.duckyzones.Main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private final Main plugin;
    private final Map<String, String> messages = new HashMap<>();

    public MessagesManager(Main plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        FileConfiguration messagesConfig = YamlConfiguration.loadConfiguration(file);
        messages.clear();

        messages.put("no-permission", messagesConfig.getString("no-permission", "No permission message missing."));
        messages.put("zone-entered", messagesConfig.getString("zone-entered", "Zone entered message missing."));
        messages.put("zone-denied", messagesConfig.getString("zone-denied", "Zone denied message missing."));
        messages.put("elytra-disabled-message", messagesConfig.getString("elytra-disabled-message", "&cElytra is disabled in this zone!"));
        messages.put("zone-teleport-back", messagesConfig.getString("zone-teleport-back", "Teleport back message missing."));
        messages.put("reload-success", messagesConfig.getString("reload-success", "Reload success message missing."));
        messages.put("invalid-zone", messagesConfig.getString("invalid-zone", "Invalid zone message missing."));
        messages.put("usage", messagesConfig.getString("usage", "&eUsage: /duckyzones reload"));
        messages.put("teleported-back", messagesConfig.getString("teleported-back", "You have been teleported back because you cannot enter that zone."));

        messages.put("reason-blocked-blocks", messagesConfig.getString("reason-blocked-blocks", "&cYou have a locked item in your inventory!"));
        messages.put("reason-blocked-effects", messagesConfig.getString("reason-blocked-effects", "&cYou have a blocked effect!"));
        messages.put("reason-missing-required-items", messagesConfig.getString("reason-missing-required-items", "&cYou don't have the required items!"));
        messages.put("reason-missing-required-effects", messagesConfig.getString("reason-missing-required-effects", "&cYou are missing the required effect!"));
        messages.put("reason-low-health", messagesConfig.getString("reason-low-health", "&cYou don't have enough health!"));
        messages.put("reason-high-health", messagesConfig.getString("reason-high-health", "&cYou have too much health!"));
        messages.put("reason-low-level", messagesConfig.getString("reason-low-level", "&cYour level is too low!"));
        messages.put("reason-high-level", messagesConfig.getString("reason-high-level", "&cYour level is too high!"));
        messages.put("reason-no-permission", messagesConfig.getString("reason-no-permission", "&cYou are not authorized to enter this zone!"));
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found for key: " + key);
    }

    public void reload() {
        loadMessages();
    }
}