package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Perceives information concerning the infrastructure, including slits, lanes, speed limits and road markings.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO update methods produce dummy "stay in infinite lane" data
public class InfrastructureCategory extends LaneBasedAbstractPerceptionCategory
{

    /** Infrastructure lane change info per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SortedSet<InfrastructureLaneChangeInfo>>> infrastructureLaneChangeInfo =
        new HashMap<>();

    /** Speed limit prospect per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SpeedLimitProspect>> speedLimitProspect = new HashMap<>();

    /** Legal Lane change possibilities per relative lane and lateral direction. */
    private final Map<RelativeLane, Map<LateralDirectionality, TimeStampedObject<Length>>> legalLaneChangePossibility =
        new HashMap<>();

    /** Physical Lane change possibilities per relative lane and lateral direction. */
    private final Map<RelativeLane, Map<LateralDirectionality, TimeStampedObject<Length>>> physicalLaneChangePossibility =
        new HashMap<>();

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
    public final void updateAll() throws GTUException
    {
        updateCrossSection();
        for (RelativeLane lane : this.crossSection.getObject())
        {
            updateInfrastructureLaneChangeInfo(lane);
            updateSpeedLimitProspect(lane);
            updateLegalLaneChangePossibility(lane, LateralDirectionality.LEFT);
            updateLegalLaneChangePossibility(lane, LateralDirectionality.RIGHT);
            updatePhysicalLaneChangePossibility(lane, LateralDirectionality.LEFT);
            updatePhysicalLaneChangePossibility(lane, LateralDirectionality.RIGHT);
        }
    }

    /**
     * Updates the infrastructural lane change info.
     * @param lane relative lateral lane
     * @throws GTUException if the GTU was not initialized
     */
    public final void updateInfrastructureLaneChangeInfo(final RelativeLane lane) throws GTUException
    {
        updateCrossSection();
        this.infrastructureLaneChangeInfo.put(RelativeLane.CURRENT, new TimeStampedObject<>(new TreeSet<>(),
            getTimestamp()));
    }

    /**
     * Updates the speed limit prospect.
     * @param lane relative lateral lane
     * @throws GTUException if the GTU was not initialized
     */
    public final void updateSpeedLimitProspect(final RelativeLane lane) throws GTUException
    {
        updateCrossSection();
        SpeedLimitProspect slp = new SpeedLimitProspect();
        slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.MAX_VEHICLE_SPEED, getGtu().getMaximumSpeed());
        try
        {
            slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.FIXED_SIGN, getGtu().getLanes().keySet().iterator().next()
                .getSpeedLimit(getGtu().getGTUType()));
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not obtain speed limit from lane for perception.", exception);
        }
        this.speedLimitProspect.put(RelativeLane.CURRENT, new TimeStampedObject<>(slp, getTimestamp()));
    }

    /**
     * Updates the distance over which lane changes remains legally possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @throws GTUException if the GTU was not initialized
     */
    public final void updateLegalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat) throws GTUException
    {
        updateCrossSection();
        Map<LateralDirectionality, TimeStampedObject<Length>> map = new HashMap<>();
        map.put(LateralDirectionality.LEFT, new TimeStampedObject<>(Length.ZERO, getTimestamp()));
        map.put(LateralDirectionality.RIGHT, new TimeStampedObject<>(Length.ZERO, getTimestamp()));
        this.legalLaneChangePossibility.put(RelativeLane.CURRENT, map);
    }

    /**
     * Updates the distance over which lane changes remains physically possible.
     * @param fromLane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @throws GTUException if the GTU was not initialized
     */
    public final void updatePhysicalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat) throws GTUException
    {
        updateCrossSection();
        Map<LateralDirectionality, TimeStampedObject<Length>> map = new HashMap<>();
        map.put(LateralDirectionality.LEFT, new TimeStampedObject<>(Length.ZERO, getTimestamp()));
        map.put(LateralDirectionality.RIGHT, new TimeStampedObject<>(Length.ZERO, getTimestamp()));
        this.physicalLaneChangePossibility.put(RelativeLane.CURRENT, map);
    }

    /**
     * Updates a set of relative lanes representing the cross section.
     * @throws GTUException if the GTU was not initialized
     */
    public final void updateCrossSection() throws GTUException
    {
        if (this.crossSection != null && this.crossSection.getTimestamp().equals(getTimestamp()))
        {
            // already done at this time
            return;
        }
        // get current cross section
        SortedSet<RelativeLane> set = new TreeSet<>();
        set.add(RelativeLane.CURRENT);
        this.crossSection = new TimeStampedObject<>(set, getTimestamp());
        // remove all mappings related to lanes no longer in the set
        removeInvalidMappings(this.infrastructureLaneChangeInfo);
        removeInvalidMappings(this.speedLimitProspect);
        removeInvalidMappings(this.legalLaneChangePossibility);
        removeInvalidMappings(this.physicalLaneChangePossibility);
    }

    /**
     * Removes all mappings to relative lanes that are not in the most recent cross sections.
     * @param map map to clear mappings from
     */
    private void removeInvalidMappings(final Map<RelativeLane, ? extends Object> map)
    {
        Iterator<RelativeLane> iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            RelativeLane lane = iterator.next();
            if (!this.crossSection.getObject().contains(lane))
            {
                iterator.remove();
            }
        }
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

    /** {@inheritDoc} */
    public final String toString()
    {
        return "InfrastructureCategory";
    }
    
}
