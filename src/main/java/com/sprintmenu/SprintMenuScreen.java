package com.sprintmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Custom-drawn Bunny DLC menu; controls are painted directly instead of vanilla ButtonWidgets. */
public final class SprintMenuScreen extends Screen {
    private static final int PANEL_H = 286;
    private static final int PAD = 18;
    private static final int HEADER_H = 58;
    private static final int TAB_H = 28;
    private static final int TAB_GAP = 7;
    private static final int ROW_H = 34;
    private static final int ROW_GAP = 10;
    private static final int PINK = 0xFFFFD6E4;
    private static final int MINT = 0xFF79E58B;
    private static final int WHITE = 0xFFF2F2F5;
    private static final int GRAY = 0xFFA0AAB1;
    private static final int BG = 0xF51C2029;

    private enum Tab {
        COMBAT("COMBAT"), PLAYER("PLAYER"), VISUALS("VISUALS");
        final String title;
        Tab(String title) { this.title = title; }
    }

    private record Toggle(String name, BooleanSupplier get, Consumer<Boolean> set, Supplier<Screen> settings) {
        Toggle(String name, BooleanSupplier get, Consumer<Boolean> set) { this(name, get, set, null); }
    }

    private static final Map<Tab, List<Toggle>> MODULES = new EnumMap<>(Tab.class);
    static {
        MODULES.put(Tab.COMBAT, List.of(
                new Toggle("Trigger Bot", TriggerBotModule::isEnabled, TriggerBotModule::setEnabled, TriggerBotSettingsScreen::new),
                new Toggle("Auto Totem", AutoTotemModule::isEnabled, AutoTotemModule::setEnabled, AutoTotemSettingsScreen::new)
        ));
        MODULES.put(Tab.PLAYER, List.of(
                new Toggle("Sprint", SprintModule::isEnabled, SprintModule::setEnabled),
                new Toggle("InvMove", InvMoveModule::isEnabled, InvMoveModule::setEnabled)
        ));
        MODULES.put(Tab.VISUALS, List.of(
                new Toggle("Target HUD", TargetHudModule::isEnabled, TargetHudModule::setEnabled, TargetHudSettingsScreen::new),
                new Toggle("Bunnies", BunnyModule::isEnabled, BunnyModule::setEnabled, BunnySettingsScreen::new)
        ));
    }

    private static Tab selected = Tab.PLAYER;
    private int panelX;
    private int panelY;
    private int panelW;
    private int tabW;
    private int tabY;
    private boolean rightWasDown;

    public SprintMenuScreen() { super(Text.literal("Bunny DLC")); }

    @Override
    protected void init() {
        panelW = Math.min(520, Math.max(320, width - 24));
        panelX = (width - panelW) / 2;
        panelY = (height - PANEL_H) / 2;
        int inner = panelW - PAD * 2;
        tabW = (inner - TAB_GAP * 2) / 3;
        tabY = panelY + HEADER_H + 8;
        rightWasDown = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawPanel(context, mouseX, mouseY);
    }

