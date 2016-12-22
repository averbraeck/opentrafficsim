package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * All directed lane change models must implement this interface. A directed lane change model is a lane change model where the
 * choice for a lateral direction has already been made.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 3 nov. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface DirectedLaneChangeModel
{
    /**
     * Compute the acceleration and lane change.
     * @param gtu GTU; the GTU for which the acceleration and lane change is computed
     * @param direction the direction of the lane we want to change to
     * @param sameLaneTraffic Collection&lt;HeadwayGTU&gt;; the set of information about observable GTUs in the current lane
     *            (can not be null and may include the <code>gtu</code>)
     * @param otherLaneTraffic Collection&lt;HeadwayGTU&gt;; the set of information about observable GTUs in the adjacent lane
     *            where GTUs should drive in the absence of other traffic (must be null if there is no such lane)
     * @param maxDistance the maximum distance that the current GTU can drive, e.g. due to a lane drop
     * @param speedLimit Speed; the local speed limit
     * @param otherLaneRouteIncentive Acceleration; route incentive to merge to the adjacent lane where GTUs should drive in the
     *            absence of other traffic
     * @param laneChangeThreshold Acceleration; threshold that prevents lane changes that have very little benefit merge to
     *            overtake other traffic
     * @param laneChangeTime time spent to overtake
     * @return LaneMovementStep; the result of the lane change and GTU following model
     * @throws GTUException when the position of the GTU on the lane(s) cannot be determined
     * @throws ParameterException in case of a parameter problem.
     * @throws OperationalPlanException if DefaultAlexander perception category is not present
     */
    @SuppressWarnings("checkstyle:parameternumber")
    DirectedLaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu, final LateralDirectionality direction,
        final Collection<Headway> sameLaneTraffic, final Collection<Headway> otherLaneTraffic, final Length maxDistance,
        final Speed speedLimit, final Acceleration otherLaneRouteIncentive, final Acceleration laneChangeThreshold,
        Duration laneChangeTime) throws GTUException, ParameterException, OperationalPlanException;

    /** @return the perception. */
    LanePerception getPerception();

    /**
     * Return the name of this GTU following model.
     * @return String; just the name of the GTU following model
     */
    String getName();

    /**
     * Return complete textual information about this instantiation of this GTU following model.
     * @return String; the name and parameter values of the GTU following model
     */
    String getLongName();

}
