package org.opentrafficsim.road.gtu.generator;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuErrorHandler;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuGenerator;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.GeneratorLanePosition;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuReal;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Lane based GTU generator. This generator generates lane based GTUs using a LaneBasedTemplateGTUType. The template is used to
 * generate a set of GTU characteristics at the times implied by the headway generator. These sets are queued until there is
 * sufficient room to construct a GTU at the specified lane locations. The speed of a construction GTU may be reduced to ensure
 * it does not run into its immediate leader GTU.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LaneBasedGtuGenerator extends LocalEventProducer implements Serializable, Identifiable, GtuGenerator
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /**
     * Event of a generated GTU. Payload: LaneBasedGtu
     */
    public static final EventType GTU_GENERATED_EVENT = new EventType("GENERATOR.GTU_GENERATED", new MetaData("GTU generated",
            "GTU was generated", new ObjectDescriptor("GTU", "The GTU itself", LaneBasedGtu.class)));

    /** FIFO for templates that have not been generated yet due to insufficient room/headway, per position, and per link. */
    private final Map<CrossSectionLink,
            Map<GeneratorLanePosition, Queue<TimeStampedObject<LaneBasedGtuCharacteristics>>>> unplacedTemplates =
                    new LinkedHashMap<>();

    /** Name of the GTU generator. */
    private final String id;

    /** Unique id in the network. */
    private final String uniqueId;

    /** Time distribution that determines the interval times between GTUs. */
    private final Generator<Duration> interarrivelTimeGenerator;

    /** Generates most properties of the GTUs. */
    private final LaneBasedGtuCharacteristicsGenerator laneBasedGtuCharacteristicsGenerator;

    /** Total number of GTUs generated so far. */
    private long generatedGTUs = 0;

    /** Retry interval for checking if a GTU can be placed. */
    private Duration reTryInterval = new Duration(0.1, DurationUnit.SI);

    /** Location provider for all generated GTUs. */
    private final GeneratorPositions generatorPositions;

    /** Network. */
    private final OtsRoadNetwork network;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** The way that this generator checks if it is safe to construct and place the next lane based GTU. */
    private final RoomChecker roomChecker;

    /** ID generator. */
    private final Supplier<String> idGenerator;

    /** Initial distance over which lane changes shouldn't be performed. */
    private Length noLaneChangeDistance = null;

    /** Whether GTUs change lane instantaneously. */
    private boolean instantaneousLaneChange = false;

    /** GTU error handler. */
    private GtuErrorHandler errorHandler = GtuErrorHandler.THROW;

    /** Vehicle generation is ignored on these lanes. */
    private Set<Lane> disabled = new LinkedHashSet<>();

    /**
     * Construct a new lane base GTU generator.
     * @param id String; name of the new GTU generator
     * @param interarrivelTimeGenerator Generator&lt;Duration&gt;; generator for the interval times between GTUs
     * @param laneBasedGtuCharacteristicsGenerator LaneBasedGtuCharacteristicsGenerator; generator of the characteristics of
     *            each GTU
     * @param generatorPositions GeneratorPositions; location and initial direction provider for all generated GTUs
     * @param network OtsRoadNetwork; the OTS network that owns the generated GTUs
     * @param simulator OtsSimulatorInterface; simulator
     * @param roomChecker RoomChecker; the way that this generator checks that there is sufficient room to place a new GTU
     * @param idGenerator Supplier&lt;String&gt;; id generator
     * @throws SimRuntimeException when <cite>startTime</cite> lies before the current simulation time
     * @throws ProbabilityException pe
     * @throws ParameterException if drawing from the interarrival generator fails
     * @throws NetworkException if the object could not be added to the network
     */
    @SuppressWarnings("parameternumber")
    public LaneBasedGtuGenerator(final String id, final Generator<Duration> interarrivelTimeGenerator,
            final LaneBasedGtuCharacteristicsGenerator laneBasedGtuCharacteristicsGenerator,
            final GeneratorPositions generatorPositions, final OtsRoadNetwork network, final OtsSimulatorInterface simulator,
            final RoomChecker roomChecker, final Supplier<String> idGenerator)
            throws SimRuntimeException, ProbabilityException, ParameterException, NetworkException
    {
        this.id = id;
        this.uniqueId = UUID.randomUUID().toString() + "_" + id;
        this.interarrivelTimeGenerator = interarrivelTimeGenerator;
        this.laneBasedGtuCharacteristicsGenerator = laneBasedGtuCharacteristicsGenerator;
        this.generatorPositions = generatorPositions;
        this.network = network;
        this.simulator = simulator;
        this.roomChecker = roomChecker;
        this.idGenerator = idGenerator;
        Duration headway = this.interarrivelTimeGenerator.draw();
        if (headway != null) // otherwise no demand at all
        {
            simulator.scheduleEventRel(headway, this, "generateCharacteristics", new Object[] {});
        }
        this.network.addNonLocatedObject(this);
    }

    /**
     * Sets the initial distance over which lane changes shouldn't be performed.
     * @param noLaneChangeDistance Length; initial distance over which lane changes shouldn't be performed
     */
    public void setNoLaneChangeDistance(final Length noLaneChangeDistance)
    {
        this.noLaneChangeDistance = noLaneChangeDistance;
    }

    /**
     * Sets whether GTUs will change lane instantaneously.
     * @param instantaneous boolean; whether GTUs will change lane instantaneously
     */
    public void setInstantaneousLaneChange(final boolean instantaneous)
    {
        this.instantaneousLaneChange = instantaneous;
    }

    /**
     * Sets the GTU error handler.
     * @param gtuErrorHandler GTUErrorHandler; GTU error handler
     */
    public void setErrorHandler(final GtuErrorHandler gtuErrorHandler)
    {
        this.errorHandler = gtuErrorHandler;
    }

    /**
     * Generate the characteristics of the next GTU.
     * @throws ProbabilityException when something is wrongly defined in the LaneBasedTemplateGTUType
     * @throws SimRuntimeException when this method fails to re-schedule itself or the call to the method that tries to place a
     *             GTU on the road
     * @throws ParameterException in case of a parameter problem
     * @throws GtuException if strategical planner cannot generate a plan
     */
    @SuppressWarnings("unused")
    private void generateCharacteristics() throws ProbabilityException, SimRuntimeException, ParameterException, GtuException
    {
        synchronized (this.unplacedTemplates)
        {
            LaneBasedGtuCharacteristics characteristics = this.laneBasedGtuCharacteristicsGenerator.draw();
            GtuType gtuType = characteristics.getGtuType();
            // gather information on number of unplaced templates per lane, and per link, for the drawing of a new position
            Map<CrossSectionLink, Map<Integer, Integer>> unplaced = new LinkedHashMap<>();
            for (CrossSectionLink link : this.unplacedTemplates.keySet())
            {
                Map<Integer, Integer> linkMap = new LinkedHashMap<>();
                Map<GeneratorLanePosition, Queue<TimeStampedObject<LaneBasedGtuCharacteristics>>> linkTemplates =
                        this.unplacedTemplates.get(link);
                for (GeneratorLanePosition lanePosition : linkTemplates.keySet())
                {
                    linkMap.put(lanePosition.getLaneNumber(), linkTemplates.get(lanePosition).size());
                }
                unplaced.put(link, linkMap);
            }
            // position draw
            GeneratorLanePosition lanePosition = this.generatorPositions.draw(gtuType, characteristics, unplaced);

            // skip if disabled at this lane-direction
            Set<Lane> lanes = new LinkedHashSet<>();
            for (LanePosition pos : lanePosition.getPosition())
            {
                lanes.add(pos.getLane());
            }
            if (Collections.disjoint(this.disabled, lanes))
            {
                queueGtu(lanePosition, characteristics);
            }

        }
        Duration headway = this.interarrivelTimeGenerator.draw();
        if (headway != null)
        {
            this.simulator.scheduleEventRel(headway, this, "generateCharacteristics", new Object[] {});
        }
    }

    /**
     * Check if the queue is non-empty and, if it is, try to place the GTUs in the queue on the road.
     * @param position GeneratorLanePosition; position
     * @throws SimRuntimeException should never happen
     * @throws GtuException when something wrong in the definition of the GTU
     * @throws OtsGeometryException when something is wrong in the definition of the GTU
     * @throws NetworkException when something is wrong with the initial location of the GTU
     * @throws NamingException ???
     * @throws ProbabilityException pe
     */
    @SuppressWarnings("unused")
    private void tryToPlaceGTU(final GeneratorLanePosition position) throws SimRuntimeException, GtuException, NamingException,
            NetworkException, OtsGeometryException, ProbabilityException
    {
        TimeStampedObject<LaneBasedGtuCharacteristics> timedCharacteristics;
        Queue<TimeStampedObject<LaneBasedGtuCharacteristics>> queue =
                this.unplacedTemplates.get(position.getLink()).get(position);

        synchronized (queue)
        {
            timedCharacteristics = queue.peek();
        }
        if (null == timedCharacteristics)
        {
            return; // Do not re-schedule this method
        }

        LaneBasedGtuCharacteristics characteristics = timedCharacteristics.getObject();
        SortedSet<HeadwayGtu> leaders = new TreeSet<>();
        for (LanePosition dirPos : position.getPosition())
        {
            getFirstLeaders(dirPos.getLane(), dirPos.getPosition().neg().minus(characteristics.getFront()),
                    dirPos.getPosition(), leaders);
        }
        Duration since = this.simulator.getSimulatorAbsTime().minus(timedCharacteristics.getTimestamp());
        Placement placement = this.roomChecker.canPlace(leaders, characteristics, since, position.getPosition());
        if (placement.canPlace())
        {
            // There is enough room; remove the template from the queue and construct the new GTU
            synchronized (queue)
            {
                queue.remove();
            }
            placeGtu(characteristics, placement.getPosition(), placement.getSpeed());
            if (queue.size() > 0)
            {
                this.simulator.scheduleEventNow(this, "tryToPlaceGTU", new Object[] {position});
            }
        }
        else if (queue.size() > 0)
        {
            this.simulator.scheduleEventRel(this.reTryInterval, this, "tryToPlaceGTU", new Object[] {position});
        }
    }

    /**
     * Adds a GTU to the generation queue. This method ignores whether vehicle generation is enabled at the location. This
     * allows an external party to govern (over some time) what vehicles are generated.
     * @param characteristics LaneBasedGtuCharacteristics; characteristics of GTU to add to the queue
     * @param position Set&lt;Lane&gt;; position to generate the GTU at
     */
    public final void queueGtu(final LaneBasedGtuCharacteristics characteristics, final Set<Lane> position)
    {
        // first find the correct GeneratorLanePosition
        GeneratorLanePosition genPosition = null;
        Set<Lane> genSet = new LinkedHashSet<>();
        for (GeneratorLanePosition lanePosition : this.generatorPositions.getAllPositions())
        {
            for (LanePosition dirPos : lanePosition.getPosition())
            {
                genSet.add(dirPos.getLane());
            }
            if (genSet.equals(position))
            {
                genPosition = lanePosition;
                break;
            }
            genSet.clear();
        }
        Throw.when(genPosition == null, IllegalStateException.class, "Position %s is not part of the generation.", position);
        try
        {
            queueGtu(genPosition, characteristics);
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Unexpected exception while scheduling tryToPlace event.", exception);
        }
    }

    /**
     * Places the characteristics in the queue pertaining to the position, and schedules a call to {@code tryToPlace} now if the
     * queue length is 1.
     * @param lanePosition GeneratorLanePosition; position to generate the GTU at
     * @param characteristics LaneBasedGtuCharacteristics; characteristics of GTU to add to the queue
     * @throws SimRuntimeException when an event is scheduled in the past
     */
    private void queueGtu(final GeneratorLanePosition lanePosition, final LaneBasedGtuCharacteristics characteristics)
            throws SimRuntimeException
    {
        if (!this.unplacedTemplates.containsKey(lanePosition.getLink()))
        {
            this.unplacedTemplates.put(lanePosition.getLink(), new LinkedHashMap<>());
        }
        Map<GeneratorLanePosition, Queue<TimeStampedObject<LaneBasedGtuCharacteristics>>> linkMap =
                this.unplacedTemplates.get(lanePosition.getLink());
        if (!linkMap.containsKey(lanePosition))
        {
            linkMap.put(lanePosition, new LinkedList<>());
        }
        Queue<TimeStampedObject<LaneBasedGtuCharacteristics>> queue = linkMap.get(lanePosition);
        queue.add(new TimeStampedObject<>(characteristics, this.simulator.getSimulatorAbsTime()));
        if (queue.size() == 1)
        {
            this.simulator.scheduleEventNow(this, "tryToPlaceGTU", new Object[] {lanePosition});
        }
    }

    /**
     * Places a GTU, regardless of whether it has room. The user of this method should verify this is the case.
     * @param characteristics LaneBasedGtuCharacteristics; characteristics
     * @param position Set&lt;LanePosition&gt;; position
     * @param speed Speed; speed
     * @throws NamingException on exception
     * @throws GtuException on exception
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     * @throws OtsGeometryException on exception
     */
    public final void placeGtu(final LaneBasedGtuCharacteristics characteristics, final Set<LanePosition> position,
            final Speed speed) throws NamingException, GtuException, NetworkException, SimRuntimeException, OtsGeometryException
    {
        String gtuId = this.idGenerator.get();
        LaneBasedGtu gtu = new LaneBasedGtu(gtuId, characteristics.getGtuType(), characteristics.getLength(),
                characteristics.getWidth(), characteristics.getMaximumSpeed(), characteristics.getFront(), this.network);
        gtu.setMaximumAcceleration(characteristics.getMaximumAcceleration());
        gtu.setMaximumDeceleration(characteristics.getMaximumDeceleration());
        gtu.setVehicleModel(characteristics.getVehicleModel());
        gtu.setNoLaneChangeDistance(this.noLaneChangeDistance);
        gtu.setInstantaneousLaneChange(this.instantaneousLaneChange);
        gtu.setErrorHandler(this.errorHandler);
        gtu.init(characteristics.getStrategicalPlannerFactory().create(gtu, characteristics.getRoute(),
                characteristics.getOrigin(), characteristics.getDestination()), position, speed);
        this.generatedGTUs++;
        fireEvent(GTU_GENERATED_EVENT, gtu);
    }

    /**
     * Adds the first GTU on the lane to the set, or any number or leaders on downstream lane(s) if there is no GTU on the lane.
     * @param lane Lane; lane to search on
     * @param startDistance Length; distance from generator location (nose) to start of the lane
     * @param beyond Length; location to search downstream of which is the generator position, or the start for downstream lanes
     * @param set Set&lt;HeadwayGtu&gt;; set to add the GTU's to
     * @throws GtuException if a GTU is incorrectly positioned on a lane
     */
    private void getFirstLeaders(final Lane lane, final Length startDistance, final Length beyond, final Set<HeadwayGtu> set)
            throws GtuException
    {
        LaneBasedGtu next = lane.getGtuAhead(beyond, RelativePosition.FRONT, this.simulator.getSimulatorAbsTime());
        if (next != null)
        {
            Length headway = startDistance.plus(next.position(lane, next.getRear()));
            if (headway.si < 300)
            {
                set.add(new HeadwayGtuReal(next, headway, true));
            }
            return;
        }
        Set<Lane> downstreamLanes = lane.nextLanes(null);
        for (Lane downstreamLane : downstreamLanes)
        {
            Length startDistanceDownstream = startDistance.plus(lane.getLength());
            if (startDistanceDownstream.si > 300)
            {
                return;
            }
            Length beyondDownstream = Length.ZERO;
            getFirstLeaders(downstreamLane, startDistanceDownstream, beyondDownstream, set);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedGtuGenerator " + this.id + " on " + this.generatorPositions.getAllPositions();
    }

    /**
     * @return generatedGTUs.
     */
    public final long getGeneratedGTUs()
    {
        return this.generatedGTUs;
    }

    /**
     * Retrieve the id of this LaneBasedGtuGenerator.
     * @return String; the id of this LaneBasedGtuGenerator
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Disable the vehicle generator during the specific time. Underlying processes such as drawing characteristics and headways
     * are continued, but simply will not result in the queuing of the GTU.
     * @param start Time; start time
     * @param end Time; end time
     * @param laneDirections Set&lt;Lane&gt;; lanes to disable generation on
     * @throws SimRuntimeException if time is incorrect
     */
    public void disable(final Time start, final Time end, final Set<Lane> laneDirections) throws SimRuntimeException
    {
        Throw.when(end.lt(start), SimRuntimeException.class, "End time %s is before start time %s.", end, start);
        this.simulator.scheduleEventAbsTime(start, this, "disable", new Object[] {laneDirections});
        this.simulator.scheduleEventAbsTime(end, this, "enable", new Object[0]);
    }

    /**
     * Disables the generator.
     * @param laneDirections Set&lt;Lane&gt;; lanes to disable generation on
     */
    @SuppressWarnings("unused")
    private void disable(final Set<Lane> laneDirections)
    {
        Throw.when(this.disabled != null && !this.disabled.isEmpty(), IllegalStateException.class,
                "Disabling a generator that is already disabled is not allowed.");
        this.disabled = laneDirections;
    }

    /**
     * Enables the generator.
     */
    @SuppressWarnings("unused")
    private void enable()
    {
        this.disabled = new LinkedHashSet<>();
    }

    /** {@inheritDoc} */
    @Override
    public String getFullId()
    {
        return this.uniqueId;
    }

    /** {@inheritDoc} */
    @Override
    public Set<GtuGeneratorPosition> getPositions()
    {
        Set<GtuGeneratorPosition> set = new LinkedHashSet<>();
        for (GeneratorLanePosition lanePosition : this.generatorPositions.getAllPositions())
        {
            DirectedPoint p = lanePosition.getPosition().iterator().next().getLocation();
            set.add(new GtuGeneratorPosition()
            {
                /** {@inheritDoc} */
                @Override
                public Point<?> getLocation() throws RemoteException
                {
                    return p;
                }

                /** {@inheritDoc} */
                @Override
                public Bounds<?, ?, ?> getBounds() throws RemoteException
                {
                    return new Bounds2d(-2.0, 2.0, -2.0, 2.0);
                }

                /** {@inheritDoc} */
                @Override
                public int getQueueCount()
                {
                    return getQueueLength(lanePosition);
                }
            });
        }
        return set;
    }

    /**
     * Returns the number of GTUs in queue at the position.
     * @param position GeneratorLanePosition; position.
     * @return int; number of GTUs in queue at the position.
     */
    private int getQueueLength(final GeneratorLanePosition position)
    {
        for (CrossSectionLink link : this.unplacedTemplates.keySet())
        {
            for (GeneratorLanePosition lanePosition : this.unplacedTemplates.get(link).keySet())
            {
                if (lanePosition.equals(position))
                {
                    return this.unplacedTemplates.get(link).get(lanePosition).size();
                }
            }
        }
        return 0;
    }

    /**
     * Interface for class that checks that there is sufficient room for a proposed new GTU and returns the maximum safe speed
     * and position for the proposed new GTU.
     */
    public interface RoomChecker
    {
        /**
         * Return the maximum safe speed and position for a new GTU with the specified characteristics. Returns
         * {@code Placement.NO} if there is no safe speed and position. This method might be called with an empty leader set
         * such that the desired speed can be implemented.
         * @param leaders SortedSet&lt;HeadwayGtu&gt;; leaders, usually 1, possibly more after a branch
         * @param characteristics LaneBasedGtuCharacteristics; characteristics of the proposed new GTU
         * @param since Duration; time since the GTU wanted to arrive
         * @param initialPosition Set&lt;LanePosition&gt;; initial position
         * @return Speed; maximum safe speed, or null if a GTU with the specified characteristics cannot be placed at the
         *         current time
         * @throws NetworkException this method may throw a NetworkException if it encounters an error in the network structure
         * @throws GtuException on parameter exception
         */
        Placement canPlace(SortedSet<HeadwayGtu> leaders, LaneBasedGtuCharacteristics characteristics, Duration since,
                Set<LanePosition> initialPosition) throws NetworkException, GtuException;
    }

    /**
     * Placement contains the information that a {@code RoomChecker} returns.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static final class Placement
    {

        /** Value if the GTU cannot be placed. */
        public static final Placement NO = new Placement();

        /** Speed. */
        private final Speed speed;

        /** Position. */
        private final Set<LanePosition> position;

        /**
         * Constructor for NO.
         */
        private Placement()
        {
            this.speed = null;
            this.position = null;
        }

        /**
         * Constructor.
         * @param speed Speed; speed
         * @param position Set&lt;LanePosition&gt;; position
         */
        public Placement(final Speed speed, final Set<LanePosition> position)
        {
            Throw.whenNull(speed, "Speed may not be null. Use Placement.NO if the GTU cannot be placed.");
            Throw.whenNull(position, "Position may not be null. Use Placement.NO if the GTU cannot be placed.");
            this.speed = speed;
            this.position = position;
        }

        /**
         * Returns whether the GTU can be placed.
         * @return whether the GTU can be placed
         */
        public boolean canPlace()
        {
            return this.speed != null && this.position != null;
        }

        /**
         * Returns the speed.
         * @return Speed; speed
         */
        public Speed getSpeed()
        {
            return this.speed;
        }

        /**
         * Returns the position.
         * @return Set&lt;LanePosition&gt;; position
         */
        public Set<LanePosition> getPosition()
        {
            return this.position;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "Placement [speed=" + this.speed + ", position=" + this.position + "]";
        }

    }

}
