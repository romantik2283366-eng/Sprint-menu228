package com.sprintmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SprintMenuScreen extends Screen {
    private static final int W = 150;
    private static final int H = 20;

    public SprintMenuScreen() {
        super(Text.literal("Menu"));
    }

    @Override
    protected void init() {
        int x = this.width / 2 - W / 2;
        int y = this.height / 2 - H / 2;

        // Кнопка функции Sprint
        addDrawableChild(ButtonWidget.builder(sprintLabel(), button -> {
            SprintModule.setEnabled(!SprintModule.isEnabled());
            button.setMessage(sprintLabel());
        }).dimensions(x, y, W, H).build());

        // Закрыть меню
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button -> this.close())
                .dimensions(x, y + 26, W, H).build());
    }

    private Text sprintLabel() {
        Text state = SprintModule.isEnabled()
                ? Text.literal("ON").formatted(Formatting.GREEN)
                : Text.literal("OFF").formatted(Formatting.RED);
        return Text.literal("Sprint: ").append(state);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, this.height / 2 - 40, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
