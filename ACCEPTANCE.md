# In-game acceptance checklist — NOT RUN

Checks completed here: 11 assignment checks, 8 arrival checks and 7 palette checks. The 12-case
persistence suite, full source build and development Mixin target scan still require
the external Recruits/Forge dependencies. These checks do not replace the tests below.

Use Forge 1.20.1, Recruits 1.15.2 and this addon on both client and server.

| Test | Action | Required result | Status |
|---|---|---|---|
| A | Five bowmen, five BOWMAN posts, Battle Stations | Five compatible occupants | Not run |
| B | Ten bowmen, five posts | Five occupants and five reserves | Not run |
| C | Auto Fill OFF, kill one occupant, then Fill Vacancies | Post survives; only one reserve replaces him after the command | Not run |
| D | Auto Fill ON, kill one occupant | One replacement within the controller interval; others stay | Not run |
| E | Five defenders, five reserves, Hold Posts ON; native move and formation | Only the reserves move; formation slots exclude defenders | Not run |
| F | Hold Posts ON; fire/hold fire, shields, targets, stance | Combat controls respond; defenders do not relocate | Not run |
| G | Hold Posts OFF, move and form whole group | Everyone obeys; defensive plan and assignments survive | Not run |
| H | After G, Battle Stations | Saved assignments return to their posts | Not run |
| I | Save, restart, logout/login and unload/reload | Identity, posts, dimensions, roles, facing, settings and assignments survive | Not run |
| J | Two garrisons and one normal group, including mixed selection | Group data independent; normal group unaffected by garrison orders | Not run |
| K | Convert a group whose native icon is not a shield | Native icon remains; `[shield]` appears beside its name in both group views | Not run |
| L | Set every group color from the palette, save and reconnect | Shield tint matches the chosen Recruits faction color and persists | Not run |
| M | Use Remove all positions on one of two garrisons | Selected plan is empty and defenders released; designation, color and other garrison remain | Not run |

Additional checks: unauthorized packets cannot edit another owner's group; blocked
placement rejected; Escape cancels; markers show only selected groups/current dimension;
server starts without client-class loading; no stale lock after removing an unloaded
recruit's post; native charge does not pull defenders out; mounted/special troops;
unreachable posts; group rename/split/merge; empty-group UI; coexistence with Workers
and the actual RTS camera addon.


0.2.0 retest: Battle Stations without a follow-up order; exact post arrival on positive
and negative coordinates; native group icons remain unchanged; tinted shield markers
appear in both group views; Remove Garrison releases troops and preserves the native group.
