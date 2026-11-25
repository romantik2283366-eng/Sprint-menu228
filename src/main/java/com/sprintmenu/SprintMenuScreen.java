package com.sprintmenu;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

/** A compact category-based control panel for Bunny Club. */
public final class SprintMenuScreen extends Screen {
    private static final int PANEL_MAX_W = 620;
    private static final int PANEL_MAX_H = 390;
    private static final int BG = 0xF510121A;
    private static final int SIDEBAR = 0xFF151821;
    private static final int SURFACE = 0xFF1D2230;
    private static final int HOVER = 0xFF272D3D;
    private static final int LINE = 0xFF2C3241;
    private static final int WHITE = 0xFFF4F5FA;
    private static final int MUTED = 0xFF9299AA;
    private static final int ACCENT = 0xFF9B82F5;
    private static final int GREEN = 0xFF62D6A9;

    private static final int TAB_BUNNY = 0;
    private static final int TAB_VISUAL = 1;
    private static final int TAB_COMBAT = 2;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int sidebarW;
    private int activeTab = TAB_BUNNY;

    public SprintMenuScreen() {
        super(Text.literal("Bunny Club"));
    }

    @Override
    protected void init() {
        panelW = Math.min(PANEL_MAX_W, Math.max(320, width - 28));
        panelH = Math.min(PANEL_MAX_H, Math.max(220, height - 24));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        sidebarW = Math.min(138, Math.max(94, panelW / 4));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Avoid vanilla's background blur: it may already be applied by the screen renderer.
        context.fill(0, 0, width, height, 0xA9080A10);

        int right = panelX + panelW;
        int bottom = panelY + panelH;
        context.fill(panelX - 2, panelY - 2, right + 2, bottom + 2, 0x453A315A);
        context.fill(panelX, panelY, right, bottom, BG);
        context.fill(panelX, panelY, panelX + sidebarW, bottom, SIDEBAR);
        context.fill(panelX + sidebarW, panelY + 18, panelX + sidebarW + 1, bottom - 18, LINE);

        drawSidebar(context, mouseX, mouseY);
        drawContent(context, mouseX, mouseY);
    }

    private void drawSidebar(DrawContext context, int mouseX, int mouseY) {
        int x = panelX + 16;
        int top = panelY + 19;
        context.fill(x, top + 1, x + 24, top + 25, ACCENT);
        context.fill(x + 6, top - 4, x + 12, top + 12, 0xFFD8CCFF);
        context.fill(x + 15, top - 4, x + 21, top + 12, 0xFFD8CCFF);
        context.drawText(textRenderer, Text.literal("BUNNY"), x + 31, top + 2, WHITE, false);
        context.drawText(textRenderer, Text.literal("CLUB"), x + 31, top + 13, MUTED, false);

        int navY = panelY + 66;
        drawNavItem(context, "Bunny", "01", TAB_BUNNY, navY, mouseX, mouseY);
        drawNavItem(context, "Visual", "02", TAB_VISUAL, navY + 38, mouseX, mouseY);
        drawNavItem(context, "Combat", "03", TAB_COMBAT, navY + 76, mouseX, mouseY);

        int statusY = panelY + panelH - 34;
        context.fill(x + 2, statusY + 3, x + 8, statusY + 9, GREEN);
        context.drawText(textRenderer, Text.literal("CLIENT ACTIVE"), x + 15, statusY, MUTED, false);
    }

    private void drawNavItem(DrawContext context, String label, String number, int tab, int y, int mouseX, int mouseY) {
        int x = panelX + 10;
        int w = sidebarW - 20;
        boolean selected = activeTab == tab;
        boolean hovered = inside(mouseX, mouseY, x, y, w, 30);
        if (selected) {
            context.fill(x, y, x + w, y + 30, 0xFF29243C);
            context.fill(x, y + 5, x + 2, y + 25, ACCENT);
        } else if (hovered) {
            context.fill(x, y, x + w, y + 30, HOVER);
        }
        context.drawText(textRenderer, Text.literal(label), x + 12, y + 10, selected ? WHITE : MUTED, false);
        int numberWidth = textRenderer.getWidth(number);
        context.drawText(textRenderer, Text.literal(number), x + w - numberWidth - 9, y + 10,
                selected ? ACCENT : 0xFF626A7C, false);
    }

    private void drawContent(DrawContext context, int mouseX, int mouseY) {
        int contentX = panelX + sidebarW + 25;
        int contentW = panelW - sidebarW - 48;
        int headerY = panelY + 22;

        String title = switch (activeTab) {
            case TAB_VISUAL -> "Visual";
            case TAB_COMBAT -> "Combat";
            default -> "Bunny";
        };
        String subtitle = switch (activeTab) {
            case TAB_VISUAL -> "Appearance and presentation";
            case TAB_COMBAT -> "Combat features";
            default -> "Your little companions";
        };
        context.drawText(textRenderer, Text.literal(title), contentX, headerY, WHITE, false);
        context.drawText(textRenderer, Text.literal(subtitle), contentX, headerY + 16, MUTED, false);
        context.fill(contentX, headerY + 34, contentX + contentW, headerY + 35, LINE);

        if (activeTab != TAB_BUNNY) {
            drawEmptyState(context, contentX, headerY + 58, contentW);
            return;
        }
        drawBunnySettings(context, contentX, contentW, headerY + 45, mouseX, mouseY);
    }

