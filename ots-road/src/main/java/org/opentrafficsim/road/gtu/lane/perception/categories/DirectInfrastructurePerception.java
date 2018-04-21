package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.lane.Lane;
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
    private Map<RelativeLane, TimeStampedObject<SpeedLimitProspect>> speedLimitProspect = new HashMap<>();

    /** Legal Lane change possibilities per relative lane and lateral direction. */
    private final Map<RelativeLane, Map<LateralDirectionality, TimeStampedObject<LaneChangePossibility>>> legalLaneChangePossibility =
            new HashMap<>();

    /** Physical Lane change possibilities per relative lane and lateral direction. */
    private final Map<RelativeLane, Map<LateralDirectionality, TimeStampedObject<LaneChangePossibility>>> physicalLaneChangePossibility =
            new HashMap<>();

    /** Cross-section. */
    private TimeStampedObject<SortedSet<RelativeLane>> crossSection;

    /** Cache for anyNextOk. */
    private final Map<LaneStructureRecord, Boolean> anyNextOkCache = new WeakHashMap<>();

    /** Set of records with accessible end as they are cut off. */
    private final Set<LaneStructureRecord> cutOff = new LinkedHashSet<>();

    /** Root. */
    private LaneStructureRecord root;

    /** Route. */
    private Route route;

    /**
     * @param perception perception
     */
    public DirectInfrastructurePerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GTUException, ParameterException
    {
        updateCrossSection();
        // clean-up
        Set<RelativeLane> cs = getCrossSection();
        this.infrastructureLaneChangeInfo.keySet().retainAll(cs);
        this.legalLaneChangePossibility.keySet().retainAll(cs);
        this.physicalLaneChangePossibility.keySet().retainAll(cs);
        this.speedLimitProspect.keySet().retainAll(cs);
        // only if required
        LaneStructureRecord newRoot = getPerception().getLaneStructure().getRootLSR();
        if (this.root == null || !newRoot.equals(this.root)
                || !Objects.equals(this.route, getPerception().getGtu().getStrategicalPlanner().getRoute())
                || this.cutOff.stream().filter((record) -> !record.isCutOffEnd()).count() > 0)
        {
            this.cutOff.clear();
            this.root = newRoot;
            this.route = getPerception().getGtu().getStrategicalPlanner().getRoute();
            this.speedLimitProspect.clear();
            for (RelativeLane lane : getCrossSection())
            {
                updateInfrastructureLaneChangeInfo(lane);
                updateLegalLaneChangePossibility(lane, LateralDirectionality.LEFT);
                updateLegalLaneChangePossibility(lane, LateralDirectionality.RIGHT);
                updatePhysicalLaneChangePossibility(lane, LateralDirectionality.LEFT);
                updatePhysicalLaneChangePossibility(lane, LateralDirectionality.RIGHT);
            }
        }
        // speed limit prospect
        for (RelativeLane lane : getCrossSection())
        {
            updateSpeedLimitProspect(lane);
        }
        for (RelativeLane lane : getCrossSection())
        {
            if (!this.infrastructureLaneChangeInfo.containsKey(lane))
            {
                updateInfrastructureLaneChangeInfo(lane); // new lane in cross section
                updateLegalLaneChangePossibility(lane, LateralDirectionality.LEFT);
                updateLegalLaneChangePossibility(lane, LateralDirectionality.RIGHT);
                updatePhysicalLaneChangePossibility(lane, LateralDirectionality.LEFT);
                updatePhysicalLaneChangePossibility(lane, LateralDirectionality.RIGHT);
            }
        }
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
                resultSet.add(InfrastructureLaneChangeInfo.fromInaccessibleLane(record.isDeadEnd()));
                this.infrastructureLaneChangeInfo.put(lane, new TimeStampedObject<>(resultSet, getTimestamp()));
                return;
            }
        }
        catch (NetworkException exception)
        {
            throw new GTUException("Route has no destination.", exception);
        }
        Map<LaneStructureRecord, InfrastructureLaneChangeInfo> currentSet = new LinkedHashMap<>();
        Map<LaneStructureRecord, InfrastructureLaneChangeInfo> nextSet = new LinkedHashMap<>();
        RelativePosition front = getPerception().getGtu().getFront();
        currentSet.put(record, new InfrastructureLaneChangeInfo(0, record, front, record.isDeadEnd()));
        while (!currentSet.isEmpty())
        {
            // move lateral
            nextSet.putAll(currentSet);
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                while (laneRecord.legalLeft() && !nextSet.containsKey(laneRecord.getLeft()))
                {
                    InfrastructureLaneChangeInfo info =
                            new InfrastructureLaneChangeInfo(nextSet.get(laneRecord).getRequiredNumberOfLaneChanges() + 1,
                                    laneRecord.getLeft(), front, laneRecord.getLeft().isDeadEnd());
                    nextSet.put(laneRecord.getLeft(), info);
                    laneRecord = laneRecord.getLeft();
                }
            }
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                while (laneRecord.legalRight() && !nextSet.containsKey(laneRecord.getRight()))
                {
                    InfrastructureLaneChangeInfo info =
                            new InfrastructureLaneChangeInfo(nextSet.get(laneRecord).getRequiredNumberOfLaneChanges() + 1,
                                    laneRecord.getRight(), front, laneRecord.getRight().isDeadEnd());
                    nextSet.put(laneRecord.getRight(), info);
                    laneRecord = laneRecord.getRight();
                }
            }
            // move longitudinal
            currentSet = nextSet;
            nextSet = new LinkedHashMap<>();
            InfrastructureLaneChangeInfo bestOk = null;
            InfrastructureLaneChangeInfo bestNotOk = null;
            boolean deadEnd = false;
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                boolean anyOk = Try.assign(() -> anyNextOk(laneRecord), "Route has no destination.");
                if (anyOk)
                {
                    // add to nextSet
                    for (LaneStructureRecord next : laneRecord.getNext())
                    {
                        InfrastructureLaneChangeInfo info = new InfrastructureLaneChangeInfo(
                                currentSet.get(laneRecord).getRequiredNumberOfLaneChanges(), next, front, next.isDeadEnd());
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
                bestOk.setDeadEnd(deadEnd);
                resultSet.add(bestOk);
            }
            currentSet = nextSet;
            nextSet = new LinkedHashMap<>();
        }

        // save
        this.infrastructureLaneChangeInfo.put(lane, new TimeStampedObject<>(resultSet, getTimestamp()));
    }

    /**
     * Returns whether the given record end is ok to pass. If not, a lane change is required before this end. The method will
     * also return true if the next node is the end node of the route, if the lane is cut off due to limited perception range,
     * or when there is a {@code SinkSensor} on the lane.
     * @param record checked record
     * @return whether the given record end is ok to pass
     * @throws NetworkException if destination could not be obtained
     * @throws GTUException if the GTU could not be obtained
     */
    private boolean anyNextOk(final LaneStructureRecord record) throws NetworkException, GTUException
    {
        if (record.isCutOffEnd())
        {
            this.cutOff.add(record);
            return true; // always ok if cut-off
        }
        // check cache
        Boolean ok = this.anyNextOkCache.get(record);
        if (ok != null)
        {
            return ok;
        }
        // sink
        for (SingleSensor s : record.getLane().getSensors())
        {
            if (s instanceof SinkSensor)
            {
                this.anyNextOkCache.put(record, true);
                return true; // ok towards sink
            }
        }
        // check destination
        Route currentRoute = getGtu().getStrategicalPlanner().getRoute();
        try
        {
            if (currentRoute != null && currentRoute.destinationNode().equals(record.getToNode()))
            {
                this.anyNextOkCache.put(record, true);
                return true;
            }
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not determine destination node.", exception);
        }
        // check dead-end
        if (record.getNext().isEmpty())
        {
            this.anyNextOkCache.put(record, false);
            return false; // never ok if dead-end
        }
        // check if we have a route
        if (currentRoute == null)
        {
            this.anyNextOkCache.put(record, true);
            return true; // if no route assume ok, i.e. simple networks without routes
        }
        // finally check route
        ok = record.allowsRouteAtEnd(currentRoute, getGtu().getGTUType());
        this.anyNextOkCache.put(record, ok);
        return ok;
    }

    /** {@inheritDoc} */
    @Override
    public final void updateSpeedLimitProspect(final RelativeLane lane) throws GTUException, ParameterException
    {
        updateCrossSection();
        checkLaneIsInCrossSection(lane);
        TimeStampedObject<SpeedLimitProspect> tsSlp = this.speedLimitProspect.get(lane);
        SpeedLimitProspect slp;
        if (tsSlp != null)
        {
            slp = tsSlp.getObject();
            slp.update(getGtu().getOdometer());
        }
        else
        {
            slp = new SpeedLimitProspect(getGtu().getOdometer());
            slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.MAX_VEHICLE_SPEED, getGtu().getMaximumSpeed(), getGtu());
        }
        try
        {
            Lane laneObj = getGtu().getReferencePosition().getLane();
            if (!slp.containsAddSource(laneObj))
            {
                slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.FIXED_SIGN, laneObj.getSpeedLimit(getGtu().getGTUType()),
                        laneObj);
            }
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
            if ((lat.isLeft() && record.legalLeft()) || (lat.isRight() && record.legalRight()))
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
                        new TimeStampedObject<>(new LaneChangePossibility(record, tail, true), getTimestamp()));
                return;
            }
            else
            {
                // can not change on current lane
                break;
            }
        }

        LaneStructureRecord prevRecord = null;
        record = getPerception().getLaneStructure().getLaneLSR(lane);
        Length dx;
        if ((lat.isLeft() && record.legalLeft()) || (lat.isRight() && record.legalRight()))
        {
            dx = getPerception().getGtu().getFront().getDx();
            while (record != null && ((lat.isLeft() && record.legalLeft()) || (lat.isRight() && record.legalRight())))
            {
                // TODO splits
                prevRecord = record;
                record = record.getNext().isEmpty() ? null : record.getNext().get(0);
            }
        }
        else
        {
            dx = tail;
            while (record != null
                    && ((lat.isLeft() && !record.legalLeft()) || (lat.isRight() && !record.legalRight())))
            {
                // TODO splits
                prevRecord = record;
                record = record.getNext().isEmpty() ? null : record.getNext().get(0);
            }
        }
        this.legalLaneChangePossibility.get(lane).put(lat,
                new TimeStampedObject<>(new LaneChangePossibility(prevRecord, dx, true), getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    // TODO implement this method, lane map needs support for this (and legal lane changes)
    public final void updatePhysicalLaneChangePossibility(final RelativeLane lane, final LateralDirectionality lat)
            throws GTUException, ParameterException
    {
        updateCrossSection();
        checkLaneIsInCrossSection(lane);

        if (this.physicalLaneChangePossibility.get(lane) == null)
        {
            this.physicalLaneChangePossibility.put(lane, new HashMap<>());
        }
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(lane);
        // check tail
        Length tail = getPerception().getGtu().getRear().getDx();
        while (record != null && record.getStartDistance().plus(record.getLane().getLength()).gt(tail))
        {
            // TODO merge
            if ((lat.isLeft() && record.physicalLeft()) || (lat.isRight() && record.physicalRight()))
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
                this.physicalLaneChangePossibility.get(lane).put(lat,
                        new TimeStampedObject<>(new LaneChangePossibility(record, tail, false), getTimestamp()));
                return;
            }
            else
            {
                // can not change on current lane
                break;
            }
        }

        LaneStructureRecord prevRecord = null;
        record = getPerception().getLaneStructure().getLaneLSR(lane);
        Length dx;
        if ((lat.isLeft() && record.physicalLeft()) || (lat.isRight() && record.physicalRight()))
        {
            dx = getPerception().getGtu().getFront().getDx();
            while (record != null && ((lat.isLeft() && record.physicalLeft()) || (lat.isRight() && record.physicalRight())))
            {
                // TODO splits
                prevRecord = record;
                record = record.getNext().isEmpty() ? null : record.getNext().get(0);
            }
        }
        else
        {
            dx = tail;
            while (record != null
                    && ((lat.isLeft() && record.getLeft() == null) || (lat.isRight() && record.getRight() == null)))
            {
                // TODO splits
                prevRecord = record;
                record = record.getNext().isEmpty() ? null : record.getNext().get(0);
            }
        }
        this.physicalLaneChangePossibility.get(lane).put(lat,
                new TimeStampedObject<>(new LaneChangePossibility(prevRecord, dx, false), getTimestamp()));
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
        return this.legalLaneChangePossibility.get(fromLane).get(lat).getObject().getDistance(lat);
    }

    /** {@inheritDoc} */
    @Override
    public final Length getPhysicalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return this.physicalLaneChangePossibility.get(fromLane).get(lat).getObject().getDistance(lat);
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
        TimeStampedObject<LaneChangePossibility> tsLcp = this.legalLaneChangePossibility.get(fromLane).get(lat);
        LaneChangePossibility lcp = tsLcp.getObject();
        return new TimeStampedObject<>(lcp.getDistance(lat), tsLcp.getTimestamp());
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
        TimeStampedObject<LaneChangePossibility> tsLcp = this.physicalLaneChangePossibility.get(fromLane).get(lat);
        LaneChangePossibility lcp = tsLcp.getObject();
        return new TimeStampedObject<>(lcp.getDistance(lat), tsLcp.getTimestamp());
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

    /**
     * Helper class to return the distance over which a lane change is or is not possible. The distance is based on a
     * LaneStructureRecord, and does not need an update as such.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class LaneChangePossibility
    {

        /** Structure the end of which determines the available distance. */
        final LaneStructureRecord record;

        /** Relative distance towards nose or tail. */
        final double dx;

        /** Whether to apply legal accessibility. */
        private final boolean legal;

        /**
         * @param record LaneStructureRecord; structure the end of which determines the available distance
         * @param dx Length; relative distance towards nose or tail
         * @param legal boolean; whether to apply legal accessibility
         */
        public LaneChangePossibility(final LaneStructureRecord record, final Length dx, final boolean legal)
        {
            this.record = record;
            this.dx = dx.si;
            this.legal = legal;
        }

        /**
         * Returns the distance over which a lane change is (&gt;0) or is not (&lt;0) possible.
         * @param lat LateralDirectionality; lateral direction
         * @return Length distance over which a lane change is (&gt;0) or is not (&lt;0) possible
         */
        final Length getDistance(final LateralDirectionality lat)
        {
            double  d = this.record.getStartDistance().si + this.record.getLane().getLength().si - this.dx;
            if (this.legal)
            {
                if ((lat.isLeft() && this.record.legalLeft()) || (lat.isRight() && this.record.legalRight()))
                {
                    return Length.createSI(d); // possible over d
                }
            }
            else
            {
                if ((lat.isLeft() && this.record.physicalRight()) || (lat.isRight() && this.record.physicalRight()))
                {
                    return Length.createSI(d); // possible over d
                }
            }
            return Length.createSI(-d); // not possible over d
        }

    }

}
