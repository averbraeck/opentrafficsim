package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;

import java.util.Collection;

/**
 * Defines a general interface for selecting the most appropriate {@link ManeuverPattern}
 * among a set of candidates for a given {@link ManeuverPattern.PatternType}.
 *
 * <p>Implementations of this interface encapsulate different selection strategies
 * (e.g., deterministic, probabilistic, or multi-criteria evaluation) and can be
 * swapped at runtime without modifying the vehicle logic.</p>
 *
 * <p>Used by {@link PatternSelector} as an interchangeable strategy module.</p>
 */
public interface PatternSelectionStrategy {

    /**
     * Selects the most appropriate {@link ManeuverPattern} for the given pattern type.
     *
     * @param chunks collection of {@link KnowledgeChunk}s providing maneuver patterns
     * @param type pattern type to filter and evaluate (e.g., COOPERATIVE)
     * @return the best-fitting pattern, or {@code null} if none are valid
     * @throws ParameterException if a parameter lookup or context evaluation fails
     */
    ManeuverPattern select(Collection<KnowledgeChunk> chunks, ManeuverPattern.PatternType type)
            throws ParameterException;
}
