# Integration notes

The supplied Workers 2.0.3 source registers `WorkerCommandScreen` through
`CommandCategoryManager.register` during client setup. Its category creates
`RecruitsCommandButton` widgets via the public `CommandScreen.addRenderableWidget`.
GarrisonCategory follows that pattern. No screen replacement, private-screen
reflection, overlay coordinate interception, or GUI replacement is used. V0.2.0 adds
client-only render-tail hooks for the bracketed, tinted shield name marker. These hooks
do not modify the native group's saved name or icon.

Recruits passes `getGroups()` to categories, not `getActiveGroups()`. The category
therefore explicitly filters `!isDisabled()` and `!removed`. Conversion is a separate
single-group request; mixed selection cannot accidentally convert a normal group.

GarrisonStore is overworld SavedData keyed by native group UUID. Posts carry their
own dimension and unique post UUID. The native group manager remains authoritative
for ownership and membership. Client packets contain group IDs, never authoritative
membership, ownership, assignments, facing, or occupation state. The server validates
ownership, selection size, placement ray, support, clearance, border and post limit.
Network messages specify direction; list length and per-tick request counts are bounded.

## Why Mixins are required

The inspected CommandEvents methods do not post a cancellable per-recipient movement
event. `isEffectedByCommand` is shared by movement AND firing/shield commands; blocking
it would implement the explicitly unwanted "ignore everything" behavior.

1. CommandEventsMixin filters the movement/face/formation lists before native
   formation math. This also covers automatic formation-follow. Empty filtered lists
   are cancelled before median/formation calculations. Four-argument overloads delegate
   to the intercepted full overloads.
2. RecruitOrdersMixin guards only repositioning setters while an active defender is
   locked. The native attack command may set a target, but its `setHoldPos` and
   `setFollowState` calls cannot replace the saved post. Aggro, firing, shields and
   target state are otherwise untouched. Released troops can accept ordinary orders;
   those orders deactivate the current post execution without clearing its assignment.
3. MeleePostMixin yields the MOVE goal when an assigned defender is away from its
   post or its target is beyond actual melee reach. Otherwise native melee can claim
   MOVE while chasing and prevent HoldPosGoal from reaching the post. Attacks at the
   post remain native. Both Mojmap and SRG selectors are explicit for Goal overrides.

The Mixins use only Recruits targets and `remap=false`; vanilla Goal override names
are listed in both namespaces. There is no generated refmap. Core Minecraft calls
in the addon must still be reobfuscated normally, which the runtime artifact does.
The standalone injection scan verifies names/descriptors, not live Mixin application.

## Assignment and lifecycle

- Existing valid assignments are retained. Only vacancies enter AssignmentPlanner.
- Candidate counts and specificity put constrained roles before general roles.
  Nearest compatible candidate wins; UUID-sorted candidates make equal-distance
  decisions stable. Compatibility/scoring work is O(posts × reserves) per planning pass.
- The controller validates once per second; scoped move/hold goal hooks own post navigation and its final approach.
  It does not issue navigation to already occupied posts each controller tick.
- Death invalidates the one assignment immediately; autofill runs on the next
  controller pass. Membership/type/ownership changes are validated periodically.
- Chunk unload preserves the assignment. Entity join reinstates active post orders
  or clears stale addon-owned hold flags. Order guards do not run before an entity
  has been added to the world, allowing its normal NBT loading.
- Assigned UUID, active execution and visible occupation state are separate.
  Hold Posts OFF deactivates execution and restores the previous holdFormation flag,
  while saved assignment and post geometry remain intact.
- Battle Stations explicitly enables Hold Posts; Fill Vacancies does not change
  the group's Hold Posts setting or recall already assigned released troops.
- Client state clears on disconnect. Markers are limited to selected groups and
  the current dimension, and require the local Show toggle.

## Further validation needed

Run the acceptance matrix, especially native charge/stance, repeated chunk unload,
mounted combat, hungry troops, dedicated-server class loading and RTS addon coexistence.
The source reference and cached JAR may differ despite sharing the 1.15.2 version;
the runtime build should be tried against the user's actual installed JAR.
