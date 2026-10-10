package dev.raybert.garrison;

import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class GarrisonStore extends SavedData {
    public final Map<UUID, GarrisonData> groups = new LinkedHashMap<>();
    public static GarrisonStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(GarrisonStore::load, GarrisonStore::new, "recruits_garrisons");
    }
    static GarrisonStore load(CompoundTag tag) {
        GarrisonStore store = new GarrisonStore();
        for (Tag entry : tag.getList("Groups", Tag.TAG_COMPOUND)) {
            GarrisonData data = GarrisonData.load((CompoundTag) entry); store.groups.put(data.group, data);
        }
        return store;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putInt("Schema", 1);
        ListTag list = new ListTag(); groups.values().forEach(g -> list.add(g.save())); tag.put("Groups", list);
        return tag;
    }
}
