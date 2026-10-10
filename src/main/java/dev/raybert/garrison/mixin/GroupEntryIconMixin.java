package dev.raybert.garrison.mixin;

import com.talhanation.recruits.client.gui.group.RecruitsGroupEntry;
import com.talhanation.recruits.world.RecruitsGroup;
import dev.raybert.garrison.client.GarrisonIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecruitsGroupEntry.class, remap = false)
public abstract class GroupEntryIconMixin {
    @Shadow @Final protected RecruitsGroup group;
    @Shadow protected ResourceLocation image;
    @Inject(method = "renderElement", at = @At("HEAD"))
    private void garrison$icon(GuiGraphics graphics, int index, int top, int left, int width, int height,
            int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY, CallbackInfo ci) {
        image = GarrisonIcon.forGroup(group);
    }
}
