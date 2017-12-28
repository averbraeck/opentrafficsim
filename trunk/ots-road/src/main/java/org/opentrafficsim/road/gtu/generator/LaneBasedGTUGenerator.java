package org.opentrafficsim.road.gtu.generator;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.GeneratorLanePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Lane based GTU generator. This generator generates lane based GTUs using a LaneBasedTemplateGTUType. The template is used to
 * generate a set of GTU characteristics at the times implied by the headway generator. These sets are queued until there is
 * sufficient room to construct a GTU at the specified lane locations. The speed of a construction GTU may be reduced to ensure
 * it does not run into its immediate leader GTU.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedGTUGenerator implements Serializable, Identifiable, Locatable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** FIFO for templates that have not been generated yet due to insufficient room/headway, per position, and per link. */
    private final Map<CrossSectionLink, Map<GeneratorLanePosition, Queue<LaneBasedGTUCharacteristics>>> unplacedTemplates =
            new HashMap<>();

    /** Name of the GTU generator. */
    private final String id;

    /** Time distribution that determines the interval times between GTUs. */
    private final Generator<Duration> interarrivelTimeGenerator;

    /** Generates most properties of the GTUs. */
    private final LaneBasedGTUCharacteristicsGenerator laneBasedGTUCharacteristicsGenerator;

    /** End time of this generator. */
    private final Time endTime;

    /** Maximum number of GTUs to generate. */
    private final long maxGTUs;

    /** Total number of GTUs generated so far. */
    private long generatedGTUs = 0;

    /** Retry interval for checking if a GTU can be placed. */
    private Duration reTryInterval = new Duration(0.1, DurationUnit.SI);

    /** Location and initial direction provider for all generated GTUs. */
    private final GeneratorPositions generatorPositions;

    /** Network. */
    private final OTSNetwork network;

    /** Simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** The way that this generator checks if it is safe to construct and place the next lane based GTU. */
    private final RoomChecker roomChecker;

    /** The GTU colorer that will be linked to each generated GTU. */
    private final GTUColorer gtuColorer;

    /** ID generator. */
    private final IdGenerator idGenerator;

    /**
     * Construct a new lane base GTU generator.
     * @param id String; name of the new GTU generator
     * @param interarrivelTimeGenerator Generator&lt;Duration&gt;; generator for the interval times between GTUs
     * @param maxGTUs long; maximum number of GTUs to generate
     * @param startTime Time; time at which the first GTU will be generated
     * @param endTime Time; time after which no more GTUs will be generated
     * @param gtuColorer GTUColorer; the GTU colorer that will be used by all generated GTUs
     * @param laneBasedGTUCharacteristicsGenerator LaneBasedGTUCharacteristicsGenerator; generator of the characteristics of
     *            each GTU
     * @param generatorPositions GeneratorPositions; location and initial direction provider for all generated GTUs
     * @param network OTSNetwork; the OTS network that owns the generated GTUs
     * @param simulator OTSDEVSSimulatorInterface; simulator
     * @param roomChecker LaneBasedGTUGenerator.RoomChecker; the way that this generator checks that there is sufficient room to
     *            place a new GTU
     * @param idGenerator IdGenerator; id generator
     * @throws SimRuntimeException when <cite>startTime</cite> lies before the current simulation time
     * @throws ProbabilityException pe
     * @throws ParameterException if drawing from the interarrival generator fails
     */
    public LaneBasedGTUGenerator(final String id, final Generator<Duration> interarrivelTimeGenerator, final long maxGTUs,
            final Time startTime, final Time endTime, final GTUColorer gtuColorer,
            final LaneBasedGTUCharacteristicsGenerator laneBasedGTUCharacteristicsGenerator,
            final GeneratorPositions generatorPositions, final OTSNetwork network, final OTSDEVSSimulatorInterface simulator,
            final RoomChecker roomChecker, final IdGenerator idGenerator)
            throws SimRuntimeException, ProbabilityException, ParameterException
    {
        this.id = id;
        this.interarrivelTimeGenerator = interarrivelTimeGenerator;
        this.laneBasedGTUCharacteristicsGenerator = laneBasedGTUCharacteristicsGenerator;
        this.endTime = endTime;
        this.maxGTUs = maxGTUs;
        this.generatorPositions = generatorPositions;
        this.network = network;
        this.simulator = simulator;
        this.roomChecker = roomChecker;
        this.gtuColorer = gtuColorer;
        this.idGenerator = idGenerator;
        simulator.scheduleEventAbs(startTime.plus(this.interarrivelTimeGenerator.draw()), this, this, "generateCharacteristics",
                new Object[] {});
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
        if (this.generatedGTUs >= this.maxGTUs || this.simulator.getSimulatorTime().get().ge(this.endTime))
        {
            return; // Do not reschedule
        }
        synchronized (this.unplacedTemplates)
        {
            this.generatedGTUs++;
            LaneBasedGTUCharacteristics characteristics = this.laneBasedGTUCharacteristicsGenerator.draw();
            GTUType gtuType = characteristics.getGTUType();
            // gather information on number of unplaced templates per lane, and per link, for the drawing of a new position
            Map<CrossSectionLink, Map<Integer, Integer>> unplaced = new HashMap<>();
            for (CrossSectionLink link : this.unplacedTemplates.keySet())
            {
                Map<Integer, Integer> linkMap = new HashMap<>();
                Map<GeneratorLanePosition, Queue<LaneBasedGTUCharacteristics>> linkTemplates = this.unplacedTemplates.get(link);
                for (GeneratorLanePosition lanePosition : linkTemplates.keySet())
                {
                    linkMap.put(lanePosition.getLaneNumber(), linkTemplates.get(lanePosition).size());
                }
                unplaced.put(link, linkMap);
            }
            // position draw
            GeneratorLanePosition lanePosition = this.generatorPositions.draw(gtuType, unplaced);
            // add template in the right map location
            if (!this.unplacedTemplates.containsKey(lanePosition.getLink()))
            {
                this.unplacedTemplates.put(lanePosition.getLink(), new HashMap<>());
            }
            Map<GeneratorLanePosition, Queue<LaneBasedGTUCharacteristics>> linkMap =
                    this.unplacedTemplates.get(lanePosition.getLink());
            if (!linkMap.containsKey(lanePosition))
            {
                linkMap.put(lanePosition, new LinkedList<>());
            }
            Queue<LaneBasedGTUCharacteristics> queue = linkMap.get(lanePosition);
            queue.add(characteristics);
            if (queue.size() == 1)
            {
                this.simulator.scheduleEventNow(this, this, "tryToPlaceGTU", new Object[] { lanePosition });
            }
        }
        if (this.generatedGTUs < this.maxGTUs)
        {
            Duration headway = this.interarrivelTimeGenerator.draw();
            if (headway != null)
            {
                this.simulator.scheduleEventRel(headway, this, this, "generateCharacteristics", new Object[] {});
            }
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
        // System.out.println("entered tryToPlaceGTU");
        LaneBasedGTUCharacteristics characteristics;
        Queue<LaneBasedGTUCharacteristics> queue = this.unplacedTemplates.get(position.getLink()).get(position);
        synchronized (queue)
        {
            characteristics = queue.peek();
        }
        if (null == characteristics)
        {
            return; // Do not re-schedule this method
        }
        Length shortestHeadway = new Length(Double.MAX_VALUE, LengthUnit.SI);
        Speed leaderSpeed = null;
        // TODO ALL? we need to stop for all gtus...
        GTUType gtuType = characteristics.getGTUType();
        for (DirectedLanePosition dlp : position.getPosition())
        {
            Lane lane = dlp.getLane();
            // GOTCHA look for the first GTU with FRONT after the start position (not REAR)
            LaneBasedGTU leader = lane.getGtuAhead(dlp.getPosition(), dlp.getGtuDirection(), RelativePosition.FRONT,
                    this.simulator.getSimulatorTime().getTime());
            // no leader on current lane, but lane may be short
            Length headway = Length.ZERO;
            GTUDirectionality leaderDir = dlp.getGtuDirection();
            // TODO look beyond splitting lane
            while (leader == null && (leaderDir.isPlus() && lane.nextLanes(gtuType).size() == 1)
                    || (leaderDir.isMinus() && lane.prevLanes(gtuType).size() == 1) && headway.si < 300)
            {
                headway = headway.plus(lane.getLength());
                if (leaderDir.isPlus())
                {
                    leaderDir = lane.nextLanes(gtuType).values().iterator().next();
                    lane = lane.nextLanes(gtuType).keySet().iterator().next();
                }
                else
                {
                    leaderDir = lane.prevLanes(gtuType).values().iterator().next();
                    lane = lane.prevLanes(gtuType).keySet().iterator().next();
                }
                leader = lane.getGtuAhead(Length.ZERO, leaderDir, RelativePosition.FRONT,
                        this.simulator.getSimulatorTime().getTime());
            }
            if (null != leader)
            {
                Length egoPos =
                        dlp.getGtuDirection().isPlus() ? dlp.getPosition() : dlp.getLane().getLength().minus(dlp.getPosition());
                Length leaderPos = leaderDir.isPlus() ? leader.position(lane, leader.getRear())
                        : lane.getLength().minus(leader.position(lane, leader.getRear()));
                headway = headway.plus(leaderPos.minus(egoPos));
                // What? Allow accident?
                // if (headway.si < 0)
                // {
                // headway = new Length(Math.abs(headway.si), headway.getUnit());
                // }
                // TODO this is a hack, what if the reference position is not the middle?
                headway = new Length(headway.si - characteristics.getLength().si / 2, LengthUnit.SI);
                if (shortestHeadway.gt(headway))
                {
                    shortestHeadway = headway;
                    leaderSpeed = leader.getSpeed();
                }
            }
        }
        if (shortestHeadway.si > 0)
        {
            Speed safeSpeed = characteristics.getMaximumSpeed();
            for (DirectedLanePosition lanePos : position.getPosition())
            {
                if (lanePos.getLane().getLaneType().isCompatible(gtuType, lanePos.getGtuDirection()))
                {
                    Speed speedLimit = lanePos.getLane().getSpeedLimit(gtuType);
                    if (speedLimit.lt(safeSpeed))
                    {
                        safeSpeed = speedLimit;
                    }
                }
            }
            if (null != leaderSpeed)
            {
                safeSpeed = this.roomChecker.canPlace(leaderSpeed, shortestHeadway, characteristics, position.getPosition());
            }
            if (null != safeSpeed)
            {
                // There is enough room; remove the template from the queue and construct the new GTU
                synchronized (queue)
                {
                    queue.remove();
                }
                if (safeSpeed.gt(characteristics.getMaximumSpeed()))
                {
                    safeSpeed = characteristics.getMaximumSpeed();
                }
                String gtuId = this.idGenerator.nextId();
                LaneBasedIndividualGTU gtu =
                        new LaneBasedIndividualGTU(gtuId, characteristics.getGTUType(), characteristics.getLength(),
                                characteristics.getWidth(), characteristics.getMaximumSpeed(), this.simulator, this.network);
                gtu.setMaximumAcceleration(new Acceleration(3.0, AccelerationUnit.METER_PER_SECOND_2));
                gtu.setMaximumDeceleration(new Acceleration(-8.0, AccelerationUnit.METER_PER_SECOND_2));
                gtu.initWithAnimation(characteristics.getStrategicalPlannerFactory().create(gtu, characteristics.getRoute()),
                        position.getPosition(), safeSpeed, DefaultCarAnimation.class, this.gtuColorer);
            }
        }
        int queueLength = queue.size();
        if (queueLength > 0)
        {
            this.simulator.scheduleEventRel(this.reTryInterval, this, this, "tryToPlaceGTU", new Object[] { position });
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
     * @param generatedGTUs set generatedGTUs.
     */
    public final void setGeneratedGTUs(final long generatedGTUs)
    {
        this.generatedGTUs = generatedGTUs;
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
     * Retrieve the end time of this LaneBasedGTUGenerator.
     * @return Time; the time after which this LaneBasedGTUGenerator will not generate any more GTUs
     */
    public final Time getEndTime()
    {
        return this.endTime;
    }

    /**
     * Retrieve the maximum number of GTUs to generate.
     * @return long; once this number of GTUS is generated, this LaneBasedGTUGenerator will stop generating any more GTUs
     */
    public final long getMaxGTUs()
    {
        return this.maxGTUs;
    }

    /**
     * Retrieve the GTUColorer that this LaneBasedGTUGenerator assigns to all generated GTUs.
     * @return GtuColorer; the GTUColorer that this LaneBasedGTUGenerator assigns to all generated GTUs
     */
    public final GTUColorer getGtuColorer()
    {
        return this.gtuColorer;
    }

    /**
     * Interface for class that checks that there is sufficient room for a proposed new GTU and returns the maximum safe speed
     * for the proposed new GTU.
     */
    public interface RoomChecker
    {
        /**
         * Return the maximum safe speed for a new GTU with the specified characteristics. Return null if there is no safe
         * speed. This method will never be called if the newly proposed GTU overlaps with the leader. Nor will this method be
         * called if there is no leader.
         * @param leaderSpeed Speed; speed of the nearest leader
         * @param headway Length; net distance to the nearest leader (always &gt; 0)
         * @param laneBasedGTUCharacteristics LaneBasedGTUCharacteristics; characteristics of the proposed new GTU
         * @param initialPosition Set&lt;DirectedLanePosition&gt;; initial position
         * @return Speed; maximum safe speed, or null if a GTU with the specified characteristics cannot be placed at the
         *         current time
         * @throws NetworkException this method may throw a NetworkException if it encounters an error in the network structure
         */
        Speed canPlace(Speed leaderSpeed, Length headway, LaneBasedGTUCharacteristics laneBasedGTUCharacteristics,
                Set<DirectedLanePosition> initialPosition) throws NetworkException;
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

    /**
     * Returns the number of vehicles in queue per included position for animation.
     * @return number of vehicles in queue per included position for animation
     */
    public Map<GeneratorLanePosition, Integer> getQueueLengths()
    {
        Map<GeneratorLanePosition, Integer> result = new HashMap<>();
        for (CrossSectionLink link : this.unplacedTemplates.keySet())
        {
            for (GeneratorLanePosition lanePosition : this.unplacedTemplates.get(link).keySet())
            {
                result.put(lanePosition, this.unplacedTemplates.get(link).get(lanePosition).size());
            }
        }
        for (GeneratorLanePosition lanePosition : this.generatorPositions.getAllPositions())
        {
            if (!result.containsKey(lanePosition))
            {
                result.put(lanePosition, 0);
            }
        }
        return result;
    }

}