    private void drawBunnySettings(DrawContext context, int x, int w, int top, int mouseX, int mouseY) {
        boolean enabled = BunnyModule.isEnabled();
        drawCard(context, x, top, w, 48, mouseX, mouseY);
        context.fill(x + 12, top + 12, x + 14, top + 36, enabled ? ACCENT : 0xFF565D6C);
        context.drawText(textRenderer, Text.literal("Bunnies"), x + 23, top + 10, WHITE, false);
        context.drawText(textRenderer, Text.literal(enabled ? "Companions are active" : "Companions are paused"),
                x + 23, top + 27, MUTED, false);
        drawSwitch(context, x + w - 49, top + 14, enabled);

        int rowGap = 7;
        int rowH = 38;
        int countY = top + 48 + rowGap;
        drawCard(context, x, countY, w, rowH, mouseX, mouseY);
        context.drawText(textRenderer, Text.literal("Count"), x + 12, countY + 7, WHITE, false);
        context.drawText(textRenderer, Text.literal("How many companions appear"), x + 12, countY + 22, MUTED, false);
        int controlsX = x + w - 102;
        drawStepButton(context, controlsX, countY + 5, "−", mouseX, mouseY);
        context.fill(controlsX + 33, countY + 5, controlsX + 66, countY + 33, 0xFF292F3D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(BunnyModule.getCount())),
                controlsX + 49, countY + 14, 0xFFFFFFFF);
        drawStepButton(context, controlsX + 69, countY + 5, "+", mouseX, mouseY);

        int colorY = countY + rowH + rowGap;
        drawCard(context, x, colorY, w, rowH, mouseX, mouseY);
        context.drawText(textRenderer, Text.literal("Fur color"), x + 12, colorY + 7, WHITE, false);
        context.drawText(textRenderer, Text.literal("Change companion appearance"), x + 12, colorY + 22, MUTED, false);
        drawPill(context, x + w - 106, colorY + 7, 94, 24, BunnyModule.colorName(), ACCENT, mouseX, mouseY);

        int performanceY = colorY + rowH + rowGap;
        drawCard(context, x, performanceY, w, rowH, mouseX, mouseY);
        context.drawText(textRenderer, Text.literal("Performance"), x + 12, performanceY + 7, WHITE, false);
        context.drawText(textRenderer, Text.literal("Reduce update work"), x + 12, performanceY + 22, MUTED, false);
        boolean optimized = BunnyModule.isOptimized();
        drawPill(context, x + w - 78, performanceY + 7, 66, 24, optimized ? "ON" : "OFF",
                optimized ? GREEN : MUTED, mouseX, mouseY);
    }

    private void drawEmptyState(DrawContext context, int x, int y, int w) {
        int boxY = Math.min(y + 17, panelY + panelH - 76);
        int boxH = Math.max(46, panelY + panelH - 28 - boxY);
        context.fill(x, boxY, x + w, boxY + boxH, SURFACE);
        int markX = x + w / 2;
        context.fill(markX - 1, boxY + 12, markX + 1, boxY + 26, ACCENT);
        context.fill(markX - 7, boxY + 18, markX + 7, boxY + 20, ACCENT);
        Text message = Text.literal("No features here");
        context.drawCenteredTextWithShadow(textRenderer, message, markX, boxY + 33, 0xFFB6BDCC);
    }

    private void drawCard(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        context.fill(x, y, x + w, y + h, inside(mouseX, mouseY, x, y, w, h) ? HOVER : SURFACE);
    }

    private void drawSwitch(DrawContext context, int x, int y, boolean on) {
        context.fill(x, y, x + 36, y + 20, on ? 0xFF514576 : 0xFF343A48);
        int knobX = on ? x + 19 : x + 3;
        context.fill(knobX, y + 3, knobX + 14, y + 17, on ? ACCENT : 0xFF9299AA);
    }

    private void drawStepButton(DrawContext context, int x, int y, String label, int mouseX, int mouseY) {
        drawCard(context, x, y, 28, 28, mouseX, mouseY);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + 14, y + 9, 0xFFFFFFFF);
    }

    private void drawPill(DrawContext context, int x, int y, int w, int h, String value, int color, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, h);
        context.fill(x, y, x + w, y + h, hovered ? 0xFF353249 : 0xFF292D3A);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(value), x + w / 2, y + 8, color);
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

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        double[] mouse = cursorPosition();
        double mx = mouse[0];
        double my = mouse[1];
        int navX = panelX + 10;
        int navW = sidebarW - 20;
        int navY = panelY + 66;

        if (inside(mx, my, navX, navY, navW, 30)) {
            activeTab = TAB_BUNNY;
            return true;
        }
        if (inside(mx, my, navX, navY + 38, navW, 30)) {
            activeTab = TAB_VISUAL;
            return true;
        }
        if (inside(mx, my, navX, navY + 76, navW, 30)) {
            activeTab = TAB_COMBAT;
            return true;
        }
        if (activeTab == TAB_BUNNY) {
            int contentX = panelX + sidebarW + 25;
            int contentW = panelW - sidebarW - 48;
            int top = panelY + 22 + 45;
            int countY = top + 55;
            int controlsX = contentX + contentW - 102;
            int colorY = countY + 45;
            int performanceY = colorY + 45;

            if (inside(mx, my, contentX, top, contentW, 48)) {
                BunnyModule.setEnabled(!BunnyModule.isEnabled());
                return true;
            }
            if (inside(mx, my, controlsX, countY + 5, 28, 28)) {
                BunnyModule.setCount(BunnyModule.getCount() - 1);
                return true;
            }
            if (inside(mx, my, controlsX + 69, countY + 5, 28, 28)) {
                BunnyModule.setCount(BunnyModule.getCount() + 1);
                return true;
            }
            if (inside(mx, my, contentX, colorY, contentW, 38)) {
                BunnyModule.nextColor();
                return true;
            }
            if (inside(mx, my, contentX, performanceY, contentW, 38)) {
                BunnyModule.setOptimized(!BunnyModule.isOptimized());
                return true;
            }
        }
        return true;
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
