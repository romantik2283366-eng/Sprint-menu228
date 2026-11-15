package com.sprintmenu;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

public final class AutoTotemSettingsScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 190;
    private static final int MINT = 0xFF79E58B;
    private static final int PINK = 0xFFFFD6E4;
    private static final int WHITE = 0xFFF2F2F5;
    private static final int BG = 0xF51C2029;

    public AutoTotemSettingsScreen() { super(Text.literal("Auto Totem")); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;
        context.fill(x - 3, y - 3, x + PANEL_W + 3, y + PANEL_H + 3, PINK);
        context.fill(x, y, x + PANEL_W, y + PANEL_H, BG);
        context.fill(x, y, x + PANEL_W, y + 4, MINT);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("BUNNY DLC • AUTO TOTEM").formatted(Formatting.BOLD), width / 2, y + 18, WHITE);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Порог здоровья: " + AutoTotemModule.getHealthThreshold() + " HP"), width / 2, y + 56, WHITE);
        drawButton(context, x + 24, y + 82, 82, 28, "−", mouseX, mouseY);
        drawButton(context, x + PANEL_W - 106, y + 82, 82, 28, "+", mouseX, mouseY);
        drawButton(context, x + 24, y + 126, PANEL_W - 48, 30, "НАЗАД", mouseX, mouseY);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Тотем из инвентаря будет перемещён во вторую руку"), width / 2, y + 66, 0xFFA0A0A8);
    }

    private void drawButton(DrawContext context, int x, int y, int w, int h, String label, int mx, int my) {
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + h;
        context.fill(x, y, x + w, y + h, hovered ? 0xFF40544A : 0xFF2A3039);
        context.fill(x, y, x + 3, y + h, hovered ? PINK : MINT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label).formatted(Formatting.BOLD), x + w / 2, y + (h - 8) / 2, WHITE);
    }

    private double[] cursorPosition() {
        if (client == null) return new double[]{-1.0, -1.0};
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer x = stack.mallocDouble(1);
            DoubleBuffer y = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(client.getWindow().getHandle(), x, y);
            double scale = client.getWindow().getScaleFactor();
            return new double[]{x.get(0) / scale, y.get(0) / scale};
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        double[] cursor = cursorPosition();
        double mouseX = cursor[0];
        double mouseY = cursor[1];
        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;
        if (inside(mouseX, mouseY, x + 24, y + 82, 82, 28)) {
            AutoTotemModule.setHealthThreshold(AutoTotemModule.getHealthThreshold() - 1);
            return true;
        }
        if (inside(mouseX, mouseY, x + PANEL_W - 106, y + 82, 82, 28)) {
            AutoTotemModule.setHealthThreshold(AutoTotemModule.getHealthThreshold() + 1);
            return true;
        }
        if (inside(mouseX, mouseY, x + 24, y + 126, PANEL_W - 48, 30)) {
            close();
            return true;
        }
        return true;
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(new SprintMenuScreen());
    }

    @Override
    public boolean shouldPause() { return false; }
}
