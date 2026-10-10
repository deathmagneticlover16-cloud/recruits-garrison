package dev.lastpagans.garrison;

import java.util.*;
import java.util.function.*;

/** Pure assignment policy. Callers supply vacant posts and unassigned, loaded recruits only. */
public final class AssignmentPlanner {
    private AssignmentPlanner() {}
    public static <P, R> Map<P, R> plan(List<P> vacancies, List<R> reserves,
            BiPredicate<R, P> accepts, ToIntFunction<P> specificity, ToDoubleBiFunction<R, P> distance) {
        List<R> available = new ArrayList<>(reserves);
        List<P> pending = new ArrayList<>(vacancies);
        Map<P, R> result = new LinkedHashMap<>();
        Map<P, Long> counts = new HashMap<>();
        for (P p : pending) counts.put(p, available.stream().filter(r -> accepts.test(r, p)).count());
        pending.sort(Comparator.comparingLong((P p) -> counts.get(p)).thenComparingInt(specificity));
        while (!pending.isEmpty() && !available.isEmpty()) {
            // Stable ordering, O(posts * recruits) compatibility work, no per-tick planning.
            P post = pending.remove(0);
            R recruit = available.stream().filter(r -> accepts.test(r, post))
                    .min(Comparator.comparingDouble(r -> distance.applyAsDouble(r, post))).orElse(null);
            if (recruit != null) { result.put(post, recruit); available.remove(recruit); }
        }
        return result;
    }
}
