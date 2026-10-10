package dev.raybert.garrison.mixin;

import com.talhanation.recruits.client.gui.group.RecruitsGroupEntry;
import com.talhanation.recruits.world.RecruitsGroup;
import dev.raybert.garrison.client.GarrisonIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecruitsGroupEntry.class, remap = false)
public abstract class GroupEntryIconMixin {
    @Shadow @Final protected RecruitsGroup group;
    @Inject(method = "renderElement", at = @At("TAIL"))
    private void garrison$marker(GuiGraphics graphics, int index, int top, int left, int width, int height,
            int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY, CallbackInfo ci) {
        int color = GarrisonIcon.markerColor(group);
        if (color < 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        int x = textX + minecraft.font.width(group.getName()) + 2;
        graphics.drawString(minecraft.font, "[", x, textY, 0xFFFFFF, false);
        int shieldX = x + minecraft.font.width("[") + 1;
        GarrisonIcon.drawShield(graphics, shieldX, textY, 9, color);
        graphics.drawString(minecraft.font, "]", shieldX + 10, textY, 0xFFFFFF, false);
    }
}
