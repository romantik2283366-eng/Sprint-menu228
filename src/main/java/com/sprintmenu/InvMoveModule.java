package com.sprintmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/** Keeps keyboard movement active while a screen (for example, inventory) is open. */
public final class InvMoveModule {
    private static boolean enabled;

    private InvMoveModule() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void load() {
        // Enabled only for the current session; no separate config file is needed.
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.currentScreen == null) {
            return;
        }

        long window = client.getWindow().getHandle();
        setPressed(client.options.forwardKey, window);
        setPressed(client.options.backKey, window);
        setPressed(client.options.leftKey, window);
        setPressed(client.options.rightKey, window);
        setPressed(client.options.jumpKey, window);
        setPressed(client.options.sneakKey, window);
    }

    private static void setPressed(KeyBinding binding, long window) {
        if (!enabled) {
            binding.setPressed(false);
            return;
        }

        InputUtil.Key key = binding.getBoundKey();
        int code = key.getCode();
        binding.setPressed(code >= 0 && InputUtil.isKeyPressed(window, code));
    }
}
