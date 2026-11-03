# Architecture

Technical routing for the current implementation, checked against game `0.98a-RC8`. Code paths below are relative to `jars/src/src/bettertithes/`; data paths are repository-relative. Search the named owner before adding another implementation.

| Reference | Scope |
|---|---|
| [CLAUDE.md](../CLAUDE.md) | Workflow, build gate and document upkeep |
| [RELEASE.md](RELEASE.md) | Branches, version numbers, release builder, package contents and publishing |
| [CHANGELOG.md](CHANGELOG.md) | Change-note format and upkeep for every implementation task |

## Start here

| Change / symptom | Route |
|---|---|
| Pather fleets in the system still attack after a tithe | `data/campaign/rules.csv` `LPTithePay` -> `rules/MakeFactionFleetsInSystemNonHostile`; then [hostility contract](#hostility) |
| A Pather fleet that arrives later is hostile | `plugins/ModPlugin.onGameLoad -> listeners/LPTitheFleetHostilityListener.reportFleetSpawned / reportFleetJumped -> makeNonHostileIfTithePaid`; [listener timing](#listener-timing) |
| Another Pather fleet in a paid system asks for a tithe again | `LPTitheCheck` conditions -> `rules/IsSystemTithePaid` -> location memory `$LP_tithePaid` |
| Tithe lasts the wrong time | Expiry arguments in `LPTithePay` (30 days) and `payMegaTitheConfirmSel` (100 days); `rules/SetSystemMemoryKey`; the listener copies the key's remaining expiry |
| A rules command is not found | `data/config/settings.json` `ruleCommandPackages` -> `bettertithes.rules` |
| Behavior in hyperspace | [Hyperspace](#hyperspace) |

## Registration and lifecycle

| Registry / hook | Owners |
|---|---|
| `mod_info.json` | `modPlugin` `bettertithes.plugins.ModPlugin`; jar `jars/src.jar`; no dependencies; not `utility` |
| `ModPlugin.onGameLoad()` | `LPTitheFleetHostilityListener.register()`: adds a new listener with `addTransientListener`, so it is not saved and is added again on every load |
| `data/config/settings.json` | `ruleCommandPackages`: `bettertithes.rules`, so `rules.csv` calls `IsSystemTithePaid`, `MakeFactionFleetsInSystemNonHostile` and `SetSystemMemoryKey` by class name |
| `data/campaign/rules.csv` | Three rows that replace vanilla rows of the same id ([tithe rows](#tithe-rows)) |
| `data/config/LunaSettingsConfig.json` | LunaLib settings icon `usefultithes.png` (mod root). Read only when LunaLib is installed; the mod has no `LunaSettings.csv` and no settings |
| `data/config/version/version_files.csv` | Version Checker entry for `usefultithes.version` |

There is no other load order: `onGameLoad` registers one listener.

### Build and checks

`tools/ReleaseBuild.java` (`build`, `package`) and `tools/ReleaseBuildChecks.java` compile against `lib/` and never ship in the release jar; they, `lib/` and `.github/workflows/release.yml` are owned by [RELEASE.md](RELEASE.md). The mod has no other checks.

## Source owners

Use `rg --files jars/src/src/bettertithes` for the complete inventory.

| File | Owner / connection |
|---|---|
| `plugins/ModPlugin.java` | Mod plugin; registers the listener on game load |
| `listeners/LPTitheFleetHostilityListener.java` | Transient campaign listener. On `reportFleetSpawned` (Luddic Path fleets, delayed 0.001 days with a `DelayedActionScript` on the fleet) and `reportFleetJumped` (Luddic Path fleets that land outside hyperspace) it calls `makeNonHostileIfTithePaid`: if the fleet's location memory holds `$LP_tithePaid`, it sets `$cfai_makeNonHostile` on the fleet with reason `$LP_tithePaid` for the key's remaining expiry and logs it |
| `rules/MakeFactionFleetsInSystemNonHostile.java` | `MakeFactionFleetsInSystemNonHostile <faction> <reason> <days>`: sets `$cfai_makeNonHostile` with the reason on every fleet of that faction in the interaction target's location. The location version of vanilla `MakeNearbyFleetsNonHostile`, which searches by range |
| `rules/SetSystemMemoryKey.java` | `SetSystemMemoryKey <key> <days>`: sets the key to `true` on the interaction target's location memory, adding `$` if missing |
| `rules/IsSystemTithePaid.java` | Condition: `$LP_tithePaid` is true on the interaction target's location memory |
| `memory/MemKeys.java` | `LP_TITHE_PAID` = `$LP_tithePaid`, the location key. `rules.csv` spells it `LP_tithePaid` as the `SetSystemMemoryKey` argument |

## Contracts

### Tithe rows

Each row replaces the whole vanilla row with the same id (mod rows win over vanilla; if another mod also replaces one of these ids, the first-sorted mod wins). Vanilla's versions are in the `starsector-knowledge` skill's `data/campaign/rules.csv`.

- `LPTitheCheck` (`BeginFleetEncounter`): vanilla's conditions plus `!IsSystemTithePaid` last, so no Pather fleet in a paid location opens with a tithe demand.
- `LPTithePay` (`payTithe`): vanilla's script plus `MakeFactionFleetsInSystemNonHostile luddic_path LP_tithe 30` and `SetSystemMemoryKey LP_tithePaid 30`.
- `payMegaTitheConfirmSel` (`payMegaTitheConfirm`): vanilla's `MakeNearbyFleetsNonHostile luddic_path LP_tithe 100000 100` replaced by `MakeFactionFleetsInSystemNonHostile luddic_path LP_tithe 100` and `SetSystemMemoryKey LP_tithePaid 100`. `HA_CMD payMegaTithe` (colony agreement, fleets sent home) is unchanged.
- Vanilla also keeps `$LP_tithePaid` in the paying fleet's own memory (`$entity.LP_tithePaid`). That is a different memory from the location key of the same name.

### Hostility

- `$cfai_makeNonHostile` is a flag with reasons (`Misc.setFlagWithReason`); it stays set while any reason is unexpired. The mod adds reason `LP_tithe` from the rows and `$LP_tithePaid` from the listener.
- `TacticalModule.isHostileTo` (obfuscated `com.fs.starfarer.campaign.ai.TacticalModule` on `0.98a-RC8`) checks `$makeNonHostileTakesPriority` with `$cfai_makeNonHostile` first, then `$cfai_makeHostile`, then `$cfai_makeNonHostile`. Only the paying fleet gets `$makeNonHostileTakesPriority` (vanilla `LPTithePay`), so another Pather fleet in the system that carries a make-hostile flag stays hostile. Vanilla's `MakeNearbyFleetsNonHostile` behaves the same.
- A flag lives on the fleet with its own expiry, so a fleet that leaves the paid system stays non-hostile until it expires.

### Listener timing

- `reportFleetSpawned` fires from the location's `addObject` the first time a fleet is added to a location. The listener still waits 0.001 days before reading the location.
- `reportFleetJumped` fires after the jump transition finishes, with the fleet already in the destination location.
- Both ignore every faction but `luddic_path`.

### Hyperspace

"Location" in the commands and the listener is the containing location, which is hyperspace when the tithe is paid there. A tithe paid in hyperspace makes every Pather fleet in all of hyperspace non-hostile, and the hyperspace key stops `LPTitheCheck` for every Pather fleet in hyperspace for the duration. `reportFleetJumped` skips fleets that land in hyperspace, so a Pather fleet that jumps into hyperspace afterwards is neither flagged nor offers a tithe. `reportFleetSpawned` does not skip hyperspace.

### Save data

- Location memory: `$LP_tithePaid`, a Boolean with expiry. Fleet memory: vanilla flag reasons. Neither needs the mod's classes to load.
- The `DelayedActionScript` from `reportFleetSpawned` is an anonymous class (`LPTitheFleetHostilityListener$1`) that holds its listener. It sits in the fleet's scripts until it runs, so a save made in that window stores both classes. Moving or renaming the listener, or removing the mod, breaks loading such a save.
- The mod is not marked `utility`.
