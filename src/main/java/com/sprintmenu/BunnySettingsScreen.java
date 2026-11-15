package com.sprintmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BunnySettingsScreen extends Screen {
    private static final int W = 260;
    private static final int H = 220;
    private static final int PAD = 16;
    private static final int HEADER_H = 34;
    private static final int BTN_H = 22;
    private static final int SIDE_W = 28;

    private static final int WHITE = 0xFFF2F2F5;
    private static final int LIGHT_GRAY = 0xFFC9C9D0;
    private static final int PANEL_BG = 0xEE2A2A2F;
    private static final int PANEL_BORDER = 0xFF55555C;
    private static final int HEADER_BG = 0xFF36363C;
    private static final int GRAY = 0xFFA0A0A8;

    private int panelX, panelY;
    private ButtonWidget countButton;
    private ButtonWidget colorButton;
    private ButtonWidget optimizeButton;

    public BunnySettingsScreen() {
        super(Text.literal("Bunnies"));
    }

    @Override
    protected void init() {
        panelX = this.width / 2 - W / 2;
        panelY = this.height / 2 - H / 2;

        int inner = W - PAD * 2;
        int y = panelY + HEADER_H + 20;

        addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> {
            BunnyModule.setCount(BunnyModule.getCount() - 1);
            refresh();
        }).dimensions(panelX + PAD, y, SIDE_W, BTN_H).build());

        countButton = ButtonWidget.builder(countLabel(), button -> {})
                .dimensions(panelX + PAD + SIDE_W + 4, y, inner - SIDE_W * 2 - 8, BTN_H).build();
        addDrawableChild(countButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            BunnyModule.setCount(BunnyModule.getCount() + 1);
            refresh();
        }).dimensions(panelX + PAD + inner - SIDE_W, y, SIDE_W, BTN_H).build());

        colorButton = ButtonWidget.builder(colorLabel(), button -> {
            BunnyModule.nextColor();
            refresh();
        }).dimensions(panelX + PAD, y + BTN_H + 8, inner, BTN_H).build();
        addDrawableChild(colorButton);

        optimizeButton = ButtonWidget.builder(optimizeLabel(), button -> {
            BunnyModule.setOptimized(!BunnyModule.isOptimized());
            refresh();
        }).dimensions(panelX + PAD, y + BTN_H * 2 + 16, inner, BTN_H).build();
        addDrawableChild(optimizeButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> this.close())
                .dimensions(panelX + PAD, panelY + H - 34, inner, BTN_H).build());
    }

    private Text countLabel() {
        return Text.literal("Количество: " + BunnyModule.getCount());
    }

    private Text colorLabel() {
        return Text.literal("Цвет: " + BunnyModule.colorName());
    }

    private Text optimizeLabel() {
        return Text.literal("Оптимизация: " + (BunnyModule.isOptimized() ? "ON" : "OFF"));
    }

    private void refresh() {
        countButton.setMessage(countLabel());
        colorButton.setMessage(colorLabel());
        optimizeButton.setMessage(optimizeLabel());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        int x1 = panelX, y1 = panelY, x2 = panelX + W, y2 = panelY + H;
        int cx = this.width / 2;

        drawEar(context, cx - 38, y1);
        drawEar(context, cx + 26, y1);

        context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, PANEL_BORDER);
        context.fill(x1, y1, x2, y2, PANEL_BG);
        context.fill(x1, y1, x2, y1 + HEADER_H, HEADER_BG);
        context.fill(x1, y1 + HEADER_H, x2, y1 + HEADER_H + 2, WHITE);
    }

    private void drawEar(DrawContext context, int x, int panelTop) {
        context.fill(x, panelTop - 24, x + 12, panelTop + 2, WHITE);
        context.fill(x + 3, panelTop - 20, x + 9, panelTop + 2, LIGHT_GRAY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("BUNNIES").formatted(Formatting.BOLD),
                this.width / 2, panelY + 13, WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Красивые зайцы, плавная работа и оптимизация"),
                this.width / 2, panelY + HEADER_H + 8, GRAY);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(new SprintMenuScreen());
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
