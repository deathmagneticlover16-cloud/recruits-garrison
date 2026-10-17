package dev.lastpagans.garrison.mixin;

import com.talhanation.recruits.client.gui.group.RecruitsGroupButton;
import com.talhanation.recruits.world.RecruitsGroup;
import dev.lastpagans.garrison.client.GarrisonIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecruitsGroupButton.class, remap = false)
public abstract class GroupButtonIconMixin {
    @Shadow private RecruitsGroup group;
    @Shadow private ResourceLocation image;
    @Dynamic("AbstractWidget renderWidget override")
    @Inject(method = {"renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", "m_87963_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"}, at = @At("HEAD"))
    private void garrison$replaceIcon(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        image = GarrisonIcon.markerColor(group) < 0 ? RecruitsGroup.IMAGES.get(group.getImage()) : null;
    }
    @Dynamic("AbstractWidget renderWidget override")
    @Inject(method = {"renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", "m_87963_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"}, at = @At("TAIL"))
    private void garrison$drawIcon(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int color = GarrisonIcon.markerColor(group);
        if (color < 0) return;
        RecruitsGroupButton button = (RecruitsGroupButton) (Object) this;
        GarrisonIcon.drawShield(graphics, button.getX() + 10, button.getY() + 8, 21, color);
    }
}
