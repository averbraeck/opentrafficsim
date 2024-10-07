package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * All directed lane change models must implement this interface. A directed lane change model is a lane change model where the
 * choice for a lateral direction has already been made.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public interface DirectedLaneChangeModel
{
    /**
     * Compute the acceleration and lane change.
     * @param gtu the GTU for which the acceleration and lane change is computed
     * @param direction the direction of the lane we want to change to
     * @param sameLaneTraffic the set of information about observable GTUs in the current lane (can not be null and may include
     *            the <code>gtu</code>)
     * @param otherLaneTraffic the set of information about observable GTUs in the adjacent lane where GTUs should drive in the
     *            absence of other traffic (must be null if there is no such lane)
     * @param maxDistance the maximum distance that the current GTU can drive, e.g. due to a lane drop
     * @param speedLimit the local speed limit
     * @param otherLaneRouteIncentive route incentive to merge to the adjacent lane where GTUs should drive in the absence of
     *            other traffic
     * @param laneChangeThreshold threshold that prevents lane changes that have very little benefit merge to overtake other
     *            traffic
     * @param laneChangeTime time spent to overtake
     * @return the result of the lane change and GTU following model
     * @throws GtuException when the position of the GTU on the lane(s) cannot be determined
     * @throws ParameterException in case of a parameter problem.
     * @throws OperationalPlanException if DefaultAlexander perception category is not present
     */
    @SuppressWarnings("checkstyle:parameternumber")
    DirectedLaneMovementStep computeLaneChangeAndAcceleration(LaneBasedGtu gtu, LateralDirectionality direction,
            Collection<Headway> sameLaneTraffic, Collection<Headway> otherLaneTraffic, Length maxDistance, Speed speedLimit,
            Acceleration otherLaneRouteIncentive, Acceleration laneChangeThreshold, Duration laneChangeTime)
            throws GtuException, ParameterException, OperationalPlanException;

    /** @return the perception. */
    LanePerception getPerception();

    /**
     * Return the name of this GTU following model.
     * @return just the name of the GTU following model
     */
    String getName();

    /**
     * Return complete textual information about this instantiation of this GTU following model.
     * @return the name and parameter values of the GTU following model
     */
    String getLongName();

}
