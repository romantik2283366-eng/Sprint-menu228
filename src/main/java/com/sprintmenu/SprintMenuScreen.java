package com.sprintmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
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

public class SprintMenuScreen extends Screen {
    private static final int PANEL_W = 440;
    private static final int PANEL_H = 250;
    private static final int PAD = 16;
    private static final int HEADER_H = 34;
    private static final int TAB_H = 22;
    private static final int TAB_GAP = 4;
    private static final int BTN_H = 22;
    private static final int GAP = 6;

    private static final int WHITE = 0xFFF2F2F5;
    private static final int LIGHT_GRAY = 0xFFC9C9D0;
    private static final int PANEL_BG = 0xF51C2029;
    private static final int PANEL_BORDER = 0xFF5C7667;
    private static final int HEADER_BG = 0xFF29343A;
    private static final int GRAY = 0xFFA0A0A8;
    private static final int MINT = 0xFF79E58B;
    private static final int PINK = 0xFFFFD6E4;

    private enum Tab {
        COMBAT("Combat"), PLAYER("Player"), VISUALS("Visuals");
        final String title;
        Tab(String title) { this.title = title; }
    }

    private record Toggle(String name, BooleanSupplier get, Consumer<Boolean> set, Supplier<Screen> settings) {
        Toggle(String name, BooleanSupplier get, Consumer<Boolean> set) { this(name, get, set, null); }
    }

