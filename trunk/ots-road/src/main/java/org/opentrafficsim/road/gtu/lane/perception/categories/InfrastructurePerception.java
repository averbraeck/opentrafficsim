package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

import nl.tudelft.simulation.language.Throw;

/**
 * Perceives information concerning the infrastructure, including slits, lanes, speed limits and road markings.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class InfrastructurePerception extends LaneBasedAbstractPerceptionCategory
{

    /** */
    private static final long serialVersionUID = 20160811L;

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
    public InfrastructurePerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll() throws GTUException, ParameterException
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
     * Updates the infrastructural lane change info. It starts at the given lane and moves downstream over the network. Whenever
     * a point is encountered where lane changes are required, this information is saved.
     * @param lane relative lateral lane
     * @throws GTUException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    public final void updateInfrastructureLaneChangeInfo(final RelativeLane lane) throws GTUException, ParameterException
    {
        if (this.infrastructureLaneChangeInfo.containsKey(lane)
                && this.infrastructureLaneChangeInfo.get(lane).getTimestamp().equals(getTimestamp()))
        {
            // already done at this time
            return;
        }
        updateCrossSection();
        checkLaneIsInCrossSection(lane);

        // start at requested lane
        SortedSet<InfrastructureLaneChangeInfo> resultSet = new TreeSet<>();
        Map<LaneStructureRecord, InfrastructureLaneChangeInfo> currentSet = new HashMap<>();
        Map<LaneStructureRecord, InfrastructureLaneChangeInfo> nextSet = new HashMap<>();
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(lane, getTimestamp());
        currentSet.put(record,
                new InfrastructureLaneChangeInfo(0, record.getLane().getLength().plus(record.getStartDistance())));
        while (!currentSet.isEmpty())
        {
            // move lateral
            nextSet.clear();
            nextSet.putAll(currentSet);
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                while (laneRecord.getLeft() != null && !nextSet.containsKey(laneRecord.getLeft()))
                {
                    InfrastructureLaneChangeInfo info =
                            new InfrastructureLaneChangeInfo(nextSet.get(laneRecord).getRequiredNumberOfLaneChanges() + 1,
                                    laneRecord.getStartDistance().plus(laneRecord.getLane().getLength()));
                    nextSet.put(laneRecord.getLeft(), info);
                    laneRecord = laneRecord.getLeft();
                }
                while (laneRecord.getRight() != null && !nextSet.containsKey(laneRecord.getRight()))
                {
                    InfrastructureLaneChangeInfo info =
                            new InfrastructureLaneChangeInfo(nextSet.get(laneRecord).getRequiredNumberOfLaneChanges() + 1,
                                    laneRecord.getStartDistance().plus(laneRecord.getLane().getLength()));
                    nextSet.put(laneRecord.getRight(), info);
                    laneRecord = laneRecord.getRight();
                }
            }
            // move longitudinal
            currentSet = nextSet;
            nextSet = new HashMap<>();
            InfrastructureLaneChangeInfo bestOk = null;
            InfrastructureLaneChangeInfo bestNotOk = null;
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                if (anyNextOk(laneRecord, getGtu().getStrategicalPlanner().getRoute()))
                {
                    // add to nextSet
                    for (LaneStructureRecord next : laneRecord.getNext())
                    {
                        InfrastructureLaneChangeInfo info =
                                new InfrastructureLaneChangeInfo(currentSet.get(laneRecord).getRequiredNumberOfLaneChanges(),
                                        currentSet.get(laneRecord).getRemainingDistance().plus(next.getLane().getLength()));
                        nextSet.put(next, info);
                    }
                    // take best ok
                    if (bestOk == null || currentSet.get(laneRecord).getRequiredNumberOfLaneChanges() < bestOk
                            .getRequiredNumberOfLaneChanges())
                    {
                        bestOk = currentSet.get(laneRecord);
                    }
                }
                else
                {
                    // take best not ok
                    if (bestNotOk == null || currentSet.get(laneRecord).getRequiredNumberOfLaneChanges() < bestNotOk
                            .getRequiredNumberOfLaneChanges())
                    {
                        bestNotOk = currentSet.get(laneRecord);
                    }
                }

            }
            // if there are lanes that are not okay and only -further- lanes that are ok, we need to change to one of the ok's
            if (bestNotOk != null && bestOk.getRequiredNumberOfLaneChanges() > bestNotOk.getRequiredNumberOfLaneChanges())
            {
                resultSet.add(bestOk);
            }
            currentSet = nextSet;
        }

        // save
        this.infrastructureLaneChangeInfo.put(lane, new TimeStampedObject<>(resultSet, getTimestamp()));
    }

    /**
     * Returns whether the given record end is ok to pass.
     * @param record checked record
     * @param route route to check at splits
     * @return whether the given record end is ok to pass
     */
    private boolean anyNextOk(final LaneStructureRecord record, final Route route)
    {
        if (record.isCutOffEnd())
        {
            return true; // always ok if cut-off
        }
        if (record.getNext().isEmpty())
        {
            return false; // never ok if dead-end
        }
        if (!record.isLinkSplit())
        {
            return true; // always ok if not a split
        }
        // split, check next based on route
        for (LaneStructureRecord next : record.getNext())
        {
            if (route.contains(next.getToNode()))
            {
                return true; // this next record goes towards a node on the route
            }
        }
        return false; // none of the next records go towards a node on the route
    }

    /**
     * Updates the speed limit prospect.
     * @param lane relative lateral lane
     * @throws GTUException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    // TODO implement this method, current implementation is a simple fix
    public final void updateSpeedLimitProspect(final RelativeLane lane) throws GTUException, ParameterException
    {
        updateCrossSection();
        checkLaneIsInCrossSection(lane);
        SpeedLimitProspect slp = new SpeedLimitProspect();
        slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.MAX_VEHICLE_SPEED, getGtu().getMaximumSpeed());
        try
        {
            slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.FIXED_SIGN, getGtu().getReferencePosition().getLane()
                .getSpeedLimit(getGtu().getGTUType()));
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not obtain speed limit from lane for perception.", exception);
        }
        this.speedLimitProspect.put(lane, new TimeStampedObject<>(slp, getTimestamp()));
    }

    /**
     * Updates the distance over which lane changes remains legally possible.
     * @param lane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @throws GTUException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    public final void updateLegalLaneChangePossibility(final RelativeLane lane, final LateralDirectionality lat)
            throws GTUException, ParameterException
    {
        updateCrossSection();
        checkLaneIsInCrossSection(lane);
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(lane, getTimestamp());
        Length dist = Length.ZERO;
        while (record != null && record.getLeft() != null)
        {
            dist = record.getStartDistance().plus(record.getLane().getLength());
            // TODO splits
            record = record.getNext().isEmpty() ? null : record.getNext().get(0);
        }
        if (this.legalLaneChangePossibility.get(lane) == null)
        {
            this.legalLaneChangePossibility.put(lane, new HashMap<>());
        }
        this.legalLaneChangePossibility.get(lane).put(lat, new TimeStampedObject<>(dist, getTimestamp()));
    }

    /**
     * Updates the distance over which lane changes remains physically possible.
     * @param lane lane from which the lane change possibility is requested
     * @param lat LEFT or RIGHT, null not allowed
     * @throws GTUException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    // TODO implement this method, lane map needs support for this (and legal lane changes)
    public final void updatePhysicalLaneChangePossibility(final RelativeLane lane, final LateralDirectionality lat)
            throws GTUException, ParameterException
    {
        updateCrossSection();
        checkLaneIsInCrossSection(lane);
        Map<LateralDirectionality, TimeStampedObject<Length>> map = new HashMap<>();
        map.put(LateralDirectionality.LEFT, new TimeStampedObject<>(Length.ZERO, getTimestamp()));
        map.put(LateralDirectionality.RIGHT, new TimeStampedObject<>(Length.ZERO, getTimestamp()));
        this.physicalLaneChangePossibility.put(RelativeLane.CURRENT, map);
    }

    /**
     * @param lane lane to check
     * @throws GTUException if the lane is not in the cross section
     */
    private void checkLaneIsInCrossSection(final RelativeLane lane) throws GTUException
    {
        Throw.when(!getCrossSection().contains(lane), GTUException.class,
                "The requeasted lane %s is not in the most recent cross section.", lane);
    }

    /**
     * Updates a set of relative lanes representing the cross section. This set consists of all lanes on the current link, and
     * an additional lane on the left and/or right side in case of a merge that is sufficiently nearby.
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter is not defined
     */
    public final void updateCrossSection() throws GTUException, ParameterException
    {
        if (this.crossSection != null && this.crossSection.getTimestamp().equals(getTimestamp()))
        {
            // already done at this time
            return;
        }
        this.crossSection = new TimeStampedObject<>(getPerception().getLaneStructure().getCrossSection(), getTimestamp());
        // remove all mappings related to lanes no longer in the set
        getPerception().getLaneStructure().removeInvalidMappings(this.infrastructureLaneChangeInfo);
        getPerception().getLaneStructure().removeInvalidMappings(this.speedLimitProspect);
        getPerception().getLaneStructure().removeInvalidMappings(this.legalLaneChangePossibility);
        getPerception().getLaneStructure().removeInvalidMappings(this.physicalLaneChangePossibility);
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
     * 
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
