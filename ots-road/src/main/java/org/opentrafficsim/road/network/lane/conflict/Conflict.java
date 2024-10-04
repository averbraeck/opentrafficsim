package org.opentrafficsim.road.network.lane.conflict;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.LaneBasedObjectIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.UpstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuReal;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLightReal;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneRecordInterface;
import org.opentrafficsim.road.gtu.lane.perception.structure.SimpleLaneRecord;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
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
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
    private AbstractPerceptionIterable<Conflict, HeadwayGtu, LaneBasedGtu, Integer> upstreamGtus;

    /** Upstream GTUs update time. */
    private Time upstreamTime;

    /** Lanes on which upstream GTUs are found. */
    private Map<LaneBasedGtu, Lane> upstreamLanes;

    /** Current downstream GTUs provider. */
    private AbstractPerceptionIterable<Conflict, HeadwayGtu, LaneBasedGtu, Integer> downstreamGtus;

    /** Downstream GTUs update time. */
    private Time downstreamTime;

    /** Lanes on which downstream GTUs are found. */
    private Map<LaneBasedGtu, Lane> downstreamLanes;

    /** Headway type for the provided GTUs. */
    private final ConflictGtuType conflictGtuType = new ConflictGtuType();

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
     * @param geometry geometry of conflict
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @param conflictRule conflict rule, i.e. determines priority, give way, stop or all-stop
     * @param permitted whether the conflict is permitted in traffic light control
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private Conflict(final Lane lane, final Length longitudinalPosition, final Length length, final Polygon2d geometry,
            final ConflictType conflictType, final ConflictRule conflictRule, final boolean permitted) throws NetworkException
    {
        super(UUID.randomUUID().toString(), lane, longitudinalPosition, geometry);
        this.length = length;
        this.conflictType = conflictType;
        this.conflictRule = conflictRule;
        this.permitted = permitted;

        // Create conflict end
        if (conflictType.equals(ConflictType.SPLIT) || conflictType.equals(ConflictType.MERGE))
        {
            Length position = conflictType.equals(ConflictType.SPLIT) ? length : lane.getLength();
            try
            {
                this.end = new ConflictEnd(this, lane, position);
            }
            catch (OtsGeometryException exception)
            {
                // does not happen
                throw new RuntimeException("Could not create dummy geometry for ConflictEnd.", exception);
            }
        }
        else
        {
            this.end = null;
        }

        // Lane record for GTU provision
        this.rootPosition = longitudinalPosition;
        this.root = new SimpleLaneRecord(lane, this.rootPosition.neg(), null);
    }

    /** {@inheritDoc} */
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
            this.upstreamGtus = new UpstreamNeighborsIterable(this, this.root, this.rootPosition, this.maxUpstreamVisibility,
                    RelativePosition.REFERENCE_POSITION, this.conflictGtuType, RelativeLane.CURRENT)
            {
                /** {@inheritDoc} */
                @Override
                protected AbstractPerceptionIterable<Conflict, HeadwayGtu, LaneBasedGtu, Integer>.Entry getNext(
                        final LaneRecordInterface<?> record, final Length position, final Integer counter) throws GtuException
                {
                    AbstractPerceptionIterable<Conflict, HeadwayGtu, LaneBasedGtu, Integer>.Entry entry =
                            super.getNext(record, position, counter);
                    if (entry != null)
                    {
                        Conflict.this.upstreamListening.add(entry.getObject());
                        Try.execute(() -> entry.getObject().addListener(Conflict.this, LaneBasedGtu.LANE_CHANGE_EVENT),
                                "Unable to listen to GTU %s.", entry.getObject());
                        Conflict.this.upstreamLanes.put(entry.getObject(), record.getLane());
                    }
                    return entry;
                }
            };
            this.upstreamTime = time;
            this.upstreamLanes = new LinkedHashMap<>();
        }
        // return iterable that uses the base iterable
        return new ConflictGtuIterable(perceivingGtu, headwayGtuType, visibility, false, this.upstreamGtus);
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
            boolean ignoreIfUpstream = false;
            this.downstreamGtus =
                    new DownstreamNeighborsIterable(this, this.root, this.rootPosition, this.maxDownstreamVisibility,
                            RelativePosition.REFERENCE_POSITION, this.conflictGtuType, RelativeLane.CURRENT, ignoreIfUpstream)
                    {
                        /** {@inheritDoc} */
                        @Override
                        protected AbstractPerceptionIterable<Conflict, HeadwayGtu, LaneBasedGtu, Integer>.Entry getNext(
                                final LaneRecordInterface<?> record, final Length position, final Integer counter)
                                throws GtuException
                        {
                            AbstractPerceptionIterable<Conflict, HeadwayGtu, LaneBasedGtu, Integer>.Entry entry =
                                    super.getNext(record, position, counter);
                            if (entry != null)
                            {
                                Conflict.this.downstreamListening.add(entry.getObject());
                                Try.execute(() -> entry.getObject().addListener(Conflict.this, LaneBasedGtu.LANE_CHANGE_EVENT),
                                        "Unable to listen to GTU %s.", entry.getObject());
                                Conflict.this.downstreamLanes.put(entry.getObject(), record.getLane());
                            }
                            return entry;
                        }
                    };
            this.downstreamTime = time;
            this.downstreamLanes = new LinkedHashMap<>();
        }
        // return iterable that uses the base iterable
        return new ConflictGtuIterable(perceivingGtu, new OverlapHeadway(headwayGtuType), visibility, true,
                this.downstreamGtus);
        // PK does not think this detects GTUs changing lane INTO a lane of concern. Is that bad?
    }

    /** {@inheritDoc} */
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
     * @return conflictType.
     */
    public ConflictType getConflictType()
    {
        return this.conflictType;
    }

    /**
     * @return conflictRule.
     */
    public ConflictRule getConflictRule()
    {
        return this.conflictRule;
    }

    /**
     * @return conflictPriority.
     */
    public ConflictPriority conflictPriority()
    {
        return this.conflictRule.determinePriority(this);
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d getGeometry()
    {
        return (Polygon2d) super.getGeometry();
    }

    /** {@inheritDoc} */
    @Override
    public Length getLength()
    {
        return this.length;
    }

    /**
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
                boolean downstream = false;
                LaneBasedObjectIterable<Conflict, HeadwayTrafficLight,
                        TrafficLight> it = new LaneBasedObjectIterable<Conflict, HeadwayTrafficLight, TrafficLight>(this,
                                TrafficLight.class, this.root, getLongitudinalPosition(), downstream, maxDistance,
                                RelativePosition.REFERENCE_POSITION, null)
                        {
                            /** {@inheritDoc} */
                            @Override
                            protected HeadwayTrafficLight perceive(final TrafficLight object, final Length distance)
                                    throws GtuException, ParameterException
                            {
                                return new HeadwayTrafficLightReal(object, distance, false);
                            }
                        };
                if (!it.isEmpty())
                {
                    this.trafficLightDistance = it.first().getDistance();
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

    /** {@inheritDoc} */
    @Override
    public double getZ() throws RemoteException
    {
        return -0.0001;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Conflict [conflictType=" + this.conflictType + ", conflictRule=" + this.conflictRule + "]";
    }

    /**
     * Light-weight lane based object to indicate the end of a conflict. It is used to perceive conflicts when a GTU is on the
     * conflict area, and hence the conflict lane based object is upstream.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
         * @throws OtsGeometryException does not happen
         */
        ConflictEnd(final Conflict conflict, final Lane lane, final Length longitudinalPosition)
                throws NetworkException, OtsGeometryException
        {
            // FIXME: the OtsLine2d object should be shared by all ConflictEnd objects (removing OtsGeometryException)
            super(conflict.getId() + "End", lane, longitudinalPosition, new Polygon2d(new Point2d(0, 0), new Point2d(1, 0)));
            this.conflict = conflict;
        }

        /** {@inheritDoc} */
        @Override
        public void init() throws NetworkException
        {
            // override makes init accessible to conflict
            super.init();
        }

        /**
         * @return conflict
         */
        public final Conflict getConflict()
        {
            return this.conflict;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "ConflictEnd [conflict=" + this.conflict + "]";
        }
    }

    /**
     * HeadwayGtuType that generates ConflictGtu's, for use within the base iterators for upstream and downstream neighbors.
     * This result is used by secondary iterators (ConflictGtuIterable) to provide the requested specific HeadwatGtuType.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private final class ConflictGtuType implements HeadwayGtuType
    {
        /** {@inheritDoc} */
        @Override
        public HeadwayGtuReal createHeadwayGtu(final LaneBasedGtu perceivingGtu, final OtsLocatable reference,
                final LaneBasedGtu perceivedGtu, final Length distance, final boolean downstream) throws GtuException
        {
            return new HeadwayGtuReal(perceivedGtu, distance, true);
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGtuReal createDownstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException, ParameterException
        {
            return new HeadwayGtuReal(perceivedGtu, distance, true); // do not change it, called by iterable assuming downstream
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGtuReal createUpstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException, ParameterException
        {
            return new HeadwayGtuReal(perceivedGtu, distance, true); // do not change it, called by iterable assuming upstream
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGtuReal createParallelGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GtuException
        {
            throw new UnsupportedOperationException("ConflictGtuType is a pass-through type, no actual perception is allowed.");
        }
    }

    /**
     * HeadwayGtuType that changes a negative headway in to an overlapping headway, by forwarding the request to a wrapped
     * HeadwayGtuType. This is used for downstream GTUs of the conflict, accounting also for the length of the conflict. Hence,
     * overlap information concerns the conflict and a downstream GTU (downstream of the start of the conflict).
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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

        /** {@inheritDoc} */
        @Override
        public HeadwayGtu createHeadwayGtu(final LaneBasedGtu perceivingGtu, final OtsLocatable reference,
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

        /** {@inheritDoc} */
        @Override
        public HeadwayGtu createDownstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException, ParameterException
        {
            throw new UnsupportedOperationException("OverlapHeadway is a pass-through type, no actual perception is allowed.");
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGtu createUpstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException, ParameterException
        {
            throw new UnsupportedOperationException("OverlapHeadway is a pass-through type, no actual perception is allowed.");
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGtu createParallelGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GtuException
        {
            return this.wrappedType.createParallelGtu(perceivingGtu, perceivedGtu, overlapFront, overlap, overlapRear);
        }
    }

    /**
     * Iterable for upstream and downstream GTUs of a conflict, which uses a base iterable.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
        private final Iterator<HeadwayGtu> baseIterator;

        /**
         * @param perceivingGtu perceiving GTU
         * @param headwayGtuType HeadwayGtu type
         * @param visibility guaranteed visibility
         * @param downstream downstream (or upstream) neighbors
         * @param base base iterable from the conflict
         */
        ConflictGtuIterable(final LaneBasedGtu perceivingGtu, final HeadwayGtuType headwayGtuType, final Length visibility,
                final boolean downstream, final AbstractPerceptionIterable<Conflict, HeadwayGtu, LaneBasedGtu, Integer> base)
        {
            super(perceivingGtu);
            this.headwayGtuType = headwayGtuType;
            this.visibility = visibility;
            this.downstream = downstream;
            this.baseIterator = base.iterator();
        }

        /** {@inheritDoc} */
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

                /** {@inheritDoc} */
                @Override
                public boolean hasNext()
                {
                    if (this.next == null)
                    {
                        if (ConflictGtuIterable.this.baseIterator.hasNext())
                        {
                            // ConflictGtuIterable is a private class, only used with ConflictGtuType
                            HeadwayGtuReal gtu = (HeadwayGtuReal) ConflictGtuIterable.this.baseIterator.next();
                            if (gtu.getGtu().getId().equals(getObject().getId()))
                            {
                                if (ConflictGtuIterable.this.baseIterator.hasNext())
                                {
                                    gtu = (HeadwayGtuReal) ConflictGtuIterable.this.baseIterator.next();
                                }
                                else
                                {
                                    return false;
                                }
                            }
                            if (gtu.getDistance() == null || gtu.getDistance().le(ConflictGtuIterable.this.visibility))
                            {
                                this.next = new PrimaryIteratorEntry(gtu.getGtu(), gtu.getDistance());
                            }
                        }
                    }
                    return this.next != null;
                }

                /** {@inheritDoc} */
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

        /** {@inheritDoc} */
        @Override
        protected HeadwayGtu perceive(final LaneBasedGtu object, final Length distance) throws GtuException, ParameterException
        {
            return this.headwayGtuType.createHeadwayGtu(getObject(), Conflict.this, object, distance, this.downstream);
        }
    }

}
