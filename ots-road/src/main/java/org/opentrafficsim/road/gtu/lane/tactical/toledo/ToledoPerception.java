package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedPerceptionCategory;
import org.opentrafficsim.road.network.LaneChangeInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Wrapper class around {@code InfrastructureCategory} that forwards all methods except for infrastructure lane change info.
 * These methods determine and return infrastructure information of type {@code InfrastructureLaneChangeInfoToledo}, which
 * includes split number.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// TODO updateInfrastructureLaneChangeInfo with split number
public class ToledoPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements LaneBasedPerceptionCategory
{

    /** */
    private static final long serialVersionUID = 20160000L;

    /** Infrastructure lane change info per relative lane. */
    private Map<RelativeLane, TimeStampedObject<SortedSet<LaneChangeInfo>>> infrastructureLaneChangeInfo;

    /** Wrapped regular infrastructureCategory. */
    private final DirectInfrastructurePerception infrastructureCategory;

    /**
     * @param perception perception
     */
    public ToledoPerception(final LanePerception perception)
    {
        super(perception);
        this.infrastructureCategory = new DirectInfrastructurePerception(perception);
    }

    /**
     * Updates the infrastructural lane change info.
     * @param lane relative lateral lane
     * @throws GtuException when GTU is not initialized
     */
    public void updateInfrastructureLaneChangeInfo(final RelativeLane lane) throws GtuException
    {
        //
    }

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
    public final SortedSet<LaneChangeInfo> getInfrastructureLaneChangeInfo(final RelativeLane lane)
    {
        return new TreeSet<>();
        // return this.infrastructureLaneChangeInfo.get(lane).getObject();
    }

    /**
     * Returns time stamped infrastructure lane change info of a lane. A set is returned as multiple points may force lane
     * changes. Which point is considered most critical is a matter of driver interpretation and may change over time. This is
     * shown below. Suppose vehicle A needs to take the off-ramp, and that behavior is that the minimum distance per required
     * lane change determines how critical it is. First, 400m before the lane-drop, the off-ramp is critical. 300m downstream,
     * the lane-drop is critical. Info is sorted by distance, closest first.
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
     * @return time stamped infrastructure lane change info of a lane
     */
    public final TimeStampedObject<SortedSet<LaneChangeInfo>> getTimeStampedInfrastructureLaneChangeInfo(
            final RelativeLane lane)
    {
        return this.infrastructureLaneChangeInfo.get(lane);
    }

    /**
     * Returns the prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane relative lateral lane
     * @return prospect for speed limits on a lane
     */
    public final SpeedLimitProspect getSpeedLimitProspect(final RelativeLane lane)
    {
        return this.infrastructureCategory.getSpeedLimitProspect(lane);
    }

    /**
     * Returns the distance over which a lane change remains legally possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    public final Length getLegalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return this.infrastructureCategory.getLegalLaneChangePossibility(fromLane, lat);
    }

    /**
     * Returns the distance over which a lane change remains physically possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    public final Length getPhysicalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return this.infrastructureCategory.getPhysicalLaneChangePossibility(fromLane, lat);
    }

    /**
     * Returns a set of relative lanes representing the cross section. Lanes are sorted left to right.
     * @return set of relative lanes representing the cross section
     */
    public final SortedSet<RelativeLane> getCrossSection()
    {
        return this.infrastructureCategory.getCrossSection();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ToledoPerceptionCategory";
    }

}
