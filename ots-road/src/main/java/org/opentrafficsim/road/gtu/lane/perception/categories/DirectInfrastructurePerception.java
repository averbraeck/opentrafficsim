package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
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

/**
 * Perceives information concerning the infrastructure, including splits, lanes, speed limits and road markings. This category
 * is optimized by cooperating closely with the {@code LaneStructure} and only updating internal information when the GTU is on
 * a new {@code Lane}. On the {@code Lane} information is defined relative to the start, and thus easily calculated at each
 * time.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// TODO: more than the lane speed limit and maximum vehicle speed in the speed limit prospect
public class DirectInfrastructurePerception extends LaneBasedAbstractPerceptionCategory implements InfrastructurePerception
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Infrastructure lane change info per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SortedSet<InfrastructureLaneChangeInfo>>> infrastructureLaneChangeInfo =
            new LinkedHashMap<>();

    /** Speed limit prospect per relative lane. */
    private Map<RelativeLane, TimeStampedObject<SpeedLimitProspect>> speedLimitProspect = new LinkedHashMap<>();

    /** Legal Lane change possibilities per relative lane and lateral direction. */
    private final Map<RelativeLane,
            Map<LateralDirectionality, TimeStampedObject<LaneChangePossibility>>> legalLaneChangePossibility =
                    new LinkedHashMap<>();

    /** Physical Lane change possibilities per relative lane and lateral direction. */
    private final Map<RelativeLane,
            Map<LateralDirectionality, TimeStampedObject<LaneChangePossibility>>> physicalLaneChangePossibility =
                    new LinkedHashMap<>();

    /** Cross-section. */
    private TimeStampedObject<SortedSet<RelativeLane>> crossSection;

    /** Cache for anyNextOk. */
    private final Map<LaneStructureRecord, Boolean> anyNextOkCache = new WeakHashMap<>();

    /** Set of records with accessible end as they are cut off. */
    private final Set<LaneStructureRecord> cutOff = new LinkedHashSet<>();

    /** Root. */
    private LaneStructureRecord root;

    /** Lanes registered to the GTU used to check if an update is required. */
    private Set<Lane> lanes;

    /** Route. */
    private Route route;

    /**
     * @param perception LanePerception; perception
     */
    public DirectInfrastructurePerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GtuException, ParameterException
    {
        updateCrossSection();
        // clean-up
        Set<RelativeLane> cs = getCrossSection();
        this.infrastructureLaneChangeInfo.keySet().retainAll(cs);
        this.legalLaneChangePossibility.keySet().retainAll(cs);
        this.physicalLaneChangePossibility.keySet().retainAll(cs);
        this.speedLimitProspect.keySet().retainAll(cs);
        // only if required
        LaneStructureRecord newRoot = getPerception().getLaneStructure().getRootRecord();
        if (this.root == null || !newRoot.equals(this.root)
                || !this.lanes.equals(getPerception().getGtu().positions(RelativePosition.REFERENCE_POSITION).keySet())
                || !Objects.equals(this.route, getPerception().getGtu().getStrategicalPlanner().getRoute())
                || this.cutOff.stream().filter((
                        record
                ) -> !record.isCutOffEnd()).count() > 0)
        {
            this.cutOff.clear();
            this.root = newRoot;
            this.lanes = getPerception().getGtu().positions(RelativePosition.REFERENCE_POSITION).keySet();
            this.route = getPerception().getGtu().getStrategicalPlanner().getRoute();
            // TODO: this is not suitable if we change lane and consider e.g. dynamic speed signs, they will be forgotten
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
    public final void updateInfrastructureLaneChangeInfo(final RelativeLane lane) throws GtuException, ParameterException
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
        LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
        try
        {
            record = getPerception().getLaneStructure().getFirstRecord(lane);
            if (!record.allowsRoute(getGtu().getStrategicalPlanner().getRoute(), getGtu().getGtuType()))
            {
                resultSet.add(InfrastructureLaneChangeInfo.fromInaccessibleLane(record.isDeadEnd()));
                this.infrastructureLaneChangeInfo.put(lane, new TimeStampedObject<>(resultSet, getTimestamp()));
                return;
            }
        }
        catch (NetworkException exception)
        {
            throw new GtuException("Route has no destination.", exception);
        }
        Map<LaneStructureRecord, InfrastructureLaneChangeInfo> currentSet = new LinkedHashMap<>();
        Map<LaneStructureRecord, InfrastructureLaneChangeInfo> nextSet = new LinkedHashMap<>();
        RelativePosition front = getPerception().getGtu().getFront();
        currentSet.put(record,
                new InfrastructureLaneChangeInfo(0, record, front, record.isDeadEnd(), LateralDirectionality.NONE));
        while (!currentSet.isEmpty())
        {
            // move lateral
            nextSet.putAll(currentSet);
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                while (laneRecord.legalLeft() && !nextSet.containsKey(laneRecord.getLeft()))
                {
                    InfrastructureLaneChangeInfo info =
                            nextSet.get(laneRecord).left(laneRecord.getLeft(), front, laneRecord.getLeft().isDeadEnd());
                    nextSet.put(laneRecord.getLeft(), info);
                    laneRecord = laneRecord.getLeft();
                }
            }
            for (LaneStructureRecord laneRecord : currentSet.keySet())
            {
                while (laneRecord.legalRight() && !nextSet.containsKey(laneRecord.getRight()))
                {
                    InfrastructureLaneChangeInfo info =
                            nextSet.get(laneRecord).right(laneRecord.getRight(), front, laneRecord.getRight().isDeadEnd());
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
                        try
                        {
                            if (next.allowsRoute(getGtu().getStrategicalPlanner().getRoute(), getGtu().getGtuType()))
                            {
                                InfrastructureLaneChangeInfo prev = currentSet.get(laneRecord);
                                InfrastructureLaneChangeInfo info =
                                        new InfrastructureLaneChangeInfo(prev.getRequiredNumberOfLaneChanges(), next, front,
                                                next.isDeadEnd(), prev.getLateralDirectionality());
                                nextSet.put(next, info);
                            }
                        }
                        catch (NetworkException exception)
                        {
                            throw new RuntimeException("Network exception while considering route on next lane.", exception);
                        }
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
                break;
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
     * @param record LaneStructureRecord; checked record
     * @return whether the given record end is ok to pass
     * @throws NetworkException if destination could not be obtained
     * @throws GtuException if the GTU could not be obtained
     */
    private boolean anyNextOk(final LaneStructureRecord record) throws NetworkException, GtuException
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
            // XXX for now, we do allow to lower speed for a DestinationSensor (e.g., to brake for parking)
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
        ok = record.allowsRouteAtEnd(currentRoute, getGtu().getGtuType());
        this.anyNextOkCache.put(record, ok);
        return ok;
    }

    /** {@inheritDoc} */
    @Override
    public final void updateSpeedLimitProspect(final RelativeLane lane) throws GtuException, ParameterException
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
                slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.FIXED_SIGN, laneObj.getSpeedLimit(getGtu().getGtuType()),
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
            throws GtuException, ParameterException
    {
        updateLaneChangePossibility(lane, lat, true, this.legalLaneChangePossibility);
    }

    /** {@inheritDoc} */
    @Override
    public final void updatePhysicalLaneChangePossibility(final RelativeLane lane, final LateralDirectionality lat)
            throws GtuException, ParameterException
    {
        updateLaneChangePossibility(lane, lat, false, this.physicalLaneChangePossibility);
    }

    /**
     * Updates the distance over which lane changes remains legally or physically possible.
     * @param lane RelativeLane; lane from which the lane change possibility is requested
     * @param lat LateralDirectionality; LEFT or RIGHT, null not allowed
     * @param legal boolean; legal, or physical otherwise
     * @param possibilityMap
     *            Map&lt;RelativeLane,Map&lt;LateralDirectionality,TimeStampedObject&lt;LaneChangePossibility&gt;&gt;&gt;;
     *            Map&lt;RelativeLane,Map&lt;LateralDirectionality,TimeStampedObject&lt;LaneChangePossibility&gt;&gt;&gt;; legal
     *            or physical possibility map
     * @throws GtuException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    private void updateLaneChangePossibility(final RelativeLane lane, final LateralDirectionality lat, final boolean legal,
            final Map<RelativeLane, Map<LateralDirectionality, TimeStampedObject<LaneChangePossibility>>> possibilityMap)
            throws GtuException, ParameterException
    {
        updateCrossSection();
        checkLaneIsInCrossSection(lane);

        if (possibilityMap.get(lane) == null)
        {
            possibilityMap.put(lane, new LinkedHashMap<>());
        }
        LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
        // check tail
        Length tail = getPerception().getGtu().getRear().getDx();
        while (record != null && record.getStartDistance().gt(tail) && !record.getPrev().isEmpty()
                && ((lat.isLeft() && record.possibleLeft(legal)) || (lat.isRight() && record.possibleRight(legal))))
        {
            if (record.getPrev().size() > 1)
            {
                // assume not possible at a merge
                possibilityMap.get(lane).put(lat, new TimeStampedObject<>(
                        new LaneChangePossibility(record.getPrev().get(0), tail, true), getTimestamp()));
                return;
            }
            else if (record.getPrev().isEmpty())
            {
                // dead-end, no lane upwards prevents a lane change
                break;
            }
            record = record.getPrev().get(0);
            if ((lat.isLeft() && !record.possibleLeft(legal)) || (lat.isRight() && !record.possibleRight(legal)))
            {
                // this lane prevents a lane change for the tail
                possibilityMap.get(lane).put(lat,
                        new TimeStampedObject<>(new LaneChangePossibility(record, tail, true), getTimestamp()));
                return;
            }
        }

        LaneStructureRecord prevRecord = null;
        record = getPerception().getLaneStructure().getFirstRecord(lane);

        Length dx;
        if ((lat.isLeft() && record.possibleLeft(legal)) || (lat.isRight() && record.possibleRight(legal)))
        {
            dx = getPerception().getGtu().getFront().getDx();
            while (record != null
                    && ((lat.isLeft() && record.possibleLeft(legal)) || (lat.isRight() && record.possibleRight(legal))))
            {
                // TODO: splits
                prevRecord = record;
                record = record.getNext().isEmpty() ? null : record.getNext().get(0);
            }
        }
        else
        {
            dx = getPerception().getGtu().getRear().getDx();
            while (record != null
                    && ((lat.isLeft() && !record.possibleLeft(legal)) || (lat.isRight() && !record.possibleRight(legal))))
            {
                // TODO: splits
                prevRecord = record;
                record = record.getNext().isEmpty() ? null : record.getNext().get(0);
            }
        }
        possibilityMap.get(lane).put(lat,
                new TimeStampedObject<>(new LaneChangePossibility(prevRecord, dx, true), getTimestamp()));
    }

    /**
     * @param lane RelativeLane; lane to check
     * @throws GtuException if the lane is not in the cross section
     */
    private void checkLaneIsInCrossSection(final RelativeLane lane) throws GtuException
    {
        Throw.when(!getCrossSection().contains(lane), GtuException.class,
                "The requeasted lane %s is not in the most recent cross section.", lane);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateCrossSection() throws GtuException, ParameterException
    {
        if (this.crossSection != null && this.crossSection.getTimestamp().equals(getTimestamp()))
        {
            // already done at this time
            return;
        }
        this.crossSection =
                new TimeStampedObject<>(getPerception().getLaneStructure().getExtendedCrossSection(), getTimestamp());
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
     * @param lane RelativeLane; relative lateral lane
     * @return time stamped infrastructure lane change info of a lane
     */
    public final TimeStampedObject<SortedSet<InfrastructureLaneChangeInfo>> getTimeStampedInfrastructureLaneChangeInfo(
            final RelativeLane lane)
    {
        return this.infrastructureLaneChangeInfo.get(lane);
    }

    /**
     * Returns the time stamped prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane RelativeLane; relative lateral lane
     * @return time stamped prospect for speed limits on a lane
     */
    public final TimeStampedObject<SpeedLimitProspect> getTimeStampedSpeedLimitProspect(final RelativeLane lane)
    {
        return this.speedLimitProspect.get(lane);
    }

    /**
     * Returns the time stamped distance over which a lane change remains legally possible.
     * @param fromLane RelativeLane; lane from which the lane change possibility is requested
     * @param lat LateralDirectionality; LEFT or RIGHT, null not allowed
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
     * @param fromLane RelativeLane; lane from which the lane change possibility is requested
     * @param lat LateralDirectionality; LEFT or RIGHT, null not allowed
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class LaneChangePossibility
    {

        /** Structure the end of which determines the available distance. */
        private final LaneStructureRecord record;

        /** Relative distance towards nose or tail. */
        private final double dx;

        /** Whether to apply legal accessibility. */
        private final boolean legal;

        /**
         * @param record LaneStructureRecord; structure the end of which determines the available distance
         * @param dx Length; relative distance towards nose or tail
         * @param legal boolean; whether to apply legal accessibility
         */
        LaneChangePossibility(final LaneStructureRecord record, final Length dx, final boolean legal)
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
            double d = this.record.getStartDistance().si + this.record.getLane().getLength().si - this.dx;
            if ((lat.isLeft() && this.record.possibleLeft(this.legal))
                    || (lat.isRight() && this.record.possibleRight(this.legal)))
            {
                return Length.instantiateSI(d); // possible over d
            }
            return Length.instantiateSI(-d); // not possible over d
        }

    }

}
