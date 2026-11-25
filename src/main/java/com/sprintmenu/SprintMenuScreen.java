package com.sprintmenu;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

/** A clean, compact control panel for the bunny module. */
public final class SprintMenuScreen extends Screen {
    private static final int PANEL_W = 420;
    private static final int PANEL_H = 286;
    private static final int BG = 0xF5111420;
    private static final int SURFACE = 0xFF1D2231;
    private static final int SURFACE_HOVER = 0xFF282F42;
    private static final int LINE = 0xFF30384B;
    private static final int WHITE = 0xFFF5F6FA;
    private static final int MUTED = 0xFF9AA4B8;
    private static final int ACCENT = 0xFF9B8CFF;
    private static final int GREEN = 0xFF69D6AE;

    private int x;
    private int y;
    private int w;
    private int h;

    public SprintMenuScreen() {
        super(ui("Bunny"));
    }

    private static Text ui(String value) {
        return Text.literal(value).setStyle(Style.EMPTY.withFont(Identifier.of("minecraft", "uniform")));
    }

    @Override
    protected void init() {
        w = Math.min(PANEL_W, Math.max(300, width - 24));
        h = Math.min(PANEL_H, Math.max(250, height - 20));
        x = (width - w) / 2;
        y = (height - h) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, width, height, 0x99080A10);

        int right = x + w;
        int bottom = y + h;
        context.fill(x - 2, y - 2, right + 2, bottom + 2, 0x553F4770);
        context.fill(x, y, right, bottom, BG);
        context.fill(x, y, right, y + 68, 0xFF191E2C);
        context.fill(x + 22, y + 67, right - 22, y + 68, LINE);

        // Minimal bunny mark and title area.
        context.fill(x + 24, y + 20, x + 31, y + 43, ACCENT);
        context.fill(x + 36, y + 17, x + 43, y + 43, ACCENT);
        context.fill(x + 26, y + 36, x + 41, y + 49, 0xFF7567D9);
        context.drawText(textRenderer, ui("BUNNY CLUB"), x + 55, y + 18, WHITE, false);
        context.drawText(textRenderer, ui("Минималистичная панель зайцев"), x + 55, y + 36, MUTED, false);
        context.drawText(textRenderer, ui("RIGHT SHIFT"), right - 94, y + 29, MUTED, false);

        int pad = 24;
        int cardX = x + pad;
        int cardW = w - pad * 2;
        int toggleY = y + 84;
        boolean enabled = BunnyModule.isEnabled();
        drawCard(context, cardX, toggleY, cardW, 54, mouseX, mouseY);
        context.drawText(textRenderer, ui("Зайцы"), cardX + 14, toggleY + 12, WHITE, false);
        context.drawText(textRenderer, ui(enabled ? "Питомцы бегают рядом с тобой" : "Модуль сейчас выключен"),
                cardX + 14, toggleY + 31, MUTED, false);
        drawSwitch(context, cardX + cardW - 55, toggleY + 18, enabled);

        int rowY = toggleY + 66;
        context.drawText(textRenderer, ui("Количество"), cardX + 2, rowY + 8, WHITE, false);
        context.drawText(textRenderer, ui("От 1 до 30 зайцев"), cardX + 2, rowY + 25, MUTED, false);
        drawSmallButton(context, cardX + cardW - 104, rowY, 32, 34, "−", mouseX, mouseY);
        drawValue(context, cardX + cardW - 68, rowY, 40, 34, Integer.toString(BunnyModule.getCount()));
        drawSmallButton(context, cardX + cardW - 24, rowY, 32, 34, "+", mouseX, mouseY);

        int colorY = rowY + 48;
        drawCard(context, cardX, colorY, cardW, 42, mouseX, mouseY);
        context.drawText(textRenderer, ui("Окрас"), cardX + 14, colorY + 14, WHITE, false);
        String color = BunnyModule.colorName() + "   ›";
        int colorWidth = textRenderer.getWidth(ui(color));
        context.drawText(textRenderer, ui(color), cardX + cardW - colorWidth - 14, colorY + 14, ACCENT, false);

