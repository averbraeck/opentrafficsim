package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;

/**
 * KnowledgeChunk designed to detect congestion and suppress discretionary lane changes.
 * <p>
 * This component forms part of <b>Layer 2 (Cognition / Motivation)</b> in the MiRoVA architecture.
 * It addresses the instability (often referred to as the "ping-pong effect") observed in
 * microscopic simulations during stop-and-go waves. When the vehicle speed drops below a specific
 * threshold, this chunk generates negative desires (inhibition) for both left and right lane changes.
 * This effectively increases the "activation energy" required for a lane change, preventing the
 * agent from reacting to minor, transient gaps in slow-moving traffic.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class CongestionChunk extends KnowledgeChunk
{

    /** * Parameter for the speed threshold below which traffic is considered 'congested'.
     * Uses the standard OTS parameter {@code ParameterTypes.VCONG}.
     */
    public static final ParameterTypeSpeed V_CONGESTION_THRESHOLD = ParameterTypes.VCONG;

    /**
     * Constructs a new CongestionChunk.
     * * @param vehicle the tactical planner governing the ego agent
     * @throws OperationalPlanException if the planner cannot be initialized
     */
    public CongestionChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException
    {
        super(vehicle);
    }

    /**
     * Determines if the congestion logic is applicable in the current context.
     * <p>
     * Applicability is defined by the ego vehicle's speed. If the speed falls below
     * the configured {@code V_CONGESTION_THRESHOLD}, the traffic is deemed congested,
     * and this chunk becomes active.
     * </p>
     * * @return {@code true} if the ego speed is below the congestion threshold, {@code false} otherwise
     * @throws ParameterException if the VCONG parameter is not defined or accessible
     */
    @Override
    public boolean isApplicable() throws ParameterException
    {
        EgoContext ego = getMirovaTacticalPlanner().getContextManager().getCategory("Ego", EgoContext.class);
        Speed congestionThreshold = getMirovaTacticalPlanner().getParameters().getParameter(V_CONGESTION_THRESHOLD);
        Speed egoSpeed = ego.getEgoSpeed();

        // Activation condition: Ego speed is slower than the definition of congestion.
        return egoSpeed.lt(congestionThreshold);
    }

    /**
     * Computes the inhibitory desire to suppress lane changes.
     * <p>
     * This method applies a negative desire equivalent to the negative of the "Free" threshold
     * ({@code dFree}) defined in the tactical planner. This neutralizes the base incentive to change
     * lanes for minor speed gains, effectively freezing the lane choice unless a mandatory
     * incentive (e.g., route) overrides it.
     * </p>
     * * @return an object containing negative desire values for both left and right directions
     * @throws ParameterException if required parameters for the calculation are missing
     */
    @Override
    public Desire computeDesire() throws ParameterException
    {
        // Retrieve the generic "free" threshold (the cost of changing lanes) and negate it.
        // This acts as a penalty/suppression for discretionary changes.
        double dLeft = -getMirovaTacticalPlanner().getDFree();
        double dRight = -getMirovaTacticalPlanner().getDFree();

        // Create a new Desire object.
        // The boolean flag 'false' indicates this is NOT a mandatory desire (it's discretionary).
        this.desire = new Desire(dLeft, dRight, false);

        return this.desire;
    }
}