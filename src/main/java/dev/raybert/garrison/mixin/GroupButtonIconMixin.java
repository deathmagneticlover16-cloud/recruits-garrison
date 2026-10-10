package dev.raybert.garrison.mixin;

import com.talhanation.recruits.client.gui.group.RecruitsGroupButton;
import com.talhanation.recruits.world.RecruitsGroup;
import dev.raybert.garrison.client.GarrisonIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecruitsGroupButton.class, remap = false)
public abstract class GroupButtonIconMixin {
    @Shadow private RecruitsGroup group;
    @Dynamic("AbstractWidget renderWidget override")
    @Inject(method = {"renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", "m_87963_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"}, at = @At("TAIL"))
    private void garrison$marker(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int color = GarrisonIcon.markerColor(group);
        if (color < 0) return;
        RecruitsGroupButton button = (RecruitsGroupButton) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        float scale = 0.65F;
        int nameX = button.getX() + (button.getWidth() - (int) (minecraft.font.width(group.getName()) * scale)) / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(nameX, button.getY() + 3, 1);
        graphics.pose().scale(scale, scale, 1.0F);
        int x = minecraft.font.width(group.getName()) + 2;
        graphics.drawString(minecraft.font, "[", x, 0, 0xFFFFFF, false);
        int markerX = x + minecraft.font.width("[");
        graphics.drawString(minecraft.font, "X", markerX, 0, color, false);
        graphics.drawString(minecraft.font, "]", markerX + minecraft.font.width("X"), 0, 0xFFFFFF, false);
        graphics.pose().popPose();
    }
}
