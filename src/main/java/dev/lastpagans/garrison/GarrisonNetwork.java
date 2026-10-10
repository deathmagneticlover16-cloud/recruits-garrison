package dev.lastpagans.garrison;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.*;
import java.util.function.*;

public final class GarrisonNetwork {
    private static final String VERSION = "3";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GarrisonMod.ID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);
    /** Installed by client setup; no client classes are loaded by dedicated servers. */
    public static Consumer<Sync> clientReceiver = packet -> {};
    public enum Action { SYNC, CONVERT, ADD, REMOVE, REMOVE_ALL, MAN, FILL, AUTO, HOLD, SET_COLOR, REMOVE_GROUP }
    public record Request(Action action, List<UUID> groups, BlockPos position, PostType type, boolean enabled, int color) {
        static Request decode(FriendlyByteBuf buf) {
            Action action = buf.readEnum(Action.class);
            int size = buf.readVarInt();
            if (size < 1 || size > 32) throw new IllegalArgumentException("Invalid group count");
            List<UUID> groups = new ArrayList<>(); for (int i = 0; i < size; i++) groups.add(buf.readUUID());
            return new Request(action, groups, buf.readBlockPos(), buf.readEnum(PostType.class), buf.readBoolean(), buf.readVarInt());
        }
        void encode(FriendlyByteBuf buf) {
            buf.writeEnum(action); buf.writeVarInt(groups.size()); groups.forEach(buf::writeUUID);
            buf.writeBlockPos(position); buf.writeEnum(type); buf.writeBoolean(enabled); buf.writeVarInt(color);
        }
        void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender(); if (player == null) return;
                CompoundTag tag = player.getPersistentData(); long tick = player.serverLevel().getGameTime();
                if (tag.getLong("GarrisonRequestTick") != tick) { tag.putLong("GarrisonRequestTick", tick); tag.putInt("GarrisonRequestCount", 0); }
                int count = tag.getInt("GarrisonRequestCount"); if (count >= 32) return;
                tag.putInt("GarrisonRequestCount", count + 1);
                GarrisonController.request(player, this);
            });
            ctx.setPacketHandled(true);
        }
    }
    public record Sync(UUID group, CompoundTag data) {
        static Sync decode(FriendlyByteBuf buf) { return new Sync(buf.readUUID(), buf.readNbt()); }
        void encode(FriendlyByteBuf buf) { buf.writeUUID(group); buf.writeNbt(data); }
        void handle(Supplier<NetworkEvent.Context> supplier) {
            supplier.get().enqueueWork(() -> clientReceiver.accept(this)); supplier.get().setPacketHandled(true);
        }
    }
    public static void register() {
        CHANNEL.registerMessage(0, Request.class, Request::encode, Request::decode, Request::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1, Sync.class, Sync::encode, Sync::decode, Sync::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    public static void sync(ServerPlayer player, UUID group, GarrisonData data) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Sync(group, data == null ? new CompoundTag() : data.save()));
    }
}
