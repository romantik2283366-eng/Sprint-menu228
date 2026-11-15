package com.sprintmenu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Defensive helper: swaps an inventory totem into the offhand below a configurable health threshold. */
public final class AutoTotemModule {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sprintmenu-autototem.properties");
    private static boolean enabled;
    private static int healthThreshold = 6;

    private AutoTotemModule() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; save(); }
    public static int getHealthThreshold() { return healthThreshold; }

    public static void setHealthThreshold(int value) {
        healthThreshold = Math.max(1, Math.min(20, value));
        save();
    }

    public static void load() {
        try (InputStream in = Files.newInputStream(FILE)) {
            Properties props = new Properties();
            props.load(in);
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "false"));
            healthThreshold = Math.max(1, Math.min(20, Integer.parseInt(props.getProperty("health", "6"))));
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    public static void tick(MinecraftClient client) {
        if (!enabled || client.player == null || client.interactionManager == null
                || client.currentScreen != null || client.player.getHealth() > healthThreshold) return;

        if (client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        PlayerInventory inventory = client.player.getInventory();
        for (int inventorySlot = 0; inventorySlot < inventory.size(); inventorySlot++) {
            if (!inventory.getStack(inventorySlot).isOf(Items.TOTEM_OF_UNDYING)) continue;

            // Player screen-handler indices: main inventory 9-35, hotbar 36-44, offhand swap button 40.
            int handlerSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
            client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId,
                    handlerSlot, 40, SlotActionType.SWAP, client.player);
            return;
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                Properties props = new Properties();
                props.setProperty("enabled", Boolean.toString(enabled));
                props.setProperty("health", Integer.toString(healthThreshold));
                props.store(out, "Bunny DLC Auto Totem");
            }
        } catch (IOException ignored) {
        }
    }
}
