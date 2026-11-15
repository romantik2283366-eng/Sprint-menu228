package com.sprintmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TargetHudSettingsScreen extends Screen {
    private static final int W = 300;
    private static final int H = 250;
    private static final int BG = 0xEE20232B;
    private static final int WHITE = 0xFFF2F2F5;
    private ButtonWidget editButton;
    private ButtonWidget widthButton;
    private ButtonWidget playersButton;
    private ButtonWidget mobsButton;
    private ButtonWidget animalsButton;

    public TargetHudSettingsScreen() { super(Text.literal("Target HUD")); }

    @Override
    protected void init() {
        int x = width / 2 - W / 2;
        int y = height / 2 - H / 2;
        editButton = addDrawableChild(ButtonWidget.builder(editLabel(), b -> {
            TargetHudModule.setEditMode(!TargetHudModule.isEditMode());
            refresh();
        }).dimensions(x + 16, y + 58, W - 32, 22).build());
        playersButton = addDrawableChild(ButtonWidget.builder(playersLabel(), b -> {
            TargetHudModule.setPlayersEnabled(!TargetHudModule.arePlayersEnabled()); refresh();
        }).dimensions(x + 16, y + 88, W - 32, 22).build());
        mobsButton = addDrawableChild(ButtonWidget.builder(mobsLabel(), b -> {
            TargetHudModule.setMobsEnabled(!TargetHudModule.areMobsEnabled()); refresh();
        }).dimensions(x + 16, y + 116, W - 32, 22).build());
        animalsButton = addDrawableChild(ButtonWidget.builder(animalsLabel(), b -> {
            TargetHudModule.setAnimalsEnabled(!TargetHudModule.areAnimalsEnabled()); refresh();
        }).dimensions(x + 16, y + 144, W - 32, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Шире"), b -> {
            TargetHudModule.setWidth(TargetHudModule.getWidth() + 10); refresh();
        }).dimensions(x + 16, y + 174, 78, 22).build());
        widthButton = addDrawableChild(ButtonWidget.builder(widthLabel(), b -> {})
                .dimensions(x + 100, y + 174, 100, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Уже"), b -> {
            TargetHudModule.setWidth(TargetHudModule.getWidth() - 10); refresh();
        }).dimensions(x + 206, y + 174, 78, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Сбросить позицию"), b -> TargetHudModule.resetPosition())
                .dimensions(x + 16, y + 202, W - 32, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> close())
                .dimensions(x + 16, y + H - 31, W - 32, 22).build());
    }

    private Text editLabel() { return Text.literal("Перетаскивание в чате: " + onOff(TargetHudModule.isEditMode())); }
    private Text playersLabel() { return Text.literal("Игроки: " + onOff(TargetHudModule.arePlayersEnabled())); }
    private Text mobsLabel() { return Text.literal("Мобы: " + onOff(TargetHudModule.areMobsEnabled())); }
    private Text animalsLabel() { return Text.literal("Животные: " + onOff(TargetHudModule.areAnimalsEnabled())); }
    private Text widthLabel() { return Text.literal("Ширина: " + TargetHudModule.getWidth()); }
    private Text onOff(boolean value) { return Text.literal(value ? "ON" : "OFF").formatted(value ? Formatting.GREEN : Formatting.RED); }

    private void refresh() {
        editButton.setMessage(editLabel());
        playersButton.setMessage(playersLabel());
        mobsButton.setMessage(mobsLabel());
        animalsButton.setMessage(animalsLabel());
        widthButton.setMessage(widthLabel());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int x = width / 2 - W / 2;
        int y = height / 2 - H / 2;
        context.fill(x - 2, y - 2, x + W + 2, y + H + 2, 0xFFF4C7D8);
        context.fill(x, y, x + W, y + H, BG);
        context.fill(x, y, x + W, y + 4, 0xFF79E58B);
        // Valid 32-bit ARGB literal: subtle translucent inner highlight.
        context.fill(x + 10, y + 10, x + W - 10, y + H - 10, 0x20FFFFFF);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("BUNNY • TARGET HUD").formatted(Formatting.BOLD), width / 2,
                height / 2 - H / 2 + 20, WHITE);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Фильтры цели и позиция HUD"), width / 2,
                height / 2 - H / 2 + 42, 0xFFA0A0A8);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(new SprintMenuScreen());
    }

    @Override
    public boolean shouldPause() { return false; }
}
