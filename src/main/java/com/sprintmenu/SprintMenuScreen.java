package com.sprintmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

/** Category-based Bunny Club menu. */
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
    private static final int VISUALS = 0;
    private static final int PLAYER = 1;

    private int panelX, panelY, panelW, panelH, sidebarW;
    private int activeTab = VISUALS;

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
        // Draw our own backdrop; Screen.renderBackground can request a second blur in 1.21.11.
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
        drawNavItem(context, "Visuals", "01", VISUALS, navY, mouseX, mouseY);
        drawNavItem(context, "Player", "02", PLAYER, navY + 38, mouseX, mouseY);
        int statusY = panelY + panelH - 34;
        context.fill(x + 2, statusY + 3, x + 8, statusY + 9, GREEN);
        context.drawText(textRenderer, Text.literal("CLIENT ACTIVE"), x + 15, statusY, MUTED, false);
    }

    private void drawNavItem(DrawContext context, String label, String number, int tab, int y, int mouseX, int mouseY) {
        int x = panelX + 10;
        int w = sidebarW - 20;
        boolean selected = activeTab == tab;
        if (selected) {
            context.fill(x, y, x + w, y + 30, 0xFF29243C);
            context.fill(x, y + 5, x + 2, y + 25, ACCENT);
        } else if (inside(mouseX, mouseY, x, y, w, 30)) {
            context.fill(x, y, x + w, y + 30, HOVER);
        }
        context.drawText(textRenderer, Text.literal(label), x + 12, y + 10, selected ? WHITE : MUTED, false);
        int numberWidth = textRenderer.getWidth(number);
        context.drawText(textRenderer, Text.literal(number), x + w - numberWidth - 9, y + 10,
                selected ? ACCENT : 0xFF626A7C, false);
    }

    private void drawContent(DrawContext context, int mouseX, int mouseY) {
        int x = panelX + sidebarW + 25;
        int w = panelW - sidebarW - 48;
        int top = panelY + 22;
        String title = activeTab == PLAYER ? "Player" : "Visuals";
        String subtitle = activeTab == PLAYER ? "Movement and player settings" : "Appearance and performance";
        context.drawText(textRenderer, Text.literal(title), x, top, WHITE, false);
        context.drawText(textRenderer, Text.literal(subtitle), x, top + 16, MUTED, false);
        context.fill(x, top + 34, x + w, top + 35, LINE);
        int rowY = top + 46;
        if (activeTab == VISUALS) {
            drawModuleRow(context, x, rowY, w, "Bunnies", "Companions around your character",
                    BunnyModule.isEnabled(), "VISUAL", mouseX, mouseY);
            drawModuleRow(context, x, rowY + 48, w, "Optimization", "Minecraft performance settings",
                    false, "SETTINGS", mouseX, mouseY);
        } else {
            drawModuleRow(context, x, rowY, w, "Auto Sprint", "Sprint while moving forward",
                    SprintModule.isEnabled(), "PLAYER", mouseX, mouseY);
        }
    }

    private void drawModuleRow(DrawContext context, int x, int y, int w, String title, String subtitle,
                               boolean enabled, String tag, int mouseX, int mouseY) {
        context.fill(x, y, x + w, y + 40, inside(mouseX, mouseY, x, y, w, 40) ? HOVER : SURFACE);
        context.fill(x + 10, y + 10, x + 12, y + 30, enabled ? ACCENT : 0xFF505767);
        context.drawText(textRenderer, Text.literal(title), x + 21, y + 7, WHITE, false);
        context.drawText(textRenderer, Text.literal(subtitle), x + 21, y + 22, MUTED, false);
        int tagW = textRenderer.getWidth(tag);
        context.drawText(textRenderer, Text.literal(tag), x + w - tagW - 43, y + 14, MUTED, false);
        if (title.equals("Optimization")) {
            context.drawText(textRenderer, Text.literal("›"), x + w - 19, y + 12, ACCENT, false);
        } else {
            drawSwitch(context, x + w - 38, y + 10, enabled);
        }
    }

    private void drawSwitch(DrawContext context, int x, int y, boolean on) {
        context.fill(x, y, x + 30, y + 18, on ? 0xFF514576 : 0xFF343A48);
        int knobX = on ? x + 15 : x + 3;
        context.fill(knobX, y + 3, knobX + 12, y + 15, on ? ACCENT : 0xFF9299AA);
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
        int button = click.button();
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return false;
        double[] mouse = cursorPosition();
        double mx = mouse[0], my = mouse[1];
        int navX = panelX + 10, navW = sidebarW - 20, navY = panelY + 66;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inside(mx, my, navX, navY, navW, 30)) {
            activeTab = VISUALS;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inside(mx, my, navX, navY + 38, navW, 30)) {
            activeTab = PLAYER;
            return true;
        }
        int contentX = panelX + sidebarW + 25;
        int contentW = panelW - sidebarW - 48;
        int rowY = panelY + 22 + 46;
        if (activeTab == VISUALS) {
            if (inside(mx, my, contentX, rowY, contentW, 40)) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    if (client != null) client.setScreen(new BunnySettingsScreen(this));
                } else {
                    BunnyModule.setEnabled(!BunnyModule.isEnabled());
                }
                return true;
            }
            if (inside(mx, my, contentX, rowY + 48, contentW, 40)) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    if (client != null) client.setScreen(new OptimizationSettingsScreen(this, client.options));
                }
                return true;
            }
        } else if (inside(mx, my, contentX, rowY, contentW, 40)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (client != null) client.setScreen(new AutoSprintSettingsScreen(this));
            } else {
                SprintModule.setEnabled(!SprintModule.isEnabled());
            }
            return true;
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

    private abstract static class SettingsScreen extends Screen {
        protected final Screen parent;
        protected final int accent = 0xFF9B82F5;
        protected SettingsScreen(Screen parent, String title) {
            super(Text.literal(title));
            this.parent = parent;
        }
        protected void drawBase(DrawContext context, String title, String subtitle) {
            context.fill(0, 0, width, height, 0xAA080A10);
            int w = Math.min(390, width - 24), h = Math.min(250, height - 20);
            int x = (width - w) / 2, y = (height - h) / 2;
            context.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x453A315A);
            context.fill(x, y, x + w, y + h, BG);
            context.drawText(textRenderer, Text.literal(title), x + 20, y + 17, WHITE, false);
            context.drawText(textRenderer, Text.literal(subtitle), x + 20, y + 34, MUTED, false);
            context.fill(x + 20, y + 53, x + w - 20, y + 54, LINE);
        }
        @Override public void close() {
            if (client != null) client.setScreen(parent);
        }
        @Override public boolean shouldPause() { return false; }
    }

    private static final class BunnySettingsScreen extends SettingsScreen {
        BunnySettingsScreen(Screen parent) { super(parent, "Bunnies"); }
        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            drawBase(context, "Bunnies", "Companion appearance and amount");
            int w = Math.min(390, width - 24), h = Math.min(250, height - 20);
            int x = (width - w) / 2, y = (height - h) / 2;
            context.drawText(textRenderer, Text.literal("Count"), x + 20, y + 76, WHITE, false);
            context.drawText(textRenderer, Text.literal("−"), x + w - 116, y + 76, accent, false);
            context.drawText(textRenderer, Text.literal(Integer.toString(BunnyModule.getCount())), x + w - 82, y + 76, WHITE, false);
            context.drawText(textRenderer, Text.literal("+"), x + w - 45, y + 76, accent, false);
            context.drawText(textRenderer, Text.literal("Fur color"), x + 20, y + 112, WHITE, false);
            context.drawText(textRenderer, Text.literal(BunnyModule.colorName() + "   ›"), x + w - 120, y + 112, accent, false);
        }
        @Override public boolean mouseClicked(Click click, boolean doubled) {
            if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;
            double[] p = cursor(); int w = Math.min(390, width - 24), h = Math.min(250, height - 20);
            int x = (width - w) / 2, y = (height - h) / 2;
            if (inside(p[0], p[1], x + w - 130, y + 68, 34, 30)) BunnyModule.setCount(BunnyModule.getCount() - 1);
            else if (inside(p[0], p[1], x + w - 60, y + 68, 40, 30)) BunnyModule.setCount(BunnyModule.getCount() + 1);
            else if (inside(p[0], p[1], x + 15, y + 100, w - 30, 36)) BunnyModule.nextColor();
            else return super.mouseClicked(click, doubled);
            return true;
        }
        private double[] cursor() {
            if (client == null) return new double[]{-1, -1};
            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer mx = stack.mallocDouble(1), my = stack.mallocDouble(1);
                GLFW.glfwGetCursorPos(client.getWindow().getHandle(), mx, my);
                double scale = client.getWindow().getScaleFactor();
                return new double[]{mx.get(0) / scale, my.get(0) / scale};
            }
        }
        private boolean inside(double mx, double my, int x, int y, int w, int h) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    private static final class OptimizationSettingsScreen extends SettingsScreen {
        private final GameOptions options;
        OptimizationSettingsScreen(Screen parent, GameOptions options) {
            super(parent, "Optimization");
            this.options = options;
        }
        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            drawBase(context, "Optimization", "Vanilla Minecraft video options");
            int w = Math.min(390, width - 24), h = Math.min(250, height - 20);
            int x = (width - w) / 2, y = (height - h) / 2;
            context.drawText(textRenderer, Text.literal("Entity shadows"), x + 20, y + 76, WHITE, false);
            context.drawText(textRenderer, Text.literal(options.getEntityShadows().getValue() ? "ON" : "OFF"),
                    x + w - 62, y + 76, accent, false);
            context.drawText(textRenderer, Text.literal("Open Minecraft video settings"), x + 20, y + 112, WHITE, false);
            context.drawText(textRenderer, Text.literal("›"), x + w - 35, y + 112, accent, false);
        }
        @Override public boolean mouseClicked(Click click, boolean doubled) {
            if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;
            double[] p = cursor(); int w = Math.min(390, width - 24), h = Math.min(250, height - 20);
            int x = (width - w) / 2, y = (height - h) / 2;
            if (p[0] >= x + 15 && p[0] <= x + w - 15 && p[1] >= y + 62 && p[1] <= y + 96) {
                options.getEntityShadows().setValue(!options.getEntityShadows().getValue());
                return true;
            }
            if (p[0] >= x + 15 && p[0] <= x + w - 15 && p[1] >= y + 98 && p[1] <= y + 132 && client != null) {
                client.setScreen(new VideoOptionsScreen(this, options));
                return true;
            }
            return true;
        }
        private double[] cursor() {
            if (client == null) return new double[]{-1, -1};
            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer mx = stack.mallocDouble(1), my = stack.mallocDouble(1);
                GLFW.glfwGetCursorPos(client.getWindow().getHandle(), mx, my);
                double scale = client.getWindow().getScaleFactor();
                return new double[]{mx.get(0) / scale, my.get(0) / scale};
            }
        }
    }

    private static final class AutoSprintSettingsScreen extends SettingsScreen {
        AutoSprintSettingsScreen(Screen parent) { super(parent, "Auto Sprint"); }
        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            drawBase(context, "Auto Sprint", "Uses Minecraft's normal sprint conditions");
            int w = Math.min(390, width - 24), h = Math.min(250, height - 20);
            int x = (width - w) / 2, y = (height - h) / 2;
            context.drawText(textRenderer, Text.literal("Automatically sprint when moving forward."), x + 20, y + 78, WHITE, false);
            context.drawText(textRenderer, Text.literal("Blocked by sneaking, item use and low hunger."), x + 20, y + 98, MUTED, false);
        }
    }
}
