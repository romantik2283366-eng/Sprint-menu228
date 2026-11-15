package com.sprintmenu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Bunny-styled target HUD with saved placement and chat-screen drag editing. */
public final class TargetHudModule {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sprintmenu-targethud.properties");
    private static final int HEIGHT = 44;
    private static boolean enabled = true;
    private static boolean editMode;
    private static int hudX = -1;
    private static int hudY = -1;
    private static int hudWidth = 180;
    private static boolean dragging;
    private static int dragOffsetX;
    private static int dragOffsetY;
    private static boolean leftWasDown;

    private TargetHudModule() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; save(); }
    public static boolean isEditMode() { return editMode; }
    public static void setEditMode(boolean value) { editMode = value; dragging = false; save(); }
    public static int getWidth() { return hudWidth; }

    public static void setWidth(int value) {
        hudWidth = Math.max(150, Math.min(250, value));
        save();
    }

    public static void resetPosition() {
        hudX = -1;
        hudY = -1;
        save();
    }

    public static void load() {
        try (InputStream in = Files.newInputStream(FILE)) {
            Properties props = new Properties();
            props.load(in);
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            hudX = Integer.parseInt(props.getProperty("x", "-1"));
            hudY = Integer.parseInt(props.getProperty("y", "-1"));
            hudWidth = Math.max(150, Math.min(250, Integer.parseInt(props.getProperty("width", "180"))));
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (!enabled || client == null || client.player == null || client.world == null) return;

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        if (hudX < 0) hudX = screenW / 2 - hudWidth / 2;
        if (hudY < 0) hudY = screenH / 2 + 24;
        hudX = Math.max(0, Math.min(hudX, screenW - hudWidth));
        hudY = Math.max(0, Math.min(hudY, screenH - HEIGHT));

        Entity target = getTarget(client);
        boolean editingChat = editMode && client.currentScreen != null
                && client.currentScreen.getClass().getSimpleName().contains("Chat");
        if (editingChat) {
            updateDrag(client, screenW, screenH);
        } else {
            dragging = false;
            leftWasDown = false;
        }
        if (target == null && !editingChat) return;

        int x = hudX;
        int y = hudY;
        int w = hudWidth;
        context.fill(x, y, x + w, y + HEIGHT, 0xD91D2028);
        context.fill(x, y, x + w, y + 3, 0xFF79E58B);
        context.fill(x + 7, y + 8, x + 17, y + 18, 0xFFFFD6E4);
        context.fill(x + 9, y + 5, x + 12, y + 13, 0xFFF2F2F5);
        context.fill(x + 13, y + 5, x + 16, y + 13, 0xFFF2F2F5);

        String name = "Перетащи HUD в чате";
        float health = 0.0F;
        float maxHealth = 1.0F;
        if (target instanceof LivingEntity living) {
            name = target.getName().getString();
            health = Math.max(0.0F, living.getHealth());
            maxHealth = Math.max(1.0F, living.getMaxHealth());
        }
        int textX = x + 24;
        context.drawText(client.textRenderer, Text.literal(trim(name, w - 34)), textX, y + 8, 0xFFFFFFFF, true);

        if (target instanceof LivingEntity) {
            int barX = x + 24;
            int barY = y + 27;
            int barW = w - 32;
            float ratio = Math.max(0.0F, Math.min(1.0F, health / maxHealth));
            context.fill(barX, barY, barX + barW, barY + 7, 0xFF45454D);
            context.fill(barX, barY, barX + Math.round(barW * ratio), barY + 7, ratio > 0.5F ? 0xFF79E58B : 0xFFFF8795);
            context.drawText(client.textRenderer,
                    Text.literal((int) Math.ceil(health) + " / " + (int) Math.ceil(maxHealth) + " HP"),
                    x + 24, y + 19, 0xFFE5E7EB, true);
        } else {
            context.drawText(client.textRenderer, Text.literal("Наведись на цель"), textX, y + 23, 0xFFE5E7EB, true);
        }
        if (editingChat) {
            context.fill(x, y + HEIGHT - 2, x + w, y + HEIGHT, 0xFFFFD6E4);
        }
    }

    private static void updateDrag(MinecraftClient client, int screenW, int screenH) {
        long window = client.getWindow().getHandle();
        double cursorX;
        double cursorY;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer x = stack.mallocDouble(1);
            DoubleBuffer y = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(window, x, y);
            double scale = client.getWindow().getScaleFactor();
            cursorX = x.get(0) / scale;
            cursorY = y.get(0) / scale;
        }

        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (leftDown && !leftWasDown && cursorX >= hudX && cursorX <= hudX + hudWidth
                && cursorY >= hudY && cursorY <= hudY + HEIGHT) {
            dragging = true;
            dragOffsetX = (int) cursorX - hudX;
            dragOffsetY = (int) cursorY - hudY;
        }
        if (dragging && leftDown) {
            hudX = Math.max(0, Math.min(screenW - hudWidth, (int) cursorX - dragOffsetX));
            hudY = Math.max(0, Math.min(screenH - HEIGHT, (int) cursorY - dragOffsetY));
        }
        if (dragging && !leftDown) {
            dragging = false;
            save();
        }
        leftWasDown = leftDown;
    }

    private static Entity getTarget(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity != null && entity != client.player && entity instanceof LivingEntity && entity.isAlive()) {
                return entity;
            }
        }
        return null;
    }

    private static String trim(String value, int maxWidth) {
        if (value.length() <= 24) return value;
        return value.substring(0, Math.max(4, Math.min(value.length(), 21))) + "...";
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                Properties props = new Properties();
                props.setProperty("enabled", Boolean.toString(enabled));
                props.setProperty("x", Integer.toString(hudX));
                props.setProperty("y", Integer.toString(hudY));
                props.setProperty("width", Integer.toString(hudWidth));
                props.store(out, "Bunny Target HUD");
            }
        } catch (IOException ignored) {
        }
    }
}
