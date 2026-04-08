package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util;

import java.util.ArrayList;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;

/**
 * Defines a general interface for selecting the most appropriate {@link ManeuverPattern}
 * among a set of candidates.
 * <p>
 * This interface represents the decision-making engine of <b>Layer 3 (Decision / Strategy)</b>
 * in the MiRoVA architecture. Implementations encapsulate different selection strategies
 * (e.g., deterministic, probabilistic, or multi-criteria evaluation) and can be
 * swapped at runtime via the {@link PatternSelector} without modifying the core vehicle logic.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public interface PatternSelectionStrategy {

    /**
     * Evaluates a list of available maneuver patterns and selects the most appropriate one
     * for execution based on the underlying strategy.
     *
     * @param patterns the list of maneuver patterns to evaluate
     * @return the best-fitting pattern, or {@code null} if none are valid or selected
     * @throws ParameterException if a parameter lookup or context evaluation fails during selection
     */
    ManeuverPattern select(ArrayList<ManeuverPattern> patterns) throws ParameterException;
}