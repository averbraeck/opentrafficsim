package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

import nl.tudelft.simulation.language.Throw;

/**
 * Perceives information concerning the infrastructure, including slits, lanes, speed limits and road markings.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DirectInfrastructurePerception extends LaneBasedAbstractPerceptionCategory implements InfrastructurePerception
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
    public DirectInfrastructurePerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateInfrastructureLaneChangeInfo(final RelativeLane lane) throws GTUException, ParameterException
    {

        if (this.infrastructureLaneChangeInfo.containsKey(lane)
                && this.infrastructureLaneChangeInfo.get(lane).getTimestamp().equals(getTimestamp()))
        {
            // already done at this time
            return;
        }
        updateCrossSection();

        // start at requested lane
        SortedSet<InfrastructureLaneChangeInfo> resultSet = new TreeSet<>();
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(lane);
        try
        {
            if (!record.allowsRoute(getGtu().getStrategicalPlanner().getRoute(), getGtu().getGTUType()))
            {
                resultSet.add(new InfrastructureLaneChangeInfo(1, Length.ZERO, record.isDeadEnd()));
                this.infrastructureLaneChangeInfo.put(lane, new TimeStampedObject<>(resultSet, getTimestamp()));
                return;
            }
        }
        catch (NetworkException exception)
        {
            throw new GTUException("Route has no destination.", exception);
        }
        Map<LaneStructureRecord, InfrastructureLaneChangeInfo> currentSet = new HashMap<>();
        Map<LaneStructureRecord, InfrastructureLaneChangeInfo> nextSet = new HashMap<>();
        currentSet.put(record, new InfrastructureLaneChangeInfo(0, record.getLane().getLength().plus(record.getStartDistance()),
                record.isDeadEnd()));
        while (!currentSet.isEmpty())
        {
            // move lateral
            nextSet.putAll(currentSet);
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                while (laneRecord.getLeft() != null && !nextSet.containsKey(laneRecord.getLeft()))
                {
                    InfrastructureLaneChangeInfo info =
                            new InfrastructureLaneChangeInfo(nextSet.get(laneRecord).getRequiredNumberOfLaneChanges() + 1,
                                    laneRecord.getStartDistance().plus(laneRecord.getLane().getLength()),
                                    laneRecord.getLeft().isDeadEnd());
                    nextSet.put(laneRecord.getLeft(), info);
                    laneRecord = laneRecord.getLeft();
                }
            }
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                while (laneRecord.getRight() != null && !nextSet.containsKey(laneRecord.getRight()))
                {
                    InfrastructureLaneChangeInfo info =
                            new InfrastructureLaneChangeInfo(nextSet.get(laneRecord).getRequiredNumberOfLaneChanges() + 1,
                                    laneRecord.getStartDistance().plus(laneRecord.getLane().getLength()),
                                    laneRecord.getRight().isDeadEnd());
                    nextSet.put(laneRecord.getRight(), info);
                    laneRecord = laneRecord.getRight();
                }
            }
            // move longitudinal
            currentSet = nextSet;
            nextSet = new HashMap<>();
            InfrastructureLaneChangeInfo bestOk = null;
            InfrastructureLaneChangeInfo bestNotOk = null;
            boolean deadEnd = false;
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                boolean anyOk;
                try
                {
                    anyOk = anyNextOk(laneRecord, getGtu().getStrategicalPlanner().getRoute(), getGtu().getGTUType());
                }
                catch (NetworkException exception)
                {
                    throw new GTUException("Route has no destination.", exception);
                }
                if (anyOk)
                {
                    // add to nextSet
                    for (LaneStructureRecord next : laneRecord.getNext())
                    {
                        InfrastructureLaneChangeInfo info =
                                new InfrastructureLaneChangeInfo(currentSet.get(laneRecord).getRequiredNumberOfLaneChanges(),
                                        currentSet.get(laneRecord).getRemainingDistance().plus(next.getLane().getLength()),
                                        next.isDeadEnd());
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
                    deadEnd = deadEnd || currentSet.get(laneRecord).isDeadEnd();
                    if (bestNotOk == null || currentSet.get(laneRecord).getRequiredNumberOfLaneChanges() < bestNotOk
                            .getRequiredNumberOfLaneChanges())
                    {
                        bestNotOk = currentSet.get(laneRecord);
                    }
                }

            }
            if (bestOk == null)
            {
                if (lane.isCurrent())
                {
                    // on the current lane, we need something to drive to
                    throw new GTUException("No lane was found on which to continue from link "
                            + currentSet.keySet().iterator().next().getLane().getParentLink().getId() + " for route "
                            + getGtu().getStrategicalPlanner().getRoute().getId());
                }
                else
                {
                    // empty set on other lanes permissible, on adjacent lanes, we might not be able to continue on our route
                    break;
                }
            }
            // if there are lanes that are not okay and only -further- lanes that are ok, we need to change to one of the ok's
            if (bestNotOk != null && bestOk.getRequiredNumberOfLaneChanges() > bestNotOk.getRequiredNumberOfLaneChanges())
            {
                resultSet.add(new InfrastructureLaneChangeInfo(bestOk.getRequiredNumberOfLaneChanges(),
                        bestOk.getRemainingDistance(), deadEnd));
            }
            currentSet = nextSet;
            nextSet = new HashMap<>();
        }

        // save
        this.infrastructureLaneChangeInfo.put(lane, new TimeStampedObject<>(resultSet, getTimestamp()));
    }

    /**
     * Returns whether the given record end is ok to pass. If not, a lane change is required before this end. The method will
     * also return true if the next node is the end node of the route, if the lane is cur off due to limited perception range,
     * or when there is a {@code SinkSensor} on the lane.
     * @param record checked record
     * @param route route to check at splits
     * @param gtuType gtu type
     * @return whether the given record end is ok to pass
     * @throws NetworkException if destination could not be obtained
     */
    private boolean anyNextOk(final LaneStructureRecord record, final Route route, final GTUType gtuType)
            throws NetworkException
    {

        if (record.isCutOffEnd())
        {
            return true; // always ok if cut-off
        }
        for (SingleSensor s : record.getLane().getSensors())
        {
            if (s instanceof SinkSensor)
            {
                return true; // ok towards sink
            }
        }
        Node nextNode = record.getDirection().isPlus() ? record.getLane().getParentLink().getEndNode()
                : record.getLane().getParentLink().getStartNode();
        try
        {
            if (route != null && route.destinationNode().equals(nextNode))
            {
                return true;
            }
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not determine destination node.", exception);
        }
        if (record.getNext().isEmpty())
        {
            return false; // never ok if dead-end
        }
        if (route == null)
        {
            return true; // if no route assume ok, i.e. simple networks without routes
        }
        return record.allowsRouteAtEnd(route, gtuType);

        // if (!record.isLinkSplit())
        // {
        // // always ok if no split
        // return true;
        // }
        // // split, check next based on route
        // for (LaneStructureRecord next : record.getNext())
        // {
        // if (route.contains(next.getToNode()))
        // {
        // return true; // this next record goes towards a node on the route
        // }
        // }
        // return false; // none of the next records go towards a node on the route
    }

    /** {@inheritDoc} */
    @Override
    // TODO implement this method, current implementation is a simple fix
    public final void updateSpeedLimitProspect(final RelativeLane lane) throws GTUException, ParameterException
    {
        updateCrossSection();
        checkLaneIsInCrossSection(lane);
        SpeedLimitProspect slp = new SpeedLimitProspect();
        slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.MAX_VEHICLE_SPEED, getGtu().getMaximumSpeed());
        try
        {
            slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.FIXED_SIGN,
                    getGtu().getReferencePosition().getLane().getSpeedLimit(getGtu().getGTUType()));
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not obtain speed limit from lane for perception.", exception);
        }
        this.speedLimitProspect.put(lane, new TimeStampedObject<>(slp, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final void updateLegalLaneChangePossibility(final RelativeLane lane, final LateralDirectionality lat)
            throws GTUException, ParameterException
    {
        updateCrossSection();
        checkLaneIsInCrossSection(lane);
        if (this.legalLaneChangePossibility.get(lane) == null)
        {
            this.legalLaneChangePossibility.put(lane, new HashMap<>());
        }
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(lane);
        // check tail
        Length tail = getPerception().getGtu().getRear().getDx();
        while (record != null && record.getStartDistance().plus(record.getLane().getLength()).gt(tail))
        {
            // TODO merge
            if ((lat.isLeft() && record.getLeft() != null) || (lat.isRight() && record.getRight() != null))
            {
                if (record.getPrev().isEmpty())
                {
                    break;
                }
                record = record.getPrev().isEmpty() ? null : record.getPrev().get(0);
            }
            else if (!record.equals(getPerception().getLaneStructure().getLaneLSR(lane)))
            {
                // tail needs to be passed the end of this record
                this.legalLaneChangePossibility.get(lane).put(lat,
                        new TimeStampedObject<>(record.getStartDistance().plus(record.getLane().getLength()), getTimestamp()));
                return;
            }
            else
            {
                // can not change on current lane
                break;
            }
        }
        record = getPerception().getLaneStructure().getLaneLSR(lane);
        Length dist = Length.ZERO;
        if ((lat.isLeft() && record.getLeft() != null) || (lat.isRight() && record.getRight() != null))
        {
            while (record != null
                    && ((lat.isLeft() && record.getLeft() != null) || (lat.isRight() && record.getRight() != null)))
            {
                dist = record.getStartDistance().plus(record.getLane().getLength());
                // TODO splits
                record = record.getNext().isEmpty() ? null : record.getNext().get(0);
            }
        }
        else
        {
            while (record != null
                    && ((lat.isLeft() && record.getLeft() == null) || (lat.isRight() && record.getRight() == null)))
            {
                dist = record.getStartDistance().plus(record.getLane().getLength()).neg();
                // TODO splits
                record = record.getNext().isEmpty() ? null : record.getNext().get(0);
            }
        }
        this.legalLaneChangePossibility.get(lane).put(lat, new TimeStampedObject<>(dist, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public final SortedSet<InfrastructureLaneChangeInfo> getInfrastructureLaneChangeInfo(final RelativeLane lane)
    {
        return this.infrastructureLaneChangeInfo.get(lane).getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final SpeedLimitProspect getSpeedLimitProspect(final RelativeLane lane)
    {
        return this.speedLimitProspect.get(lane).getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLegalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return this.legalLaneChangePossibility.get(fromLane).get(lat).getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Length getPhysicalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return this.physicalLaneChangePossibility.get(fromLane).get(lat).getObject();
    }

    /** {@inheritDoc} */
    @Override
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
    @Override
    public final String toString()
    {
        return "DirectInfrastructurePerception";
    }

}
