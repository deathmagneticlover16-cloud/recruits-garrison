package dev.raybert.garrison.client;

import com.talhanation.recruits.client.events.CommandCategoryManager;
import com.talhanation.recruits.client.gui.CommandScreen;
import dev.raybert.garrison.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import java.util.*;

@Mod.EventBusSubscriber(modid = GarrisonMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GarrisonClient {
    public static final Map<UUID, GarrisonData> DATA = new HashMap<>();
    public static boolean show;
    public static PostType type = PostType.ANY;
    private static UUID editing;
    private static boolean removing;
    private static final Map<UUID, Long> requested = new HashMap<>();
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GarrisonNetwork.clientReceiver = packet -> {
                if (packet.data() == null || packet.data().isEmpty()) DATA.remove(packet.group());
                else DATA.put(packet.group(), GarrisonData.load(packet.data()));
                Minecraft mc = Minecraft.getInstance();
                // Public native setter rebuilds its controls, preserving group/category selection.
                if (mc.screen instanceof CommandScreen screen) screen.setTightFormation(CommandScreen.tightFormation);
            };
            CommandCategoryManager.register(new GarrisonCategory(), 100);
            MinecraftForge.EVENT_BUS.register(new GarrisonClient());
            MinecraftForge.EVENT_BUS.register(new PostRenderer());
        });
    }
    public static void refresh(List<UUID> groups) {
        long now = System.currentTimeMillis();
        List<UUID> stale = groups.stream().filter(id -> now - requested.getOrDefault(id, 0L) > 2000).limit(32).toList();
        if (stale.isEmpty()) return;
        stale.forEach(id -> requested.put(id, now));
        send(GarrisonNetwork.Action.SYNC, stale, false);
    }
    public static void send(GarrisonNetwork.Action action, List<UUID> groups, boolean enabled) {
        if (!groups.isEmpty()) GarrisonNetwork.CHANNEL.sendToServer(new GarrisonNetwork.Request(action, groups, BlockPos.ZERO, type, enabled, GarrisonData.DEFAULT_COLOR));
    }
    public static void setColor(UUID group, int color) {
        if (GarrisonPalette.valid(color)) GarrisonNetwork.CHANNEL.sendToServer(new GarrisonNetwork.Request(
                GarrisonNetwork.Action.SET_COLOR, List.of(group), BlockPos.ZERO, type, false, color));
    }
    public static void begin(UUID group, boolean remove) {
        editing = group; removing = remove; show = true;
        Minecraft mc = Minecraft.getInstance(); mc.setScreen(null);
        if (mc.player != null) mc.player.displayClientMessage(Component.literal(remove
                ? "Right-click the floor below a post to remove it. Escape cancels."
                : "Place " + type + " post: face its desired direction and right-click the floor. Escape cancels."), true);
    }
    @SubscribeEvent public void interact(InputEvent.InteractionKeyMappingTriggered event) {
        if (editing == null || !event.isUseItem()) return;
        event.setCanceled(true); event.setSwingHand(false);
        Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return;
        HitResult hit = mc.player.pick(64, 1, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) return;
        GarrisonNetwork.CHANNEL.sendToServer(new GarrisonNetwork.Request(removing ? GarrisonNetwork.Action.REMOVE : GarrisonNetwork.Action.ADD,
                List.of(editing), block.getBlockPos(), type, false, GarrisonData.DEFAULT_COLOR));
        editing = null;
    }
    @SubscribeEvent public void key(InputEvent.Key event) {
        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() == GLFW.GLFW_PRESS) editing = null;
    }
    @SubscribeEvent public void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        DATA.clear(); requested.clear(); editing = null; show = false;
    }
}
