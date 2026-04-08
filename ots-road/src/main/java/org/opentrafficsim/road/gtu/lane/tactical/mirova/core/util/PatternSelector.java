package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util;

import java.util.ArrayList;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;

/**
 * Central facade for selecting {@link ManeuverPattern}s.
 * <p>
 * Forms the core of <b>Layer 3 (Decision / Strategy)</b> in the MiRoVA architecture.
 * It acts as the arbitrator that evaluates active and physically feasible maneuver patterns
 * from Layer 4 and selects the most appropriate one to execute based on the current strategy.
 * </p>
 * <p>
 * This class delegates the actual selection logic to a configurable
 * {@link PatternSelectionStrategy}. The strategy can be swapped dynamically,
 * allowing different selection mechanisms (e.g., deterministic, probabilistic,
 * learning-based) to be plugged in at runtime. By default, the
 * {@link DeterministicSpecificityStrategy} is used.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public final class PatternSelector {

    /** Active selection strategy instance. */
    private static PatternSelectionStrategy strategy = new DeterministicSpecificityStrategy();

    /** * Private constructor to prevent instantiation of this utility class.
     */
    private PatternSelector() {
        // Utility class
    }

    /**
     * Selects the most suitable pattern from a given list using the current strategy.
     *
     * @param patterns the list of available maneuver patterns to evaluate
     * @return the best-fitting pattern, or {@code null} if none match
     * @throws ParameterException if context or parameter lookup fails during selection
     */
    public static ManeuverPattern select(final ArrayList<ManeuverPattern> patterns)
            throws ParameterException {
        return strategy.select(patterns);
    }

    /**
     * Filters a list of patterns to those that are currently running or contextually
     * relevant and physically feasible.
     *
     * @param listPatterns the list of all potential maneuver patterns to evaluate
     * @return a filtered list of patterns that pass both context and ability checks, or are already running
     * @throws ParameterException if a perception or parameter lookup fails during checks
     */
    public static ArrayList<ManeuverPattern> getAllRelevantPatterns(final ArrayList<ManeuverPattern> listPatterns)
            throws ParameterException {
        ArrayList<ManeuverPattern> listRelevantPatterns = new ArrayList<>();

        for (ManeuverPattern p : listPatterns) {
            if (p.isRunning()) {
                listRelevantPatterns.add(p);
            }
            else if (p.checkContext() && p.checkAbility()) {
                listRelevantPatterns.add(p);
            }
        }
        return listRelevantPatterns;
    }

    /**
     * Replaces the active selection strategy.
     * <p>
     * This allows injecting custom strategies at runtime, e.g.:
     * <pre>{@code
     * PatternSelector.setStrategy(new ProbabilisticPatternSelectionStrategy());
     * }</pre>
     * </p>
     *
     * @param newStrategy the new {@link PatternSelectionStrategy} to use
     * @throws IllegalArgumentException if the provided strategy is null
     */
    public static void setStrategy(final PatternSelectionStrategy newStrategy) {
        if (newStrategy == null) {
            throw new IllegalArgumentException("PatternSelectionStrategy cannot be null.");
        }
        strategy = newStrategy;
    }

    /**
     * Returns the currently active selection strategy.
     * * @return the active {@link PatternSelectionStrategy} instance
     */
    public static PatternSelectionStrategy getStrategy() {
        return strategy;
    }
}