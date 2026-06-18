package com.victorfaurschou.fuzzycommandhistory.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "fuzzy-command-history")
public class FuzzyCommandHistoryConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(min = 10, max = 10000)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int historySize = 1000;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean executeOnSubmit = false;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public Scale displayScale = Scale.X100;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.BoundedDiscrete(min = 8, max = 20)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int lineHeight = 10;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.BoundedDiscrete(min = 3, max = 30)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int maxVisible = 10;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.BoundedDiscrete(min = -1, max = 100)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int resultsOpacity = -1;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.BoundedDiscrete(min = -1, max = 100)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int inputOpacity = -1;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean highlightMatches = true;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.ColorPicker(allowAlpha = false)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int highlightColor = 0x55FF55;

    @Override
    public void validatePostLoad() {
        if (displayScale == null) displayScale = Scale.X100;
    }

    public enum Scale {
        X025(0.25),
        X050(0.50),
        X075(0.75),
        X100(1.00),
        X125(1.25),
        X150(1.50),
        X175(1.75),
        X200(2.00);

        public final double factor;

        Scale(double factor) {
            this.factor = factor;
        }

        @Override
        public String toString() {
            long whole = (long) factor;
            return factor == whole ? whole + "x" : factor + "x";
        }
    }
}
