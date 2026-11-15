package com.sprintmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.text.Text;

/** Visual target HUD: shows the name and HP of the entity under the crosshair. */
public final class TargetHudModule {
    private static final int MAX_DISTANCE = 60;
    private static boolean enabled = true;

    private TargetHudModule() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (!enabled || client == null || client.player == null || client.world == null) return;

        Entity target = getTarget(client);
        if (target == null || !(target instanceof LivingEntity living)) return;
        if (target == client.player) return;

        double distance = client.player.distanceTo(target);
        if (distance > MAX_DISTANCE) return;

        int x = context.getScaledWindowWidth() / 2 - 90;
        int y = Math.max(28, context.getScaledWindowHeight() / 2 + 18);
        int w = 180;
        int h = 40;

        context.fill(x, y, x + w, y + h, 0xA0000000);
        context.fill(x, y, x + w, y + 2, 0xFF7AE582);
        context.fill(x, y + 2, x + w, y + 3, 0x30FFFFFF);

        context.drawText(client.textRenderer, Text.literal(target.getName().getString()), x + 8, y + 8, 0xFFFFFF, true);
        int hpCurrent = (int) Math.ceil(living.getHealth());
        int hpMax = (int) Math.ceil(living.getMaxHealth());
        context.drawText(client.textRenderer, Text.literal("HP: " + hpCurrent + " / " + hpMax), x + 8, y + 22, 0xB5F7C5, true);
    }

    private static Entity getTarget(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity != null && entity != client.player && entity instanceof LivingEntity) {
                return entity;
            }
        }
        return null;
    }
}
