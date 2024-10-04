package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.LaneChangeInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface InfrastructurePerception extends LaneBasedPerceptionCategory
{

    /**
     * Returns infrastructure lane change info of a lane. A set is returned as multiple points may force lane changes. Which
     * point is considered most critical is a matter of driver interpretation and may change over time. This is shown below.
     * Suppose vehicle A needs to take the off-ramp, and that behavior is that the minimum distance per required lane change
     * determines how critical it is. First, 400m before the lane-drop, the off-ramp is critical. 300m downstream, the lane-drop
     * is critical. Info is sorted by distance, closest first.
     * 
     * <pre>
     * _______
     * _ _A_ _\_________
     * _ _ _ _ _ _ _ _ _
     * _________ _ _ ___
     *          \_______
     *     (-)        Lane-drop: 1 lane change  in 400m (400m per lane change)
     *     (--------) Off-ramp:  3 lane changes in 900m (300m per lane change, critical)
     *     
     *     (-)        Lane-drop: 1 lane change  in 100m (100m per lane change, critical)
     *     (--------) Off-ramp:  3 lane changes in 600m (200m per lane change)
     * </pre>
     * 
     * @param lane relative lateral lane
     * @return infrastructure lane change info of a lane
     */
    SortedSet<LaneChangeInfo> getLegalLaneChangeInfo(RelativeLane lane);

    /**
     * Returns infrastructure lane change info of a lane. A set is returned as multiple points may force lane changes. Which
     * point is considered most critical is a matter of driver interpretation and may change over time. This is shown below.
     * Suppose vehicle A needs to take the off-ramp, and that behavior is that the minimum distance per required lane change
     * determines how critical it is. First, 400m before the lane-drop, the off-ramp is critical. 300m downstream, the lane-drop
     * is critical. Info is sorted by distance, closest first.
     * 
     * <pre>
     * _______
     * _ _A_ _\_________
     * _ _ _ _ _ _ _ _ _
     * _________ _ _ ___
     *          \_______
     *     (-)        Lane-drop: 1 lane change  in 400m (400m per lane change)
     *     (--------) Off-ramp:  3 lane changes in 900m (300m per lane change, critical)
     *     
     *     (-)        Lane-drop: 1 lane change  in 100m (100m per lane change, critical)
     *     (--------) Off-ramp:  3 lane changes in 600m (200m per lane change)
     * </pre>
     * 
     * @param lane relative lateral lane
     * @return infrastructure lane change info of a lane
     */
    SortedSet<LaneChangeInfo> getPhysicalLaneChangeInfo(RelativeLane lane);

    /**
     * Returns the prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane relative lateral lane
     * @return prospect for speed limits on a lane
     */
    SpeedLimitProspect getSpeedLimitProspect(RelativeLane lane);

    /**
     * Returns the distance over which a lane change remains legally possible. Negative values indicate the distance over which
     * a lane change is legally not possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    Length getLegalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns the distance over which a lane change remains physically possible. Negative values indicate the distance over
     * which a lane change is physically not possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    Length getPhysicalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns a set of relative lanes representing the cross section. Lanes are sorted left to right.
     * @return set of relative lanes representing the cross section
     */
    SortedSet<RelativeLane> getCrossSection();

}
