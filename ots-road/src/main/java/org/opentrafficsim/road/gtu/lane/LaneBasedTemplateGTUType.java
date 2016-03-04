package org.opentrafficsim.road.gtu.lane;

import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * Generate lane based GTUs using a template.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 29, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedTemplateGTUType extends TemplateGTUType
{
    /** */
    private static final long serialVersionUID = 20160101L;

    /** Generator for lane perception. */
    private final Generator<LanePerceptionFull> perceptionGenerator;

    /** Generator for the strategical planner. */
    private final Generator<LaneBasedStrategicalPlanner> strategicalPlannerGenerator;

    /** Generator for the initial speed of the next GTU. */
    private Generator<Speed> initialSpeedGenerator;

    /** Perception of the next GTU. */
    private LanePerceptionFull perception = null;

    /** Strategical planner of the next GTU. */
    private LaneBasedStrategicalPlanner strategicalPlanner = null;

    /** Initial longitudinal positions of all generated GTUs. */
    private Set<DirectedLanePosition> initialLongitudinalPositions;

    /** Initial speed of the next GTU. */
    private Speed initialSpeed;

    /** The OTSNetwork that all generated GTUs will be registered in. */
    private OTSNetwork network;

    /**
     * @param typeId The id of the GTUType to make it identifiable.
     * @param idGenerator IdGenerator; the id generator used to generate names for GTUs constructed using this TemplateGTUType.
     *            Provide null to use the default id generator of AbstractGTU.
     * @param lengthGenerator Generator&lt;Length.Rel&gt; generator for the length of the GTU type (parallel with driving
     *            direction).
     * @param widthGenerator Generator&lt;Length.Rel&gt;; generator for the width of the GTU type (perpendicular to driving
     *            direction).
     * @param maximumSpeedGenerator Generator&lt;Speed&gt;; generator for the maximum speed of the GTU type (in the driving
     *            direction).
     * @param simulator the simulator.
     * @param strategicalPlannerGenerator Generator&lt;LaneBasedStrategicalPlanner&gt;; generator for the strategical planner
     *            (e.g., route determination)
     * @param perceptionGenerator Generator&lt;LanePerceptionFull&gt;; generator for the lane-based perception model of
     *            generated GTUs
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; the initial lanes, directions and positions of
     *            generated GTUs
     * @param initialSpeedGenerator Generator&lt;Speed&gt;; the generator for the initial speed of generated GTUs
     * @param network OTSNetwork; the network that all generated GTUs are registered in
     * @throws GTUException when GTUType defined more than once
     */
    public LaneBasedTemplateGTUType(final String typeId, IdGenerator idGenerator, final Generator<Length.Rel> lengthGenerator,
            final Generator<Length.Rel> widthGenerator, final Generator<Speed> maximumSpeedGenerator,
            final OTSDEVSSimulatorInterface simulator, Generator<LaneBasedStrategicalPlanner> strategicalPlannerGenerator,
            Generator<LanePerceptionFull> perceptionGenerator, final Set<DirectedLanePosition> initialLongitudinalPositions,
            Generator<Speed> initialSpeedGenerator, OTSNetwork network) throws GTUException
    {
        super(typeId, idGenerator, lengthGenerator, widthGenerator, maximumSpeedGenerator, simulator);
        this.strategicalPlannerGenerator = strategicalPlannerGenerator;
        this.perceptionGenerator = perceptionGenerator;
        this.initialLongitudinalPositions = initialLongitudinalPositions;
        this.initialSpeedGenerator = initialSpeedGenerator;
        this.network = network;
    }

    /**
     * Generate the properties of the next GTU.
     * @throws ProbabilityException when a generator is improperly configured
     */
    public void generateCharacteristics() throws ProbabilityException
    {
        super.generateCharacteristics();
        this.perception = this.perceptionGenerator.draw();
        this.strategicalPlanner = this.strategicalPlannerGenerator.draw();
        this.initialSpeed = this.initialSpeedGenerator.draw();
    }

    /** {@inheritDoc} */
    public void clearCharacteristics()
    {
        super.clearCharacteristics();
        this.perception = null;
        this.strategicalPlanner = null;
    }

    /**
     * @return LanePerceptionFull; the perception for the next GTU
     */
    public LanePerceptionFull getPerception()
    {
        return this.perception;
    }

    /**
     * @return LaneBasedStrategicalPlanner; the strategical planner for the next GTU
     */
    public LaneBasedStrategicalPlanner getStrategicalPlanner()
    {
        return this.strategicalPlanner;
    }

    /**
     * @return Speed; the initial speed of the next GTU
     */
    public Speed getInitialSpeed()
    {
        return this.initialSpeed;
    }

    /**
     * @return Network; the network for all generated GTUs
     */
    public OTSNetwork getNetwork()
    {
        return this.network;
    }

    /**
     * Build a GTU with the current set of characteristics and put it on the road. This method <b>does not</b> check if there is
     * sufficient room.
     * @param nextAction UpdateCharacteristics; if YES; the stored characteristics are cleared and a new set is generated; if
     *            NO; the stored characteristics are not changed; if CLEAR; the stored characteristics are cleared, but no new
     *            set is generated.
     * @return LaneBasedGTU; the newly constructed lane based GTU
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     * @throws OTSGeometryException when the initial path is wrong
     * @throws NamingException when ???
     * @throws ProbabilityException if UpdateCharacteristics is YES and one of the generators is wrongly configured
     */
    public LaneBasedGTU generateGTU(final UpdateCharacteristics nextAction) throws NamingException, NetworkException,
            SimRuntimeException, GTUException, OTSGeometryException, ProbabilityException
    {
        IdGenerator idGenerator = super.getIdGenerator();
        String id = null == idGenerator ? null : idGenerator.nextId();
        LaneBasedGTU result =
                new LaneBasedIndividualGTU(id, super.getGtuType(), this.initialLongitudinalPositions, getInitialSpeed(),
                        super.getLength(), super.getWidth(), super.getMaximumVelocity(), super.getSimulator(),
                        this.getStrategicalPlanner(), this.getPerception(), getNetwork());
        if (UpdateCharacteristics.CLEAR == nextAction || UpdateCharacteristics.YES == nextAction)
        {
            clearCharacteristics();
        }
        if (UpdateCharacteristics.YES == nextAction)
        {
            generateCharacteristics();
        }
        return result;
    }

    /**
     * Values for the <cite>nextAction</cite> parameter of <cite>generateGTU</cite>. 
     */
    public enum UpdateCharacteristics
    {
        /** Clear characteristics and generate a new set. */
        YES,
        /** Do not change the current set of characteristics. */
        NO,
        /** Clear characteristics but do not generate a new set. */
        CLEAR
    }

}
