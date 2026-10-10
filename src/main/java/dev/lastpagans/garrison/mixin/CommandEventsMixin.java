package dev.lastpagans.garrison.mixin;

import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import dev.lastpagans.garrison.GarrisonController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;

/** Filter before formation slot calculation, including Recruits' automatic formation-follow. */
@Mixin(value = CommandEvents.class, remap = false)
public abstract class CommandEventsMixin {
    @ModifyVariable(method = {
        "onMovementCommand(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/List;IIZZ)V",
        "applyFormation(ILjava/util/List;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/phys/Vec3;ZZ)V",
        "onFaceCommand"}, at = @At("HEAD"), argsOnly = true)
    private static List<AbstractRecruitEntity> garrison$reserves(List<AbstractRecruitEntity> input) {
        return new ArrayList<>(input.stream().filter(r -> !GarrisonController.locked(r)).toList());
    }
    @Inject(method = "onMovementCommand(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/List;IIZZ)V", at = @At("HEAD"), cancellable = true)
    private static void garrison$emptyMove(ServerPlayer player, List<AbstractRecruitEntity> list, int state, int formation, boolean tight, boolean hold, CallbackInfo ci) {
        if (list.stream().allMatch(GarrisonController::locked)) ci.cancel();
    }
    @Inject(method = "applyFormation(ILjava/util/List;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/phys/Vec3;ZZ)V", at = @At("HEAD"), cancellable = true)
    private static void garrison$emptyFormation(int formation, List<AbstractRecruitEntity> list, ServerPlayer player, Vec3 position, boolean tight, boolean hold, CallbackInfo ci) {
        if (list.stream().allMatch(GarrisonController::locked)) ci.cancel();
    }
}
