package dev.lastpagans.garrison;

/** The 25 unit colors exposed by Recruits' faction creation menu, in native order. */
public final class GarrisonPalette {
    public static final int DEFAULT_COLOR = 0;
    public static final int[] RGB = {
            0xFFFFFF, 0x000000, 0xD3D3D3, 0x808080, 0x404040,
            0x337B9A, 0x335C9A, 0x15253E, 0x269C08, 0x008000,
            0x0E3903, 0xB91515, 0x941010, 0x4A0808, 0x8C4C18,
            0x5E3B1D, 0x3F2713, 0x009696, 0x174444, 0x0B2222,
            0xEDCA12, 0xE56A11, 0xFF00FF, 0x570265, 0xDD9A19
    };
    public static final String[] NAMES = {
            "white", "black", "light_gray", "gray", "dark_gray",
            "light_blue", "blue", "dark_blue", "light_green", "green",
            "dark_green", "light_red", "red", "dark_red", "light_brown",
            "brown", "dark_brown", "light_cyan", "cyan", "dark_cyan",
            "yellow", "orange", "magenta", "purple", "gold"
    };

    private GarrisonPalette() {}

    public static boolean valid(int index) {
        return index >= 0 && index < RGB.length;
    }
}
