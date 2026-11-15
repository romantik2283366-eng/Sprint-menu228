package com.sprintmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SprintMenuScreen extends Screen {
    private static final int PANEL_W = 230;
    private static final int BTN_W = 200;
    private static final int BTN_H = 22;
    private static final int GAP = 6;
    private static final int HEADER_H = 34;

    private static final int WHITE = 0xFFF2F2F5;
    private static final int LIGHT_GRAY = 0xFFC9C9D0;
    private static final int PANEL_BG = 0xEE2A2A2F;
    private static final int PANEL_BORDER = 0xFF55555C;
    private static final int HEADER_BG = 0xFF36363C;
    private static final int GRAY = 0xFFA0A0A8;

    private record Toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {}

    // Чтобы добавить новую функцию, допиши сюда ещё одну строку new Toggle(...)
    private final List<Toggle> toggles = List.of(
            new Toggle("Sprint", SprintModule::isEnabled, SprintModule::setEnabled)
    );

    private int panelX, panelY, panelH;

    public SprintMenuScreen() {
        super(Text.literal("Bunny DLC"));
    }

    @Override
    protected void init() {
        panelH = HEADER_H + 24 + toggles.size() * (BTN_H + GAP) + BTN_H + 28;
        panelX = this.width / 2 - PANEL_W / 2;
        panelY = this.height / 2 - panelH / 2;

        int bx = panelX + (PANEL_W - BTN_W) / 2;
        int y = panelY + HEADER_H + 24;

        for (Toggle t : toggles) {
            addDrawableChild(ButtonWidget.builder(label(t), button -> {
                t.set().accept(!t.get().getAsBoolean());
                button.setMessage(label(t));
            }).dimensions(bx, y, BTN_W, BTN_H).build());
            y += BTN_H + GAP;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button -> this.close())
                .dimensions(bx, y, BTN_W, BTN_H).build());
    }

    private Text label(Toggle t) {
        Text state = t.get().getAsBoolean()
                ? Text.literal("ON").formatted(Formatting.GREEN)
                : Text.literal("OFF").formatted(Formatting.RED);
        return Text.literal(t.name() + ": ").append(state);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        int x1 = panelX, y1 = panelY, x2 = panelX + PANEL_W, y2 = panelY + panelH;
        int cx = this.width / 2;

        // Заячьи ушки над панелью
        drawEar(context, cx - 38, y1);
        drawEar(context, cx + 26, y1);

        // Панель
        context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, PANEL_BORDER);
        context.fill(x1, y1, x2, y2, PANEL_BG);

        // Шапка
        context.fill(x1, y1, x2, y1 + HEADER_H, HEADER_BG);
        context.fill(x1, y1 + HEADER_H, x2, y1 + HEADER_H + 2, WHITE);

        // Нижняя линия
        context.fill(x1 + 14, y2 - 24, x2 - 14, y2 - 23, PANEL_BORDER);
    }

    private void drawEar(DrawContext context, int x, int panelTop) {
        context.fill(x, panelTop - 24, x + 12, panelTop + 2, WHITE);
        context.fill(x + 3, panelTop - 20, x + 9, panelTop + 2, LIGHT_GRAY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int bx = panelX + (PANEL_W - BTN_W) / 2;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("BUNNY DLC").formatted(Formatting.BOLD),
                cx, panelY + 12, WHITE);

        context.drawTextWithShadow(this.textRenderer,
                Text.literal("MOVEMENT"), bx, panelY + HEADER_H + 10, GRAY);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Right Shift - открыть / закрыть"),
                cx, panelY + panelH - 17, GRAY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
