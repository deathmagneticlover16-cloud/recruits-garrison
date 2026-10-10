package dev.raybert.garrison;

import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.world.RecruitsGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.util.*;
import static dev.raybert.garrison.GarrisonData.*;

public final class GarrisonController {
    private static final ThreadLocal<Integer> INTERNAL = ThreadLocal.withInitial(() -> 0);
    private static final String OWNED_HOLD = "GarrisonManagedHold";
    private static final String OLD_HOLD = "GarrisonPreviousHoldFormation";
    public static void internal(Runnable action) {
        INTERNAL.set(INTERNAL.get() + 1);
        try { action.run(); } finally { INTERNAL.set(INTERNAL.get() - 1); }
    }
    public static RecruitsGroup group(UUID id) {
        return RecruitEvents.recruitsGroupsManager == null ? null : RecruitEvents.recruitsGroupsManager.getGroup(id);
    }
    public static GarrisonData data(AbstractRecruitEntity r) {
        if (!r.isAddedToWorld() || !(r.level() instanceof ServerLevel level) || r.getGroup() == null) return null;
        GarrisonData g = GarrisonStore.get(level.getServer()).groups.get(r.getGroup());
        if (g == null || !g.owner.equals(r.getOwnerUUID())) return null;
        RecruitsGroup nativeGroup = group(g.group);
        return nativeGroup != null && nativeGroup.getUUID().equals(g.group) ? g : null;
    }
    public static boolean locked(AbstractRecruitEntity r) {
        GarrisonData g = data(r);
        if (g == null || !g.holdPosts || !g.deployed) return false;
        Post p = g.assignments.get(r.getUUID());
        return p != null && p.active && p.dimension.equals(r.level().dimension().location());
    }
    public static boolean blockMovement(AbstractRecruitEntity r) { return INTERNAL.get() == 0 && locked(r); }
    public static void ordinaryMovement(AbstractRecruitEntity r) {
        if (INTERNAL.get() != 0 || locked(r)) return;
        GarrisonData g = data(r);
        Post p = g == null ? null : g.assignments.get(r.getUUID());
        if (p != null && p.active) {
            p.active = false; p.state = State.RELEASED; release(r);
            GarrisonStore.get(r.getServer()).setDirty();
        }
    }
    public static boolean awayFromPost(AbstractRecruitEntity r) {
        GarrisonData g = data(r);
        Post p = g == null ? null : g.assignments.get(r.getUUID());
        return p != null && !PostMovement.reached(r, p);
    }
    static AbstractRecruitEntity find(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity instanceof AbstractRecruitEntity r) return r;
        }
        return null; // Unloaded is NOT dead.
    }
    static void deploy(AbstractRecruitEntity r, Post p) {
        p.active = true;
        internal(() -> {
            CompoundTag tag = r.getPersistentData();
            if (!tag.getBoolean(OWNED_HOLD)) tag.putBoolean(OLD_HOLD, r.holdFormation);
            tag.putBoolean(OWNED_HOLD, true);
            r.getNavigation().stop();
            r.setShouldRest(false);
            r.setShouldMount(false);
            r.forcedUpkeep = false;
            r.setUpkeepTimer(r.getUpkeepCooldown());
            r.clearTarget();
            r.setIsFollowing(false);
            r.setHoldPos(p.center()); r.setFollowState(3);
            r.setMovePos(p.position);
            r.setShouldMovePos(true);
            r.isInFormation = false; r.holdFormation = true;
            r.ownerRot = p.facing; r.rotateTicks = 40;
            CommandEvents.checkPatrolLeaderState(r);
            // The garrison command itself issues the move. Do not wait for another order
            // or rely solely on a hold goal becoming eligible on a later tick.
            p.nextPathTick = 0;
            PostMovement.startPath(r, p);
        });
    }
    static void release(AbstractRecruitEntity r) {
        CompoundTag tag = r.getPersistentData();
        if (!tag.getBoolean(OWNED_HOLD)) return;
        internal(() -> {
            r.getNavigation().stop(); r.setFollowState(2);
            r.clearMovePos();
            r.holdFormation = tag.getBoolean(OLD_HOLD);
            tag.remove(OWNED_HOLD); tag.remove(OLD_HOLD);
        });
    }
    static void releaseAll(MinecraftServer server, GarrisonData g) {
        for (UUID id : g.assignments.keySet()) { AbstractRecruitEntity r = find(server, id); if (r != null) release(r); }
        g.posts.forEach(p -> p.active = false);
    }
    /** Keeps valid/unloaded assignments. Only definitive membership changes or death free a post. */
    static void validate(MinecraftServer server, GarrisonData g) {
        RecruitsGroup nativeGroup = group(g.group);
        Set<UUID> members = nativeGroup == null ? Set.of() : new HashSet<>(nativeGroup.members);
        for (Post p : g.posts) {
            if (p.recruit == null) { p.state = State.VACANT; continue; }
            AbstractRecruitEntity r = find(server, p.recruit);
            boolean invalid = !members.contains(p.recruit) || (r != null && (!r.isAlive()
                    || !Objects.equals(r.getGroup(), g.group) || !Objects.equals(r.getOwnerUUID(), g.owner) || !p.type.accepts(r)));
            if (invalid) { if (r != null) release(r); p.recruit = null; p.state = State.VACANT; continue; }
            if (r == null || !r.level().dimension().location().equals(p.dimension)) { p.state = State.ABSENT; continue; }
            if (!p.active || !g.deployed) { release(r); p.state = State.RELEASED; continue; }
            if (!r.getPersistentData().getBoolean(OWNED_HOLD)) deploy(r, p);
            p.state = PostMovement.reached(r, p) ? State.OCCUPIED : State.MOVING;
            // The scoped native-goal hooks own navigation and final approach.
            if (p.state == State.OCCUPIED && r.getTarget() == null) {
                r.ownerRot = p.facing; r.rotateTicks = 20;
            }
        }
        g.reindex();
    }
    static void fill(MinecraftServer server, GarrisonData g) {
        RecruitsGroup nativeGroup = group(g.group);
        if (nativeGroup == null) return;
        List<AbstractRecruitEntity> reserves = new ArrayList<>();
        for (UUID id : nativeGroup.members) {
            if (g.assignments.containsKey(id)) continue;
            AbstractRecruitEntity r = find(server, id);
            if (r != null && r.isAlive() && g.owner.equals(r.getOwnerUUID()) && g.group.equals(r.getGroup())) reserves.add(r);
        }
        reserves.sort(Comparator.comparing(Entity::getUUID));
        Map<Post, AbstractRecruitEntity> plan = AssignmentPlanner.plan(g.posts.stream().filter(p -> p.recruit == null).toList(),
                reserves, GarrisonController::compatible, p -> p.type.specificity(), (r, p) -> r.distanceToSqr(p.center()));
        for (Map.Entry<Post, AbstractRecruitEntity> assignment : plan.entrySet()) {
            Post p = assignment.getKey(); AbstractRecruitEntity best = assignment.getValue();
            p.recruit = best.getUUID(); p.state = State.MOVING;
            g.assignments.put(p.recruit, p); reserves.remove(best); deploy(best, p);
        }
    }
    private static boolean compatible(AbstractRecruitEntity r, Post p) {
        return p.dimension.equals(r.level().dimension().location()) && p.type.accepts(r);
    }
    public static void request(ServerPlayer player, GarrisonNetwork.Request req) {
        MinecraftServer server = player.getServer();
        GarrisonStore store = GarrisonStore.get(server);
        LinkedHashSet<UUID> requested = new LinkedHashSet<>(req.groups());
        if (requested.isEmpty() || requested.size() > 32) return;
        boolean edit = req.action() == GarrisonNetwork.Action.ADD || req.action() == GarrisonNetwork.Action.REMOVE
                || req.action() == GarrisonNetwork.Action.REMOVE_ALL || req.action() == GarrisonNetwork.Action.SET_COLOR;
        if (edit && requested.size() != 1) return;
        for (UUID id : requested) {
            RecruitsGroup nativeGroup = group(id);
            if (nativeGroup == null || !nativeGroup.getUUID().equals(id) || nativeGroup.removed
                    || !player.getUUID().equals(nativeGroup.getPlayerUUID())) continue;
            GarrisonData g = store.groups.get(id);
            if (req.action() == GarrisonNetwork.Action.REMOVE_GROUP) {
                if (requested.size() == 1 && g != null && g.owner.equals(player.getUUID())) {
                    releaseAll(server, g);
                    store.groups.remove(id); store.setDirty();
                    GarrisonNetwork.sync(player, id, null);
                }
                continue;
            }
            if (req.action() == GarrisonNetwork.Action.SYNC) { GarrisonNetwork.sync(player, id, g); continue; }
            if (req.action() == GarrisonNetwork.Action.CONVERT) {
                if (requested.size() == 1 && g == null) {
                    g = new GarrisonData(id, player.getUUID()); store.groups.put(id, g);
                }
            } else if (g != null && g.owner.equals(player.getUUID())) {
                validate(server, g);
                switch (req.action()) {
                    case ADD -> {
                        BlockPos feet = validatedPlacement(player, req.position());
                        if (feet == null || g.posts.size() >= 256) { tell(player, "Invalid post: aim at a solid floor with two clear blocks above it (within 64 blocks). Maximum 256 posts per group."); break; }
                        boolean duplicate = g.posts.stream().anyMatch(p -> p.position.equals(feet) && p.dimension.equals(player.level().dimension().location()));
                        if (!duplicate) g.posts.add(new Post(UUID.randomUUID(), player.level().dimension().location(), feet, player.getYRot(), req.type()));
                    }
                    case REMOVE -> {
                        BlockHitResult hit = ray(player);
                        if (hit.getType() != HitResult.Type.BLOCK) break;
                        Post nearest = g.posts.stream().filter(p -> p.dimension.equals(player.level().dimension().location()))
                                .filter(p -> p.position.distSqr(hit.getBlockPos().above()) <= 4)
                                .min(Comparator.comparingDouble(p -> p.position.distSqr(hit.getBlockPos().above()))).orElse(null);
                        if (nearest != null) {
                            if (nearest.recruit != null) { AbstractRecruitEntity r = find(server, nearest.recruit); if (r != null) release(r); }
                            g.posts.remove(nearest); g.reindex();
                        }
                    }
                    case REMOVE_ALL -> {
                        releaseAll(server, g);
                        g.clearPosts();
                    }
                    case MAN -> {
                        g.deployed = true; g.holdPosts = true;
                        for (Post p : g.posts) if (p.recruit != null) {
                            AbstractRecruitEntity r = find(server, p.recruit); if (r != null && compatible(r, p)) deploy(r, p);
                        }
                        fill(server, g);
                    }
                    case FILL -> {
                        g.deployed = true;
                        fill(server, g);
                    }
                    case AUTO -> g.autoFill = req.enabled();
                    case SET_COLOR -> {
                        if (GarrisonPalette.valid(req.color())) g.color = req.color();
                    }
                    case HOLD -> {
                        g.holdPosts = req.enabled();
                        if (!g.holdPosts) releaseAll(server, g);
                        else if (g.deployed) for (Post p : g.posts) if (p.recruit != null) {
                            AbstractRecruitEntity r = find(server, p.recruit); if (r != null && compatible(r, p)) deploy(r, p);
                        }
                    }
                    default -> { }
                }
            }
            if (g != null) { validate(server, g); store.setDirty(); GarrisonNetwork.sync(player, id, g); }
        }
    }
    private static BlockHitResult ray(ServerPlayer player) {
        return player.level().clip(new net.minecraft.world.level.ClipContext(player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(64)),
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
    }
    private static BlockPos validatedPlacement(ServerPlayer player, BlockPos claimed) {
        BlockHitResult hit = ray(player);
        if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(claimed)) return null;
        BlockPos feet = claimed.above(); ServerLevel level = player.serverLevel();
        if (!level.hasChunkAt(feet) || !level.getWorldBorder().isWithinBounds(feet) || feet.getY() >= level.getMaxBuildHeight() - 1) return null;
        if (!level.getBlockState(claimed).isFaceSturdy(level, claimed, net.minecraft.core.Direction.UP)) return null;
        return level.noCollision(new AABB(feet).inflate(-0.15, 0, -0.15).expandTowards(0, 0.8, 0)) ? feet : null;
    }
    private static void tell(ServerPlayer p, String message) { p.sendSystemMessage(Component.literal("Garrison: " + message)); }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (event.phase != TickEvent.Phase.END || server == null || server.getTickCount() % 20 != 0) return;
        GarrisonStore store = GarrisonStore.get(server);
        for (GarrisonData g : store.groups.values()) {
            CompoundTag before = g.save();
            validate(server, g);
            if (g.autoFill && g.holdPosts && g.deployed && g.posts.stream().anyMatch(p -> p.recruit == null)) fill(server, g);
            if (!before.equals(g.save())) {
                store.setDirty(); ServerPlayer owner = server.getPlayerList().getPlayer(g.owner);
                if (owner != null) GarrisonNetwork.sync(owner, g.group, g);
            }
        }
    }
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST) public void death(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof AbstractRecruitEntity r) || !(r.level() instanceof ServerLevel level)) return;
        GarrisonData g = data(r); if (g == null) return;
        Post p = g.assignments.remove(r.getUUID());
        if (p != null) { p.recruit = null; p.state = State.VACANT; GarrisonStore.get(level.getServer()).setDirty();
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(g.owner);
            if (owner != null) GarrisonNetwork.sync(owner, g.group, g);
        }
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) for (GarrisonData g : GarrisonStore.get(player.getServer()).groups.values())
            if (g.owner.equals(player.getUUID())) GarrisonNetwork.sync(player, g.group, g);
    }
    @SubscribeEvent public void join(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof AbstractRecruitEntity r) || !(event.getLevel() instanceof ServerLevel level)) return;
        level.getServer().execute(() -> {
            GarrisonData g = data(r); Post p = g == null ? null : g.assignments.get(r.getUUID());
            if (p == null || !p.active || !p.dimension.equals(r.level().dimension().location())) release(r);
            else deploy(r, p);
        });
    }
}
