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

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.GeneratorLanePosition;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUReal;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Lane based GTU generator. This generator generates lane based GTUs using a LaneBasedTemplateGTUType. The template is used to
 * generate a set of GTU characteristics at the times implied by the headway generator. These sets are queued until there is
 * sufficient room to construct a GTU at the specified lane locations. The speed of a construction GTU may be reduced to ensure
 * it does not run into its immediate leader GTU.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedGTUGenerator extends EventProducer implements Serializable, Identifiable, GtuGeneratorQueue
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /**
     * Event of a generated GTU. Payload: LaneBasedIndividualGTU
     */
    public static final EventType GTU_GENERATED_EVENT = new EventType("GENERATOR.GTU_GENERATED");

    /** FIFO for templates that have not been generated yet due to insufficient room/headway, per position, and per link. */
    private final Map<CrossSectionLink, Map<GeneratorLanePosition, Queue<TimeStampedObject<LaneBasedGTUCharacteristics>>>> unplacedTemplates =
            new LinkedHashMap<>();

    /** Name of the GTU generator. */
    private final String id;

    /** Time distribution that determines the interval times between GTUs. */
    private final Generator<Duration> interarrivelTimeGenerator;

    /** Generates most properties of the GTUs. */
    private final LaneBasedGTUCharacteristicsGenerator laneBasedGTUCharacteristicsGenerator;

    /** Total number of GTUs generated so far. */
    private long generatedGTUs = 0;

    /** Retry interval for checking if a GTU can be placed. */
    private Duration reTryInterval = new Duration(0.1, DurationUnit.SI);

    /** Location and initial direction provider for all generated GTUs. */
    private final GeneratorPositions generatorPositions;

    /** Network. */
    private final OTSRoadNetwork network;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** The way that this generator checks if it is safe to construct and place the next lane based GTU. */
    private final RoomChecker roomChecker;

    /** ID generator. */
    private final IdGenerator idGenerator;

    /** Initial distance over which lane changes shouldn't be performed. */
    private Length noLaneChangeDistance = null;

    /** Vehicle generation is ignored on these lanes. */
    private Set<LaneDirection> disabled = new LinkedHashSet<>();

    /**
     * Construct a new lane base GTU generator.
     * @param id String; name of the new GTU generator
     * @param interarrivelTimeGenerator Generator&lt;Duration&gt;; generator for the interval times between GTUs
     * @param laneBasedGTUCharacteristicsGenerator LaneBasedGTUCharacteristicsGenerator; generator of the characteristics of
     *            each GTU
     * @param generatorPositions GeneratorPositions; location and initial direction provider for all generated GTUs
     * @param network OTSRoadNetwork; the OTS network that owns the generated GTUs
     * @param simulator OTSSimulatorInterface; simulator
     * @param roomChecker RoomChecker; the way that this generator checks that there is sufficient room to place a new GTU
     * @param idGenerator IdGenerator; id generator
     * @throws SimRuntimeException when <cite>startTime</cite> lies before the current simulation time
     * @throws ProbabilityException pe
     * @throws ParameterException if drawing from the interarrival generator fails
     */
    @SuppressWarnings("parameternumber")
    public LaneBasedGTUGenerator(final String id, final Generator<Duration> interarrivelTimeGenerator,
            final LaneBasedGTUCharacteristicsGenerator laneBasedGTUCharacteristicsGenerator,
            final GeneratorPositions generatorPositions, final OTSRoadNetwork network, final OTSSimulatorInterface simulator,
            final RoomChecker roomChecker, final IdGenerator idGenerator)
            throws SimRuntimeException, ProbabilityException, ParameterException
    {
        this.id = id;
        this.interarrivelTimeGenerator = interarrivelTimeGenerator;
        this.laneBasedGTUCharacteristicsGenerator = laneBasedGTUCharacteristicsGenerator;
        this.generatorPositions = generatorPositions;
        this.network = network;
        this.simulator = simulator;
        this.roomChecker = roomChecker;
        this.idGenerator = idGenerator;
        Duration headway = this.interarrivelTimeGenerator.draw();
        if (headway != null) // otherwise no demand at all
        {
            simulator.scheduleEventRel(headway, this, this, "generateCharacteristics", new Object[] {});
        }
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
     * Generate the characteristics of the next GTU.
     * @throws ProbabilityException when something is wrongly defined in the LaneBasedTemplateGTUType
     * @throws SimRuntimeException when this method fails to re-schedule itself or the call to the method that tries to place a
     *             GTU on the road
     * @throws ParameterException in case of a parameter problem
     * @throws GTUException if strategical planner cannot generate a plan
     */
    @SuppressWarnings("unused")
    private void generateCharacteristics() throws ProbabilityException, SimRuntimeException, ParameterException, GTUException
    {
        synchronized (this.unplacedTemplates)
        {
            // this.generatedGTUs++;
            LaneBasedGTUCharacteristics characteristics = this.laneBasedGTUCharacteristicsGenerator.draw();
            GTUType gtuType = characteristics.getGTUType();
            // gather information on number of unplaced templates per lane, and per link, for the drawing of a new position
            Map<CrossSectionLink, Map<Integer, Integer>> unplaced = new LinkedHashMap<>();
            for (CrossSectionLink link : this.unplacedTemplates.keySet())
            {
                Map<Integer, Integer> linkMap = new LinkedHashMap<>();
                Map<GeneratorLanePosition, Queue<TimeStampedObject<LaneBasedGTUCharacteristics>>> linkTemplates =
                        this.unplacedTemplates.get(link);
                for (GeneratorLanePosition lanePosition : linkTemplates.keySet())
                {
                    linkMap.put(lanePosition.getLaneNumber(), linkTemplates.get(lanePosition).size());
                }
                unplaced.put(link, linkMap);
            }
            // position draw
            Speed desiredSpeed = characteristics.getStrategicalPlannerFactory().peekDesiredSpeed(gtuType,
                    this.generatorPositions.speedLimit(gtuType), characteristics.getMaximumSpeed());
            GeneratorLanePosition lanePosition =
                    this.generatorPositions.draw(gtuType, unplaced, desiredSpeed, characteristics.getRoute());

            // skip if disabled at this lane-direction
            Set<LaneDirection> lanes = new LinkedHashSet<>();
            for (DirectedLanePosition pos : lanePosition.getPosition())
            {
                lanes.add(pos.getLaneDirection());
            }
            if (Collections.disjoint(this.disabled, lanes))
            {
                queueGtu(lanePosition, characteristics);
            }

        }
        Duration headway = this.interarrivelTimeGenerator.draw();
        if (headway != null)
        {
            this.simulator.scheduleEventRel(headway, this, this, "generateCharacteristics", new Object[] {});
        }
    }

    /**
     * Check if the queue is non-empty and, if it is, try to place the GTUs in the queue on the road.
     * @param position GeneratorLanePosition; position
     * @throws SimRuntimeException should never happen
     * @throws GTUException when something wrong in the definition of the GTU
     * @throws OTSGeometryException when something is wrong in the definition of the GTU
     * @throws NetworkException when something is wrong with the initial location of the GTU
     * @throws NamingException ???
     * @throws ProbabilityException pe
     */
    @SuppressWarnings("unused")
    private void tryToPlaceGTU(final GeneratorLanePosition position) throws SimRuntimeException, GTUException, NamingException,
            NetworkException, OTSGeometryException, ProbabilityException
    {
        TimeStampedObject<LaneBasedGTUCharacteristics> timedCharacteristics;
        Queue<TimeStampedObject<LaneBasedGTUCharacteristics>> queue =
                this.unplacedTemplates.get(position.getLink()).get(position);

        synchronized (queue)
        {
            timedCharacteristics = queue.peek();
        }
        if (null == timedCharacteristics)
        {
            return; // Do not re-schedule this method
        }

        LaneBasedGTUCharacteristics characteristics = timedCharacteristics.getObject();
        SortedSet<HeadwayGTU> leaders = new TreeSet<>();
        for (DirectedLanePosition dirPos : position.getPosition())
        {
            getFirstLeaders(dirPos.getLaneDirection(), dirPos.getPosition().neg().minus(characteristics.getFront()),
                    dirPos.getPosition(), leaders);
        }
        Duration since = this.simulator.getSimulatorTime().minus(timedCharacteristics.getTimestamp());
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
                this.simulator.scheduleEventNow(this, this, "tryToPlaceGTU", new Object[] { position });
            }
        }
        else if (queue.size() > 0)
        {
            this.simulator.scheduleEventRel(this.reTryInterval, this, this, "tryToPlaceGTU", new Object[] { position });
        }
    }

    /**
     * Adds a GTU to the generation queue. This method ignores whether vehicle generation is enabled at the location. This
     * allows an external party to govern (over some time) what vehicles are generated.
     * @param characteristics LaneBasedGTUCharacteristics; characteristics of GTU to add to the queue
     * @param position Set&lt;LaneDirection&gt;; position to generate the GTU at
     */
    public final void queueGtu(final LaneBasedGTUCharacteristics characteristics, final Set<LaneDirection> position)
    {
        // first find the correct GeneratorLanePosition
        GeneratorLanePosition genPosition = null;
        Set<LaneDirection> genSet = new LinkedHashSet<>();
        for (GeneratorLanePosition lanePosition : this.generatorPositions.getAllPositions())
        {
            for (DirectedLanePosition dirPos : lanePosition.getPosition())
            {
                genSet.add(dirPos.getLaneDirection());
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
     * @param characteristics LaneBasedGTUCharacteristics; characteristics of GTU to add to the queue
     * @throws SimRuntimeException when an event is scheduled in the past
     */
    private void queueGtu(final GeneratorLanePosition lanePosition, final LaneBasedGTUCharacteristics characteristics)
            throws SimRuntimeException
    {
        if (!this.unplacedTemplates.containsKey(lanePosition.getLink()))
        {
            this.unplacedTemplates.put(lanePosition.getLink(), new LinkedHashMap<>());
        }
        Map<GeneratorLanePosition, Queue<TimeStampedObject<LaneBasedGTUCharacteristics>>> linkMap =
                this.unplacedTemplates.get(lanePosition.getLink());
        if (!linkMap.containsKey(lanePosition))
        {
            linkMap.put(lanePosition, new LinkedList<>());
        }
        Queue<TimeStampedObject<LaneBasedGTUCharacteristics>> queue = linkMap.get(lanePosition);
        queue.add(new TimeStampedObject<>(characteristics, this.simulator.getSimulatorTime()));
        if (queue.size() == 1)
        {
            this.simulator.scheduleEventNow(this, this, "tryToPlaceGTU", new Object[] { lanePosition });
        }
    }

    /**
     * Places a GTU, regardless of whether it has room. The user of this method should verify this is the case.
     * @param characteristics LaneBasedGTUCharacteristics; characteristics
     * @param position Set&lt;DirectedLanePosition&gt;; position
     * @param speed Speed; speed
     * @throws NamingException on exception
     * @throws GTUException on exception
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     * @throws OTSGeometryException on exception
     */
    public final void placeGtu(final LaneBasedGTUCharacteristics characteristics, final Set<DirectedLanePosition> position,
            final Speed speed) throws NamingException, GTUException, NetworkException, SimRuntimeException, OTSGeometryException
    {
        String gtuId = this.idGenerator.nextId();
        LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU(gtuId, characteristics.getGTUType(),
                characteristics.getLength(), characteristics.getWidth(), characteristics.getMaximumSpeed(),
                characteristics.getFront(), this.simulator, this.network);
        gtu.setMaximumAcceleration(characteristics.getMaximumAcceleration());
        gtu.setMaximumDeceleration(characteristics.getMaximumDeceleration());
        gtu.setVehicleModel(characteristics.getVehicleModel());
        gtu.setNoLaneChangeDistance(this.noLaneChangeDistance);
        gtu.init(characteristics.getStrategicalPlannerFactory().create(gtu, characteristics.getRoute(),
                characteristics.getOrigin(), characteristics.getDestination()), position, speed);
        this.generatedGTUs++;
        fireEvent(GTU_GENERATED_EVENT, gtu);
    }

    /**
     * Adds the first GTU on the lane to the set, or any number or leaders on downstream lane(s) if there is no GTU on the lane.
     * @param lane LaneDirection; lane to search on
     * @param startDistance Length; distance from generator location (nose) to start of the lane
     * @param beyond Length; location to search downstream of which is the generator position, or the start for downstream lanes
     * @param set Set&lt;HeadwayGTU&gt;; set to add the GTU's to
     * @throws GTUException if a GTU is incorrectly positioned on a lane
     */
    private void getFirstLeaders(final LaneDirection lane, final Length startDistance, final Length beyond,
            final Set<HeadwayGTU> set) throws GTUException
    {
        LaneBasedGTU next = lane.getLane().getGtuAhead(beyond, lane.getDirection(), RelativePosition.FRONT,
                this.simulator.getSimulatorTime());
        if (next != null)
        {
            Length headway;
            if (lane.getDirection().isPlus())
            {
                headway = startDistance.plus(next.position(lane.getLane(), next.getRear()));
            }
            else
            {
                headway = startDistance.plus(lane.getLane().getLength().minus(next.position(lane.getLane(), next.getRear())));
            }
            if (headway.si < 300)
            {
                set.add(new HeadwayGTUReal(next, headway, true));
            }
            return;
        }
        ImmutableMap<Lane, GTUDirectionality> downstreamLanes =
                lane.getLane().downstreamLanes(lane.getDirection(), this.network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        for (Lane downstreamLane : downstreamLanes.keySet())
        {
            Length startDistanceDownstream = startDistance.plus(lane.getLane().getLength());
            if (startDistanceDownstream.si > 300)
            {
                return;
            }
            GTUDirectionality dir = downstreamLanes.get(downstreamLane);
            Length beyondDownstream = dir.isPlus() ? Length.ZERO : downstreamLane.getLength();
            getFirstLeaders(new LaneDirection(downstreamLane, dir), startDistanceDownstream, beyondDownstream, set);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedGTUGenerator " + this.id + " on " + this.generatorPositions;
    }

    /**
     * @return generatedGTUs.
     */
    public final long getGeneratedGTUs()
    {
        return this.generatedGTUs;
    }

    /**
     * Retrieve the id of this LaneBasedGTUGenerator.
     * @return String; the id of this LaneBasedGTUGenerator
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
     * @param laneDirections Set&lt;LaneDirection&gt;; lanes to disable generation on
     * @throws SimRuntimeException if time is incorrect
     */
    public void disable(final Time start, final Time end, final Set<LaneDirection> laneDirections) throws SimRuntimeException
    {
        Throw.when(end.lt(start), SimRuntimeException.class, "End time %s is before start time %s.", end, start);
        this.simulator.scheduleEventAbs(start, this, this, "disable", new Object[] { laneDirections });
        this.simulator.scheduleEventAbs(end, this, this, "enable", new Object[0]);
    }

    /**
     * Disables the generator.
     * @param laneDirections Set&lt;LaneDirection&gt;; lanes to disable generation on
     */
    @SuppressWarnings("unused")
    private void disable(final Set<LaneDirection> laneDirections)
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
         * @param leaders SortedSet&lt;HeadwayGTU&gt;; leaders, usually 1, possibly more after a branch
         * @param characteristics LaneBasedGTUCharacteristics; characteristics of the proposed new GTU
         * @param since Duration; time since the GTU wanted to arrive
         * @param initialPosition Set&lt;DirectedLanePosition&gt;; initial position
         * @return Speed; maximum safe speed, or null if a GTU with the specified characteristics cannot be placed at the
         *         current time
         * @throws NetworkException this method may throw a NetworkException if it encounters an error in the network structure
         * @throws GTUException on parameter exception
         */
        Placement canPlace(SortedSet<HeadwayGTU> leaders, LaneBasedGTUCharacteristics characteristics, Duration since,
                Set<DirectedLanePosition> initialPosition) throws NetworkException, GTUException;
    }

    /**
     * Placement contains the information that a {@code RoomChecker} returns.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static final class Placement
    {

        /** Value if the GTU cannot be placed. */
        public static final Placement NO = new Placement();

        /** Speed. */
        private final Speed speed;

        /** Position. */
        private final Set<DirectedLanePosition> position;

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
         * @param position Set&lt;DirectedLanePosition&gt;; position
         */
        public Placement(final Speed speed, final Set<DirectedLanePosition> position)
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
         * @return Set&lt;DirectedLanePosition&gt;; position
         */
        public Set<DirectedLanePosition> getPosition()
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

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return this.generatorPositions.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return this.generatorPositions.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    public Map<DirectedPoint, Integer> getQueueLengths()
    {
        Map<DirectedPoint, Integer> result = new LinkedHashMap<>();
        for (CrossSectionLink link : this.unplacedTemplates.keySet())
        {
            for (GeneratorLanePosition lanePosition : this.unplacedTemplates.get(link).keySet())
            {
                result.put(lanePosition.getPosition().iterator().next().getLocation(),
                        this.unplacedTemplates.get(link).get(lanePosition).size());
            }
        }
        for (GeneratorLanePosition lanePosition : this.generatorPositions.getAllPositions())
        {
            DirectedPoint p = lanePosition.getPosition().iterator().next().getLocation();
            if (!result.containsKey(p))
            {
                result.put(p, 0);
            }
        }
        return result;
    }

}
