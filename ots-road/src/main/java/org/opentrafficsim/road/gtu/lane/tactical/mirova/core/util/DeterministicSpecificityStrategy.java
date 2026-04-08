package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;

/**
 * Default deterministic selection strategy for Maneuver Patterns.
 * <p>
 * This class forms the default logic for <b>Layer 3 (Decision / Strategy)</b> in the
 * MiRoVA architecture. It implements a lightweight deterministic rule set to choose
 * the best maneuver to execute:
 * <ul>
 * <li>Only patterns for which <b>all required contextual conditions</b> are satisfied
 * ({@link ManeuverPattern#checkContext()}) and physically feasible
 * ({@link ManeuverPattern#checkAbility()}) are considered.</li>
 * <li>Among those, the pattern with the <b>largest number of required context keys</b>
 * (i.e., the most specific pattern) is preferred.</li>
 * <li>If two patterns have the same specificity, a predefined
 * <b>context-type priority hierarchy</b> is applied to resolve ties.</li>
 * </ul>
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class DeterministicSpecificityStrategy implements PatternSelectionStrategy {

    /**
     * Selects the most specific and applicable {@link ManeuverPattern} from a given list.
     * <p>
     * The method iterates over all provided patterns. Each pattern is tested for both
     * contextual and physical validity. Only fully applicable patterns are considered.
     * The pattern with the highest number of required context categories is selected
     * as the most specific one.
     * </p>
     *
     * @param patterns the list of maneuver patterns to evaluate
     * @return the best-fitting pattern, or {@code null} if none are valid
     * @throws ParameterException if a perception or parameter lookup fails during context or ability checks
     */
    @Override
    public ManeuverPattern select(final ArrayList<ManeuverPattern> patterns)
            throws ParameterException {

        ManeuverPattern best = null;
        int maxKeys = -1;

        for (ManeuverPattern p : patterns) {
            if (p.checkContext() && p.checkAbility()) {
                int specificity = p.getRequiredContextKeys().size();
                if (specificity > maxKeys) {
                    best = p;
                    maxKeys = specificity;
                } else if (specificity == maxKeys) {
                    best = resolveTie(best, p);
                }
            }
        }

        return best;
    }

    /**
     * Resolves a tie between two equally specific {@link ManeuverPattern}s.
     * <p>
     * Tie-breaking is based on a predefined context-type priority hierarchy.
     * Context categories that appear later in the list are considered more
     * specific (and therefore higher priority) than those appearing earlier.
     * </p>
     *
     * @param a the first maneuver pattern
     * @param b the second maneuver pattern
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
     * a pattern's required context keys and a given context-type priority list.
     * <p>
     * Contexts appearing later in the list are given higher weight to
     * reflect greater situational specificity.
     * </p>
     *
     * @param keys     the set of context keys used by a pattern
     * @param priority the global ordered list of context-type priorities
     * @return the accumulated priority score
     */
    private int priorityScore(final Set<String> keys, final List<String> priority) {
        int score = 0;
        for (String k : keys) {
            int idx = priority.indexOf(k);
            if (idx >= 0) {
                score += (priority.size() - idx);
            }
        }
        return score;
    }
}