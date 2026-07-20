# Protego

**A simple, zero-dependency entity protection and sanitization plugin for Minecraft servers.**

*Name inspired by the Shield Charm from Harry Potter.*

## Features

### Entity Protection
- **Hanging Entity Shield**: Prevents projectiles and explosions from destroying Item Frames, Paintings, and other hanging entities

### Entity Spawn Control
- **Beehive Sanitization**: Automatically clears bees from hives/nests with more than 3 bees (prevents NBT stuffed-beehive lag exploits)
- **Block Entity Types**: Completely prevent specific entity types from spawning (e.g., Wither, Ender Dragon, Primed TNT)
- **Strip Functionality**: Allow entities to spawn but neuter dangerous functionality (command block minecarts execute nothing, spawner minecarts never spawn)
- **Global Chunk Limits**: Set a maximum number of entities per chunk to prevent lag (with configurable exclusions e.g., Display entities)

### NBT Sanitization
- **Tag Blacklist**: Strip dangerous NBT tags from all spawned entities (Motion, Invulnerable, Passengers, etc.)
- **Passenger Control**: Blacklist or whitelist specific passenger entity types, or nuke all passengers entirely
- **Recursive Sanitization**: Automatically sanitizes nested passenger entities to infinite depth

### Cleanup Commands
- **`/evanesco`**: Detects and removes Armor Stands with negative Health/DeathTime values that bypass vanilla cleanup
*Name inspired by the Vanishing Spell from Harry Potter.*

## Installation
1. Download the latest release
2. Place `Protego.jar` in your server's `plugins` folder
3. Restart the server
4. Edit `plugins/Protego/config.yml` as needed
5. Reload with `/protego reload` or restart again

## Configuration
The configuration file is automatically generated on first run. The default configuration is shown below:

```yaml
# Entity types that are completely blocked from spawning
# Use Bukkit EntityType names (uppercase)
blocked-entity-types:
  - ENDER_DRAGON
  - WITHER
  - FALLING_BLOCK
  - TNT

# Entity types that can be spawned but will be stripped specially
# Use Bukkit EntityType names (uppercase)
# ENTITIES OTHER THAN THOSE LISTED ARE NOT CURRENTLY SUPPORTED!
strip-functionality-types:
  - MINECART_COMMAND
  - MINECART_MOB_SPAWNER

# NBT tag blacklist - these tags will be stripped from ALL spawned entities
# If this list is not empty, it takes priority over the whitelist
# Common dangerous tags: Passengers, custom_name, Invulnerable, etc.
nbt-blacklist:
  - Motion

# Passenger Type blacklist - these Passengers will be stripped from ALL spawned entities
# If this list is not empty, it takes priority over the whitelist
passenger-blacklist: []

# Passenger Type whitelist - ONLY these Passengers will be kept (all others stripped)
# Only used if passenger-blacklist is empty
passenger-whitelist:
  - TEXT_DISPLAY
  - BLOCK_DISPLAY
  - ITEM_DISPLAY
  - MARKER
  - INTERACTION

# Global maximum number of entities allowed per chunk (-1 for unlimited)
global-entity-per-chunk-limit: 100

# Entity types that don't count toward the per-chunk limit
chunk-limit-exclusions:
  - "TEXT_DISPLAY"
  - "BLOCK_DISPLAY"
  - "ITEM_DISPLAY"
  - "MARKER"
  - "INTERACTION"
```

## Commands
| Command     | Description                                                    | Permission         |
|-------------|----------------------------------------------------------------|--------------------|
| `/evanesco` | Removes all Armor Stands with negative Health/DeathTime values | `protego.evanesco` |
| `/protego`  | Reloads the plugin configuration                               | `protego.reload`   |

## Permissions
| Permission         | Description                                                                                  | Default |
|--------------------|----------------------------------------------------------------------------------------------|---------|
| `protego.evanesco` | Allows use of the `/evanesco` command                                                        | OP      |
| `protego.notify`   | Receive notifications when Protego sanitizes a beehive or `/evanesco` cleans up armor stands | OP      |
| `protego.reload`   | Reloads the plugin configuration                                                             | OP      |

## Technical Details
- **Single Plugin**: No additional plugins or external libraries required
- **NMS Integration**: Uses direct NMS access for entity manipulation and sanitization
- **Version-Specific**: Tied to specific Minecraft versions due to NMS usage (requires rebuild per MC version)
- **Performance**: Uses efficient chunk-based entity scanning and event-driven sanitization
- **Compatibility**: Works with Paper, Purpur, and other Bukkit-compatible server software
- **API Version**: 1.21+

## Why Protego?
Most entity protection plugins are bloated, poorly maintained, or require complex configurations. Protego is designed to be:

- **Simple**: One config file, clear options, no bloat
- **Fast**: Minimal overhead, event-driven design
- **Secure**: Prevents common NBT exploits and lag machines
- **Flexible**: Granular control over what entities can exist and how they behave

## License
This plugin is open source. Do whatever you want with it.

## Credits
- **Kaddicus** - Original author and maintainer
- **Harry Potter** - For the inspiration (Protego = Shield Charm, Evanesco = Vanishing Spell)