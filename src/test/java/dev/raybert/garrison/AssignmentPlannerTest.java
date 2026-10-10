package dev.raybert.garrison;

import java.util.*;

/** Run with Java 17, no game or test framework required. */
public final class AssignmentPlannerTest {
    record Soldier(String name, String role, int x) {}
    record Post(String name, String role, int x) {}
    static int checks;
    static Map<Post, Soldier> plan(List<Post> posts, List<Soldier> soldiers) {
        return AssignmentPlanner.plan(posts, soldiers, (r, p) -> p.role.equals("ANY") || p.role.equals(r.role),
                p -> p.role.equals("ANY") ? 1 : 0, (r, p) -> Math.abs(r.x - p.x));
    }
    static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        List<Soldier> ten = new ArrayList<>(); List<Post> five = new ArrayList<>();
        for (int i = 0; i < 10; i++) ten.add(new Soldier("bow" + i, "BOW", i));
        for (int i = 0; i < 5; i++) five.add(new Post("post" + i, "BOW", i));
        Map<Post, Soldier> first = plan(five, ten);
        check(first.size() == 5, "Five defenders");
        check(new HashSet<>(first.values()).size() == 5, "No double assignments");
        List<Soldier> reserves = ten.stream().filter(s -> !first.containsValue(s)).toList();
        check(reserves.size() == 5, "Five reserves");
        Map<Post, Soldier> replacement = plan(List.of(five.get(2)), reserves);
        check(replacement.size() == 1, "Exactly one replacement");
        check(replacement.get(five.get(2)).equals(ten.get(5)), "Nearest reserve");
        check(first.size() == 5 && ten.size() == 10 && five.size() == 5, "Inputs unchanged");
        check(plan(List.of(new Post("s", "SHIELD", 0)), ten).isEmpty(), "Incompatible units rejected");
        Post any = new Post("any", "ANY", 0), bow = new Post("bow", "BOW", 10);
        Soldier archer = new Soldier("only bow", "BOW", 0), shield = new Soldier("shield", "SHIELD", 1);
        Map<Post, Soldier> specialized = plan(List.of(any, bow), List.of(archer, shield));
        check(specialized.get(bow).equals(archer) && specialized.get(any).equals(shield), "Specialized post is not starved by ANY");
        check(plan(five, List.of()).isEmpty(), "No reserves is safe");
        check(plan(List.of(), ten).isEmpty(), "No vacancies does not redeploy defenders");
        check(plan(List.of(any), List.of(shield)).get(any).equals(shield), "Independent group can reuse the same policy");
        System.out.println("PASS: " + checks + " assignment checks");
    }
}
