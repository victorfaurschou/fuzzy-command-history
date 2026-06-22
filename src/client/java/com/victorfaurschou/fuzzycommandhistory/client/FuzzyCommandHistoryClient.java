package com.victorfaurschou.fuzzycommandhistory.client;

import com.mojang.brigadier.Command;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class FuzzyCommandHistoryClient implements ClientModInitializer {
    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(FuzzyCommandHistoryConfig.class, GsonConfigSerializer::new);

        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.fuzzy-command-history.open",
            GLFW.GLFW_KEY_Y,
            KeyMapping.Category.MISC
        ));

        ClientSendMessageEvents.COMMAND.register(command ->
            FuzzyHistory.getInstance().add("/" + command)
        );

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommands.literal("fuzzy-command-history")
                .then(ClientCommands.literal("clear").executes(ctx -> {
                    FuzzyHistory.getInstance().clear();
                    ctx.getSource().sendFeedback(Component.literal("Command history cleared."));
                    return Command.SINGLE_SUCCESS;
                })))
        );

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                if (mc.gui.screen() == null && mc.gui.overlay() == null) {
                    mc.gui.setScreen(new FuzzySearchScreen("", true));
                }
            }
        });

    }
}
