package com.sprintmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Chat-configurable keyboard toggles for client modules. */
public final class ModuleKeybinds {
    private static final Path FILE = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getConfigDir().resolve("sprintmenu-keybinds.properties");
    private static final Map<String, Binding> MODULES = new LinkedHashMap<>();
    private static final Map<String, Integer> BINDS = new LinkedHashMap<>();
    private static final Map<Integer, Boolean> WAS_DOWN = new LinkedHashMap<>();

    private record Binding(BooleanSupplier enabled, Consumer<Boolean> toggle) {}

    private ModuleKeybinds() {}

    public static void load() {
        MODULES.clear();
        MODULES.put("triggerbot", new Binding(TriggerBotModule::isEnabled,
                value -> TriggerBotModule.setEnabled(value)));
        MODULES.put("sprint", new Binding(SprintModule::isEnabled, SprintModule::setEnabled));
        MODULES.put("invmove", new Binding(InvMoveModule::isEnabled, InvMoveModule::setEnabled));
        MODULES.put("bunnies", new Binding(BunnyModule::isEnabled, BunnyModule::setEnabled));
        MODULES.put("targethud", new Binding(TargetHudModule::isEnabled, TargetHudModule::setEnabled));
        MODULES.put("autototem", new Binding(AutoTotemModule::isEnabled, AutoTotemModule::setEnabled));
        BINDS.clear();
        WAS_DOWN.clear();
        for (String module : MODULES.keySet()) BINDS.put(module, GLFW.GLFW_KEY_UNKNOWN);

        try (InputStream in = Files.newInputStream(FILE)) {
            Properties props = new Properties();
            props.load(in);
            for (String module : MODULES.keySet()) {
                BINDS.put(module, parseKey(props.getProperty(module, "NONE")));
            }
        } catch (IOException ignored) {
            // No saved keybinds yet.
        }
    }

    /** Handles only local .bind commands; returns false to prevent sending a command to the server. */
    public static boolean handleChatCommand(MinecraftClient client, String message) {
        if (message == null || !message.trim().toLowerCase().startsWith(".bind")) return false;
        String[] parts = message.trim().split("\\s+");
        if (parts.length == 2 && parts[1].equalsIgnoreCase("list")) {
            StringBuilder result = new StringBuilder("Бинды:");
            for (String module : MODULES.keySet()) {
                result.append(" ").append(module).append("=").append(keyName(BINDS.getOrDefault(module, -1)));
            }
            reply(client, result.toString());
            return true;
        }
        if (parts.length != 3) {
            reply(client, "Использование: .bind <triggerbot|sprint|invmove|bunnies|targethud|autototem> <клавиша|off> или .bind list");
            return true;
        }

        String module = parts[1].toLowerCase();
        if (!MODULES.containsKey(module)) {
            reply(client, "Неизвестная функция: " + module);
            return true;
        }
        int key = parseKey(parts[2]);
        if (key == Integer.MIN_VALUE) {
            reply(client, "Неизвестная клавиша: " + parts[2]);
            return true;
        }

        BINDS.put(module, key);
        WAS_DOWN.remove(key);
        save();
        reply(client, key == GLFW.GLFW_KEY_UNKNOWN
                ? "Бинд снят: " + module
                : "Бинд установлен: " + module + " → " + keyName(key));
        return true;
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.getWindow() == null) return;
        long window = client.getWindow().getHandle();
        boolean canToggle = client.player != null && client.currentScreen == null;
        for (Map.Entry<String, Integer> entry : BINDS.entrySet()) {
            int key = entry.getValue();
            if (key == GLFW.GLFW_KEY_UNKNOWN) continue;
            boolean down = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            boolean wasDown = WAS_DOWN.getOrDefault(key, false);
            WAS_DOWN.put(key, down);
            if (canToggle && down && !wasDown) {
                Binding binding = MODULES.get(entry.getKey());
                boolean newState = !binding.enabled().getAsBoolean();
                binding.toggle().accept(newState);
                reply(client, entry.getKey() + (newState ? " включён" : " выключен"));
            }
        }
    }

    private static int parseKey(String value) {
        String key = value.trim().toUpperCase();
        if (key.equals("OFF") || key.equals("NONE") || key.equals("UNBIND")) return GLFW.GLFW_KEY_UNKNOWN;
        if (key.length() == 1) {
            char c = key.charAt(0);
            if (c >= 'A' && c <= 'Z') return GLFW.GLFW_KEY_A + (c - 'A');
            if (c >= '0' && c <= '9') return GLFW.GLFW_KEY_0 + (c - '0');
        }
        if (key.matches("F([1-9]|1[0-2])")) return GLFW.GLFW_KEY_F1 + Integer.parseInt(key.substring(1)) - 1;
        return switch (key) {
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            case "ESC", "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE;
            case "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LCTRL", "CTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LALT", "ALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "INSERT" -> GLFW.GLFW_KEY_INSERT;
            case "DELETE", "DEL" -> GLFW.GLFW_KEY_DELETE;
            case "HOME" -> GLFW.GLFW_KEY_HOME;
            case "END" -> GLFW.GLFW_KEY_END;
            default -> Integer.MIN_VALUE;
        };
    }

    private static String keyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) return String.valueOf((char) ('A' + key - GLFW.GLFW_KEY_A));
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) return String.valueOf((char) ('0' + key - GLFW.GLFW_KEY_0));
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            default -> "KEY_" + key;
        };
    }

    private static void reply(MinecraftClient client, String text) {
        if (client != null && client.player != null) client.player.sendMessage(Text.literal("[Bunny DLC] " + text), false);
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Properties props = new Properties();
            for (Map.Entry<String, Integer> entry : BINDS.entrySet()) {
                props.setProperty(entry.getKey(), keyName(entry.getValue()));
            }
            try (OutputStream out = Files.newOutputStream(FILE)) {
                props.store(out, "Bunny DLC keyboard binds");
            }
        } catch (IOException ignored) {
        }
    }
}
