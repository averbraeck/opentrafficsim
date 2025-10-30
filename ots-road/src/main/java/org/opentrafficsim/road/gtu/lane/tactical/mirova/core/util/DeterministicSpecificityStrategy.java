package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.KnowledgeChunk;

import java.util.*;

/**
 * Default deterministic selection strategy.
 *
 * <p>In its current form, the selector implements a lightweight deterministic rule set:
 * <ul>
 *   <li>Only patterns for which <b>all required contextual conditions</b> are satisfied
 *       ({@link ManeuverPattern#checkContext()}) and physically feasible
 *       ({@link ManeuverPattern#checkAbility()}) are considered.</li>
 *   <li>Among those, the pattern with the <b>largest number of required context keys</b>
 *       (i.e., the most specific pattern) is preferred.</li>
 *   <li>If two patterns have the same specificity, a predefined
 *       <b>context-type priority hierarchy</b> is applied to resolve ties.</li>
 * </ul>
 *
 * <p>The class exposes only static methods and cannot be instantiated.</p>
 *
 * <p>Example usage inside a tactical planner or vehicle class:
 * <pre>{@code
 * ManeuverPattern selected =
 *     PatternSelector.select(vehicle.getKnowledgeChunks(), PatternType.COOPERATIVE);
 * }</pre></p>
 */
public class DeterministicSpecificityStrategy implements PatternSelectionStrategy {

    /**
     * Selects the most specific and applicable {@link ManeuverPattern} of a given type.
     *
     * <p>The method iterates over all {@link KnowledgeChunk}s and their corresponding
     * pattern suppliers. Each pattern is tested for both contextual and physical validity.
     * Only fully applicable patterns are considered. The pattern with the highest
     * number of required context categories is selected as the most specific one.</p>
     *
     * @param chunks the collection of {@link KnowledgeChunk}s providing maneuver patterns
     * @param type the pattern type to filter for (e.g., COOPERATIVE, TACTICAL_LC)
     * @return the best-fitting pattern, or {@code null} if none are valid
     * @throws ParameterException if a perception or parameter lookup fails during context or ability checks
     */
    @Override
    public ManeuverPattern select(final Collection<KnowledgeChunk> chunks, final ManeuverPattern.PatternType type)
            throws ParameterException {

        ManeuverPattern best = null;
        int maxKeys = -1;

        for (KnowledgeChunk chunk : chunks) {
            for (var supplier : chunk.getManeuverPatterns()) {
                ManeuverPattern p = supplier.get();
                System.out.printf("Evaluating pattern: " + p.toString() + "\n");
                if (p.getType() != type) continue;
                System.out.printf(" - Type matches (%s)\n", type);
                if (p.checkContext() && p.checkAbility()) {
                    int specificity = p.getRequiredContextKeys().size();
                    System.out.printf(" - Applicable with specificity %d\n", specificity);
                    if (specificity > maxKeys) {
                        best = p;
                        maxKeys = specificity;
                        System.out.printf(" --> New best pattern selected.\n");
                    } else if (specificity == maxKeys) {
                        best = resolveTie(best, p);
                        System.out.printf(" --> Tie resolved, selected pattern: %s\n", best.toString());
                    }
                }
            }
        }
        return best;
    }

    /**
     * Resolves a tie between two equally specific {@link ManeuverPattern}s.
     *
     * <p>Tie-breaking is based on a predefined context-type priority hierarchy.
     * Context categories that appear later in the list are considered more
     * specific (and therefore higher priority) than those appearing earlier.</p>
     *
     * <p>Example priority order (from generic to specific):</p>
     * <pre>{@code
     * ["Ego", "Infrastructure", "MacroTraffic", "Neighbors"]
     * }</pre>
     *
     * @param a first maneuver pattern
     * @param b second maneuver pattern
     * @return the pattern considered more specific according to context-type priority
     */
    private ManeuverPattern resolveTie(final ManeuverPattern a, final ManeuverPattern b) {
        List<String> priority = List.of("Ego", "Infrastructure", "MacroTraffic", "Neighbors");
        int scoreA = priorityScore(a.getRequiredContextKeys(), priority);
        int scoreB = priorityScore(b.getRequiredContextKeys(), priority);
        return (scoreB > scoreA) ? b : a;
    }

    /**
     * Computes a numerical specificity score based on the intersection between
     * a pattern’s required context keys and a given context-type priority list.
     *
     * <p>Contexts appearing later in the list are given higher weight to
     * reflect greater situational specificity.</p>
     *
     * @param keys the set of context keys used by a pattern
     * @param priority the global ordered list of context-type priorities
     * @return the accumulated priority score
     */
    private int priorityScore(final Set<String> keys, final List<String> priority) {
        int score = 0;
        for (String k : keys) {
            int idx = priority.indexOf(k);
            if (idx >= 0) score += (priority.size() - idx);
        }
        return score;
    }
}
