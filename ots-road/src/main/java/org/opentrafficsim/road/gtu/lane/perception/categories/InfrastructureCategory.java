package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Map;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class InfrastructureCategory extends AbstractPerceptionCategory
{

    /** Infrastructure lane change info per relative lane. */
    private Map<RelativeLane, TimeStampedObject<SortedSet<InfrastructureLaneChangeInfo>>> infrastructureLaneChangeInfo;
    
    /** Speed limit prospect per relative lane. */
    private Map<RelativeLane, TimeStampedObject<SpeedLimitProspect>> speedLimitProspect;
    
    /** Legal Lane change possibilities per relative lane and lateral direction. */
    private Map<RelativeLane, Map<LateralDirectionality, TimeStampedObject<Length>>> legalLaneChangePossibility;
    
    /** Physical Lane change possibilities per relative lane and lateral direction. */
    private Map<RelativeLane, Map<LateralDirectionality, TimeStampedObject<Length>>> physicalLaneChangePossibility;
    
    /** Cross-section. */
    private TimeStampedObject<SortedSet<RelativeLane>> crossSection;
    
    /**
     * @param perception perception
     */
    public InfrastructureCategory(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll()
    {
        //
    }

    /**
     * Updates the infrastructural lane change info.
     * @param lane relative lateral lane
     */
    public final void updateInfrastructureLaneChangeInfo(final RelativeLane lane)
    {
        //
    }

    /**
     * Updates the speed limit prospect.
     * @param lane relative lateral lane
     */
    public final void updateSpeedLimitProspect(final RelativeLane lane)
    {
        //
    }

    /**
     * Updates the distance over which lane changes remains legally possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     */
    public final void updateLegalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        //
    }

    /**
     * Updates the distance over which lane changes remains physically possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     */
    public final void updatePhysicalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        //
    }

    /**
     * Updates a set of relative lanes representing the cross section.
     */
    public final void updateCrossSection()
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
     * @param lane relative lateral lane
     * @return infrastructure lane change info of a lane
     */
    public final SortedSet<InfrastructureLaneChangeInfo> getInfrastructureLaneChangeInfo(final RelativeLane lane)
    {
        return this.infrastructureLaneChangeInfo.get(lane).getObject();
    }

    /**
     * Returns the prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane relative lateral lane
     * @return prospect for speed limits on a lane
     */
    public final SpeedLimitProspect getSpeedLimitProspect(final RelativeLane lane)
    {
        return this.speedLimitProspect.get(lane).getObject();
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
        return this.legalLaneChangePossibility.get(fromLane).get(lat).getObject();
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
        return this.physicalLaneChangePossibility.get(fromLane).get(lat).getObject();
    }

    /**
     * Returns a set of relative lanes representing the cross section. Lanes are sorted left to right.
     * @return set of relative lanes representing the cross section
     */
    public final SortedSet<RelativeLane> getCrossSection()
    {
        return this.crossSection.getObject();
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
     * @param lane relative lateral lane
     * @return time stamped infrastructure lane change info of a lane
     */
    public final TimeStampedObject<SortedSet<InfrastructureLaneChangeInfo>> getTimeStampedInfrastructureLaneChangeInfo(
        final RelativeLane lane)
    {
        return this.infrastructureLaneChangeInfo.get(lane);
    }

    /**
     * Returns the time stamped prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane relative lateral lane
     * @return time stamped prospect for speed limits on a lane
     */
    public final TimeStampedObject<SpeedLimitProspect> getTimeStampedSpeedLimitProspect(final RelativeLane lane)
    {
        return this.speedLimitProspect.get(lane);
    }

    /**
     * Returns the time stamped distance over which a lane change remains legally possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return time stamped distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    public final TimeStampedObject<Length> getTimeStampedLegalLaneChangePossibility(final RelativeLane fromLane,
        final LateralDirectionality lat)
    {
        return this.legalLaneChangePossibility.get(fromLane).get(lat);
    }

    /**
     * Returns the time stamped distance over which a lane change remains physically possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @return time stamped distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    public final TimeStampedObject<Length> getTimeStampedPhysicalLaneChangePossibility(final RelativeLane fromLane,
        final LateralDirectionality lat)
    {
        return this.physicalLaneChangePossibility.get(fromLane).get(lat);
    }

    /**
     * Returns a time stamped set of relative lanes representing the cross section. Lanes are sorted left to right.
     * @return time stamped set of relative lanes representing the cross section
     */
    public final TimeStampedObject<SortedSet<RelativeLane>> getTimeStampedCrossSection()
    {
        return this.crossSection;
    }

}
