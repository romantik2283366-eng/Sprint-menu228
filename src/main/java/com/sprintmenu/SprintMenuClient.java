package com.sprintmenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SprintMenuClient implements ClientModInitializer {
    public static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("sprintmenu", "main"));

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        SprintModule.load();
        InvMoveModule.load();
        BunnyModule.load();
        TargetHudModule.load();
        AutoTotemModule.load();
        ModuleKeybinds.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.sprintmenu.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));

        ClientSendMessageEvents.ALLOW_CHAT.register(message ->
                !ModuleKeybinds.handleChatCommand(MinecraftClient.getInstance(), message));

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
                TargetHudModule.render(drawContext, MinecraftClient.getInstance()));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new SprintMenuScreen());
            }
            ModuleKeybinds.tick(client);
            SprintModule.tick(client);
            InvMoveModule.tick(client);
            BunnyModule.tick(client);
            TriggerBotModule.tick(client);
            AutoTotemModule.tick(client);
        });
    }
}
