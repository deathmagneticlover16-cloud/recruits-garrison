package dev.lastpagans.garrison.mixin;

import com.talhanation.recruits.client.gui.group.RecruitsCategoryButton;
import dev.lastpagans.garrison.client.GarrisonIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Retains the native button/background/click behavior; swaps only this category's art. */
@Mixin(value = RecruitsCategoryButton.class, remap = false)
public abstract class CategoryIconMixin {
    @Shadow private ItemStack renderItem;
    @Shadow private int x;
    @Shadow private int y;
    @Unique private ItemStack garrison$original;
    @Dynamic("AbstractWidget render override")
    @Inject(method = {"render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"}, at = @At("HEAD"))
    private void garrison$hideItem(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        garrison$original = GarrisonIcon.isCategory(renderItem) ? renderItem : null;
        if (garrison$original != null) renderItem = ItemStack.EMPTY;
    }
    @Dynamic("AbstractWidget render override")
    @Inject(method = {"render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"}, at = @At("TAIL"))
    private void garrison$draw(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (garrison$original == null) return;
        renderItem = garrison$original; garrison$original = null;
        graphics.blit(GarrisonIcon.TEXTURE, x, y, 16, 16, 0, 0, 21, 21, 21, 21);
    }
}
