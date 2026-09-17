# More Beyond Ordinary

> A large content and mechanics expansion for **Minecraft 1.7.10**, developed for the **Ariamis** RPG project.

![Minecraft 1.7.10](https://img.shields.io/badge/Minecraft-1.7.10-62B47A?style=flat-square)
![Forge 10.13.4.1614](https://img.shields.io/badge/Forge-10.13.4.1614-1F6FEB?style=flat-square)
![Java 8](https://img.shields.io/badge/Java-8-ED8B00?style=flat-square)
![Version 1.23.5](https://img.shields.io/badge/Version-1.23.5-2EA44F?style=flat-square)
![License](https://img.shields.io/badge/License-GPL--3.0%20%2B%20CC%20BY--NC--SA%203.0-6E7781?style=flat-square)

[Русская версия](README_ru.md)

## About

**More Beyond Ordinary (MBO)** expands Minecraft 1.7.10 with new gameplay systems, map-making tools, building content, RPG equipment and numerous improvements to vanilla mechanics.

The mod was originally created for the **[Ariamis](https://ariamis.cc)** RPG server and is designed primarily around multiplayer worlds, adventure maps and heavily customized modpacks.

## Main mechanics

### Moving platforms

MBO includes a moving-platform system that can turn groups of blocks into moving structures. Platforms support configurable movement, return behaviour, redstone activation, onboard controls and client-side interpolation.

The system is intended for elevators, bridges, traps, moving dungeon elements and other interactive structures.

### Dungeon and map-making tools

The mod provides a set of systems for building scripted locations and dungeons:

- trigger zones with configurable filters and cooldowns;
- passable, illusory and switchable collider walls;
- configurable actions for dungeon events;
- destructible and configurable loot containers;
- traps, entity spawning, item drops and other triggered actions;
- in-game tools for configuring dungeon elements.

### Locks and lockpicking

Chests, doors and trapdoors can use configurable locks. Locks support different difficulty levels, automatic relocking and an interactive lockpicking minigame.

This system is intended for RPG locations, quests, treasure rooms and protected areas.

### Waterlogging backport

MBO backports waterlogging-style behaviour to Minecraft 1.7.10. Supported non-full blocks can coexist with water while preserving appropriate rendering, flow and entity behaviour.

### Spectator mode

A spectator mode is backported to 1.7.10 with flight, noclip, invisibility, invulnerability and restricted interaction. In addition to the extended `/gamemode` command, MBO adds the modern-style **F3 + F4** quick gamemode switcher.

### CoreMod improvements

MBO contains an ASM CoreMod that modifies and extends several vanilla systems. Among other changes, it provides:

- smooth opening animations for doors, trapdoors and fence gates;
- improved fence and glass-pane connections;
- cauldron improvements;
- trapdoor placement fixes;
- ladder, rail and piston fixes;
- custom tooltip frames;
- improved signs and sign editing;
- custom **Unicode glyph** support, allowing icons and small images to be embedded directly into item descriptions and interface text;
- falling leaf particles from foliage for a more lively environment;
- multiplayer ping display improvements;
- additional rendering and interface fixes.

## Building and decorative content

MBO adds a large collection of building blocks and block families, including new stone and brick variants, roof tiles, walls, stairs, slabs, fences and several blocks backported from newer Minecraft versions. Wooden building sets are expanded with many variants of **doors, trapdoors, fence gates, buttons, pressure plates** and other matching elements across different wood types.

The mod also contains a large library of **GeckoLib-based 3D decorative models** for interiors, settlements, workshops, crypts, dungeons and other RPG locations. Many of these models support collision, rotation or animation.

## Equipment and RPG content

The mod expands the equipment system with new melee and ranged weapons, armor sets and **Baubles**-compatible jewelry.

It also adds **dozens of custom status and visual effects**, alongside food, drinks and other RPG-oriented content that complements exploration, combat and character progression.

## Additional content

MBO includes several smaller gameplay and building additions, such as:

- wood-variant boats and chest boats;
- banners and banner patterns;
- stonecutter-style crafting;
- additional storage and utility blocks;
- fog and barrier blocks for map makers;
- selected mechanics and blocks backported from newer Minecraft versions;
- extensions to vanilla commands such as `/effect` and `/gamemode`.

## Magic

MBO includes a magic system presented primarily through **wands and spell scrolls**.

Magic covers combat, support and utility abilities and uses custom visual effects and particles. Some magical content also integrates with other supported mods when they are installed.

## Mod integrations

MBO includes support or integration for several popular 1.7.10 mods:

| Mod | Integration |
|:---|:---|
| [Baubles](https://www.curseforge.com/minecraft/mc-mods/baubles) | Equipment slots for jewelry |
| [GeckoLib](https://github.com/bernie-g/geckolib) | Animated 3D models |
| [Thaumcraft 4](https://www.curseforge.com/minecraft/mc-mods/thaumcraft) | Additional compatibility and magic-related content |
| [MineFantasy 2](https://www.curseforge.com/minecraft/mc-mods/minefantasy2) | Attribute and stamina integration |
| [Biomes O' Plenty](https://www.curseforge.com/minecraft/mc-mods/biomes-o-plenty) | Additional boat variants |
| [Carpenter's Blocks](https://www.curseforge.com/minecraft/mc-mods/carpenters-blocks) | Door animation compatibility |
| [CustomNPC+](https://www.curseforge.com/minecraft/mc-mods/custom-npcs) | NPC integration |
| [Waila](https://www.curseforge.com/minecraft/mc-mods/waila) | Block tooltip support |
| [NEI](https://www.curseforge.com/minecraft/mc-mods/not-enough-items-1-8) | Recipe and tooltip compatibility |
| [Antique Atlas](https://www.curseforge.com/minecraft/mc-mods/antique-atlas) | Map integration |

## Dependencies

### Required

- **Minecraft 1.7.10**
- **Minecraft Forge 10.13.4.1614**
- **Java 8**
- [GeckoLib Unofficial 1.7.10](https://github.com/AriamisProject/geckolib-1.7.10)
- [Baubles 1.0.1.10](https://www.curseforge.com/minecraft/mc-mods/baubles)

Some features are enabled only when their corresponding optional integration mods are installed.

## Building from source

Clone the repository:

```bash
git clone https://github.com/GivlerHP/MoreBeyondOrdinary-1.7.10-.git
cd MoreBeyondOrdinary-1.7.10-
```

Prepare the ForgeGradle workspace and build the mod:

```bash
./gradlew setupDecompWorkspace
./gradlew build
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

The compiled JAR files are generated in `build/libs/`.

## License

The project uses a dual-license model:

| Component | License |
|:---|:---|
| Source code (`src/main/java/`) | [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.en.html) or later |
| Mod assets (`src/main/resources/assets/mbo/`) — textures, `.geo.json` models, animations, sounds, localization, block/item models, and other assets | [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/) |

See [LICENSE](LICENSE) for the full license text.

## Credits

**Author:** [Givler](https://discordapp.com/users/439862750764728343/)

**Contributors:** Higgs, Garden, goodbird, Yowan, zxterProxy, Luna

MBO was created for the **Ariamis** project with help from its community.
