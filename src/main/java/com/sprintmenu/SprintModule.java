package com.sprintmenu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Automatic sprint with a short post-attack pause to avoid repeatedly cancelling hit knockback/sprint reset. */
public final class SprintModule {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sprintmenu.properties");
    private static boolean enabled = false;

    private SprintModule() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (!enabled || p == null || client.currentScreen != null) return;

        boolean movementAndSprintAllowed = client.options.forwardKey.isPressed()
                && !p.isSneaking()
                && !p.isUsingItem()
                && !p.horizontalCollision
                && !p.hasStatusEffect(StatusEffects.BLINDNESS)
                && (p.getHungerManager().getFoodLevel() > 6 || p.getAbilities().allowFlying);

        // Minecraft deliberately stops sprinting on a hit. Wait until the attack cooldown
        // has mostly recovered before re-enabling it, rather than toggling sprint every tick.
        boolean attackRecovered = p.getAttackCooldownProgress(0.0F) >= 0.9F;
        if (movementAndSprintAllowed && attackRecovered && !p.isSprinting()) {
            p.setSprinting(true);
        }
    }

    public static void load() {
        try (InputStream in = Files.newInputStream(FILE)) {
            Properties props = new Properties();
            props.load(in);
            enabled = Boolean.parseBoolean(props.getProperty("sprint", "false"));
        } catch (IOException ignored) {
            // Config file does not exist yet; keep defaults.
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                Properties props = new Properties();
                props.setProperty("sprint", Boolean.toString(enabled));
                props.store(out, "Sprint Menu");
            }
        } catch (IOException ignored) {
        }
    }
}
