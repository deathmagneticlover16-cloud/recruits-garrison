# 0.2.0

- Renames the mod to **Recruits: Garrison** while retaining the `garrison` mod ID
  and existing world data.
- Keeps each Recruits group's chosen icon. Garrison groups now show a native shield
  marker in square brackets beside the group name in command and group-list views.
- Adds a group-specific **Set Group Color** palette using Recruits' 25 faction unit
  colors. The selected shield color is saved per garrison and synchronized to clients.
- Adds group-specific **Remove all positions**, which releases assigned defenders and
  clears that group's posts and assignments without removing its garrison designation.
- Network protocol changes from 2 to 3. Install 0.2.0 on both client and server.

# 0.1.1

- Uses the supplied, unchanged `castle.png` for the mod logo, native Garrison
  category and designated groups in both the command screen and group manager.
  Client-only texture hooks preserve the group's original saved icon and restore
  it when the designation is removed. No new gameplay item is registered.
- Adds **Remove group from garrison** for a single selected garrison. It releases
  defenders, deletes that group's defensive plan/settings, clears its client state,
  and leaves the native group and its recruits intact. Other garrisons are untouched.
- Battle Stations now explicitly starts navigation and sets native move-to state,
  resets the upkeep timer, clears stale targeting/following, and establishes the post
  hold order in one server-side action.
- Active post movement hooks into the native move/hold goals. It uses a path to the
  exact saved block with reach radius zero, avoiding the Recruits overload that
  truncates negative coordinates and accepts a neighboring target node.
- A collision-checked final approach uses the recruit's MoveControl to finish the
  last step to the center. It never teleports or changes normal non-garrison paths.
- Movement, melee eligibility and OCCUPIED status share one arrival rule: on the
  saved block, within 0.25 blocks horizontally of its center and 0.25 vertically of
  standing height. Standing on an adjacent block is not enough.
- The shown box outlines the actual supporting block originally clicked. Existing
  saved posts retain their coordinates; no migration or re-placement is required.
- Network protocol changes from 1 to 2. Install 0.1.1 on both client and server.

Validation: source compilation, assignment/persistence checks, added arrival edge-case
checks and injection target inspection. No Minecraft launch/playtest was performed
in this environment. The reported behavior fixes still require an in-game retest.

Checked for this build: 28 automated checks passed and 21 injection target matches.
