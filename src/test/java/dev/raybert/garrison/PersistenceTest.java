package dev.raybert.garrison;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class PersistenceTest {
    static int checks;
    static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        UUID owner = UUID.randomUUID(), group = UUID.randomUUID(), recruit = UUID.randomUUID();
        GarrisonData g = new GarrisonData(group, owner); g.autoFill = true; g.holdPosts = false; g.deployed = true; g.color = 12;
        GarrisonData.Post p = new GarrisonData.Post(UUID.randomUUID(), new ResourceLocation("minecraft:overworld"),
                new BlockPos(12, 80, -16), 135, PostType.BOWMAN);
        p.recruit = recruit; p.active = false; p.state = GarrisonData.State.RELEASED; g.posts.add(p); g.reindex();
        GarrisonData copy = GarrisonData.load(g.save());
        check(copy.group.equals(group) && copy.owner.equals(owner), "Identity persists");
        check(copy.autoFill && !copy.holdPosts && copy.deployed, "Settings persist");
        check(copy.color == 12, "Group marker color persists");
        check(copy.assignments.containsKey(recruit), "Released assignment persists");
        check(copy.posts.get(0).position.equals(p.position), "Position persists");
        check(copy.posts.get(0).facing == 135 && copy.posts.get(0).type == PostType.BOWMAN, "Facing and type persist");
        check(!copy.posts.get(0).active, "Release persists");
        p.recruit = null; g.reindex(); copy = GarrisonData.load(g.save());
        check(copy.posts.size() == 1 && copy.assignments.isEmpty(), "Death leaves the post, not an assignment");
        GarrisonStore store = new GarrisonStore(); store.groups.put(group, g);
        UUID second = UUID.randomUUID(); store.groups.put(second, new GarrisonData(second, owner));
        GarrisonStore loaded = GarrisonStore.load(store.save(new CompoundTag()));
        check(loaded.groups.size() == 2 && loaded.groups.get(second).posts.isEmpty(), "Groups remain independent");
        check(loaded.groups.get(group).posts.size() == 1, "World store round trip preserves plan");
        loaded.groups.get(group).clearPosts();
        check(loaded.groups.get(group).posts.isEmpty() && loaded.groups.get(group).assignments.isEmpty()
                && !loaded.groups.get(group).deployed, "Remove all positions clears only the selected plan");
        check(loaded.groups.get(second).posts.isEmpty() && loaded.groups.containsKey(second), "Remove all positions leaves other groups intact");
        System.out.println("PASS: " + checks + " persistence checks");
    }
}
