package org.opentrafficsim.road.gtu.generator;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.scalar.Time.Rel;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
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
public class LaneBasedGTUGenerator
{
    /** FIFO for templates that have not been generated yet due to insufficient room/headway. */
    private final Queue<LaneBasedGTUCharacteristics> unplacedTemplates = new LinkedList<LaneBasedGTUCharacteristics>();

    /** Time distribution that determines the interval times between GTUs. */
    private final ContinuousDistDoubleScalar.Rel<Time.Rel, TimeUnit> interarrivelTimeDistribution;

    /** Generates most properties of the GTUs. */
    private final LaneBasedTemplateGTUType laneBasedTemplateGTUType;

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

    /**
     * Construct a new lane base GTU generator.
     * @param interarrivelTimeDistribution ContinuousDistDoubleScalar.Rel&lt;Time.Rel, TimeUnit&gt;; generator for the interval
     *            times between GTUs
     * @param maxGTUs long; maximum number of GTUs to generate
     * @param startTime Time.Abs; time at which the first GTU will be generated
     * @param endTime Time.Abs; time after which no more GTUs will be generated
     * @param gtuColorer GTUColorer; the GTU colorer that will be used by all generated GTUs
     * @param laneBasedTemplateGTUType LaneBasedTemplateGTUType; the template that will generate the characteristics of each GTU
     * @param initialLongitudinalPositions SET&lt;DirectedLanePosition&gt;; the location and initial direction of all generated
     *            GTUs
     * @param network OTSNetwork; the OTS network that owns the generated GTUs
     * @param roomChecker LaneBasedGTUGenerator.RoomChecker; the way that this generator checks that there is sufficient room to
     *            place a new GTU
     * @throws SimRuntimeException when <cite>startTime</cite> lies before the current simulation time
     */
    public LaneBasedGTUGenerator(final ContinuousDistDoubleScalar.Rel<Time.Rel, TimeUnit> interarrivelTimeDistribution,
            final long maxGTUs, final Time.Abs startTime, final Time.Abs endTime, final GTUColorer gtuColorer,
            final LaneBasedTemplateGTUType laneBasedTemplateGTUType,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final OTSNetwork network, RoomChecker roomChecker)
            throws SimRuntimeException
    {
        this.interarrivelTimeDistribution = interarrivelTimeDistribution;
        this.laneBasedTemplateGTUType = laneBasedTemplateGTUType;
        this.endTime = endTime;
        this.maxGTUs = maxGTUs;
        this.initialLongitudinalPositions = initialLongitudinalPositions;
        this.roomChecker = roomChecker;
        laneBasedTemplateGTUType.getSimulator().scheduleEventAbs(startTime, this, this, "generateCharacteristics",
                new Object[] {});
    }

    /**
     * Generate the characteristics of the next GTU.
     * @throws ProbabilityException when something is wrongly defined in the LaneBasedTemplateGTUType
     * @throws SimRuntimeException when this method fails to re-schedule itself or the call to the method that tries to place a
     *             GTU on the road
     */
    @SuppressWarnings("unused")
    private void generateCharacteristics() throws ProbabilityException, SimRuntimeException
    {
        OTSDEVSSimulatorInterface simulator = this.laneBasedTemplateGTUType.getSimulator();
        if (this.generatedGTUs >= this.maxGTUs
                || this.laneBasedTemplateGTUType.getSimulator().getSimulatorTime().get().ge(this.endTime))
        {
            return; // Do not reschedule
        }
        synchronized (this.unplacedTemplates)
        {
            this.generatedGTUs++;
            this.unplacedTemplates.add(this.laneBasedTemplateGTUType.draw());
            if (this.unplacedTemplates.size() == 1)
            {
                simulator.scheduleEventNow(this, this, "tryToPlaceGTU", new Object[] {});
            }
        }
        if (this.generatedGTUs < this.maxGTUs)
        {
            simulator.scheduleEventRel(this.interarrivelTimeDistribution.draw(), this, this, "generateCharacteristics",
                    new Object[] {});
        }
    }

    /**
     * Check if the queue is non-empty and, if it is, try to place the GTUs in the queue on the road.
     * @throws SimRuntimeException should never happen
     * @throws GTUException when something wrong in the definition of the GTU
     * @throws OTSGeometryException when something is wrong in the definition of the GTU
     * @throws NetworkException when something is wrong with the initial location of the GTU
     * @throws NamingException ???
     */
    @SuppressWarnings("unused")
    private void tryToPlaceGTU() throws SimRuntimeException, GTUException, NamingException, NetworkException,
            OTSGeometryException
    {
        LaneBasedGTUCharacteristics characteristics;
        OTSDEVSSimulatorInterface simulator = this.laneBasedTemplateGTUType.getSimulator();
        synchronized (this.unplacedTemplates)
        {
            characteristics = this.unplacedTemplates.peek();
        }
        if (null == characteristics)
        {
            return; // Do not re-schedule this method
        }
        Length.Rel shortestHeadway = null;
        Speed leaderSpeed = null;
        for (DirectedLanePosition dlp : this.initialLongitudinalPositions)
        {
            Lane lane = dlp.getLane();
            LaneBasedGTU leader =
                    lane.getGtuAhead(dlp.getPosition(), dlp.getGtuDirection(), RelativePosition.REAR, simulator
                            .getSimulatorTime().getTime());
            if (null != leader)
            {
                Length.Rel headway = leader.position(lane, leader.getRear()).minus(dlp.getPosition());
                if (headway.si < 0)
                {
                    headway = new Length.Rel(Math.abs(headway.si), headway.getUnit());
                }
                if (null == shortestHeadway || shortestHeadway.gt(headway))
                {
                    shortestHeadway = headway;
                    leaderSpeed = leader.getVelocity();
                }
            }
        }
        if (null != shortestHeadway)
        {
            Speed safeSpeed = this.roomChecker.canPlace(leaderSpeed, shortestHeadway, characteristics);
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
                String id = null == characteristics.getIdGenerator() ? null : characteristics.getIdGenerator().nextId();
                new LaneBasedIndividualGTU(id, characteristics.getGTUType(), this.initialLongitudinalPositions, safeSpeed,
                        characteristics.getLength(), characteristics.getWidth(), characteristics.getMaximumVelocity(),
                        simulator, characteristics.getStrategicalPlanner(), characteristics.getPerception(),
                        characteristics.getNetwork());
            }
        }
        if (this.unplacedTemplates.size() > 0)
        {
            this.laneBasedTemplateGTUType.getSimulator().scheduleEventRel(this.reTryInterval, this, this, "tryToPlaceGTU",
                    new Object[] {});
        }
    }

    /**
     * Interface for a method that checks that there is sufficient room for a proposed new GTU and returns the maximum safe
     * speed for the proposed new GTU.
     */
    interface RoomChecker
    {
        /**
         * Return the maximum safe speed for a new GTU with the specified characteristics. Return null if there is no safe
         * speed.
         * @param leaderSpeed Speed; velocity of the nearest leader
         * @param headway Length.Rel; distance to the nearest leader
         * @param laneBasedGTUCharacteristics LaneBasedGTUCharacteristics; characteristics of the proposed new GTU
         * @return Speed; maximum safe speed, or null if a GTU with the specified characteristics cannot be placed at the
         *         current time
         */
        public Speed canPlace(final Speed leaderSpeed, final Length.Rel headway,
                final LaneBasedGTUCharacteristics laneBasedGTUCharacteristics);
    }

}
