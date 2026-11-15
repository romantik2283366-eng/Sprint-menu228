package com.sprintmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TargetHudSettingsScreen extends Screen {
    private static final int W = 280;
    private static final int H = 194;
    private static final int BG = 0xEE2A2A2F;
    private static final int WHITE = 0xFFF2F2F5;
    private ButtonWidget editButton;
    private ButtonWidget widthButton;

    public TargetHudSettingsScreen() { super(Text.literal("Target HUD")); }

    @Override
    protected void init() {
        int x = width / 2 - W / 2;
        int y = height / 2 - H / 2;
        editButton = addDrawableChild(ButtonWidget.builder(editLabel(), b -> {
            TargetHudModule.setEditMode(!TargetHudModule.isEditMode());
            refresh();
        }).dimensions(x + 16, y + 58, W - 32, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Шире"), b -> {
            TargetHudModule.setWidth(TargetHudModule.getWidth() + 10);
            refresh();
        }).dimensions(x + 16, y + 88, (W - 36) / 2, 22).build());
        widthButton = addDrawableChild(ButtonWidget.builder(widthLabel(), b -> {}).dimensions(x + W / 2 - 40, y + 88, 80, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Уже"), b -> {
            TargetHudModule.setWidth(TargetHudModule.getWidth() - 10);
            refresh();
        }).dimensions(x + W - 16 - (W - 36) / 2, y + 88, (W - 36) / 2, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Сбросить позицию"), b -> TargetHudModule.resetPosition())
                .dimensions(x + 16, y + 118, W - 32, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> close())
                .dimensions(x + 16, y + H - 34, W - 32, 22).build());
    }

    private Text editLabel() {
        return Text.literal("Редактирование в чате: " + (TargetHudModule.isEditMode() ? "ON" : "OFF"));
    }

    private Text widthLabel() { return Text.literal("Ширина: " + TargetHudModule.getWidth()); }

    private void refresh() {
        editButton.setMessage(editLabel());
        widthButton.setMessage(widthLabel());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int x = width / 2 - W / 2;
        int y = height / 2 - H / 2;
        context.fill(x - 1, y - 1, x + W + 1, y + H + 1, 0xFF55555C);
        context.fill(x, y, x + W, y + H, BG);
        context.fill(x, y, x + W, y + 3, 0xFF79E58B);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("TARGET HUD • BUNNY STYLE").formatted(Formatting.BOLD), width / 2, height / 2 - H / 2 + 18, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Включи режим, закрой меню и перетащи HUD в чате"), width / 2, height / 2 - H / 2 + 42, 0xFFA0A0A8);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(new SprintMenuScreen());
    }

    @Override
    public boolean shouldPause() { return false; }
}
