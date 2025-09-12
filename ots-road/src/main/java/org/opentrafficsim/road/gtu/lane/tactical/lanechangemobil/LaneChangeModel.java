package org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;

/**
 * All lane change models must implement this interface. <br>
 * TODO: Lane change models should use the perceived nearby GTUs rather than a provided list of same lane traffic, etc.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public interface LaneChangeModel
{
    /**
     * Compute the acceleration and lane change.
     * @param gtu the GTU for which the acceleration and lane change is computed
     * @param sameLaneTraffic the set of observable GTUs in the current lane (can not be null and may include the
     *            <code>gtu</code>)
     * @param rightLaneTraffic the set of observable GTUs in the adjacent lane where GTUs should drive in the absence of other
     *            traffic (must be null if there is no such lane)
     * @param leftLaneTraffic the set of observable GTUs in the adjacent lane into which GTUs should merge to overtake other
     *            traffic (must be null if there is no such lane)
     * @param speedLimit the local speed limit
     * @param preferredLaneRouteIncentive route incentive to merge to the adjacent lane where GTUs should drive in the absence
     *            of other traffic
     * @param laneChangeThreshold threshold that prevents lane changes that have very little benefit
     * @param nonPreferredLaneRouteIncentive route incentive to merge to the adjacent lane into which GTUs should merge to
     *            overtake other traffic
     * @return the result of the lane change and GTU following model
     * @throws GtuException when the speed of the GTU can not be determined
     * @throws ParameterException in case of a parameter problem.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    LaneMovementStep computeLaneChangeAndAcceleration(LaneBasedGtu gtu, Collection<PerceivedObject> sameLaneTraffic,
            Collection<PerceivedObject> rightLaneTraffic, Collection<PerceivedObject> leftLaneTraffic, Speed speedLimit,
            Acceleration preferredLaneRouteIncentive, Acceleration laneChangeThreshold,
            Acceleration nonPreferredLaneRouteIncentive) throws GtuException, ParameterException;

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
