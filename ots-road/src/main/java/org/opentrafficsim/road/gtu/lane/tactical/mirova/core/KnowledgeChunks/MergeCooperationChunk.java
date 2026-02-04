package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel.MergeCooperationPattern;

/**
 * KnowledgeChunk designed to handle cooperative merging scenarios.
 * <p>
 * This chunk is responsible for identifying situations where cooperation with merging vehicles
 * is beneficial or necessary. It registers the {@link MergeCooperationPattern} which executes
 * the cooperative behavior (e.g., opening a gap).
 * </p>
 * <p>
 * Currently, this chunk does not contribute to the global desire vector (returns neutral desire),
 * as the cooperative behavior is primarily triggered reactively via the pattern's context check,
 * rather than through a proactive lane change desire.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MergeCooperationChunk extends KnowledgeChunk {

    /**
     * Constructor.
     * @param vehicle the tactical planner
     * @throws OperationalPlanException if pattern instantiation fails
     */
    public MergeCooperationChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException {
        super(vehicle);
        // Register the specific maneuver pattern for this chunk
        //this.addManeuverPattern(() -> new MergeCooperationPattern(this));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This chunk is always applicable in principle, as merge situations can occur dynamically.
     * Specific applicability is determined by the pattern's checkContext() method.
     * </p>
     */
    @Override
    public boolean isApplicable() throws ParameterException {
        return true;
    }

    /**
     * Computes the desire to change lanes based on cooperation needs.
     * <p>
     * Currently implemented to return a neutral desire (0.0 for both directions).
     * Future implementations could increase lane change desire to the left to proactively
     * vacate the lane for merging traffic (Courtesy Lane Change).
     * </p>
     *
     * @return Desire(0, 0) - Neutral desire.
     * @throws ParameterException if parameters are missing
     */
    @Override
    public Desire computeDesire() throws ParameterException {
        // Currently, we do not influence the desire vector.
        // The cooperation is handled procedurally by the PatternSelector invoking the MergeCooperationPattern
        // based on the pattern's specific context checks (infrastructure & neighbors).
        return new Desire(0.0, 0.0, false);
    }
}