    private static final Map<Tab, List<Toggle>> MODULES = new EnumMap<>(Tab.class);
    static {
        MODULES.put(Tab.COMBAT, List.of(
                new Toggle("Trigger Bot", TriggerBotModule::isEnabled, TriggerBotModule::setEnabled, TriggerBotSettingsScreen::new)
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
    private int panelX, panelY, panelW, tabW, tabY;
    private final List<ButtonWidget> toggleButtons = new ArrayList<>();
    private boolean rightWasDown = false;

    public SprintMenuScreen() { super(Text.literal("Bunny DLC")); }

    @Override
    protected void init() {
        panelW = Math.min(PANEL_W, this.width - 20);
        panelX = this.width / 2 - panelW / 2;
        panelY = this.height / 2 - PANEL_H / 2;
        int inner = panelW - PAD * 2;
        Tab[] tabs = Tab.values();
        tabW = (inner - TAB_GAP * (tabs.length - 1)) / tabs.length;
        tabY = panelY + HEADER_H + 10;
        toggleButtons.clear();
        rightWasDown = isRightDown();

        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            int tx = panelX + PAD + i * (tabW + TAB_GAP);
            addDrawableChild(ButtonWidget.builder(tabLabel(tab), button -> {
                selected = tab;
                this.clearChildren();
                this.init();
            }).dimensions(tx, tabY, tabW, TAB_H).build());
        }

        int colW = (inner - GAP) / 2;
        int startY = tabY + TAB_H + 16;
        List<Toggle> list = MODULES.get(selected);
        for (int i = 0; i < list.size(); i++) {
            Toggle t = list.get(i);
            int x = panelX + PAD + (i % 2) * (colW + GAP);
            int y = startY + (i / 2) * (BTN_H + GAP);
            ButtonWidget btn = ButtonWidget.builder(label(t), button -> {
                t.set().accept(!t.get().getAsBoolean());
                button.setMessage(label(t));
            }).dimensions(x, y, colW, BTN_H).build();
            if (t.settings() != null) btn.setTooltip(Tooltip.of(Text.literal("ПКМ - настройки")));
            addDrawableChild(btn);
            toggleButtons.add(btn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button -> close())
                .dimensions(panelX + PAD, panelY + PANEL_H - 54, inner, BTN_H).build());
    }

    private boolean isRightDown() {
        if (client == null) return false;
        return GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
    }

    private void handleRightClick(int mouseX, int mouseY) {
        List<Toggle> list = MODULES.get(selected);
        for (int i = 0; i < toggleButtons.size() && i < list.size(); i++) {
            Toggle t = list.get(i);
            if (t.settings() != null && toggleButtons.get(i).isMouseOver(mouseX, mouseY)) {
                client.setScreen(t.settings().get());
                return;
            }
        }
    }

    private Text tabLabel(Tab tab) {
        return tab == selected ? Text.literal("✦ " + tab.title).formatted(Formatting.WHITE, Formatting.BOLD)
                : Text.literal(tab.title).formatted(Formatting.GRAY);
    }

    private Text label(Toggle t) {
        Text state = t.get().getAsBoolean() ? Text.literal("ON").formatted(Formatting.GREEN)
                : Text.literal("OFF").formatted(Formatting.RED);
        return Text.literal("  " + t.name() + "   •   ").append(state);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int x1 = panelX, y1 = panelY, x2 = panelX + panelW, y2 = panelY + PANEL_H;
        int cx = width / 2;
        context.fill(x1 - 4, y1 - 4, x2 + 4, y2 + 4, 0x60353D49);
        context.fill(x1 - 2, y1 - 2, x2 + 2, y2 + 2, PINK);
        context.fill(x1, y1, x2, y2, PANEL_BG);
        context.fill(x1, y1, x2, y1 + HEADER_H, HEADER_BG);
        context.fill(x1, y1 + HEADER_H, x2, y1 + HEADER_H + 2, MINT);

        drawEar(context, cx - 36, y1);
        drawEar(context, cx + 24, y1);
        drawFace(context, cx, y1 + 8);

        context.fill(panelX + PAD - 2, tabY - 2, panelX + panelW - PAD + 2, tabY + TAB_H + 3, 0x554F6370);
        int ux = panelX + PAD + selected.ordinal() * (tabW + TAB_GAP);
        context.fill(ux, tabY + TAB_H + 1, ux + tabW, tabY + TAB_H + 4, PINK);

        int startY = tabY + TAB_H + 16;
        List<Toggle> list = MODULES.get(selected);
        for (int i = 0; i < list.size(); i++) {
            int x = panelX + PAD + (i % 2) * ((panelW - PAD * 2 + GAP) / 2);
            int y = startY + (i / 2) * (BTN_H + GAP);
            context.fill(x - 2, y - 2, x + (panelW - PAD * 2 - GAP) / 2 + 2, y + BTN_H + 2,
                    list.get(i).get().getAsBoolean() ? 0x354FAF73 : 0x303C4652);
            context.fill(x - 2, y - 2, x, y + BTN_H + 2, list.get(i).get().getAsBoolean() ? MINT : PINK);
        }
        context.fill(x1 + PAD, y2 - 26, x2 - PAD, y2 - 25, PANEL_BORDER);
    }

    private void drawEar(DrawContext context, int x, int panelTop) {
        context.fill(x, panelTop - 22, x + 10, panelTop + 1, PINK);
        context.fill(x + 3, panelTop - 19, x + 7, panelTop, WHITE);
    }

    private void drawFace(DrawContext context, int cx, int y) {
        context.fill(cx - 2, y + 7, cx + 2, y + 10, PINK);
        context.fill(cx - 8, y + 5, cx - 5, y + 8, WHITE);
        context.fill(cx + 5, y + 5, cx + 8, y + 8, WHITE);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        boolean down = isRightDown();
        if (down && !rightWasDown) handleRightClick(mouseX, mouseY);
        rightWasDown = down;
        int cx = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("BUNNY CLUB").formatted(Formatting.BOLD), cx, panelY + 13, WHITE);
        if (MODULES.get(selected).isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Пока нет функций"), cx, tabY + TAB_H + 40, GRAY);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("✿  Right Shift — меню     •     ПКМ — настройки  ✿"), cx, panelY + PANEL_H - 17, PINK);
    }

    @Override
    public boolean shouldPause() { return false; }
}
