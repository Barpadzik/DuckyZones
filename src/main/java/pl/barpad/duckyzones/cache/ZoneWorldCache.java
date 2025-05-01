package pl.barpad.duckyzones.cache;

import org.bukkit.World;
import pl.barpad.duckyzones.managers.ZoneManager;
import pl.barpad.duckyzones.model.Zone;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZoneWorldCache {

    private final Set<String> worldsWithZones = new HashSet<>();

    public ZoneWorldCache(ZoneManager zoneManager) {
        refresh(zoneManager);
    }

    public void refresh(ZoneManager zoneManager) {
        worldsWithZones.clear();
        List<Zone> zones = zoneManager.getZones();
        for (Zone zone : zones) {
            World world = zone.getWorld();
            if (world != null) {
                worldsWithZones.add(world.getName());
            }
        }
    }

    public boolean isWorldWithZones(World world) {
        return world != null && worldsWithZones.contains(world.getName());
    }
}