# Recruits Garrison Groups 0.1.1 — experimental V1

Forge 1.20.1 addon for Villager Recruits 1.15.2. Install the runtime JAR on both
client and server alongside Recruits. Workers is an architectural reference;
it is not a dependency. This addon does not replace Recruits.

## Status

Source compiled against a cached Recruits 1.15.2 development JAR and Forge
1.20.1-47.2.0 API. The runtime artifact was remapped to Forge SRG names with
AutoRenamingTool 2.0.3. Its declared minimum Forge version is 47.4.10, matching
the supplied Recruits source build.

Eleven assignment-policy checks, nine NBT persistence checks and eight arrival checks passed.
Development injection target matches were checked against the cached
Recruits JAR. These checks are NOT an in-game acceptance test or a Mixin
transformation/launch test. No Minecraft client or dedicated server was launched.
Treat this as a first test build, not a verified release.

The environment denied Gradle's network download. Java compilation and remapping
produced their outputs but reported AccessDeniedException during dependency archive
cleanup. The artifacts and test outputs were inspected afterward. Build normally
with the included Gradle project on a machine with dependency access before release.

## Using it

1. Create a normal group using Recruits. Select only that group in the command screen.
2. Open the castle-icon **Garrison** category and click **Make Garrison**.
3. Cycle **Type**, click **Add Position**, face the desired direction, and right-click
   a solid floor block in the world. Escape cancels placement. Repeat for each post.
4. Click **Battle Stations**. This restores saved assignments, fills vacancies,
   and enables Hold Posts. Unassigned members remain reserves.
5. Native movement and formations affect reserves while Hold Posts is ON.
   Native fire controls, shields, stances and target selection still reach defenders.
6. **Fill Vacancies** assigns only unassigned recruits to empty posts. It does not
   reshuffle existing assignments, including released assignments.
7. **Auto Fill** replaces dead/invalid defenders while deployed with Hold Posts ON.
   It pauses while Hold Posts is OFF so it cannot pull the released army back.
8. **Hold Posts OFF** releases defenders without deleting posts or assignments.
   **Battle Stations** recalls them. Turning Hold Posts back ON also recalls saved
   assigned defenders, but does not itself fill vacancies.
9. **Show** displays nearby selected garrisons' post type, state and facing.
   **Remove Position** then right-clicking beneath a post removes that post.

Explicit Fill Vacancies also works before initial deployment or while Hold Posts
is OFF. Only newly assigned reserves receive post orders; released assigned troops
are not recalled. When Hold Posts is OFF those new troops still accept native moves.

Position editing and designation require exactly one selected group. Group orders
filter mixed selections to existing garrisons. A mixed ON/OFF toggle enables all
selected garrisons; toggling a uniformly ON selection disables them all.

## V1 boundaries

- Up to 256 posts per group; placement within 64 blocks; markers within 96 blocks.
- Placement requires a solid top face and standing clearance. No rotation editor.
- Native pathfinding is retained. An unreachable post remains assigned/moving;
  the addon does not teleport troops, break blocks, or guarantee a traversable path.
- Unloaded or other-dimension soldiers retain their assignments as ABSENT.
  They are not treated as dead and are not replaced merely because they unloaded.
- Mounted recruits and special troop AI require in-game testing. No custom mounted
  navigation, siege AI, supply, morale, sectors, rally points or new formations.
- Native survival AI remains native; this is an order filter, not an invulnerability
  or teleport lock. Hunger, fleeing and physical displacement need integration tests.
- Group identity is the existing UUID. Rename is safe. Merge/split does not copy or
  merge defensive plans automatically; departed members vacate their old posts.
- Recruits hides groups with no nearby counted members in its command UI. The addon
  does not replace that behavior with a separate group-management screen.
- No integration claim is made for the separate RTS camera mod's custom movement
  paths. Native command routes and guarded recruit setters are covered here.

## Build

Requires Java 17 and internet access for initial Forge/Gradle dependencies.
Put your existing **production Recruits 1.15.2 JAR** in `libs/`, named exactly
`recruits-1.20.1-1.15.2.jar`. Recruits and Workers binaries/source are not bundled.

```powershell
.\gradlew.bat clean build
```

`check` runs the assignment and persistence test mains. Runtime output is
`build/libs/recruits-garrison-1.20.1-0.1.1.jar`.
Do not install a `-dev.jar` in Minecraft.

See `ACCEPTANCE.md` for the required in-game checklist and `INTEGRATION.md` for
the exact extension points and why the small Mixins exist.
