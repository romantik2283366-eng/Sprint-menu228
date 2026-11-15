package com.sprintmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TriggerBotSettingsScreen extends Screen {
    private static final int W = 280;
    private static final int H = 170;
    private static final int BG = 0xEE2A2A2F;
    private static final int WHITE = 0xFFF2F2F5;
    private ButtonWidget critButton;

    public TriggerBotSettingsScreen() { super(Text.literal("Trigger Bot")); }

    @Override
    protected void init() {
        int x = width / 2 - W / 2;
        int y = height / 2 - H / 2;
        critButton = addDrawableChild(ButtonWidget.builder(critLabel(), b -> {
            TriggerBotModule.setSmartCrits(!TriggerBotModule.isSmartCrits());
            critButton.setMessage(critLabel());
        }).dimensions(x + 16, y + 65, W - 32, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> close())
                .dimensions(x + 16, y + H - 34, W - 32, 22).build());
    }

    private Text critLabel() {
        return Text.literal("Умные криты: " + (TriggerBotModule.isSmartCrits() ? "ON" : "OFF"));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int x = width / 2 - W / 2;
        int y = height / 2 - H / 2;
        context.fill(x - 1, y - 1, x + W + 1, y + H + 1, 0xFF55555C);
        context.fill(x, y, x + W, y + H, BG);
        context.fill(x, y, x + W, y + 3, 0xFFFFD6E4);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("TRIGGER BOT").formatted(Formatting.BOLD), width / 2, height / 2 - H / 2 + 20, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Бьёт только цель под прицелом и по ванильному кулдауну"), width / 2, height / 2 - H / 2 + 42, 0xFFA0A0A8);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(new SprintMenuScreen());
    }

    @Override
    public boolean shouldPause() { return false; }
}
