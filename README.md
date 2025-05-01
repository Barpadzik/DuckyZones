# 🧩 DuckyZones

**Create custom zones with powerful rules and effects in your Minecraft world!**  
Highly configurable, efficient, and perfect for custom maps, events, or gameplay control.

---

## 📦 Description

**DuckyZones** allows you to define specific rectangular areas (zones) in your world where certain rules and effects apply.  
Whether you're building PvP arenas, adventure maps, or controlled environments — this plugin gives you full control over player behavior inside defined areas.

---

## ⚙️ Features

- 🔲 Create custom zones with two corners  
- 🌍 Per-zone world support  
- ❌ Block specific items or effects inside a zone  
- ✅ Require specific items or effects to enter  
- 🪂 Disable Elytra inside zones (auto-removes Elytra from player)  
- ❤️ Configure health and XP level limits  
- 🧪 Apply potion effects automatically in a zone  
- 🚫 Custom denial messages for rule violations  
- 🔃 Easy reload support without restarting the server  
- 🗂️ Simple `config.yml` layout  
- ☕ Built with **Java 16**, supports **Minecraft 1.16.5 → 1.21+**

---

## 🛠️ Configuration

### 📁 `config.yml`

```yaml
# List of Items (1.21.5) : https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
# List of Effects (1.21.5) : https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html
zones:
  spawn: # Zone name
    world: world # World where zone is
    corner1: world,100,64,100 # First Zone Corner
    corner2: world,110,70,110 # Second Zone Corner
    elytra-disabled: false # Should be elytra disabled in that zone? This will remove the elytra from the player
    elytra-disabled-message: "Elytra is disabled in this zone!"
    blocked-items: # Player with these items in inventory cannot enter the zone
      - TNT
      - ENDER_PEARL
    blocked-effects: # Player with these effects cannot enter the zone
      - SPEED
      - INVISIBILITY
    required-items: # Only Player with these items in inventory can enter the zone
      - DIAMOND
      - TOTEM_OF_UNDYING
    required-effects:  # Only Player with these effects can enter the zone
      - NIGHT_VISION
    zone-effects: # Effects that will be applied after entering the zone, after leaving it they will disappear separator; means the power of the effect, e.g. SPEED;1 = Speed 2 in the game
      - SPEED;1
      - NIGHT_VISION;0
    min-health: 5.0 # Players who do not have this number of hearts cannot enter
    max-health: 20.0 # Players who have this number of hearts or more cannot enter
    min-level: 5 # Players who do not have this amount of exp level cannot enter
    max-level: 100 # Players who have this amount of exp level or more cannot enter
    deny-message: "&cYou can't enter this area!" # Players who are unable to enter the zone will receive this message in chat when trying to enter
    zone-teleport-back-command: "spawn %player%" # The command that will be executed when the player enters the zone e.g. through teleportation, to call the player's nickname, just type %player%
    entry-deny-title: "&cEntrance prohibited &4✖" # Title that will appear on the screen when a player tries to enter the zone without meeting the requirements
    entry-deny-subtitle: "%reason%" # SubTitle that will appear on the screen when a player tries to enter the zone without meeting the requirements
    entry-deny-actionbar: "&eGet away from the zone!" # ActionBar that will appear on the screen when a player tries to enter the zone without meeting the requirements
    entry-deny-bossbar: "&4Entrance to the zone blocked!" # BossBar that will appear on the screen when a player tries to enter the zone without meeting the requirements
    entry-deny-sound: ENTITY.VILLAGER.NO # Sound that will play on the screen when a player tries to enter the zone without meeting the requirements
