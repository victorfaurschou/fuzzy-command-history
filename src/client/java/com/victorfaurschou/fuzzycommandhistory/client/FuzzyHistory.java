package com.victorfaurschou.fuzzycommandhistory.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FuzzyHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuzzyHistory.class);
    private static final FuzzyHistory INSTANCE = new FuzzyHistory();

    private final Path path;
    private final ArrayDeque<String> commands = new ArrayDeque<>();

    private FuzzyHistory() {
        this.path = FabricLoader.getInstance().getConfigDir().resolve("fuzzy-command-history-commands.txt");
        load();
    }

    public static FuzzyHistory getInstance() {
        return INSTANCE;
    }

    public void add(String command) {
        if (command.isBlank()) return;
        commands.remove(command);
        commands.addLast(command);
        trim();
        save();
    }

    // oldest first, most recent last — caller reverses for display if needed
    public List<String> getHistory() {
        return new ArrayList<>(commands);
    }

    private int getLimit() {
        return AutoConfig.getConfigHolder(FuzzyCommandHistoryConfig.class).getConfig().historySize;
    }

    private void trim() {
        int limit = getLimit();
        while (commands.size() > limit) commands.removeFirst();
    }

    private void load() {
        if (Files.exists(path)) {
            try {
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    if (!line.isBlank()) commands.addLast(line);
                }
                trim();
            } catch (IOException e) {
                LOGGER.warn("Failed to load command history from {}", path, e);
            }
        } else {
            // first run: seed from vanilla commandHistory
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                Collection<String> vanilla = mc.commandHistory().history();
                for (String cmd : vanilla) {
                    if (!cmd.isBlank()) commands.addLast(cmd);
                }
                trim();
                save();
            }
        }
    }

    private void save() {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, commands, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Failed to save command history to {}", path, e);
        }
    }
}
