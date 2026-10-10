package dev.raybert.garrison.client;

import com.talhanation.recruits.world.RecruitsGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public final class GarrisonIcon {
    public static final ResourceLocation TEXTURE = new ResourceLocation("garrison", "textures/gui/castle.png");
    private static final String MARKER = "GarrisonCategoryIcon";
    private GarrisonIcon() {}
    public static ResourceLocation forGroup(RecruitsGroup group) {
        if (GarrisonClient.DATA.containsKey(group.getUUID())) return TEXTURE;
        return RecruitsGroup.IMAGES.get(group.getImage());
    }
    /** The category API accepts ItemStack only. This client-only tag selects a texture renderer. */
    public static ItemStack category() {
        ItemStack stack = new ItemStack(Items.PAPER); stack.getOrCreateTag().putBoolean(MARKER, true); return stack;
    }
    public static boolean isCategory(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(MARKER);
    }
}
