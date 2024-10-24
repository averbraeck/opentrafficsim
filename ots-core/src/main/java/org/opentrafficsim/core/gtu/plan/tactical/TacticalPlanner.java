package org.opentrafficsim.core.gtu.plan.tactical;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeClass;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Tactical planners generate operational plans that are in line with reaching the goals of the strategical plan. The modeler is
 * totally free how the tactical planners are generated. Usually there is one tactical planner at work, but it is of course
 * possible to have multiple parallel planners that assess the situation and an overarching tactical planner that acts as a
 * tie-breaker. Alternatively, a single tactical planner is activated depending on the situation. Suppose we drive on a
 * multi-lane stretch of road. The tactical planner can use the headway and lane change algorithms to generate the right
 * operational plans (movements). When the GTU nears a crossing, another tactical planner can be activated that takes care of
 * going left, straight or right (based on a consultation of the strategical plan) and for providing a safe passage of the
 * crossing. After the crossing, the standard headway and lane-change tactical planner take over.<br>
 * The operational plans that the tactical planners generate can have a very different duration. In case of a standard headway
 * and lane change algorithm with a time step of 0.5 seconds, each operational plan can take 0.5 seconds. Alternatively,
 * operational plans can have a very different duration, depending on the situation. When changing lanes, the complete movement
 * to the other lane can be one operational plan. When it is not busy, and the GTU can move for 5 seconds without problems, the
 * operational plan can take 5 seconds. Based on external stimuli (for which the Perception unit of the GTU is responsible),
 * operational plans can always be interrupted.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @param <G> GTU type
 * @param <P> perception type
 */
public interface TacticalPlanner<G extends Gtu, P extends Perception<G>>
{

    /** Parameter type for tactical planner. */
    @SuppressWarnings("rawtypes")
    ParameterTypeClass<TacticalPlanner> TACTICAL_PLANNER =
            new ParameterTypeClass<>("tac.plan.", "Tactical planner", TacticalPlanner.class);

    /**
     * Returns the GTU.
     * @return GTU
     */
    G getGtu();

    /**
     * generate an operational plan, for now or for in the future.
     * @param startTime the time from which the new operational plan has to be operational
     * @param locationAtStartTime the location of the GTU at the start time of the new plan
     * @return a new operational plan
     * @throws GtuException when there is a problem with the state of the GTU when planning a path
     * @throws NetworkException when there is a problem with the network on which the GTU is driving
     * @throws ParameterException when there is a problem with a parameter
     */
    // @docs/06-behavior/tactical-planner.md
    OperationalPlan generateOperationalPlan(Time startTime, OrientedPoint2d locationAtStartTime)
            throws GtuException, NetworkException, ParameterException;

    /** @return the perception unit belonging to this tactical planner. */
    P getPerception();

}
