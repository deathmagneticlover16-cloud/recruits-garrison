package dev.raybert.garrison.client;

import dev.raybert.garrison.GarrisonPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.UUID;

/** Group-specific palette using the same colors and translation keys as Recruits factions. */
public final class GarrisonColorScreen extends Screen {
    private static final int COLUMNS = 5;
    private static final int CELL = 24;
    private final Screen parent;
    private final UUID group;
    private final String groupName;
    private int selected;
    private int paletteX;
    private int paletteY;

    public GarrisonColorScreen(Screen parent, UUID group, String groupName, int selected) {
        super(Component.literal("Set Group Color"));
        this.parent = parent;
        this.group = group;
        this.groupName = groupName;
        this.selected = GarrisonPalette.valid(selected) ? selected : 0;
    }

    @Override protected void init() {
        paletteX = (width - COLUMNS * CELL) / 2;
        paletteY = (height - COLUMNS * CELL) / 2 - 4;
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 50, paletteY + COLUMNS * CELL + 18, 100, 20).build());
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, paletteY - 34, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal(groupName), width / 2, paletteY - 21, 0xB0B0B0);
        for (int i = 0; i < GarrisonPalette.RGB.length; i++) {
            int x = paletteX + (i % COLUMNS) * CELL;
            int y = paletteY + (i / COLUMNS) * CELL;
            int border = i == selected ? 0xFFFFFFFF : isOver(mouseX, mouseY, x, y) ? 0xFFAAAAAA : 0xFF444444;
            graphics.fill(x, y, x + CELL - 2, y + CELL - 2, border);
            graphics.fill(x + 2, y + 2, x + CELL - 4, y + CELL - 4, 0xFF202020);
            graphics.drawCenteredString(font, "X", x + (CELL - 2) / 2, y + 7, GarrisonPalette.RGB[i]);
        }
        int hovered = indexAt(mouseX, mouseY);
        int label = hovered >= 0 ? hovered : selected;
        graphics.drawCenteredString(font, Component.translatable("gui.recruits.team_color." + GarrisonPalette.NAMES[label]),
                width / 2, paletteY + COLUMNS * CELL + 5, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int index = indexAt(mouseX, mouseY);
            if (index >= 0) {
                selected = index;
                GarrisonClient.setColor(group, index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int indexAt(double mouseX, double mouseY) {
        if (mouseX < paletteX || mouseY < paletteY || mouseX >= paletteX + COLUMNS * CELL || mouseY >= paletteY + COLUMNS * CELL) return -1;
        int column = (int) (mouseX - paletteX) / CELL;
        int row = (int) (mouseY - paletteY) / CELL;
        int index = row * COLUMNS + column;
        return index < GarrisonPalette.RGB.length ? index : -1;
    }

    private static boolean isOver(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + CELL - 2 && mouseY >= y && mouseY < y + CELL - 2;
    }

    @Override public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override public boolean isPauseScreen() {
        return false;
    }
}
