package org.opentrafficsim.road.gtu.generator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
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
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
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
import org.opentrafficsim.road.gtu.lane.LaneBookkeeping;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics.Overlap;
import org.opentrafficsim.road.network.RoadNetwork;
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
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneBasedGtuGenerator extends LocalEventProducer implements GtuGenerator
{
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
    private final Supplier<Duration> interarrivelTimeGenerator;

    /** Generates most properties of the GTUs. */
    private final LaneBasedGtuCharacteristicsGenerator laneBasedGtuCharacteristicsGenerator;

    /** Total number of GTUs generated so far. */
    private long generatedGTUs = 0;

    /** Retry interval for checking if a GTU can be placed. */
    private Duration reTryInterval = new Duration(0.1, DurationUnit.SI);

    /** Location provider for all generated GTUs. */
    private final GeneratorPositions generatorPositions;

    /** Cached view objects to expose information on generator queue's. */
    private Set<GtuGeneratorPosition> positions;

    /** Network. */
    private final RoadNetwork network;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** The way that this generator checks if it is safe to construct and place the next lane based GTU. */
    private final RoomChecker roomChecker;

    /** ID generator. */
    private final Supplier<String> idGenerator;

    /** Initial distance over which lane changes shouldn't be performed. */
    private Length noLaneChangeDistance = null;

    /** Lane bookkeeping. */
    private LaneBookkeeping bookkeeping = LaneBookkeeping.EDGE;

    /** GTU error handler. */
    private GtuErrorHandler errorHandler = GtuErrorHandler.THROW;

    /** Vehicle generation is ignored on these lanes. */
    private Set<Lane> disabled = new LinkedHashSet<>();

    /** Order of GTU ids. Default is in order of successful generation. Otherwise its in order of characteristics drawing. */
    private boolean idsInCharacteristicsOrder = false;

    /** Map of ids drawn at time of GTU characteristics drawing, if idsInCharacteristicsOrder = true. */
    private Map<LaneBasedGtuCharacteristics, String> unplacedIds = null;

    /** This enables to check whether idsInCharacteristicsOrder can still be set. */
    private boolean firstCharacteristicsDrawn = false;

    /**
     * Construct a new lane base GTU generator. If the ID generator is an instance of IdsWithCharacteristics and its
     * {@code hasIds()} method returns true, IDs are assigned in order of GTU characteristics.
     * @param id name of the new GTU generator
     * @param interarrivelTimeGenerator generator for the interval times between GTUs
     * @param laneBasedGtuCharacteristicsGenerator generator of the characteristics of each GTU
     * @param generatorPositions location and initial direction provider for all generated GTUs
     * @param network the OTS network that owns the generated GTUs
     * @param simulator simulator
     * @param roomChecker the way that this generator checks that there is sufficient room to place a new GTU
     * @param idGenerator id generator
     * @throws SimRuntimeException when <cite>startTime</cite> lies before the current simulation time
     * @throws ParameterException if drawing from the interarrival generator fails
     * @throws NetworkException if the object could not be added to the network
     */
    @SuppressWarnings("parameternumber")
    public LaneBasedGtuGenerator(final String id, final Supplier<Duration> interarrivelTimeGenerator,
            final LaneBasedGtuCharacteristicsGenerator laneBasedGtuCharacteristicsGenerator,
            final GeneratorPositions generatorPositions, final RoadNetwork network, final OtsSimulatorInterface simulator,
            final RoomChecker roomChecker, final Supplier<String> idGenerator)
            throws SimRuntimeException, ParameterException, NetworkException
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
        Duration headway = this.interarrivelTimeGenerator.get();
        if (headway != null) // otherwise no demand at all
        {

            simulator.scheduleEventRel(headway,
                    () -> Try.execute(() -> generateCharacteristics(), "Exception generating characteristics."));
        }
        this.network.addNonLocatedObject(this);
        if (this.idGenerator instanceof IdsWithCharacteristics ids && ids.hasIds())
        {
            setIdsInCharacteristicsOrder(true); // also creates the unplaced ids map
        }
    }

    /**
     * Sets the initial distance over which lane changes shouldn't be performed.
     * @param noLaneChangeDistance initial distance over which lane changes shouldn't be performed
     */
    public void setNoLaneChangeDistance(final Length noLaneChangeDistance)
    {
        this.noLaneChangeDistance = noLaneChangeDistance;
    }

    /**
     * Sets how lane bookkeeping at lane changes is done.
     * @param bookkeeping how lane bookkeeping at lane changes is done
     */
    public void setBookkeeping(final LaneBookkeeping bookkeeping)
    {
        this.bookkeeping = bookkeeping;
    }

    /**
     * Sets the GTU error handler.
     * @param gtuErrorHandler GTU error handler
     */
    public void setErrorHandler(final GtuErrorHandler gtuErrorHandler)
    {
        this.errorHandler = gtuErrorHandler;
    }

    /**
     * Sets what order should be used for the ids. By default this is in the order of successful GTU generation. If however the
     * id generator is an instance of {@code IdsWithCharacteristics} returning true for {@code hasIds()}, it is by default in
     * the order of characteristics drawing.
     * @param idsInCharacteristicsOrder ids in order of drawing characteristics, or successful generation otherwise.
     */
    public void setIdsInCharacteristicsOrder(final boolean idsInCharacteristicsOrder)
    {
        Throw.when(this.firstCharacteristicsDrawn, IllegalStateException.class,
                "Id order cannot be set once GTU characteristics were drawn.");
        this.unplacedIds = new LinkedHashMap<>();
        this.idsInCharacteristicsOrder = idsInCharacteristicsOrder;
    }

    /**
     * Generate the characteristics of the next GTU.
     * @throws SimRuntimeException when this method fails to re-schedule itself or the call to the method that tries to place a
     *             GTU on the road
     * @throws ParameterException in case of a parameter problem
     * @throws GtuException if strategical planner cannot generate a plan
     */
    @SuppressWarnings("unused")
    private void generateCharacteristics() throws SimRuntimeException, ParameterException, GtuException
    {
        this.firstCharacteristicsDrawn = true;
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
            if (!this.disabled.contains(lanePosition.getPosition().lane()))
            {
                if (this.idsInCharacteristicsOrder)
                {
                    this.unplacedIds.put(characteristics, this.idGenerator.get());
                }
                queueGtu(lanePosition, characteristics);
            }
        }
        // @docs/02-model-structure/dsol.md#event-based-simulation
        Duration headway = this.interarrivelTimeGenerator.get();
        if (headway != null)
        {
            this.simulator.scheduleEventRel(headway,
                    () -> Try.execute(() -> generateCharacteristics(), "Exception generating characteristics."));
        }
        // @end
    }

    /**
     * Check if the queue is non-empty and, if it is, try to place the GTUs in the queue on the road.
     * @param position position
     * @throws SimRuntimeException should never happen
     * @throws GtuException when something wrong in the definition of the GTU
     * @throws NetworkException when something is wrong with the initial location of the GTU
     * @throws NamingException ???
     */
    @SuppressWarnings("unused")
    private void tryToPlaceGTU(final GeneratorLanePosition position)
            throws SimRuntimeException, GtuException, NamingException, NetworkException
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

        LaneBasedGtuCharacteristics characteristics = timedCharacteristics.object();
        SortedSet<PerceivedGtu> leaders = new TreeSet<>();
        getFirstLeaders(position.getPosition().lane(),
                position.getPosition().position().neg().minus(characteristics.getFront()), position.getPosition().position(),
                leaders);
        Duration since = this.simulator.getSimulatorTime().minus(timedCharacteristics.timestamp());
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
                this.simulator.scheduleEventNow(
                        () -> Try.execute(() -> tryToPlaceGTU(position), "Exception during attempt to place GTU."));
            }
        }
        // @docs/02-model-structure/dsol.md#event-based-simulation (without the 'else')
        else if (queue.size() > 0)
        {
            this.simulator.scheduleEventRel(this.reTryInterval,
                    () -> Try.execute(() -> tryToPlaceGTU(position), "Exception during attempt to place GTU."));
        }
        // @end
    }

    /**
     * Adds a GTU to the generation queue. This method ignores whether vehicle generation is enabled at the location. This
     * allows an external party to govern (over some time) what vehicles are generated.
     * @param characteristics characteristics of GTU to add to the queue
     * @param lane position to generate the GTU at
     */
    public final void queueGtu(final LaneBasedGtuCharacteristics characteristics, final Lane lane)
    {
        // first find the correct GeneratorLanePosition
        GeneratorLanePosition genPosition = null;
        for (GeneratorLanePosition lanePosition : this.generatorPositions.getAllPositions())
        {
            if (lanePosition.getPosition().lane().equals(lane))
            {
                genPosition = lanePosition;
                break;
            }
        }
        Throw.when(genPosition == null, IllegalStateException.class, "Lane %s is not part of the generation.", lane);
        try
        {
            queueGtu(genPosition, characteristics);
        }
        catch (SimRuntimeException exception)
        {
            throw new OtsRuntimeException("Unexpected exception while scheduling tryToPlace event.", exception);
        }
    }

    /**
     * Places the characteristics in the queue pertaining to the position, and schedules a call to {@code tryToPlace} now if the
     * queue length is 1.
     * @param lanePosition position to generate the GTU at
     * @param characteristics characteristics of GTU to add to the queue
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
        queue.add(new TimeStampedObject<>(characteristics, this.simulator.getSimulatorTime()));
        // @docs/02-model-structure/dsol.md#event-based-simulation
        if (queue.size() == 1)
        {
            this.simulator.scheduleEventNow(
                    () -> Try.execute(() -> tryToPlaceGTU(lanePosition), "Exception during attempt to place GTU."));
        }
        // @end
    }

    /**
     * Places a GTU, regardless of whether it has room. The user of this method should verify this is the case.
     * @param characteristics characteristics
     * @param position position
     * @param speed speed
     * @throws NamingException on exception
     * @throws GtuException on exception
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     */
    public final void placeGtu(final LaneBasedGtuCharacteristics characteristics, final LanePosition position,
            final Speed speed) throws NamingException, GtuException, NetworkException, SimRuntimeException
    {
        String gtuId = this.idsInCharacteristicsOrder ? this.unplacedIds.remove(characteristics) : this.idGenerator.get();
        LaneBasedGtu gtu = new LaneBasedGtu(gtuId, characteristics.getGtuType(), characteristics.getLength(),
                characteristics.getWidth(), characteristics.getMaximumSpeed(), characteristics.getFront(), this.network);
        gtu.setMaximumAcceleration(characteristics.getMaximumAcceleration());
        gtu.setMaximumDeceleration(characteristics.getMaximumDeceleration());
        gtu.setVehicleModel(characteristics.getVehicleModel());
        gtu.setNoLaneChangeDistance(this.noLaneChangeDistance);
        gtu.setBookkeeping(this.bookkeeping);
        gtu.setErrorHandler(this.errorHandler);
        gtu.init(characteristics.getStrategicalPlannerFactory().create(gtu, characteristics.getRoute(),
                characteristics.getOrigin(), characteristics.getDestination()), position.getLocation(), speed);
        this.generatedGTUs++;
        fireEvent(GTU_GENERATED_EVENT, gtu);
    }

    /**
     * Adds the first GTU on the lane to the set, or any number or leaders on downstream lane(s) if there is no GTU on the lane.
     * @param lane lane to search on
     * @param startDistance distance from generator location (nose) to start of the lane
     * @param beyond location to search downstream of, which is the generator position, or the lane start for downstream lanes
     * @param set set to add the GTU's to
     * @throws GtuException if a GTU is incorrectly positioned on a lane
     */
    private void getFirstLeaders(final Lane lane, final Length startDistance, final Length beyond, final Set<PerceivedGtu> set)
            throws GtuException
    {
        Optional<LaneBasedGtu> next = lane.getGtuAhead(beyond, RelativePosition.FRONT, this.simulator.getSimulatorTime());
        if (next.isPresent())
        {
            Length headway = startDistance.plus(next.get().getPosition(lane, next.get().getRear()));
            if (headway.si < 300)
            {
                set.add(PerceivedGtu.of(next.get(), new Kinematics.Record(headway, next.get().getSpeed(),
                        next.get().getAcceleration(), true, Overlap.AHEAD)));
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

    @Override
    public final String toString()
    {
        return "LaneBasedGtuGenerator " + this.id + " on " + this.generatorPositions.getAllPositions();
    }

    /**
     * Returns the number of generated GTUs.
     * @return generatedGTUs.
     */
    public final long getGeneratedGTUs()
    {
        return this.generatedGTUs;
    }

    /**
     * Retrieve the id of this LaneBasedGtuGenerator.
     * @return the id of this LaneBasedGtuGenerator
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Disable the vehicle generator during the specific time. Underlying processes such as drawing characteristics and headways
     * are continued, but simply will not result in the queuing of the GTU.
     * @param start start time
     * @param end end time
     * @param lane lane to disable generation on
     * @throws SimRuntimeException if time is incorrect
     */
    public void disable(final Duration start, final Duration end, final Lane lane) throws SimRuntimeException
    {
        Throw.when(end.lt(start), SimRuntimeException.class, "End time %s is before start time %s.", end, start);
        this.simulator.scheduleEventAbs(start, () -> disable(lane));
        this.simulator.scheduleEventAbs(end, () -> enable());
    }

    /**
     * Disables the generator.
     * @param lane lanes to disable generation on
     */
    @SuppressWarnings("unused")
    private void disable(final Lane lane)
    {
        Throw.when(this.disabled != null && !this.disabled.isEmpty(), IllegalStateException.class,
                "Disabling a generator that is already disabled is not allowed.");
        this.disabled.add(lane);
    }

    /**
     * Enables the generator.
     */
    @SuppressWarnings("unused")
    private void enable()
    {
        this.disabled = new LinkedHashSet<>();
    }

    @Override
    public String getFullId()
    {
        return this.uniqueId;
    }

    @Override
    public Set<GtuGeneratorPosition> getPositions()
    {
        if (this.positions == null)
        {
            this.positions = new LinkedHashSet<>();
            for (GeneratorLanePosition lanePosition : this.generatorPositions.getAllPositions())
            {
                LanePosition pos = lanePosition.getPosition();
                DirectedPoint2d p = pos.getLocation();
                this.positions.add(new GtuGeneratorPosition()
                {
                    @Override
                    public DirectedPoint2d getLocation()
                    {
                        return p;
                    }

                    @Override
                    public Bounds2d getRelativeBounds()
                    {
                        return new Bounds2d(0.0, 0.0);
                    }

                    @Override
                    public int getQueueSize()
                    {
                        return LaneBasedGtuGenerator.this.getQueueSize(lanePosition);
                    }

                    @Override
                    public String getId()
                    {
                        return LaneBasedGtuGenerator.this.id + "@" + lanePosition.getLink().getId() + "." + pos.lane().getId();
                    }
                });
            }
        }
        return this.positions;
    }

    /**
     * Returns the number of GTUs in queue at the position.
     * @param position position.
     * @return number of GTUs in queue at the position.
     */
    private int getQueueSize(final GeneratorLanePosition position)
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
         * @param leaders leaders, usually 1, possibly more after a branch
         * @param characteristics characteristics of the proposed new GTU
         * @param since time since the GTU wanted to arrive
         * @param initialPosition initial position
         * @return maximum safe speed, or Placement.NO if a GTU with the specified characteristics cannot be placed at the
         *         current time
         * @throws NetworkException this method may throw a NetworkException if it encounters an error in the network structure
         * @throws GtuException on parameter exception
         */
        Placement canPlace(SortedSet<PerceivedGtu> leaders, LaneBasedGtuCharacteristics characteristics, Duration since,
                LanePosition initialPosition) throws NetworkException, GtuException;
    }

    /**
     * Placement contains the information that a {@code RoomChecker} returns.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static final class Placement
    {

        /** Value if the GTU cannot be placed. */
        public static final Placement NO = new Placement();

        /** Speed. */
        private final Speed speed;

        /** Position. */
        private final LanePosition position;

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
         * @param speed speed
         * @param position position
         */
        public Placement(final Speed speed, final LanePosition position)
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
         * @return speed
         */
        public Speed getSpeed()
        {
            return this.speed;
        }

        /**
         * Returns the position.
         * @return position
         */
        public LanePosition getPosition()
        {
            return this.position;
        }

        @Override
        public String toString()
        {
            return "Placement [speed=" + this.speed + ", position=" + this.position + "]";
        }

    }

    /**
     * Id suppliers that implement this interface can indicate whether they are coupled to GTU characteristics information. In
     * that case the GTU generator will assign the IDs in order of GTU characteristics.
     */
    public interface IdsWithCharacteristics extends Supplier<String>
    {
        /**
         * Returns whether the characteristics include GTU IDs.
         * @return whether the characteristics include GTU IDs
         */
        boolean hasIds();
    }

}
