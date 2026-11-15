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

/** Функция "Sprint": автоматический бег при движении вперёд. */
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

        boolean canSprint = client.options.forwardKey.isPressed()
                && !p.isSprinting()
                && !p.isSneaking()
                && !p.isUsingItem()
                && !p.horizontalCollision
                && !p.hasStatusEffect(StatusEffects.BLINDNESS)
                && (p.getHungerManager().getFoodLevel() > 6 || p.getAbilities().allowFlying);

        if (canSprint) {
            p.setSprinting(true);
        }
    }

    public static void load() {
        try (InputStream in = Files.newInputStream(FILE)) {
            Properties props = new Properties();
            props.load(in);
            enabled = Boolean.parseBoolean(props.getProperty("sprint", "false"));
        } catch (IOException ignored) {
            // файла ещё нет — значения по умолчанию
        }
    }

    private static void save() {
        try (OutputStream out = Files.newOutputStream(FILE)) {
            Properties props = new Properties();
            props.setProperty("sprint", Boolean.toString(enabled));
            props.store(out, "Sprint Menu");
        } catch (IOException ignored) {
        }
    }
}
