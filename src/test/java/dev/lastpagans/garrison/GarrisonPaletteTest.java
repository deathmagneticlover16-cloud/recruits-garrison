package dev.lastpagans.garrison;

import java.util.Arrays;

public final class GarrisonPaletteTest {
    private static int checks;
    private static void check(boolean condition, String description) {
        checks++; if (!condition) throw new AssertionError(description);
    }
    public static void main(String[] args) {
        check(GarrisonPalette.RGB.length == 25, "Recruits unit palette has 25 colors");
        check(GarrisonPalette.NAMES.length == GarrisonPalette.RGB.length, "Every color has a translation name");
        check(GarrisonPalette.valid(GarrisonPalette.DEFAULT_COLOR), "Default color is valid");
        check(!GarrisonPalette.valid(-1), "Negative color index is rejected");
        check(!GarrisonPalette.valid(GarrisonPalette.RGB.length), "Out-of-range color index is rejected");
        check(Arrays.stream(GarrisonPalette.NAMES).distinct().count() == GarrisonPalette.NAMES.length, "Color names are unique");
        check(Arrays.stream(GarrisonPalette.RGB).allMatch(color -> color >= 0 && color <= 0xFFFFFF), "Colors are RGB values");
        System.out.println("PASS: " + checks + " palette checks");
    }
}
