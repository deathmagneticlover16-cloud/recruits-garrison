package dev.lastpagans.garrison.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.recruits.world.RecruitsGroup;
import dev.lastpagans.garrison.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public final class GarrisonIcon {
    public static final ResourceLocation TEXTURE = new ResourceLocation("garrison", "textures/gui/castle.png");
    public static final ResourceLocation SHIELD = new ResourceLocation("recruits", "textures/gui/image/group/shield.png");
    private static final String MARKER = "GarrisonCategoryIcon";
    private GarrisonIcon() {}
    public static int markerColor(RecruitsGroup group) {
        GarrisonData data = GarrisonClient.DATA.get(group.getUUID());
        return data == null ? -1 : GarrisonPalette.RGB[data.color];
    }
    public static void drawShield(GuiGraphics graphics, int x, int y, int size, int rgb) {
        float red = ((rgb >> 16) & 255) / 255.0F;
        float green = ((rgb >> 8) & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        graphics.blit(SHIELD, x, y, 0, 0, size, size, 21, 21);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
    /** The category API accepts ItemStack only. This client-only tag selects a texture renderer. */
    public static ItemStack category() {
        ItemStack stack = new ItemStack(Items.PAPER); stack.getOrCreateTag().putBoolean(MARKER, true); return stack;
    }
    public static boolean isCategory(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(MARKER);
    }
}
