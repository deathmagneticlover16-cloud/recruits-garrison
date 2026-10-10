package dev.raybert.garrison;

/** One definition shared by movement, melee eligibility and the visible marker. */
public final class PostArrival {
    private PostArrival() {}
    public static boolean reached(double x, double y, double z, int blockX, int feetY, int blockZ) {
        double dx = x - (blockX + 0.5), dz = z - (blockZ + 0.5);
        return Math.floor(x) == blockX && Math.floor(z) == blockZ
                && dx * dx + dz * dz <= 0.0625 && Math.abs(y - feetY) <= 0.25;
    }
}
