package dev.raybert.garrison.mixin;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.RecruitMeleeAttackGoal;
import com.talhanation.recruits.util.AttackUtil;
import dev.raybert.garrison.GarrisonController;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Native melee claims MOVE while chasing. Yield to native HoldPosGoal until at the post. */
@Mixin(value = RecruitMeleeAttackGoal.class, remap = false)
public abstract class MeleePostMixin {
    @Shadow @Final protected AbstractRecruitEntity recruit;
    // These override vanilla Goal methods. Both dev (Mojmap) and runtime (SRG)
    // names are intentional: the addon has no refmap or MixinGradle dependency.
    @Dynamic("Vanilla Goal overrides use SRG names in a production Forge jar")
    @Inject(method = {"canUse()Z", "canContinueToUse()Z", "m_8036_()Z", "m_8045_()Z"}, at = @At("HEAD"), cancellable = true, require = 2)
    private void garrison$stay(CallbackInfoReturnable<Boolean> cir) {
        if (GarrisonController.locked(recruit) && (GarrisonController.awayFromPost(recruit)
                || recruit.getTarget() == null || recruit.distanceToSqr(recruit.getTarget()) > AttackUtil.getAttackReachSqr(recruit))) cir.setReturnValue(false);
    }
}