        int optimizeY = colorY + 52;
        drawCard(context, cardX, optimizeY, cardW, 42, mouseX, mouseY);
        context.drawText(textRenderer, ui("Оптимизация"), cardX + 14, optimizeY + 14, WHITE, false);
        String optimize = BunnyModule.isOptimized() ? "ВКЛ" : "ВЫКЛ";
        int optimizeWidth = textRenderer.getWidth(ui(optimize));
        context.drawText(textRenderer, ui(optimize), cardX + cardW - optimizeWidth - 14,
                optimizeY + 14, BunnyModule.isOptimized() ? GREEN : MUTED, false);

        int footerY = bottom - 30;
        context.fill(cardX, footerY - 8, cardX + cardW, footerY - 7, LINE);
        context.drawText(textRenderer, ui("Кликни по строке, чтобы изменить настройку"), cardX, footerY + 2, MUTED, false);
        Text close = ui("ESC  ·  ЗАКРЫТЬ");
        context.drawText(textRenderer, close, right - pad - textRenderer.getWidth(close), footerY + 2, MUTED, false);
    }

    private void drawCard(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        context.fill(x, y, x + w, y + h, hovered ? SURFACE_HOVER : SURFACE);
    }

    private void drawSwitch(DrawContext context, int x, int y, boolean enabled) {
        context.fill(x, y, x + 38, y + 20, enabled ? 0xFF544B91 : 0xFF3B4252);
        int knobX = enabled ? x + 21 : x + 3;
        context.fill(knobX, y + 3, knobX + 14, y + 17, enabled ? ACCENT : 0xFF9AA4B8);
    }

    private void drawSmallButton(DrawContext context, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        drawCard(context, x, y, w, h, mouseX, mouseY);
        Text text = ui(label);
        context.drawCenteredTextWithShadow(textRenderer, text, x + w / 2, y + 12, WHITE.getStyle().getColor() == null ? 0xFFFFFFFF : 0xFFFFFFFF);
    }

    private void drawValue(DrawContext context, int x, int y, int w, int h, String value) {
        context.fill(x, y, x + w, y + h, 0xFF272D3D);
        context.drawCenteredTextWithShadow(textRenderer, ui(value), x + w / 2, y + 12, 0xFFFFFFFF);
    }

    private double[] cursorPosition() {
        if (client == null) return new double[]{-1, -1};
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer mx = stack.mallocDouble(1);
            DoubleBuffer my = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(client.getWindow().getHandle(), mx, my);
            double scale = client.getWindow().getScaleFactor();
            return new double[]{mx.get(0) / scale, my.get(0) / scale};
        }
    }

    private boolean inside(double mx, double my, int bx, int by, int bw, int bh) {
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        double[] mouse = cursorPosition();
        double mx = mouse[0];
        double my = mouse[1];
        int cardX = x + 24;
        int cardW = w - 48;
        int toggleY = y + 84;
        int rowY = toggleY + 66;
        int colorY = rowY + 48;
        int optimizeY = colorY + 52;

        if (inside(mx, my, cardX, toggleY, cardW, 54)) {
            BunnyModule.setEnabled(!BunnyModule.isEnabled());
            return true;
        }
        if (inside(mx, my, cardX + cardW - 104, rowY, 32, 34)) {
            BunnyModule.setCount(BunnyModule.getCount() - 1);
            return true;
        }
        if (inside(mx, my, cardX + cardW - 24, rowY, 32, 34)) {
            BunnyModule.setCount(BunnyModule.getCount() + 1);
            return true;
        }
        if (inside(mx, my, cardX, colorY, cardW, 42)) {
            BunnyModule.nextColor();
            return true;
        }
        if (inside(mx, my, cardX, optimizeY, cardW, 42)) {
            BunnyModule.setOptimized(!BunnyModule.isOptimized());
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