    private void drawPanel(DrawContext context, int mouseX, int mouseY) {
        int x2 = panelX + panelW;
        int y2 = panelY + PANEL_H;
        context.fill(panelX - 7, panelY - 7, x2 + 7, y2 + 7, 0x60353D49);
        context.fill(panelX - 3, panelY - 3, x2 + 3, y2 + 3, PINK);
        context.fill(panelX, panelY, x2, y2, BG);
        context.fill(panelX, panelY, x2, panelY + HEADER_H, 0xFF29343A);
        context.fill(panelX, panelY + HEADER_H, x2, panelY + HEADER_H + 3, MINT);

        drawBunnyMark(context, panelX + 22, panelY + 17);
        context.drawText(textRenderer, Text.literal("BUNNY DLC").formatted(Formatting.BOLD), panelX + 58, panelY + 14, WHITE, true);
        context.drawText(textRenderer, Text.literal("CUSTOM CLIENT MENU  •  v1.21.11"), panelX + 58, panelY + 32, GRAY, false);
        context.drawRightAlignedText(textRenderer, Text.literal("● ONLINE"), x2 - 18, panelY + 22, MINT, true);

        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            int tx = panelX + PAD + i * (tabW + TAB_GAP);
            drawCard(context, tx, tabY, tabW, TAB_H, selected == tab, mouseX, mouseY);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.title), tx + tabW / 2, tabY + 10,
                    selected == tab ? WHITE : GRAY);
        }

        List<Toggle> modules = MODULES.get(selected);
        int contentY = tabY + TAB_H + 18;
        int colGap = 12;
        int colW = (panelW - PAD * 2 - colGap) / 2;
        for (int i = 0; i < modules.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int bx = panelX + PAD + col * (colW + colGap);
            int by = contentY + row * (ROW_H + ROW_GAP);
            drawModuleCard(context, modules.get(i), bx, by, colW, ROW_H, mouseX, mouseY);
        }
        if (modules.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("НЕТ АКТИВНЫХ МОДУЛЕЙ"), width / 2,
                    contentY + 30, GRAY);
        }

        int footerY = y2 - 29;
        context.fill(panelX + PAD, footerY - 7, x2 - PAD, footerY - 6, 0xFF46515A);
        context.drawText(textRenderer, Text.literal("ПКМ по карточке — настройки"), panelX + PAD, footerY, GRAY, false);
        int closeX = x2 - PAD - 86;
        drawCard(context, closeX, footerY - 7, 86, 22, false, mouseX, mouseY);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("ЗАКРЫТЬ"), closeX + 43, footerY, WHITE);
    }

    private void drawModuleCard(DrawContext context, Toggle module, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, h);
        context.fill(x, y, x + w, y + h, hovered ? 0xFF303A43 : 0xFF252C34);
        context.fill(x, y, x + 3, y + h, module.get().getAsBoolean() ? MINT : PINK);
        context.drawText(textRenderer, Text.literal(module.name()).formatted(Formatting.BOLD), x + 12, y + 6, WHITE, true);
        context.drawText(textRenderer, Text.literal(module.settings() == null ? "TOGGLE" : "TOGGLE  •  ПКМ SETTINGS"), x + 12, y + 20, GRAY, false);
        int pillW = 40;
        int px = x + w - pillW - 8;
        context.fill(px, y + 7, px + pillW, y + h - 7, module.get().getAsBoolean() ? 0xFF2C6948 : 0xFF45434A);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(module.get().getAsBoolean() ? "ON" : "OFF"),
                px + pillW / 2, y + 12, module.get().getAsBoolean() ? MINT : PINK);
    }

    private void drawCard(DrawContext context, int x, int y, int w, int h, boolean active, int mx, int my) {
        boolean hovered = inside(mx, my, x, y, w, h);
        context.fill(x, y, x + w, y + h, active ? 0xFF40554C : hovered ? 0xFF343E47 : 0xFF252C34);
        context.fill(x, y + h - 2, x + w, y + h, active ? MINT : hovered ? PINK : 0xFF46515A);
    }

    private void drawBunnyMark(DrawContext context, int x, int y) {
        context.fill(x + 3, y, x + 8, y + 14, PINK);
        context.fill(x + 11, y, x + 16, y + 14, PINK);
        context.fill(x, y + 8, x + 19, y + 25, 0xFFECF1F2);
        context.fill(x + 4, y + 14, x + 6, y + 16, 0xFF29343A);
        context.fill(x + 13, y + 14, x + 15, y + 16, 0xFF29343A);
        context.fill(x + 8, y + 18, x + 11, y + 20, PINK);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);
        for (int i = 0; i < Tab.values().length; i++) {
            int tx = panelX + PAD + i * (tabW + TAB_GAP);
            if (inside(mouseX, mouseY, tx, tabY, tabW, TAB_H)) {
                selected = Tab.values()[i];
                return true;
            }
        }

        int footerY = panelY + PANEL_H - 29;
        int closeX = panelX + panelW - PAD - 86;
        if (button == 0 && inside(mouseX, mouseY, closeX, footerY - 7, 86, 22)) {
            close();
            return true;
        }

        List<Toggle> modules = MODULES.get(selected);
        int contentY = tabY + TAB_H + 18;
        int colGap = 12;
        int colW = (panelW - PAD * 2 - colGap) / 2;
        for (int i = 0; i < modules.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int bx = panelX + PAD + col * (colW + colGap);
            int by = contentY + row * (ROW_H + ROW_GAP);
            if (!inside(mouseX, mouseY, bx, by, colW, ROW_H)) continue;
            Toggle module = modules.get(i);
            if (button == 1 && module.settings() != null) {
                client.setScreen(module.settings().get());
            } else if (button == 0) {
                module.set().accept(!module.get().getAsBoolean());
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
