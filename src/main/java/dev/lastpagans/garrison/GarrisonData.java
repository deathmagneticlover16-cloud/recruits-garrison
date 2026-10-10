package dev.lastpagans.garrison;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class GarrisonData {
    public static final int DEFAULT_COLOR = GarrisonPalette.DEFAULT_COLOR;
    public static final int COLOR_COUNT = GarrisonPalette.RGB.length;
    public final UUID group;
    public final UUID owner;
    public int color = DEFAULT_COLOR;
    public boolean autoFill;
    public boolean holdPosts = true;
    public boolean deployed;
    public final List<Post> posts = new ArrayList<>();
    public final Map<UUID, Post> assignments = new HashMap<>();
    public GarrisonData(UUID group, UUID owner) { this.group = group; this.owner = owner; }
    public void reindex() {
        assignments.clear();
        for (Post p : posts) if (p.recruit != null) {
            if (assignments.putIfAbsent(p.recruit, p) != null) p.recruit = null;
        }
    }
    public void clearPosts() {
        posts.clear();
        assignments.clear();
        deployed = false;
    }
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Group", group); tag.putUUID("Owner", owner);
        tag.putInt("Color", color);
        tag.putBoolean("AutoFill", autoFill); tag.putBoolean("HoldPosts", holdPosts);
        tag.putBoolean("Deployed", deployed);
        ListTag list = new ListTag(); posts.forEach(p -> list.add(p.save())); tag.put("Posts", list);
        return tag;
    }
    public static GarrisonData load(CompoundTag tag) {
        GarrisonData data = new GarrisonData(tag.getUUID("Group"), tag.getUUID("Owner"));
        int color = tag.getInt("Color");
        data.color = color >= 0 && color < COLOR_COUNT ? color : DEFAULT_COLOR;
        data.autoFill = tag.getBoolean("AutoFill"); data.holdPosts = tag.getBoolean("HoldPosts");
        data.deployed = tag.getBoolean("Deployed");
        for (Tag entry : tag.getList("Posts", Tag.TAG_COMPOUND)) data.posts.add(Post.load((CompoundTag) entry));
        data.reindex(); return data;
    }
    public enum State { VACANT, MOVING, OCCUPIED, ABSENT, RELEASED }
    public static final class Post {
        public final UUID id;
        public final ResourceLocation dimension;
        public final BlockPos position;
        public final float facing;
        public final PostType type;
        public UUID recruit;
        public boolean active;
        public long nextPathTick; // Runtime retry budget; deliberately not saved or synced.
        public State state = State.VACANT;
        public Post(UUID id, ResourceLocation dimension, BlockPos position, float facing, PostType type) {
            this.id = id; this.dimension = dimension; this.position = position.immutable();
            this.facing = facing; this.type = type;
        }
        public Vec3 center() { return Vec3.atBottomCenterOf(position); }
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", id); tag.putString("Dimension", dimension.toString());
            tag.putLong("Position", position.asLong()); tag.putFloat("Facing", facing);
            tag.putString("Type", type.name()); tag.putString("State", state.name());
            tag.putBoolean("Active", active);
            if (recruit != null) tag.putUUID("Recruit", recruit);
            return tag;
        }
        static Post load(CompoundTag tag) {
            Post p = new Post(tag.getUUID("Id"), new ResourceLocation(tag.getString("Dimension")),
                    BlockPos.of(tag.getLong("Position")), tag.getFloat("Facing"), PostType.valueOf(tag.getString("Type")));
            if (tag.hasUUID("Recruit")) p.recruit = tag.getUUID("Recruit");
            p.active = tag.getBoolean("Active");
            if (tag.contains("State")) p.state = State.valueOf(tag.getString("State"));
            return p;
        }
    }
}
