package dev.lastpagans.garrison;

import com.talhanation.recruits.entities.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public enum PostType {
    ANY, RANGED, MELEE, BOWMAN, CROSSBOWMAN, SHIELDMAN, RECRUIT;
    private static final Map<PostType, Predicate<AbstractRecruitEntity>> RULES = new LinkedHashMap<>();
    static {
        RULES.put(ANY, r -> true);
        RULES.put(RANGED, r -> r instanceof IRangedRecruit);
        RULES.put(MELEE, r -> !(r instanceof IRangedRecruit));
        RULES.put(BOWMAN, r -> r instanceof BowmanEntity);
        RULES.put(CROSSBOWMAN, r -> r instanceof CrossBowmanEntity);
        RULES.put(SHIELDMAN, r -> r instanceof RecruitShieldmanEntity);
        RULES.put(RECRUIT, r -> r instanceof RecruitEntity);
    }
    public boolean accepts(AbstractRecruitEntity recruit) { return RULES.get(this).test(recruit); }
    /** Register expanded compatibility from common setup, before gameplay. */
    public static void register(PostType type, Predicate<AbstractRecruitEntity> rule) { RULES.put(type, rule); }
    public int specificity() { return this == ANY ? 2 : this == RANGED || this == MELEE ? 1 : 0; }
}
