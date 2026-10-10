package dev.lastpagans.garrison;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.phys.Vec3;

/** Scoped final approach for active posts; normal Recruits orders keep native navigation. */
public final class PostMovement {
    private PostMovement() {}
    public static boolean reached(AbstractRecruitEntity r, GarrisonData.Post p) {
        return PostArrival.reached(r.getX(), r.getY(), r.getZ(), p.position.getX(), p.position.getY(), p.position.getZ());
    }
    public static boolean startPath(AbstractRecruitEntity r, GarrisonData.Post p) {
        // Recruits' double-coordinate overload truncates negative coordinates and
        // allows a one-node reach radius. Use the exact saved block and zero radius.
        return r.getNavigation().moveTo(r.getNavigation().createPath(p.position, 0), r.moveSpeed);
    }
    public static GarrisonData.Post activePost(AbstractRecruitEntity r) {
        GarrisonData g = GarrisonController.data(r);
        GarrisonData.Post p = g == null || !g.deployed ? null : g.assignments.get(r.getUUID());
        return p != null && p.active && p.dimension.equals(r.level().dimension().location()) ? p : null;
    }
    public static void tick(AbstractRecruitEntity r, GarrisonData.Post p) {
        if (reached(r, p)) {
            if (!r.getNavigation().isDone()) r.getNavigation().stop();
            if (r.getShouldMovePos()) GarrisonController.internal(() -> {
                r.setShouldMovePos(false); r.clearMovePos(); r.reachedMovePos = true;
            });
            return;
        }
        Vec3 target = p.center();
        double dx = target.x - r.getX(), dy = target.y - r.getY(), dz = target.z - r.getZ();
        // Navigation may declare its last waypoint reached before the soldier is on
        // the marked block. Walk the last step through native MoveControl, never teleport.
        // Check the swept body volume first so this cannot cut through wall corners.
        if (!r.isPassenger() && dx * dx + dz * dz <= 2.25 && Math.abs(dy) <= 0.5
                && r.level().noCollision(r, r.getBoundingBox().expandTowards(dx, 0, dz).deflate(0.01))) {
            if (!r.getNavigation().isDone()) r.getNavigation().stop();
            r.getMoveControl().setWantedPosition(target.x, target.y, target.z, r.moveSpeed);
            return;
        }
        long now = r.level().getGameTime();
        if (now >= p.nextPathTick) {
            boolean started = startPath(r, p);
            p.nextPathTick = now + (started ? 20 : 40);
        }
    }
}
