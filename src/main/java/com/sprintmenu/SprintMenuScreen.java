package com.sprintmenu;

import net.minecraft.client.MinecraftClient;
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
    private static final int PANEL_BG = 0xEE2A2A2F;
    private static final int PANEL_BORDER = 0xFF55555C;
    private static final int HEADER_BG = 0xFF36363C;
    private static final int GRAY = 0xFFA0A0A8;

    private enum Tab {
        COMBAT("Combat"),
        PLAYER("Player"),
        VISUALS("Visuals");

        final String title;

        Tab(String title) {
            this.title = title;
        }
    }

    private record Toggle(String name, BooleanSupplier get, Consumer<Boolean> set, Supplier<Screen> settings) {
        Toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
            this(name, get, set, null);
        }
    }

    private static final Map<Tab, List<Toggle>> MODULES = new EnumMap<>(Tab.class);

    static {
        MODULES.put(Tab.COMBAT, List.of());
        MODULES.put(Tab.PLAYER, List.of(
                new Toggle("Sprint", SprintModule::isEnabled, SprintModule::setEnabled),
                new Toggle("InvMove", InvMoveModule::isEnabled, InvMoveModule::setEnabled)
        ));
        MODULES.put(Tab.VISUALS, List.of(
                new Toggle("Target HUD", TargetHudModule::isEnabled, TargetHudModule::setEnabled),
                new Toggle("Bunnies", BunnyModule::isEnabled, BunnyModule::setEnabled, BunnySettingsScreen::new)
        ));
    }

    private static Tab selected = Tab.PLAYER;

    private int panelX, panelY, panelW, tabW, tabY;
    private final List<ButtonWidget> toggleButtons = new ArrayList<>();
    private boolean rightWasDown = false;

    public SprintMenuScreen() {
        super(Text.literal("Bunny DLC"));
    }

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
            if (t.settings() != null) {
                btn.setTooltip(Tooltip.of(Text.literal("ПКМ - настройки")));
            }
            addDrawableChild(btn);
            toggleButtons.add(btn);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button -> this.close())
                .dimensions(panelX + PAD, panelY + PANEL_H - 54, inner, BTN_H).build());
    }

    private boolean isRightDown() {
        if (this.client == null) return false;
        long window = this.client.getWindow().getHandle();
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
    }

    private void handleRightClick(int mouseX, int mouseY) {
        List<Toggle> list = MODULES.get(selected);
        for (int i = 0; i < toggleButtons.size() && i < list.size(); i++) {
            Toggle t = list.get(i);
            if (t.settings() != null && toggleButtons.get(i).isMouseOver(mouseX, mouseY)) {
                this.client.setScreen(t.settings().get());
                return;
            }
        }
    }

    private Text tabLabel(Tab tab) {
        return tab == selected
                ? Text.literal(tab.title).formatted(Formatting.WHITE, Formatting.BOLD)
                : Text.literal(tab.title).formatted(Formatting.GRAY);
    }

    private Text label(Toggle t) {
        Text state = t.get().getAsBoolean()
                ? Text.literal("ON").formatted(Formatting.GREEN)
                : Text.literal("OFF").formatted(Formatting.RED);
        return Text.literal(t.name() + ": ").append(state);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        int x1 = panelX, y1 = panelY, x2 = panelX + panelW, y2 = panelY + PANEL_H;
        int cx = this.width / 2;

        drawEar(context, cx - 38, y1);
        drawEar(context, cx + 26, y1);

        context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, PANEL_BORDER);
        context.fill(x1, y1, x2, y2, PANEL_BG);

        context.fill(x1, y1, x2, y1 + HEADER_H, HEADER_BG);
        context.fill(x1, y1 + HEADER_H, x2, y1 + HEADER_H + 2, WHITE);

        int ux = panelX + PAD + selected.ordinal() * (tabW + TAB_GAP);
        context.fill(ux, tabY + TAB_H + 2, ux + tabW, tabY + TAB_H + 4, WHITE);

        context.fill(x1 + PAD, y2 - 26, x2 - PAD, y2 - 25, PANEL_BORDER);
    }

    private void drawEar(DrawContext context, int x, int panelTop) {
        context.fill(x, panelTop - 24, x + 12, panelTop + 2, WHITE);
        context.fill(x + 3, panelTop - 20, x + 9, panelTop + 2, LIGHT_GRAY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        boolean down = isRightDown();
        if (down && !rightWasDown) {
            handleRightClick(mouseX, mouseY);
        }
        rightWasDown = down;

        int cx = this.width / 2;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("BUNNY DLC").formatted(Formatting.BOLD),
                cx, panelY + 13, WHITE);

        if (MODULES.get(selected).isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("Пока нет функций"),
                    cx, tabY + TAB_H + 40, GRAY);
        }

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Right Shift - меню   |   ПКМ - настройки"),
                cx, panelY + PANEL_H - 17, GRAY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
