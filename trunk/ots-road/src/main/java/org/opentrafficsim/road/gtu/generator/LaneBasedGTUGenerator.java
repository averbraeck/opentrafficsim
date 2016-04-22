package org.opentrafficsim.road.gtu.generator;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Lane based GTU generator. This generator generates lane based GTUs using a LaneBasedTemplateGTUType. The template is used to
 * generate a set of GTU characteristics at the times implied by the headway generator. These sets are queued until there is
 * sufficient room to construct a GTU at the specified lane locations. The speed of a construction GTU may be reduced to ensure
 * it does not run into its immediate leader GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedGTUGenerator implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** FIFO for templates that have not been generated yet due to insufficient room/headway. */
    private final Queue<LaneBasedGTUCharacteristics> unplacedTemplates = new LinkedList<LaneBasedGTUCharacteristics>();

    /** Name of the GTU generator. */
    private final String id;

    /** Time distribution that determines the interval times between GTUs. */
    private final Generator<Time.Rel> interarrivelTimeGenerator;

    /** Generates most properties of the GTUs. */
    private final LaneBasedGTUCharacteristicsGenerator laneBasedGTUCharacteristicsGenerator;

    /** End time of this generator. */
    private final Time.Abs endTime;

    /** Maximum number of GTUs to generate. */
    private final long maxGTUs;

    /** Total number of GTUs generated so far. */
    private long generatedGTUs = 0;

    /** Retry interval for checking if a GTU can be placed. */
    Time.Rel reTryInterval = new Time.Rel(0.1, TimeUnit.SI);

    /** The location and initial direction of the generated GTUs. */
    final Set<DirectedLanePosition> initialLongitudinalPositions;

    /** The way that this generator checks if it is safe to construct and place the next lane based GTU. */
    final RoomChecker roomChecker;

    /** The GTU colorer that will be linked to each generated GTU. */
    final GTUColorer gtuColorer;

    /**
     * Construct a new lane base GTU generator.
     * @param id String; name of the new GTU generator
     * @param interarrivelTimeGenerator Generator&lt;Time.Rel&gt;; generator for the interval times between GTUs
     * @param maxGTUs long; maximum number of GTUs to generate
     * @param startTime Time.Abs; time at which the first GTU will be generated
     * @param endTime Time.Abs; time after which no more GTUs will be generated
     * @param gtuColorer GTUColorer; the GTU colorer that will be used by all generated GTUs
     * @param laneBasedGTUCharacteristicsGenerator LaneBasedGTUCharacteristicsGenerator; generator of the characteristics of
     *            each GTU
     * @param initialLongitudinalPositions SET&lt;DirectedLanePosition&gt;; the location and initial direction of all generated
     *            GTUs
     * @param network OTSNetwork; the OTS network that owns the generated GTUs
     * @param roomChecker LaneBasedGTUGenerator.RoomChecker; the way that this generator checks that there is sufficient room to
     *            place a new GTU
     * @throws SimRuntimeException when <cite>startTime</cite> lies before the current simulation time
     * @throws ProbabilityException pe
     */
    public LaneBasedGTUGenerator(String id, final Generator<Time.Rel> interarrivelTimeGenerator, final long maxGTUs,
            final Time.Abs startTime, final Time.Abs endTime, final GTUColorer gtuColorer,
            final LaneBasedGTUCharacteristicsGenerator laneBasedGTUCharacteristicsGenerator,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final OTSNetwork network, RoomChecker roomChecker)
            throws SimRuntimeException, ProbabilityException
    {
        this.id = id;
        this.interarrivelTimeGenerator = interarrivelTimeGenerator;
        this.laneBasedGTUCharacteristicsGenerator = laneBasedGTUCharacteristicsGenerator;
        this.endTime = endTime;
        this.maxGTUs = maxGTUs;
        this.initialLongitudinalPositions = initialLongitudinalPositions;
        this.roomChecker = roomChecker;
        this.gtuColorer = gtuColorer;
        laneBasedGTUCharacteristicsGenerator.getSimulator().scheduleEventAbs(startTime, this, this, "generateCharacteristics",
                new Object[] {});
    }

    /**
     * Generate the characteristics of the next GTU.
     * @throws ProbabilityException when something is wrongly defined in the LaneBasedTemplateGTUType
     * @throws SimRuntimeException when this method fails to re-schedule itself or the call to the method that tries to place a
     *             GTU on the road
     * @throws ParameterException in case of a parameter problem
     */
    @SuppressWarnings("unused")
    private void generateCharacteristics() throws ProbabilityException, SimRuntimeException, ParameterException
    {
        OTSDEVSSimulatorInterface simulator = this.laneBasedGTUCharacteristicsGenerator.getSimulator();
        if (this.generatedGTUs >= this.maxGTUs
                || this.laneBasedGTUCharacteristicsGenerator.getSimulator().getSimulatorTime().get().ge(this.endTime))
        {
            return; // Do not reschedule
        }
        synchronized (this.unplacedTemplates)
        {
            this.generatedGTUs++;
            this.unplacedTemplates.add(this.laneBasedGTUCharacteristicsGenerator.draw());
            if (this.unplacedTemplates.size() == 1)
            {
                simulator.scheduleEventNow(this, this, "tryToPlaceGTU", new Object[] {});
            }
        }
        if (this.generatedGTUs < this.maxGTUs)
        {
            simulator.scheduleEventRel(this.interarrivelTimeGenerator.draw(), this, this, "generateCharacteristics",
                    new Object[] {});
        }
    }

    /** Last reported queue length. */
    private int lastReportedQueueLength = 0;

    /**
     * Check if the queue is non-empty and, if it is, try to place the GTUs in the queue on the road.
     * @throws SimRuntimeException should never happen
     * @throws GTUException when something wrong in the definition of the GTU
     * @throws OTSGeometryException when something is wrong in the definition of the GTU
     * @throws NetworkException when something is wrong with the initial location of the GTU
     * @throws NamingException ???
     * @throws ProbabilityException pe
     */
    @SuppressWarnings("unused")
    private void tryToPlaceGTU() throws SimRuntimeException, GTUException, NamingException, NetworkException,
            OTSGeometryException, ProbabilityException
    {
        // System.out.println("entered tryToPlaceGTU");
        LaneBasedGTUCharacteristics characteristics;
        OTSDEVSSimulatorInterface simulator = this.laneBasedGTUCharacteristicsGenerator.getSimulator();
        synchronized (this.unplacedTemplates)
        {
            characteristics = this.unplacedTemplates.peek();
        }
        if (null == characteristics)
        {
            return; // Do not re-schedule this method
        }
        Length.Rel shortestHeadway = new Length.Rel(Double.MAX_VALUE, LengthUnit.SI);
        Speed leaderSpeed = null;
        for (DirectedLanePosition dlp : this.initialLongitudinalPositions)
        {
            Lane lane = dlp.getLane();
            // GOTCHA look for the first GTU with FRONT after the start position (not REAR)
            LaneBasedGTU leader =
                    lane.getGtuAhead(dlp.getPosition(), dlp.getGtuDirection(), RelativePosition.FRONT, simulator
                            .getSimulatorTime().getTime());
            if (null != leader)
            {
                Length.Rel headway = leader.position(lane, leader.getRear()).minus(dlp.getPosition());
                if (headway.si < 0)
                {
                    headway = new Length.Rel(Math.abs(headway.si), headway.getUnit());
                }
                headway = new Length.Rel(headway.si - characteristics.getLength().si / 2, LengthUnit.SI);
                if (shortestHeadway.gt(headway))
                {
                    shortestHeadway = headway;
                    leaderSpeed = leader.getSpeed();
                }
            }
        }
        if (shortestHeadway.si > 0)
        {
            Speed safeSpeed = characteristics.getVelocity();
            if (null != leaderSpeed)
            {
                safeSpeed = this.roomChecker.canPlace(leaderSpeed, shortestHeadway, characteristics);
            }
            if (null != safeSpeed)
            {
                // There is enough room; remove the template from the queue and construct the new GTU
                synchronized (this.unplacedTemplates)
                {
                    this.unplacedTemplates.remove();
                }
                if (safeSpeed.gt(characteristics.getMaximumVelocity()))
                {
                    safeSpeed = characteristics.getMaximumVelocity();
                }
                String gtuId = null == characteristics.getIdGenerator() ? null : characteristics.getIdGenerator().nextId();
                new LaneBasedIndividualGTU(gtuId, characteristics.getGTUType(), this.initialLongitudinalPositions, safeSpeed,
                        characteristics.getLength(), characteristics.getWidth(), characteristics.getMaximumVelocity(),
                        simulator, characteristics.getStrategicalPlanner(), characteristics.getPerception(),
                        DefaultCarAnimation.class, this.gtuColorer, characteristics.getNetwork());
                // System.out.println("tryToPlace: Constructed GTU on " + this.initialLongitudinalPositions);
            }
        }
        int queueLength = this.unplacedTemplates.size();
        if (queueLength != this.lastReportedQueueLength)
        {
            System.out.println("Generator " + this.id + ": queue length is " + queueLength + " at time "
                    + simulator.getSimulatorTime().get());
            this.lastReportedQueueLength = queueLength;
        }
        if (queueLength > 0)
        {
            // System.out.println("Re-scheduling tryToPlace, queue length is " + this.unplacedTemplates.size());
            this.laneBasedGTUCharacteristicsGenerator.getSimulator().scheduleEventRel(this.reTryInterval, this, this,
                    "tryToPlaceGTU", new Object[] {});
        }
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return "LaneBasedGTUGenerator " + this.id + " on " + this.initialLongitudinalPositions;
    }

    /**
     * @return generatedGTUs.
     */
    public long getGeneratedGTUs()
    {
        return this.generatedGTUs;
    }

    /**
     * @param generatedGTUs set generatedGTUs.
     */
    public void setGeneratedGTUs(long generatedGTUs)
    {
        this.generatedGTUs = generatedGTUs;
    }

    /**
     * Retrieve the id of this LaneBasedGTUGenerator.
     * @return String; the id of this LaneBasedGTUGenerator
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Retrieve the end time of this LaneBasedGTUGenerator.
     * @return Time.Abs; the time after which this LaneBasedGTUGenerator will not generate any more GTUs
     */
    public Time.Abs getEndTime()
    {
        return this.endTime;
    }

    /**
     * Retrieve the maximum number of GTUs to generate.
     * @return long; once this number of GTUS is generated, this LaneBasedGTUGenerator will stop generating any more GTUs
     */
    public long getMaxGTUs()
    {
        return this.maxGTUs;
    }

    /**
     * Retrieve the GTUColorer that this LaneBasedGTUGenerator assigns to all generated GTUs.
     * @return GtuColorer; the GTUColorer that this LaneBasedGTUGenerator assigns to all generated GTUs
     */
    public GTUColorer getGtuColorer()
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
         * @param leaderSpeed Speed; velocity of the nearest leader
         * @param headway Length.Rel; net distance to the nearest leader (always &gt; 0)
         * @param laneBasedGTUCharacteristics LaneBasedGTUCharacteristics; characteristics of the proposed new GTU
         * @return Speed; maximum safe speed, or null if a GTU with the specified characteristics cannot be placed at the
         *         current time
         * @throws NetworkException this method may throw a NetworkException if it encounters an error in the network structure
         */
        public Speed canPlace(final Speed leaderSpeed, final Length.Rel headway,
                final LaneBasedGTUCharacteristics laneBasedGTUCharacteristics) throws NetworkException;
    }

}
