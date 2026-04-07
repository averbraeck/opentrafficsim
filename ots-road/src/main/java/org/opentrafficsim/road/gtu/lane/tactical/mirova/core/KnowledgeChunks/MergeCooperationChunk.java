package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;

/**
 * KnowledgeChunk designed to handle cooperative merging scenarios.
 * <p>
 * This component forms part of <b>Layer 2 (Cognition / Motivation)</b> in the MiRoVA architecture.
 * It is responsible for identifying situations where cooperation with merging vehicles
 * is beneficial or necessary.
 * </p>
 * <p>
 * Currently, this chunk does not contribute to the global desire vector (returns neutral desire),
 * as the cooperative behavior (e.g., opening a gap) is primarily triggered reactively via a
 * procedural parallel maneuver pattern in Layer 4, rather than through a proactive lane change desire.
 * Future implementations could increase lane change desire to the left to proactively
 * vacate the lane for merging traffic (Courtesy Lane Change).
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MergeCooperationChunk extends KnowledgeChunk {

    /**
     * Constructs a new MergeCooperationChunk.
     *
     * @param vehicle the tactical planner governing the ego agent
     * @throws OperationalPlanException if chunk instantiation fails
     */
    public MergeCooperationChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException {
        super(vehicle);
    }

    /**
     * Determines if the merge cooperation logic is applicable.
     * <p>
     * This chunk is always applicable in principle, as merge situations can occur dynamically.
     * </p>
     *
     * @return {@code true} as cooperation checks must always run
     * @throws ParameterException if parameter evaluation fails
     */
    @Override
    public boolean isApplicable() throws ParameterException {
        return true;
    }

    /**
     * Computes the desire to change lanes based on cooperation needs.
     * <p>
     * Currently implemented to return a neutral desire (0.0 for both directions).
     * Future implementations will handle the Courtesy Lane Change here.
     * </p>
     *
     * @return a neutral {@link Desire} object (0.0 for both directions)
     * @throws ParameterException if required parameters are missing
     */
    @Override
    public Desire computeDesire() throws ParameterException {
        // Currently, we do not influence the desire vector.
        // The cooperation is handled procedurally by the PatternSelector invoking
        // the MergeCooperationPattern in Layer 4 based on specific context checks.
        this.desire = new Desire(0.0, 0.0, false);
        return this.desire;
    }
}