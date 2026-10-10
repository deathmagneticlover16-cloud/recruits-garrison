package dev.raybert.garrison.mixin;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.RecruitHoldPosGoal;
import com.talhanation.recruits.entities.ai.RecruitMoveToPosGoal;
import dev.raybert.garrison.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {RecruitHoldPosGoal.class, RecruitMoveToPosGoal.class}, remap = false)
public abstract class PostMovementMixin {
    @Shadow @Final private AbstractRecruitEntity recruit;
    @Dynamic("Goal.tick uses its SRG name in production")
    @Inject(method = {"tick()V", "m_8037_()V"}, at = @At("HEAD"), cancellable = true)
    private void garrison$precisePost(CallbackInfo ci) {
        GarrisonData.Post post = PostMovement.activePost(recruit);
        if (post == null) return;
        PostMovement.tick(recruit, post);
        ci.cancel();
    }
}
