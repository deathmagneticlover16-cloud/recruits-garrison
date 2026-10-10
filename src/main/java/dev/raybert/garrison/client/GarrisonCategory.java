package dev.raybert.garrison.client;

import com.talhanation.recruits.client.gui.CommandScreen;
import com.talhanation.recruits.client.gui.commandscreen.ICommandCategory;
import com.talhanation.recruits.client.gui.group.RecruitsCommandButton;
import com.talhanation.recruits.world.RecruitsGroup;
import dev.raybert.garrison.*;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import java.util.*;
import java.util.function.Predicate;
import static dev.raybert.garrison.GarrisonNetwork.Action.*;

public final class GarrisonCategory implements ICommandCategory {
    @Override public Component getToolTipName() { return Component.literal("Garrison"); }
    @Override public ItemStack getIcon() { return GarrisonIcon.category(); }
    @Override public void createButtons(CommandScreen screen, int x, int y, List<RecruitsGroup> groups, Player player) {
        // Recruits supplies ALL visible groups here, not just selected ones.
        List<UUID> selected = groups.stream().filter(g -> !g.isDisabled() && !g.removed).map(RecruitsGroup::getUUID).toList();
        GarrisonClient.refresh(selected);
        List<UUID> garrisons = selected.stream().filter(GarrisonClient.DATA::containsKey).toList();
        boolean single = selected.size() == 1 && garrisons.size() == 1;
        int left = x - 5, right = x + 100, top = y - 62;
        button(screen, left, top, "Make Garrison", "Designate exactly one selected normal group as a garrison.",
                selected.size() == 1 && garrisons.isEmpty(), () -> GarrisonClient.send(CONVERT, selected, true));
        button(screen, right, top, "Type: " + GarrisonClient.type, "Cycle the type for the next defensive post.", single, () -> {
            GarrisonClient.type = PostType.values()[(GarrisonClient.type.ordinal() + 1) % PostType.values().length];
            screen.setTightFormation(CommandScreen.tightFormation);
        });
        button(screen, left, top + 23, "Add Position", "Choose a type, then right-click a floor block in the world.", single,
                () -> GarrisonClient.begin(garrisons.get(0), false));
        button(screen, right, top + 23, "Remove Position", "Right-click the floor below a saved post to remove it.", single,
                () -> GarrisonClient.begin(garrisons.get(0), true));
        button(screen, left, top + 46, "Battle Stations", "Return to saved assignments, fill vacant posts, and enable Hold Posts.", !garrisons.isEmpty(),
                () -> GarrisonClient.send(MAN, garrisons, true));
        button(screen, right, top + 46, "Fill Vacancies", "Only unassigned recruits move. Existing assignments remain unchanged.", !garrisons.isEmpty(),
                () -> GarrisonClient.send(FILL, garrisons, true));
        button(screen, left, top + 75, "Auto Fill: " + state(garrisons, g -> g.autoFill), "Automatic replacements while deployed with Hold Posts enabled.", !garrisons.isEmpty(),
                () -> GarrisonClient.send(AUTO, garrisons, !all(garrisons, g -> g.autoFill)));
        button(screen, right, top + 75, "Hold Posts: " + state(garrisons, g -> g.holdPosts), "ON: assigned defenders ignore relocation. Firing, shields and targets remain native. OFF: release everyone, retaining assignments.", !garrisons.isEmpty(),
                () -> GarrisonClient.send(HOLD, garrisons, !all(garrisons, g -> g.holdPosts)));
        button(screen, left, top + 98, "Show: " + (GarrisonClient.show ? "ON" : "OFF"), "Show selected garrisons' positions, facing, types and status within 96 blocks.", !garrisons.isEmpty(), () -> {
            GarrisonClient.show = !GarrisonClient.show; screen.setTightFormation(CommandScreen.tightFormation);
        });
        RecruitsCommandButton remove = new RecruitsCommandButton(left, top + 121,
                Component.literal("Remove group from garrison"), b -> GarrisonClient.send(REMOVE_GROUP, garrisons, false));
        remove.setWidth(205); remove.active = single;
        remove.setTooltip(Tooltip.create(Component.literal("Return this group to normal. Releases defenders and removes its saved posts; keeps the group and all recruits.")));
        screen.addRenderableWidget(remove);
    }
    private static boolean all(List<UUID> ids, Predicate<GarrisonData> test) { return !ids.isEmpty() && ids.stream().allMatch(id -> test.test(GarrisonClient.DATA.get(id))); }
    private static String state(List<UUID> ids, Predicate<GarrisonData> test) {
        if (ids.isEmpty()) return "--";
        if (all(ids, test)) return "ON";
        return ids.stream().noneMatch(id -> test.test(GarrisonClient.DATA.get(id))) ? "OFF" : "MIXED";
    }
    private static void button(CommandScreen screen, int x, int y, String label, String hint, boolean enabled, Runnable action) {
        RecruitsCommandButton button = new RecruitsCommandButton(x, y, Component.literal(label), b -> action.run());
        button.setWidth(100); button.active = enabled; button.setTooltip(Tooltip.create(Component.literal(hint)));
        screen.addRenderableWidget(button);
    }
}
