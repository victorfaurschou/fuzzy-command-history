package com.victorfaurschou.fuzzycommandhistory.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FuzzySearchScreen extends Screen {
    private static final int INPUT_X = 4;
    private static final int INPUT_HEIGHT = 12;

    private final String previousInput;
    private final boolean openedFromGame;
    private EditBox searchBox;
    private List<String> allHistory;
    private List<String> filtered;
    private String[] currentTokens = new String[0];
    private List<Set<Integer>> matchedIndicesCache = List.of();
    private int selectedIndex;

    public FuzzySearchScreen(String previousInput, boolean openedFromGame) {
        super(Component.literal("Fuzzy Command History"));
        this.previousInput = previousInput;
        this.openedFromGame = openedFromGame;
    }

    private static FuzzyCommandHistoryConfig getConfig() {
        return AutoConfig.getConfigHolder(FuzzyCommandHistoryConfig.class).getConfig();
    }

    @Override
    protected void init() {
        minecraft.gui.getChat().discardDraft();
        allHistory = FuzzyHistory.getInstance().getHistory();
        updateFilter(previousInput);

        int counterReserve = font.width("9999/9999") + 6;
        int inputWidth = width - INPUT_X - 2 - counterReserve;

        searchBox = new EditBox(font, INPUT_X, height - INPUT_HEIGHT, inputWidth, INPUT_HEIGHT, Component.literal(""));
        searchBox.setMaxLength(256);
        searchBox.setBordered(false);
        searchBox.setCanLoseFocus(false);
        searchBox.setValue(previousInput);
        searchBox.setResponder(this::updateFilter);
        addRenderableWidget(searchBox);
        setInitialFocus(searchBox);
    }

    private void updateFilter(String query) {
        FuzzyCommandHistoryConfig cfg = getConfig();
        currentTokens = query.isBlank() ? new String[0] : query.toLowerCase().split("\\s+");
        filtered = applyFilter(allHistory, currentTokens);
        selectedIndex = filtered.size() - 1;
        if (cfg.highlightMatches && currentTokens.length > 0) {
            List<Set<Integer>> cache = new java.util.ArrayList<>(filtered.size());
            for (String entry : filtered) cache.add(matchIndices(currentTokens, entry));
            matchedIndicesCache = cache;
        } else {
            matchedIndicesCache = List.of();
        }
    }

    private List<String> applyFilter(List<String> all, String[] tokens) {
        if (tokens.length == 0) return List.copyOf(all);
        record Scored(String entry, String lc, int score) {}
        return all.stream()
            .map(s -> new Scored(s, s.toLowerCase(), 0))
            .filter(sc -> {
                for (String token : tokens) {
                    if (!sc.lc().contains(token)) return false;
                }
                return true;
            })
            .map(sc -> {
                int score = 0;
                for (String token : tokens) score += sc.lc().indexOf(token);
                return new Scored(sc.entry(), sc.lc(), score);
            })
            .sorted(Comparator.comparingInt(Scored::score))
            .map(Scored::entry)
            .toList();
    }

    private Set<Integer> matchIndices(String[] tokens, String candidate) {
        Set<Integer> indices = new HashSet<>();
        String lc = candidate.toLowerCase();
        for (String token : tokens) {
            int idx = lc.indexOf(token);
            while (idx != -1) {
                for (int k = 0; k < token.length(); k++) indices.add(idx + k);
                idx = lc.indexOf(token, idx + 1);
            }
        }
        return indices;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            if (openedFromGame) {
                minecraft.setScreen(null);
            } else {
                minecraft.setScreen(new ChatScreen(searchBox.getValue(), false));
            }
            return true;
        }
        if (event.isConfirmation()) {
            boolean execute = event.hasControlDown() || getConfig().executeOnSubmit;
            confirmSelection(execute);
            return true;
        }
        if (event.isUp() && !filtered.isEmpty()) {
            selectedIndex = Math.max(0, selectedIndex - 1);
            return true;
        }
        if (event.isDown() && !filtered.isEmpty()) {
            selectedIndex = Math.min(filtered.size() - 1, selectedIndex + 1);
            return true;
        }
        if (event.isCycleFocus()) {
            if (!filtered.isEmpty()) {
                if (event.hasShiftDown()) {
                    selectedIndex = Math.max(0, selectedIndex - 1);
                } else {
                    selectedIndex = Math.min(filtered.size() - 1, selectedIndex + 1);
                }
            }
            return true;
        }
        return super.keyPressed(event);
    }

    private void confirmSelection(boolean execute) {
        String selected = filtered.isEmpty() ? searchBox.getValue() : filtered.get(selectedIndex);
        if (execute && minecraft.player != null) {
            minecraft.gui.getChat().addRecentChat(selected);
            minecraft.setScreen(null);
            if (selected.startsWith("/")) {
                minecraft.player.connection.sendCommand(selected.substring(1));
            } else {
                minecraft.player.connection.sendChat(selected);
            }
        } else {
            minecraft.setScreen(new ChatScreen(selected, false));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        FuzzyCommandHistoryConfig cfg = getConfig();
        double displayScaleD = cfg.displayScale.factor;
        float displayScaleF = (float) displayScaleD;
        int lineHeight = cfg.lineHeight;
        int maxVisible = cfg.maxVisible;
        double vanillaOpacity = (double) (Double) minecraft.options.textBackgroundOpacity().get();
        int resultsAlpha = cfg.resultsOpacity == -1
                ? (int) (vanillaOpacity * 255.0)
                : (int) (cfg.resultsOpacity / 100.0 * 255.0);
        int inputAlpha = cfg.inputOpacity == -1
                ? (int) (vanillaOpacity * 255.0)
                : (int) (cfg.inputOpacity / 100.0 * 255.0);
        int resultsBgColor = resultsAlpha << 24;
        int inputBgColor = inputAlpha << 24;

        // Input background: 2px inset left/right, 2px above EditBox top, 2px from screen bottom
        extractor.fill(2, height - INPUT_HEIGHT - 2, width - 2, height - 2, inputBgColor);

        // Results in display-scale space
        extractor.pose().pushMatrix();
        extractor.pose().scale(displayScaleF, displayScaleF);

        // 2px gap between results bottom and input fill top (fill top = height - INPUT_HEIGHT - 2)
        int scaledBottom = (int) ((height - INPUT_HEIGHT - 4) / displayScaleD);
        int scaledStartX = (int) (2 / displayScaleD);
        int scaledTextX = (int) (INPUT_X / displayScaleD);
        int scaledWidth = (int) ((width - 4) / displayScaleD);

        if (!filtered.isEmpty()) {
            int visibleCount = Math.min(filtered.size(), maxVisible);
            int listTop = scaledBottom - visibleCount * lineHeight;

            extractor.fill(scaledStartX, listTop - 1, scaledStartX + scaledWidth, scaledBottom, resultsBgColor);

            int end = Math.min(filtered.size(), Math.max(selectedIndex + 1, maxVisible));
            int start = Math.max(0, end - maxVisible);
            int textOffsetY = (lineHeight - font.lineHeight) / 2;
            for (int i = start; i < end; i++) {
                int y = listTop + (i - start) * lineHeight;
                if (i == selectedIndex) {
                    extractor.fill(scaledStartX, y, scaledStartX + scaledWidth, y + lineHeight, 0x60FFFFFF);
                }
                boolean selected = i == selectedIndex;
                int normalColor = selected ? 0xFFFFFFFF : 0xFFAAAAAA;
                int matchColor  = 0xFF000000 | cfg.highlightColor;
                String entry = filtered.get(i);
                Set<Integer> matched = (selected || matchedIndicesCache.isEmpty())
                        ? Set.of()
                        : matchedIndicesCache.get(i);
                if (matched.isEmpty()) {
                    extractor.text(font, entry, scaledTextX, y + textOffsetY, normalColor);
                } else {
                    int cx = scaledTextX;
                    int runStart = 0;
                    while (runStart < entry.length()) {
                        boolean isMatch = matched.contains(runStart);
                        int runEnd = runStart + 1;
                        while (runEnd < entry.length() && matched.contains(runEnd) == isMatch) runEnd++;
                        String seg = entry.substring(runStart, runEnd);
                        extractor.text(font, seg, cx, y + textOffsetY, isMatch ? matchColor : normalColor);
                        cx += font.width(seg);
                        runStart = runEnd;
                    }
                }
            }
        } else {
            int listTop = scaledBottom - lineHeight;
            int textOffsetY = (lineHeight - font.lineHeight) / 2;
            extractor.fill(scaledStartX, listTop - 1, scaledStartX + scaledWidth, scaledBottom, resultsBgColor);
            String emptyText = allHistory.isEmpty() ? "No history yet" : "No results";
            extractor.text(font, Component.literal(emptyText), scaledTextX, listTop + textOffsetY, 0xFF666666);
        }

        extractor.pose().popMatrix();

        String counter = filtered.size() + "/" + allHistory.size();
        int counterX = width - 2 - font.width(counter);
        int counterY = height - INPUT_HEIGHT + (INPUT_HEIGHT - font.lineHeight) / 2;
        extractor.text(font, counter, counterX, counterY, 0xFFAAAAAA);

        super.extractRenderState(extractor, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
