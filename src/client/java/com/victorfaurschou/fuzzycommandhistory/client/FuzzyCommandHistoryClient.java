package com.victorfaurschou.fuzzycommandhistory.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.KeyMapping;
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

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                if (mc.gui.screen() == null && mc.gui.overlay() == null) {
                    mc.gui.setScreen(new FuzzySearchScreen("", true));
                }
            }
        });

    }
}
