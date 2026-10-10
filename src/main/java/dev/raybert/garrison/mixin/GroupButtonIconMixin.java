package dev.raybert.garrison.mixin;

import com.talhanation.recruits.client.gui.group.RecruitsGroupButton;
import com.talhanation.recruits.world.RecruitsGroup;
import dev.raybert.garrison.client.GarrisonIcon;
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
    private void garrison$icon(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        image = GarrisonIcon.forGroup(group);
    }
}
