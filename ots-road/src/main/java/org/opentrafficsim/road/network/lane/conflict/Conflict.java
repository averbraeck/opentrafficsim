package org.opentrafficsim.road.network.lane.conflict;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.line.Polygon2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneRecordInterface;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.gtu.lane.perception.structure.SimpleLaneRecord;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

/**
 * Conflicts deal with traffic on different links/roads that need to consider each other as their paths may be in conflict
 * spatially. A single {@code Conflict} represents the one-sided consideration of a conflicting situation. I.e., what is
 * considered <i>a single conflict in traffic theory, is represented by two {@code Conflict}s</i>, one on each of the
 * conflicting {@code Lane}s.<br>
 * <br>
 * This class provides easy access to upstream and downstream GTUs through {@code PerceptionIterable}s using methods
 * {@code getUpstreamGtus} and {@code getDownstreamGtus}. These methods are efficient in that they reuse underlying data
 * structures if the GTUs are requested at the same time by another GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class Conflict extends AbstractLaneBasedObject implements EventListener
{

    /** */
    private static final long serialVersionUID = 20160915L;

    /** Conflict type, i.e. crossing, merge or split. */
    private final ConflictType conflictType;

    /** Conflict rule, i.e. priority, give way, stop or all-stop. */
    private final ConflictRule conflictRule;

    /** End of conflict. */
    private final ConflictEnd end;

    /** Accompanying other conflict. */
    private Conflict otherConflict;

    /** The length of the conflict along the lane centerline. */
    private final Length length;

    /** Whether the conflict is a permitted conflict in traffic light control. */
    private final boolean permitted;

    /** Distance to upstream traffic light. */
    private Length trafficLightDistance;

    /** Maximum maximum search distance. */
    private Length maxMaxTrafficLightDistance;

    /////////////////////////////////////////////////////////////////
    // Properties regarding upstream and downstream GTUs provision //
    /////////////////////////////////////////////////////////////////

    /** Root for GTU search. */
    private final SimpleLaneRecord root;

    /** Position on the root. */
    private final Length rootPosition;

    /** Current upstream GTUs provider. */
    private Iterable<Entry<LaneBasedGtu>> upstreamGtus;

    /** Upstream GTUs update time. */
    private Time upstreamTime;

    /** Lanes on which upstream GTUs are found. */
    private Map<LaneBasedGtu, Lane> upstreamLanes;

    /** Current downstream GTUs provider. */
    private Iterable<Entry<LaneBasedGtu>> downstreamGtus;

    /** Downstream GTUs update time. */
    private Time downstreamTime;

    /** Lanes on which downstream GTUs are found. */
    private Map<LaneBasedGtu, Lane> downstreamLanes;

    /** Distance within which upstreamGTUs are provided (is automatically enlarged). */
    private Length maxUpstreamVisibility = Length.ZERO;

    /** Distance within which downstreamGTUs are provided (is automatically enlarged). */
    private Length maxDownstreamVisibility = Length.ZERO;

    /** Set of upstream GTU that invalidate the iterable when any changes lane. */
    private Set<LaneBasedGtu> upstreamListening = new LinkedHashSet<>();

    /** Set of upstream GTU that invalidate the iterable when any changes lane. */
    private Set<LaneBasedGtu> downstreamListening = new LinkedHashSet<>();

    /////////////////////////////////////////////////////////////////

    /**
     * Construct a new Conflict.
     * @param lane lane where this conflict starts
     * @param longitudinalPosition position of start of conflict on lane
     * @param length length of the conflict along the lane centerline
     * @param contour contour of conflict
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @param conflictRule conflict rule, i.e. determines priority, give way, stop or all-stop
     * @param permitted whether the conflict is permitted in traffic light control
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private Conflict(final Lane lane, final Length longitudinalPosition, final Length length, final Polygon2d contour,
            final ConflictType conflictType, final ConflictRule conflictRule, final boolean permitted) throws NetworkException
    {
        super(UUID.randomUUID().toString(), lane, longitudinalPosition, LaneBasedObject.makeLine(lane, longitudinalPosition),
                contour);
        this.length = length;
        this.conflictType = conflictType;
        this.conflictRule = conflictRule;
        this.permitted = permitted;

        // Create conflict end
        if (conflictType.equals(ConflictType.SPLIT) || conflictType.equals(ConflictType.MERGE))
        {
            Length position = conflictType.equals(ConflictType.SPLIT) ? length : lane.getLength();
            this.end = new ConflictEnd(this, lane, position);
        }
        else
        {
            this.end = null;
        }

        // Lane record for GTU provision
        this.rootPosition = longitudinalPosition;
        this.root = new SimpleLaneRecord(lane, this.rootPosition.neg(), null);
    }

    @Override
    protected void init() throws NetworkException
    {
        super.init();
        if (this.end != null)
        {
            this.end.init();
        }
    }

    /**
     * Make sure the conflict can provide the given upstream visibility.
     * @param visibility visibility to guarantee
     */
    private void provideUpstreamVisibility(final Length visibility)
    {
        if (visibility.gt(this.maxUpstreamVisibility))
        {
            this.maxUpstreamVisibility = visibility;
            this.upstreamTime = null;
            this.downstreamTime = null;
        }
    }

    /**
     * Make sure the conflict can provide the given downstream visibility.
     * @param visibility visibility to guarantee
     */
    private void provideDownstreamVisibility(final Length visibility)
    {
        if (visibility.gt(this.maxDownstreamVisibility))
        {
            this.maxDownstreamVisibility = visibility;
            this.upstreamTime = null;
            this.downstreamTime = null;
        }
    }

    /**
     * Provides the upstream GTUs.
     * @param perceivingGtu perceiving GTU
     * @param headwayGtuType headway GTU type to use
     * @param visibility distance over which GTU's are provided
     * @return iterable over the upstream GTUs
     */
    public PerceptionCollectable<HeadwayGtu, LaneBasedGtu> getUpstreamGtus(final LaneBasedGtu perceivingGtu,
            final HeadwayGtuType headwayGtuType, final Length visibility)
    {
        provideUpstreamVisibility(visibility);
        Time time = this.getLane().getLink().getSimulator().getSimulatorAbsTime();
        if (this.upstreamTime == null || !time.eq(this.upstreamTime))
        {
            for (LaneBasedGtu gtu : this.upstreamListening)
            {
                Try.execute(() -> gtu.removeListener(this, LaneBasedGtu.LANE_CHANGE_EVENT), "Unable to unlisten to GTU %s.",
                        gtu);
            }
            this.upstreamListening.clear();
            // setup a base iterable to provide the GTUs
            this.upstreamGtus = new NavigatingIterable<LaneBasedGtu, SimpleLaneRecord>(LaneBasedGtu.class,
                    this.maxUpstreamVisibility, Set.of(this.root), (l) -> l.getPrev(), (l) ->
                    {
                        // this lister finds the relevant sublist of GTUs and reverses it
                        List<LaneBasedGtu> gtus = l.getLane().getGtuList().toList();
                        if (gtus.isEmpty())
                        {
                            return gtus;
                        }
                        int from = 0;
                        while (from < gtus.size() && position(gtus.get(from), l, RelativePosition.REFERENCE).lt0())
                        {
                            from++;
                        }
                        int to = gtus.size() - 1;
                        Length pos = Length.min(l.getStartDistance().neg(), l.getLength());
                        while (to >= 0 && position(gtus.get(to), l, RelativePosition.FRONT).gt(pos))
                        {
                            to--;
                        }
                        if (from > to)
                        {
                            return Collections.emptyList();
                        }
                        if (from > 0 || to < gtus.size() - 1)
                        {
                            gtus = gtus.subList(from, to + 1);
                        }
                        Collections.reverse(gtus);
                        gtus.forEach((g) -> this.upstreamLanes.put(g, l.getLane()));
                        return gtus;
                    }, (t, r) -> r.getStartDistance().neg().minus(position(t, r, RelativePosition.FRONT)));
            this.upstreamTime = time;
            this.upstreamLanes = new LinkedHashMap<>();
        }
        // return iterable that uses the base iterable
        return new ConflictGtuIterable(perceivingGtu, headwayGtuType, visibility, false, new Reiterable(this.upstreamGtus));
        // PK does not think this detects GTUs changing lane INTO a lane of concern. Is that bad?
    }

    /**
     * Provides the downstream GTUs.
     * @param perceivingGtu perceiving GTU
     * @param headwayGtuType headway GTU type to use
     * @param visibility distance over which GTU's are provided
     * @return iterable over the downstream GTUs
     */
    public PerceptionCollectable<HeadwayGtu, LaneBasedGtu> getDownstreamGtus(final LaneBasedGtu perceivingGtu,
            final HeadwayGtuType headwayGtuType, final Length visibility)
    {
        provideDownstreamVisibility(visibility);
        Time time = this.getLane().getLink().getSimulator().getSimulatorAbsTime();
        if (this.downstreamTime == null || !time.eq(this.downstreamTime))
        {
            for (LaneBasedGtu gtu : this.downstreamListening)
            {
                Try.execute(() -> gtu.removeListener(this, LaneBasedGtu.LANE_CHANGE_EVENT), "Unable to unlisten to GTU %s.",
                        gtu);
            }
            this.downstreamListening.clear();
            // setup a base iterable to provide the GTUs
            this.downstreamGtus = new NavigatingIterable<LaneBasedGtu, SimpleLaneRecord>(LaneBasedGtu.class,
                    this.maxDownstreamVisibility, Set.of(this.root), (l) -> l.getPrev(), (l) ->
                    {
                        // this lister finds the relevant sublist of GTUs
                        List<LaneBasedGtu> gtus = l.getLane().getGtuList().toList();
                        if (gtus.isEmpty())
                        {
                            return gtus;
                        }
                        int from = 0;
                        Length pos = Length.max(l.getStartDistance().neg(), Length.ZERO);
                        while (from < gtus.size() && position(gtus.get(from), l, RelativePosition.FRONT).lt(pos))
                        {
                            from++;
                        }
                        int to = gtus.size() - 1;
                        while (to >= 0 && position(gtus.get(to), l, RelativePosition.REFERENCE).gt(l.getLength()))
                        {
                            to--;
                        }
                        if (from > to)
                        {
                            return Collections.emptyList();
                        }
                        if (from > 0 || to < gtus.size() - 1)
                        {
                            gtus = gtus.subList(from, to + 1);
                        }
                        gtus.forEach((g) -> this.downstreamLanes.put(g, l.getLane()));
                        return gtus;
                    }, (t, r) -> r.getStartDistance().plus(position(t, r, RelativePosition.REAR)));
            this.downstreamTime = time;
            this.downstreamLanes = new LinkedHashMap<>();
        }
        // return iterable that uses the base iterable
        return new ConflictGtuIterable(perceivingGtu, new OverlapHeadway(headwayGtuType), visibility, true,
                new Reiterable(this.downstreamGtus));
        // PK does not think this detects GTUs changing lane INTO a lane of concern. Is that bad?
    }

    /**
     * Returns the position of the GTU on the lane of the given record.
     * @param gtu gtu.
     * @param record lane record.
     * @param positionType RelativePosition.Type; relative position type.
     * @return position of the GTU on the lane of the given record.
     */
    private Length position(final LaneBasedGtu gtu, final LaneRecordInterface<?> record,
            final RelativePosition.Type positionType)
    {
        return Try.assign(() -> gtu.getPosition(record.getLane(), gtu.getRelativePositions().get(positionType)),
                "Unable to obtain position %s of GTU.", positionType);
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        Object[] payload = (Object[]) event.getContent();
        LaneBasedGtu gtu = (LaneBasedGtu) getLane().getNetwork().getGTU((String) payload[0]);
        if (this.upstreamListening.contains(gtu))
        {
            this.upstreamTime = null;
        }
        if (this.downstreamListening.contains(gtu))
        {
            this.downstreamTime = null;
        }
    }

    /**
     * Returns the conflict type.
     * @return conflictType.
     */
    public ConflictType getConflictType()
    {
        return this.conflictType;
    }

    /**
     * Returns the conflict rule.
     * @return conflictRule.
     */
    public ConflictRule getConflictRule()
    {
        return this.conflictRule;
    }

    /**
     * Returns the conflict priority.
     * @return conflictPriority.
     */
    public ConflictPriority conflictPriority()
    {
        return this.conflictRule.determinePriority(this);
    }

    @Override
    public Length getLength()
    {
        return this.length;
    }

    /**
     * Returns the other conflict.
     * @return otherConflict.
     */
    public Conflict getOtherConflict()
    {
        return this.otherConflict;
    }

    /**
     * If permitted, traffic upstream of traffic lights may not be ignored, as these can have green light.
     * @return permitted.
     */
    public boolean isPermitted()
    {
        return this.permitted;
    }

    /**
     * Returns the distance to an upstream traffic light.
     * @param maxDistance maximum distance of traffic light
     * @return distance to upstream traffic light, infinite if beyond maximum distance
     */
    public Length getTrafficLightDistance(final Length maxDistance)
    {
        if (this.trafficLightDistance == null)
        {
            if (this.maxMaxTrafficLightDistance == null || this.maxMaxTrafficLightDistance.lt(maxDistance))
            {
                this.maxMaxTrafficLightDistance = maxDistance;
                NavigatingIterable<TrafficLight, SimpleLaneRecord> iterable =
                        new NavigatingIterable<>(TrafficLight.class, maxDistance, Set.of(this.root), (l) ->
                        {
                            // this navigator only returns records when there are no TrafficLights on the lane
                            List<LaneBasedObject> list =
                                    l.getLane().getLaneBasedObjects(Length.ZERO, l.getStartDistance().neg());
                            if (list.stream().anyMatch((o) -> o instanceof TrafficLight))
                            {
                                return Collections.emptySet();
                            }
                            return l.getPrev();
                        }, (l) ->
                        {
                            // this lister finds the first TrafficLight and returns it as the only TrafficLight in the list
                            List<LaneBasedObject> list =
                                    l.getLane().getLaneBasedObjects(Length.ZERO, l.getStartDistance().neg());
                            for (int index = list.size() - 1; index >= 0; index--)
                            {
                                if (list.get(index) instanceof TrafficLight)
                                {
                                    return List.of(list.get(index));
                                }
                            }
                            return Collections.emptyList();
                        }, (t, l) -> l.getStartDistance().neg().minus(t.getLongitudinalPosition()));
                Iterator<Entry<TrafficLight>> iterator = iterable.iterator();
                if (iterator.hasNext())
                {
                    this.trafficLightDistance = iterator.next().distance();
                }
            }
        }
        if (this.trafficLightDistance != null && maxDistance.ge(this.trafficLightDistance))
        {
            return this.trafficLightDistance;
        }
        return Length.POSITIVE_INFINITY;
    }

    /**
     * Creates a pair of conflicts.
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @param conflictRule conflict rule
     * @param permitted whether the conflict is permitted in traffic light control
     * @param lane1 lane of conflict 1
     * @param longitudinalPosition1 longitudinal position of conflict 1
     * @param length1 {@code Length} of conflict 1
     * @param geometry1 geometry of conflict 1
     * @param lane2 lane of conflict 2
     * @param longitudinalPosition2 longitudinal position of conflict 2
     * @param length2 {@code Length} of conflict 2
     * @param geometry2 geometry of conflict 2
     * @param simulator the simulator for animation and timed events
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void generateConflictPair(final ConflictType conflictType, final ConflictRule conflictRule,
            final boolean permitted, final Lane lane1, final Length longitudinalPosition1, final Length length1,
            final Polygon2d geometry1, final Lane lane2, final Length longitudinalPosition2, final Length length2,
            final Polygon2d geometry2, final OtsSimulatorInterface simulator) throws NetworkException
    {
        // lane, longitudinalPosition, length and geometry are checked in AbstractLaneBasedObject
        Throw.whenNull(conflictType, "Conflict type may not be null.");

        Conflict conf1 = new Conflict(lane1, longitudinalPosition1, length1, geometry1, conflictType, conflictRule, permitted);
        conf1.init(); // fire events and register on lane
        Conflict conf2 = new Conflict(lane2, longitudinalPosition2, length2, geometry2, conflictType, conflictRule, permitted);
        conf2.init(); // fire events and register on lane
        conf1.otherConflict = conf2;
        conf2.otherConflict = conf1;
    }

    @Override
    public String toString()
    {
        return "Conflict [conflictType=" + this.conflictType + ", conflictRule=" + this.conflictRule + "]";
    }

    /**
     * Light-weight lane based object to indicate the end of a conflict. It is used to perceive conflicts when a GTU is on the
     * conflict area, and hence the conflict lane based object is upstream.
     */
    public class ConflictEnd extends AbstractLaneBasedObject
    {
        /** */
        private static final long serialVersionUID = 20161214L;

        /** Conflict at start of conflict area. */
        private final Conflict conflict;

        /**
         * Construct a new ConflictEnd object.
         * @param conflict conflict at start of conflict area
         * @param lane lane
         * @param longitudinalPosition position along the lane of the end of the conflict
         * @throws NetworkException on network exception
         */
        ConflictEnd(final Conflict conflict, final Lane lane, final Length longitudinalPosition) throws NetworkException
        {
            super(conflict.getId() + "End", lane, longitudinalPosition, LaneBasedObject.makeLine(lane, longitudinalPosition));
            this.conflict = conflict;
        }

        @Override
        public void init() throws NetworkException
        {
            // override makes init accessible to conflict
            super.init();
        }

        /**
         * Returns the conflict.
         * @return conflict
         */
        public final Conflict getConflict()
        {
            return this.conflict;
        }

        @Override
        public final String toString()
        {
            return "ConflictEnd [conflict=" + this.conflict + "]";
        }
    }

    /**
     * HeadwayGtuType that changes a negative headway in to an overlapping headway, by forwarding the request to a wrapped
     * HeadwayGtuType. This is used for downstream GTUs of the conflict, accounting also for the length of the conflict. Hence,
     * overlap information concerns the conflict and a downstream GTU (downstream of the start of the conflict).
     */
    private class OverlapHeadway implements HeadwayGtuType
    {
        /** Wrapped headway type. */
        private HeadwayGtuType wrappedType;

        /**
         * Constructor.
         * @param wrappedType wrapped headway type
         */
        OverlapHeadway(final HeadwayGtuType wrappedType)
        {
            this.wrappedType = wrappedType;
        }

        @Override
        public HeadwayGtu createHeadwayGtu(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
                final LaneBasedGtu perceivedGtu, final Length dist, final boolean downstream)
                throws GtuException, ParameterException
        {
            if (dist.ge(getLength()))
            {
                // GTU fully downstream of the conflict
                return this.wrappedType.createHeadwayGtu(perceivingGtu, reference, perceivedGtu, dist.minus(getLength()),
                        downstream);
            }
            else
            {
                Length overlapRear = dist;
                Length overlap = getLength(); // start with conflict length
                Lane lane = downstream ? Conflict.this.downstreamLanes.get(perceivedGtu)
                        : Conflict.this.upstreamLanes.get(perceivedGtu);
                Length overlapFront = dist.plus(perceivedGtu.getProjectedLength(lane)).minus(getLength());
                if (overlapFront.lt0())
                {
                    overlap = overlap.plus(overlapFront); // subtract front being before the conflict end
                }
                if (overlapRear.gt0())
                {
                    overlap = overlap.minus(overlapRear); // subtract rear being past the conflict start
                }
                return createParallelGtu(perceivingGtu, perceivedGtu, overlapFront, overlap, overlapRear);
            }
        }

        @Override
        public HeadwayGtu createDownstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException, ParameterException
        {
            throw new UnsupportedOperationException("OverlapHeadway is a pass-through type, no actual perception is allowed.");
        }

        @Override
        public HeadwayGtu createUpstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException, ParameterException
        {
            throw new UnsupportedOperationException("OverlapHeadway is a pass-through type, no actual perception is allowed.");
        }

        @Override
        public HeadwayGtu createParallelGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GtuException
        {
            return this.wrappedType.createParallelGtu(perceivingGtu, perceivedGtu, overlapFront, overlap, overlapRear);
        }
    }

    /**
     * Iterable for upstream and downstream GTUs of a conflict, which uses a base iterable.
     */
    private class ConflictGtuIterable extends AbstractPerceptionReiterable<LaneBasedGtu, HeadwayGtu, LaneBasedGtu>
    {
        /** HeadwayGtu type. */
        private final HeadwayGtuType headwayGtuType;

        /** Guaranteed visibility. */
        private final Length visibility;

        /** Downstream (or upstream) neighbors. */
        private final boolean downstream;

        /** Base iterator of the base iterable. */
        private final Iterator<Entry<LaneBasedGtu>> base;

        /**
         * @param perceivingGtu perceiving GTU
         * @param headwayGtuType HeadwayGtu type
         * @param visibility guaranteed visibility
         * @param downstream downstream (or upstream) neighbors
         * @param base base iterable from the conflict
         */
        ConflictGtuIterable(final LaneBasedGtu perceivingGtu, final HeadwayGtuType headwayGtuType, final Length visibility,
                final boolean downstream, final Iterable<Entry<LaneBasedGtu>> base)
        {
            super(perceivingGtu);
            this.headwayGtuType = headwayGtuType;
            this.visibility = visibility;
            this.downstream = downstream;
            this.base = base.iterator();
        }

        @Override
        protected Iterator<PrimaryIteratorEntry> primaryIterator()
        {
            /**
             * Iterator that iterates over PrimaryIteratorEntry objects.
             */
            class ConflictGtuIterator implements Iterator<PrimaryIteratorEntry>
            {
                /** Next entry. */
                private PrimaryIteratorEntry next;

                @Override
                public boolean hasNext()
                {
                    if (this.next == null)
                    {
                        if (ConflictGtuIterable.this.base.hasNext())
                        {
                            Entry<LaneBasedGtu> gtu = ConflictGtuIterable.this.base.next();
                            if (gtu.object().getId().equals(getObject().getId()))
                            {
                                if (ConflictGtuIterable.this.base.hasNext())
                                {
                                    gtu = ConflictGtuIterable.this.base.next();
                                }
                                else
                                {
                                    return false;
                                }
                            }
                            if (gtu.distance() == null || gtu.distance().le(ConflictGtuIterable.this.visibility))
                            {
                                this.next = new PrimaryIteratorEntry(gtu.object(), gtu.distance());
                            }
                        }
                    }
                    return this.next != null;
                }

                @Override
                public PrimaryIteratorEntry next()
                {
                    if (hasNext())
                    {
                        PrimaryIteratorEntry out = this.next;
                        this.next = null;
                        return out;
                    }
                    throw new NoSuchElementException();
                }
            }
            return new ConflictGtuIterator();
        }

        @Override
        protected HeadwayGtu perceive(final LaneBasedGtu object, final Length distance) throws GtuException, ParameterException
        {
            return this.headwayGtuType.createHeadwayGtu(getObject(), Conflict.this, object, distance, this.downstream);
        }
    }

    /**
     * Reiterable of which the main purpose is efficiency. Storing the result for multiple GTUs is more efficient than invoking
     * the NavigatingIterable logic for each.
     */
    private class Reiterable implements Iterable<Entry<LaneBasedGtu>>
    {
        /** Base iterator from NavigatingIterable. */
        private final Iterator<Entry<LaneBasedGtu>> base;

        /** List of found GTUs so far. */
        private final List<Entry<LaneBasedGtu>> soFar = new ArrayList<>();

        /**
         * Constructor.
         * @param base base iterable.
         */
        Reiterable(final Iterable<Entry<LaneBasedGtu>> base)
        {
            this.base = base.iterator();
        }

        @Override
        public Iterator<Entry<LaneBasedGtu>> iterator()
        {
            return new Iterator<Entry<LaneBasedGtu>>()
            {
                private int index = 0;

                @Override
                public Entry<LaneBasedGtu> next()
                {
                    return Reiterable.this.soFar.get(this.index++);
                }

                @Override
                public boolean hasNext()
                {
                    if (this.index >= Reiterable.this.soFar.size() && Reiterable.this.base.hasNext())
                    {
                        Reiterable.this.soFar.add(Reiterable.this.base.next());
                    }
                    return this.index < Reiterable.this.soFar.size();
                }
            };
        }
    }

}
