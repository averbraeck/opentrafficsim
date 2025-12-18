package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;

import java.util.Collection;

/**
 * Central facade for selecting {@link ManeuverPattern}s.
 *
 * <p>This class delegates the actual selection logic to a configurable
 * {@link PatternSelectionStrategy}. The strategy can be swapped dynamically,
 * allowing different selection mechanisms (e.g., deterministic, probabilistic,
 * learning-based) to be plugged in at runtime.</p>
 *
 * <p>By default, the {@link DeterministicSpecificityStrategy} is used.</p>
 */
public final class PatternSelector {

    /** Active selection strategy instance. */
    private static PatternSelectionStrategy strategy = new DeterministicSpecificityStrategy();

    /** keep constructor private to prevent instantiation.     */
    private PatternSelector() {}

    /**
     * Selects the most suitable pattern of a given type using the current strategy.
     *
     * @param chunks the vehicle's knowledge chunks providing maneuver patterns
     * @param type the pattern type to evaluate
     * @return the best-fitting pattern, or {@code null} if none match
     * @throws ParameterException if context or parameter lookup fails
     */
    public static ManeuverPattern select(final Collection<KnowledgeChunk> chunks, final ManeuverPattern.PatternType type)
            throws ParameterException {
        return strategy.select(chunks, type);
    }

    /**
     * Replaces the active selection strategy.
     *
     * <p>This allows injecting custom strategies at runtime, e.g.:
     * <pre>{@code
     * PatternSelector.setStrategy(new ProbabilisticPatternSelectionStrategy());
     * }</pre></p>
     *
     * @param newStrategy the new {@link PatternSelectionStrategy} to use
     */
    public static void setStrategy(final PatternSelectionStrategy newStrategy) {
        if (newStrategy == null)
            throw new IllegalArgumentException("PatternSelectionStrategy cannot be null.");
        strategy = newStrategy;
    }

    /** Returns the currently active selection strategy.
     * @return */
    public static PatternSelectionStrategy getStrategy() {
        return strategy;
    }
}
