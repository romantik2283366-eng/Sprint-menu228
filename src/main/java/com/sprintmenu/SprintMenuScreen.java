package com.sprintmenu;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A compact, multi-column Bunny DLC module list with a neutral gray/white palette. */
public final class SprintMenuScreen extends Screen {
    private static final int PANEL_H = 246;
    private static final int PAD = 12;
    private static final int HEADER_H = 38;
    private static final int COLUMN_GAP = 7;
    private static final int COLUMN_HEADER_H = 24;
    private static final int ITEM_H = 25;
    private static final int ITEM_GAP = 4;
    private static final int FOOTER_H = 23;

    private static final int WHITE = 0xFFF2F3F4;
    private static final int TEXT = 0xFFD4D6D8;
    private static final int MUTED = 0xFF92969A;
    private static final int PANEL = 0xF018191B;
    private static final int HEADER = 0xFF222427;
    private static final int COLUMN = 0xB8222427;
    private static final int CARD = 0xCC2B2D30;
    private static final int CARD_HOVER = 0xE03B3E42;
    private static final int LINE = 0xFF515459;
    private static final int ON = 0xFFE6E8EA;
    private static final int OFF = 0xFF777B80;

    private record Toggle(String name, BooleanSupplier get, Consumer<Boolean> set, Supplier<Screen> settings) {
        Toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
            this(name, get, set, null);
        }
    }

    private static final String[] GROUP_NAMES = {"COMBAT", "MOVEMENT", "VISUALS"};
    private static final List<Toggle>[] GROUPS = new List[]{
            List.of(
                    new Toggle("Trigger Bot", TriggerBotModule::isEnabled, TriggerBotModule::setEnabled, TriggerBotSettingsScreen::new),
                    new Toggle("Auto Totem", AutoTotemModule::isEnabled, AutoTotemModule::setEnabled, AutoTotemSettingsScreen::new)
            ),
            List.of(
                    new Toggle("Sprint", SprintModule::isEnabled, SprintModule::setEnabled),
                    new Toggle("InvMove", InvMoveModule::isEnabled, InvMoveModule::setEnabled)
            ),
            List.of(
                    new Toggle("Target HUD", TargetHudModule::isEnabled, TargetHudModule::setEnabled, TargetHudSettingsScreen::new),
                    new Toggle("Bunnies", BunnyModule::isEnabled, BunnyModule::setEnabled, BunnySettingsScreen::new)
            )
    };

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int columnW;
    private int contentY;

    public SprintMenuScreen() {
        super(Text.literal("Bunny DLC"));
    }

    @Override
    protected void init() {
        panelW = Math.max(260, Math.min(900, width - 16));
        panelH = Math.max(190, Math.min(PANEL_H, height - 12));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        int innerW = panelW - PAD * 2;
        columnW = (innerW - COLUMN_GAP * (GROUPS.length - 1)) / GROUPS.length;
        contentY = panelY + HEADER_H + 8;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawPanel(context, mouseX, mouseY);
    }

    private void drawPanel(DrawContext context, int mouseX, int mouseY) {
        int right = panelX + panelW;
        int bottom = panelY + panelH;
        context.fill(panelX - 3, panelY - 3, right + 3, bottom + 3, 0x8036383B);
        context.fill(panelX, panelY, right, bottom, PANEL);
        context.fill(panelX, panelY, right, panelY + HEADER_H, HEADER);
        context.fill(panelX, panelY + HEADER_H, right, panelY + HEADER_H + 1, LINE);

        context.drawText(textRenderer, Text.literal("BUNNY DLC").formatted(Formatting.BOLD),
                panelX + PAD, panelY + 8, WHITE, true);
        context.drawText(textRenderer, Text.literal("CLIENT MODULES"),
                panelX + PAD, panelY + 23, MUTED, false);
        Text status = Text.literal("●  LOCAL");
        context.drawText(textRenderer, status, right - PAD - textRenderer.getWidth(status), panelY + 15, TEXT, false);

        int footerY = bottom - FOOTER_H;
        for (int group = 0; group < GROUPS.length; group++) {
            int x = panelX + PAD + group * (columnW + COLUMN_GAP);
            context.fill(x, contentY, x + columnW, footerY - 7, COLUMN);
            context.fill(x, contentY, x + columnW, contentY + COLUMN_HEADER_H, 0xFF303236);
            context.drawText(textRenderer, Text.literal(GROUP_NAMES[group]).formatted(Formatting.BOLD),
                    x + 8, contentY + 8, TEXT, true);

            int itemY = contentY + COLUMN_HEADER_H + 6;
            List<Toggle> modules = GROUPS[group];
            for (int i = 0; i < modules.size(); i++) {
                Toggle module = modules.get(i);
                int y = itemY + i * (ITEM_H + ITEM_GAP);
                drawModule(context, module, x + 5, y, columnW - 10, mouseX, mouseY);
            }
        }

        context.fill(panelX + PAD, footerY - 1, right - PAD, footerY, LINE);
        context.drawText(textRenderer, Text.literal("ЛКМ — переключить    •    ПКМ — настройки"),
                panelX + PAD, footerY + 7, MUTED, false);
        Text close = Text.literal("ESC  •  CLOSE");
        context.drawText(textRenderer, close, right - PAD - textRenderer.getWidth(close), footerY + 7, TEXT, false);
    }

    private void drawModule(DrawContext context, Toggle module, int x, int y, int w, int mouseX, int mouseY) {
        boolean enabled = module.get().getAsBoolean();
        boolean hovered = inside(mouseX, mouseY, x, y, w, ITEM_H);
        context.fill(x, y, x + w, y + ITEM_H, hovered ? CARD_HOVER : CARD);
        context.drawText(textRenderer, Text.literal(module.name()), x + 7, y + 8, enabled ? WHITE : TEXT, false);
        int dotColor = enabled ? ON : OFF;
        context.fill(x + w - 13, y + 9, x + w - 7, y + 15, dotColor);
        if (module.settings() != null) {
            context.fill(x + w - 23, y + 10, x + w - 19, y + 14, MUTED);
        }
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

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double[] cursor = cursorPosition();
        double mouseX = cursor[0];
        double mouseY = cursor[1];
        int button = click.button();
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return false;

        int footerY = panelY + panelH - FOOTER_H;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && inside(mouseX, mouseY, panelX + panelW - PAD - 82, footerY, 82, FOOTER_H)) {
            close();
            return true;
        }

        for (int group = 0; group < GROUPS.length; group++) {
            int x = panelX + PAD + group * (columnW + COLUMN_GAP);
            int itemY = contentY + COLUMN_HEADER_H + 6;
            List<Toggle> modules = GROUPS[group];
            for (int i = 0; i < modules.size(); i++) {
                int y = itemY + i * (ITEM_H + ITEM_GAP);
                if (!inside(mouseX, mouseY, x + 5, y, columnW - 10, ITEM_H)) continue;
                Toggle module = modules.get(i);
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && module.settings() != null) {
                    if (client != null) client.setScreen(module.settings().get());
                    return true;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    module.set().accept(!module.get().getAsBoolean());
                    return true;
                }
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
