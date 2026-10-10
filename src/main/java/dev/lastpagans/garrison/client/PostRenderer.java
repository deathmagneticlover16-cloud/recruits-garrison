package dev.lastpagans.garrison.client;

import com.mojang.blaze3d.vertex.*;
import com.talhanation.recruits.client.ClientManager;
import dev.lastpagans.garrison.GarrisonData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import java.util.*;

public final class PostRenderer {
    private final MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(new BufferBuilder(4096));
    @SubscribeEvent public void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!GarrisonClient.show || mc.level == null || mc.player == null
                || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || ClientManager.groups == null) return;
        Set<UUID> selected = new HashSet<>();
        ClientManager.groups.stream().filter(g -> !g.isDisabled() && !g.removed).forEach(g -> selected.add(g.getUUID()));
        Vec3 camera = event.getCamera().getPosition(); PoseStack pose = event.getPoseStack();
        for (UUID id : selected) {
            GarrisonData g = GarrisonClient.DATA.get(id); if (g == null) continue;
            for (GarrisonData.Post p : g.posts) {
                if (!p.dimension.equals(mc.level.dimension().location()) || p.center().distanceToSqr(camera) > 96 * 96) continue;
                float red = p.state == GarrisonData.State.OCCUPIED ? 0.2f : 1f;
                float green = p.state == GarrisonData.State.VACANT ? 0.2f : 0.8f;
                float blue = p.state == GarrisonData.State.ABSENT || p.state == GarrisonData.State.RELEASED ? 1f : 0.2f;
                Vec3 at = p.center().subtract(camera); pose.pushPose(); pose.translate(at.x, at.y + 0.03, at.z);
                VertexConsumer lines = buffers.getBuffer(RenderType.lines());
                // Outline the actual supporting block selected by the player.
                LevelRenderer.renderLineBox(pose, lines, new AABB(-0.5, -1.03, -0.5, 0.5, 0, 0.5), red, green, blue, 1);
                float dx = (float)-Math.sin(Math.toRadians(p.facing)), dz = (float)Math.cos(Math.toRadians(p.facing));
                Matrix4f matrix = pose.last().pose();
                lines.vertex(matrix, 0, 0.12f, 0).color(red, green, blue, 1).normal(pose.last().normal(), dx, 0, dz).endVertex();
                lines.vertex(matrix, dx, 0.12f, dz).color(red, green, blue, 1).normal(pose.last().normal(), dx, 0, dz).endVertex();
                pose.translate(0, 0.45, 0); pose.mulPose(event.getCamera().rotation()); pose.scale(-0.018f, -0.018f, 0.018f);
                String label = p.type + " / " + p.state;
                mc.font.drawInBatch(label, -mc.font.width(label) / 2f, 0, 0xFFFFFFFF, false,
                        pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0x60000000, 15728880);
                pose.popPose();
            }
        }
        buffers.endBatch();
    }
}
