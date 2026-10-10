package dev.lastpagans.garrison;

public final class PostArrivalTest {
    private static int checks;
    private static void check(boolean condition, String description) {
        checks++; if (!condition) throw new AssertionError(description);
    }
    public static void main(String[] args) {
        check(PostArrival.reached(10.5, 65, 20.5, 10, 65, 20), "Marked block center is occupied");
        check(PostArrival.reached(10.7, 65, 20.5, 10, 65, 20), "Small final-step tolerance");
        check(!PostArrival.reached(9.9, 65, 20.5, 10, 65, 20), "Adjacent block is never occupied");
        check(!PostArrival.reached(10.01, 65, 20.01, 10, 65, 20), "Must finish approach inside block");
        check(!PostArrival.reached(10.5, 64, 20.5, 10, 65, 20), "Below post is not occupied");
        check(!PostArrival.reached(10.5, 66, 20.5, 10, 65, 20), "Above post is not occupied");
        check(PostArrival.reached(-10.5, 65, -20.5, -11, 65, -21), "Negative-coordinate block center");
        check(!PostArrival.reached(-9.9, 65, -20.5, -11, 65, -21), "Negative coordinates do not truncate into wrong block");
        System.out.println("PASS: " + checks + " arrival checks");
    }
}
