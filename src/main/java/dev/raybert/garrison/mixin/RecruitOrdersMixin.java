package dev.raybert.garrison.mixin;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import dev.raybert.garrison.GarrisonController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Protect only movement setters. Targets, aggro state, shields and firing stay native. */
@Mixin(value = AbstractRecruitEntity.class, remap = false)
public abstract class RecruitOrdersMixin {
    private boolean garrison$guard() {
        AbstractRecruitEntity r = (AbstractRecruitEntity)(Object)this;
        if (GarrisonController.blockMovement(r)) return true;
        GarrisonController.ordinaryMovement(r); return false;
    }
    @Inject(method = "setFollowState", at = @At("HEAD"), cancellable = true)
    private void garrison$follow(int state, CallbackInfo ci) { if (garrison$guard()) ci.cancel(); }
    @Inject(method = "setHoldPos", at = @At("HEAD"), cancellable = true)
    private void garrison$hold(Vec3 pos, CallbackInfo ci) { if (garrison$guard()) ci.cancel(); }
    @Inject(method = "setMovePos", at = @At("HEAD"), cancellable = true)
    private void garrison$move(BlockPos pos, CallbackInfo ci) { if (garrison$guard()) ci.cancel(); }
    @Inject(method = {"setShouldFollow", "setShouldProtect", "setShouldMovePos", "setShouldMount", "setShouldRest"}, at = @At("HEAD"), cancellable = true)
    private void garrison$movementFlag(boolean value, CallbackInfo ci) {
        if (value && garrison$guard()) ci.cancel();
    }
}
