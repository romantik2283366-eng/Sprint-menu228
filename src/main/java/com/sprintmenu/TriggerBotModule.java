package com.sprintmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Optional crosshair-only trigger bot. It never aims or sends custom packets. */
public final class TriggerBotModule {
    private static boolean enabled;
    private static boolean smartCrits;

    private TriggerBotModule() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static boolean isSmartCrits() { return smartCrits; }
    public static void setSmartCrits(boolean value) { smartCrits = value; }

    public static void tick(MinecraftClient client) {
        if (!enabled || client.player == null || client.world == null || client.interactionManager == null
                || client.currentScreen != null || client.player.isUsingItem()) return;

        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof LivingEntity target) || target == client.player || !target.isAlive()) return;

        if (client.player.getAttackCooldownProgress(0.0F) < 1.0F) return;
        if (smartCrits && !isFallingForCritical(client)) return;

        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    private static boolean isFallingForCritical(MinecraftClient client) {
        var player = client.player;
        return !player.isOnGround()
                && player.getVelocity().y < -0.02
                && !player.isClimbing()
                && !player.isTouchingWater()
                && !player.hasVehicle()
                && !player.isSprinting();
    }
}
